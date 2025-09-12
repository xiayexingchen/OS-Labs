package com.example.producer.service;

import com.example.producer.dto.ProducerConsumerStatusDTO;
import com.example.producer.model.BufferItem;
import com.example.producer.model.Consumer;
import com.example.producer.model.Producer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ProducerConsumerService {
    private List<BufferItem> buffer;
    private int bufferSize;
    private int headPointer;
    private int tailPointer;
    private int itemCount;
    private List<Producer> producers;
    private List<Consumer> consumers;
    private int nextItemValue;
    private boolean isRunning;
    private int simulationSpeed;
    private int productionSpeed;  // 生产速度，单位毫秒，范围1000-5000
    private int consumptionSpeed; // 消费速度，单位毫秒，范围1000-5000
    private ExecutorService producerThreadPool;
    private ExecutorService consumerThreadPool;
    private List<String> operationLogs;
    private int totalProduced;
    private int totalConsumed;
    private int bufferFullCount;
    private int bufferEmptyCount;
    
    // 添加锁和条件变量来实现真实的生产者消费者等待/唤醒机制
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public ProducerConsumerService() {
        reset();
    }

    public synchronized void init(int bufferSize, int producerCount, int consumerCount, int simulationSpeed, int productionSpeed, int consumptionSpeed) {
        this.bufferSize = bufferSize;
        this.simulationSpeed = simulationSpeed;
        // 限制生产速度和消费速度在1秒到5秒之间
        this.productionSpeed = Math.max(1000, Math.min(5000, productionSpeed));
        this.consumptionSpeed = Math.max(1000, Math.min(5000, consumptionSpeed));
        this.buffer = new ArrayList<>(Collections.nCopies(bufferSize, null));
        this.headPointer = 0;
        this.tailPointer = 0;
        this.itemCount = 0;
        this.nextItemValue = 1;
        this.isRunning = false;

        // 初始化生产者（默认处于等待状态）
        this.producers = new ArrayList<>();
        for (int i = 0; i < producerCount; i++) {
            Producer producer = new Producer("P" + (i + 1));
            producer.setWaiting(true);
            this.producers.add(producer);
        }

        // 初始化消费者（默认处于等待状态）
        this.consumers = new ArrayList<>();
        for (int i = 0; i < consumerCount; i++) {
            Consumer consumer = new Consumer("C" + (i + 1));
            consumer.setWaiting(true);
            this.consumers.add(consumer);
        }

        // 重置统计数据
        this.operationLogs = new ArrayList<>();
        this.totalProduced = 0;
        this.totalConsumed = 0;
        this.bufferFullCount = 0;
        this.bufferEmptyCount = 0;

        addLog("系统已初始化");
    }

    public synchronized void startSimulation() {
        if (isRunning) return;

        isRunning = true;
        
        // 创建生产者和消费者线程池
        producerThreadPool = Executors.newFixedThreadPool(producers.size());
        consumerThreadPool = Executors.newFixedThreadPool(consumers.size());
        
        // 启动所有生产者线程
        for (Producer producer : producers) {
            producerThreadPool.submit(() -> runProducer(producer));
        }
        
        // 启动所有消费者线程
        for (Consumer consumer : consumers) {
            consumerThreadPool.submit(() -> runConsumer(consumer));
        }
        
        addLog("模拟已开始");
    }

    public synchronized void stopSimulation() {
        if (!isRunning) return;

        isRunning = false;
        
        // 唤醒所有等待的线程
        lock.lock();
        try {
            notFull.signalAll();
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
        
        // 关闭线程池
        if (producerThreadPool != null) {
            producerThreadPool.shutdownNow();
        }
        if (consumerThreadPool != null) {
            consumerThreadPool.shutdownNow();
        }
        
        addLog("模拟已停止");
    }

    public synchronized void reset() {
        stopSimulation();
        bufferSize = 5;
        buffer = new ArrayList<>(Collections.nCopies(bufferSize, null));
        headPointer = 0;
        tailPointer = 0;
        itemCount = 0;
        producers = new ArrayList<>();
        consumers = new ArrayList<>();
        nextItemValue = 1;
        isRunning = false;
        simulationSpeed = 1000;
        productionSpeed = 3000;  // 默认生产速度3秒
        consumptionSpeed = 3000; // 默认消费速度3秒
        operationLogs = new ArrayList<>();
        totalProduced = 0;
        totalConsumed = 0;
        bufferFullCount = 0;
        bufferEmptyCount = 0;
    }
    
    // 生产者线程的运行方法
    private void runProducer(Producer producer) {
        while (isRunning) {
            try {
                // 模拟生产者处理时间（基于生产速度的随机延迟）
                Thread.sleep(getProductionDelay());
                
                lock.lock();
                try {
                    // 缓冲区满时等待
                    while (isRunning && itemCount >= bufferSize) {
                        producer.setWaiting(true);
                        addLog(producer.getId() + " 因为缓冲区满进入等待状态");
                        bufferFullCount++;
                        notFull.await(simulationSpeed, TimeUnit.MILLISECONDS); // 带超时的等待
                        
                        if (!isRunning) break; // 检查是否已停止模拟
                    }
                    
                    if (!isRunning) break;
                    
                    // 生产物品
                    producer.setWaiting(false);
                    produceItem(producer);
                    
                    // 通知等待的消费者
                    notEmpty.signal();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // 线程结束时设置为等待状态
        lock.lock();
        try {
            producer.setWaiting(true);
        } finally {
            lock.unlock();
        }
    }
    
    // 消费者线程的运行方法
    private void runConsumer(Consumer consumer) {
        while (isRunning) {
            try {
                // 模拟消费者处理时间（基于消费速度的随机延迟）
                Thread.sleep(getConsumptionDelay());
                
                lock.lock();
                try {
                    // 缓冲区空时等待
                    while (isRunning && itemCount == 0) {
                        consumer.setWaiting(true);
                        addLog(consumer.getId() + " 因为缓冲区空进入等待状态");
                        bufferEmptyCount++;
                        notEmpty.await(simulationSpeed, TimeUnit.MILLISECONDS); // 带超时的等待
                        
                        if (!isRunning) break; // 检查是否已停止模拟
                    }
                    
                    if (!isRunning) break;
                    
                    // 消费物品
                    consumer.setWaiting(false);
                    consumeItem(consumer);
                    
                    // 通知等待的生产者
                    notFull.signal();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // 线程结束时设置为等待状态
        lock.lock();
        try {
            consumer.setWaiting(true);
        } finally {
            lock.unlock();
        }
    }

    private void produceItem(Producer producer) {
        if (!isRunning || itemCount >= bufferSize) return;
        
        // 生产物品
        BufferItem newItem = new BufferItem();
        newItem.setValue(nextItemValue++);
        newItem.setProducerId(producer.getId());
        newItem.setTimestamp(LocalDateTime.now());

        // 在队尾位置添加元素
        buffer.set(tailPointer, newItem);

        // 更新队尾指针
        tailPointer = (tailPointer + 1) % bufferSize;

        // 增加计数
        itemCount++;
        totalProduced++;
        producer.setItemsProduced(producer.getItemsProduced() + 1);

        addLog(producer.getId() + " 在队尾位置 " + ((tailPointer - 1 + bufferSize) % bufferSize) + " 生产了物品 " + newItem.getValue());
    }

    private void consumeItem(Consumer consumer) {
        if (!isRunning || itemCount == 0) return;
        
        // 从队头取出元素
        BufferItem consumedItem = buffer.get(headPointer);
        if (consumedItem == null || consumedItem.isConsumed()) return;

        // 计算等待时间
        long waitTime = java.time.Duration.between(consumedItem.getTimestamp(), LocalDateTime.now()).toMillis();
        consumedItem.setWaitTime(waitTime);
        
        // 标记物品为已消费，并记录消费者ID
        consumedItem.setConsumed(true);
        consumedItem.setConsumerId(consumer.getId());
        
        // 保留物品在缓冲区中，不设为null


        // 更新队头指针
        headPointer = (headPointer + 1) % bufferSize;

        // 减少计数
        itemCount--;
        totalConsumed++;
        consumer.setItemsConsumed(consumer.getItemsConsumed() + 1);

        addLog(consumer.getId() + " 从队头位置 " + ((headPointer - 1 + bufferSize) % bufferSize) + " 消费了物品 " + 
                consumedItem.getValue() + "，等待时间: " + waitTime + "ms，由 " + consumedItem.getProducerId() + " 生产");
    }
    
    // 获取生产延迟时间（基于生产速度）
    private long getProductionDelay() {
        // 在生产速度基础上添加±20%的随机波动，使模拟更真实
        double randomFactor = 0.8 + Math.random() * 0.4; // 0.8到1.2之间的随机因子
        return (long)(productionSpeed * randomFactor);
    }
    
    // 获取消费延迟时间（基于消费速度）
    private long getConsumptionDelay() {
        // 在消费速度基础上添加±20%的随机波动，使模拟更真实
        double randomFactor = 0.8 + Math.random() * 0.4; // 0.8到1.2之间的随机因子
        return (long)(consumptionSpeed * randomFactor);
    }

    private synchronized void addLog(String message) {
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logMessage = "[" + timestamp + "] " + message;
        operationLogs.add(0, logMessage); // 添加到列表头部

        // 限制日志数量
        if (operationLogs.size() > 20) {
            operationLogs.remove(operationLogs.size() - 1);
        }
    }

    public synchronized ProducerConsumerStatusDTO getStatus() {
        // 创建状态快照
        List<BufferItem> bufferSnapshot = new ArrayList<>(buffer);
        List<Producer> producerSnapshot = new ArrayList<>(producers);
        List<Consumer> consumerSnapshot = new ArrayList<>(consumers);
        List<String> logSnapshot = new ArrayList<>(operationLogs);

        ProducerConsumerStatusDTO.Stats stats = new ProducerConsumerStatusDTO.Stats(
                totalProduced, totalConsumed, bufferFullCount, bufferEmptyCount
        );

        return new ProducerConsumerStatusDTO(
                bufferSnapshot,
                headPointer,
                tailPointer,
                itemCount,
                bufferSize,
                producerSnapshot,
                consumerSnapshot,
                stats,
                logSnapshot
        );
    }

    public boolean isRunning() {
        return isRunning;
    }
    
    // 生产速度的getter和setter方法
    public int getProductionSpeed() {
        return productionSpeed;
    }
    
    public synchronized void setProductionSpeed(int productionSpeed) {
        // 限制生产速度在1秒到5秒之间
        this.productionSpeed = Math.max(1000, Math.min(5000, productionSpeed));
        addLog("生产速度已设置为: " + (this.productionSpeed / 1000.0) + "秒");
    }
    
    // 消费速度的getter和setter方法
    public int getConsumptionSpeed() {
        return consumptionSpeed;
    }
    
    public synchronized void setConsumptionSpeed(int consumptionSpeed) {
        // 限制消费速度在1秒到5秒之间
        this.consumptionSpeed = Math.max(1000, Math.min(5000, consumptionSpeed));
        addLog("消费速度已设置为: " + (this.consumptionSpeed / 1000.0) + "秒");
    }
}