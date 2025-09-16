package com.example.producer.service;

import com.example.producer.dto.ProducerConsumerStatusDTO;
import com.example.producer.model.BufferItem;
import com.example.producer.model.Consumer;
import com.example.producer.model.Producer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProducerConsumerService {
    private ConcurrentLinkedQueue<BufferItem> buffer; // 无锁队列
    private int bufferSize;
    private List<Producer> producers;
    private List<Consumer> consumers;
    private AtomicInteger nextItemValue = new AtomicInteger(1);
    private volatile boolean isRunning;
    private int simulationSpeed;
    private int productionSpeed;  // 生产速度，单位毫秒，范围1000-5000
    private int consumptionSpeed; // 消费速度，单位毫秒，范围1000-5000
    private ExecutorService producerThreadPool;
    private ExecutorService consumerThreadPool;
    private List<String> operationLogs;
    private AtomicInteger totalProduced = new AtomicInteger(0);
    private AtomicInteger totalConsumed = new AtomicInteger(0);
    private AtomicInteger bufferFullCount = new AtomicInteger(0);
    private AtomicInteger bufferEmptyCount = new AtomicInteger(0);
    private AtomicInteger itemCount = new AtomicInteger(0); // 使用原子变量计数
    private List<BufferItem> consumedItemsHistory = Collections.synchronizedList(new ArrayList<>()); // 已消费物品历史记录

    // 初始化系统
    public synchronized void init(int bufferSize, int producerCount, int consumerCount, int simulationSpeed, int productionSpeed, int consumptionSpeed) {
        // 停止之前的模拟（如果有）
        stopSimulation();

        // 设置参数
        this.bufferSize = bufferSize;
        this.simulationSpeed = simulationSpeed;
        this.productionSpeed = productionSpeed;
        this.consumptionSpeed = consumptionSpeed;

        // 初始化数据结构
        buffer = new ConcurrentLinkedQueue<>();
        producers = new ArrayList<>();
        consumers = new ArrayList<>();
        operationLogs = Collections.synchronizedList(new ArrayList<>());
        consumedItemsHistory.clear(); // 清空历史记录

        // 重置计数器
        nextItemValue.set(1);
        totalProduced.set(0);
        totalConsumed.set(0);
        bufferFullCount.set(0);
        bufferEmptyCount.set(0);
        itemCount.set(0);

        // 创建生产者
        for (int i = 1; i <= producerCount; i++) {
            Producer producer = new Producer("P" + i);
            producer.setWaiting(true); // 覆盖构造函数中的默认值
            producers.add(producer);
        }

        // 创建消费者
        for (int i = 1; i <= consumerCount; i++) {
            Consumer consumer = new Consumer("C" + i);
            consumer.setWaiting(true); // 覆盖构造函数中的默认值
            consumers.add(consumer);
        }

        addLog("系统已初始化: 缓冲区大小=" + bufferSize + ", 生产者数量=" + producerCount + ", 消费者数量=" + consumerCount);
    }

    // 重置系统
    public synchronized void reset() {
        stopSimulation();
        // 移除硬编码的缓冲区大小设置，保持用户设置的缓冲区大小
        buffer = new ConcurrentLinkedQueue<>();
        producers = new ArrayList<>();
        consumers = new ArrayList<>();
        nextItemValue.set(1);
        isRunning = false;
        simulationSpeed = 1000;
        productionSpeed = 5000;  // 默认生产速度5秒，增加延迟使模拟更易于观察
        consumptionSpeed = 5000; // 默认消费速度5秒，增加延迟使模拟更易于观察
        operationLogs = Collections.synchronizedList(new ArrayList<>());
        totalProduced.set(0);
        totalConsumed.set(0);
        bufferFullCount.set(0);
        bufferEmptyCount.set(0);
        itemCount.set(0);
        consumedItemsHistory.clear(); // 清空历史记录
    }

    // 生产者线程的运行方法
    private void runProducer(Producer producer) {
        while (isRunning) {
            try {
                // 模拟生产者处理时间（基于生产速度的随机延迟）
                Thread.sleep(getProductionDelay());
                
                // 无锁方式检查缓冲区是否已满
                if (buffer.size() >= bufferSize) {
                    producer.setWaiting(true);
                    addLog(producer.getId() + " 因为缓冲区满进入等待状态");
                    bufferFullCount.incrementAndGet();
                    // 短暂休眠后重试，避免忙等待
                    Thread.sleep(100);
                    continue;
                }
                
                producer.setWaiting(false);
                // 生产物品并添加到缓冲区
                BufferItem newItem = new BufferItem();
                newItem.setValue(nextItemValue.getAndIncrement());
                newItem.setProducerId(producer.getId());
                newItem.setTimestamp(LocalDateTime.now());
                newItem.setConsumed(false);
                
                // 使用ConcurrentLinkedQueue的无锁添加操作
                buffer.offer(newItem);
                itemCount.incrementAndGet();
                totalProduced.incrementAndGet();
                
                // 使用同步块更新生产者状态
                synchronized (producer) {
                    producer.setItemsProduced(producer.getItemsProduced() + 1);
                }
                
                addLog(producer.getId() + " 生产了物品 " + newItem.getValue());
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // 线程结束时设置为等待状态
        producer.setWaiting(true);
    }

    // 消费者线程的运行方法
    private void runConsumer(Consumer consumer) {
        while (isRunning) {
            try {
                // 模拟消费者处理时间（基于消费速度的随机延迟）
                Thread.sleep(getConsumptionDelay());
                
                // 无锁方式检查缓冲区是否为空
                if (buffer.isEmpty()) {
                    consumer.setWaiting(true);
                    addLog(consumer.getId() + " 因为缓冲区空进入等待状态");
                    bufferEmptyCount.incrementAndGet();
                    // 短暂休眠后重试，避免忙等待
                    Thread.sleep(100);
                    continue;
                }
                
                consumer.setWaiting(false);
                // 使用ConcurrentLinkedQueue的无锁移除操作
                BufferItem consumedItem = buffer.poll();
                
                if (consumedItem != null) {
                    itemCount.decrementAndGet();
                    totalConsumed.incrementAndGet();
                    
                    // 计算等待时间
                    long waitTime = java.time.Duration.between(consumedItem.getTimestamp(), LocalDateTime.now()).toMillis();
                    consumedItem.setWaitTime(waitTime);
                    
                    // 标记物品为已消费，并记录消费者ID
                    consumedItem.setConsumed(true);
                    consumedItem.setConsumerId(consumer.getId());
                    
                    // 添加到已消费物品历史记录
                    consumedItemsHistory.add(consumedItem);
                    // 限制历史记录数量，只保留最近50条
                    if (consumedItemsHistory.size() > 50) {
                        consumedItemsHistory.remove(0);
                    }
                    
                    // 使用同步块更新消费者状态
                    synchronized (consumer) {
                        consumer.setItemsConsumed(consumer.getItemsConsumed() + 1);
                    }
                    
                    addLog(consumer.getId() + " 消费了物品 " + 
                            consumedItem.getValue() + "，等待时间: " + waitTime + "ms，由 " + consumedItem.getProducerId() + " 生产");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // 线程结束时设置为等待状态
        consumer.setWaiting(true);
    }

    // 开始模拟
    public synchronized void startSimulation() {
        if (isRunning) return;
        
        isRunning = true;
        
        // 创建线程池
        producerThreadPool = Executors.newFixedThreadPool(producers.size());
        consumerThreadPool = Executors.newFixedThreadPool(consumers.size());
        
        // 启动生产者线程
        for (Producer producer : producers) {
            producerThreadPool.submit(() -> runProducer(producer));
        }
        
        // 启动消费者线程
        for (Consumer consumer : consumers) {
            consumerThreadPool.submit(() -> runConsumer(consumer));
        }
        
        addLog("模拟已开始");
    }

    // 停止模拟
    public synchronized void stopSimulation() {
        if (!isRunning) return;
        
        isRunning = false;
        
        // 尝试优雅关闭线程池
        if (producerThreadPool != null) {
            producerThreadPool.shutdownNow();
        }
        
        if (consumerThreadPool != null) {
            consumerThreadPool.shutdownNow();
        }
        
        // 设置所有生产者和消费者为等待状态
        for (Producer producer : producers) {
            producer.setWaiting(true);
        }
        
        for (Consumer consumer : consumers) {
            consumer.setWaiting(true);
        }
        
        addLog("模拟已停止");
    }

    // 获取当前状态
    public synchronized ProducerConsumerStatusDTO getStatus() {
        // 创建状态快照
        List<BufferItem> bufferSnapshot = new ArrayList<>(buffer);
        List<Producer> producerSnapshot = new ArrayList<>(producers);
        List<Consumer> consumerSnapshot = new ArrayList<>(consumers);
        List<String> logSnapshot = new ArrayList<>(operationLogs);

        ProducerConsumerStatusDTO.Stats stats = new ProducerConsumerStatusDTO.Stats(
                totalProduced.get(), totalConsumed.get(), bufferFullCount.get(), bufferEmptyCount.get()
        );

        // 由于使用ConcurrentLinkedQueue，headPointer和tailPointer不再有意义，设为0
        return new ProducerConsumerStatusDTO(
                bufferSnapshot,
                0, // headPointer
                bufferSnapshot.size(), // tailPointer表示当前队列大小
                itemCount.get(),
                bufferSize,
                producerSnapshot,
                consumerSnapshot,
                stats,
                logSnapshot
        );
    }

    // 获取已消费物品历史记录
    public synchronized List<BufferItem> getConsumedItemsHistory() {
        return new ArrayList<>(consumedItemsHistory);
    }

    // 添加日志
    private void addLog(String message) {
        String log = "[" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message;
        operationLogs.add(log);
        
        // 限制日志数量
        if (operationLogs.size() > 50) {
            operationLogs.remove(0);
        }
    }

    // 获取生产延迟（随机化以避免同步）
    private long getProductionDelay() {
        // 在生产速度的基础上，增加±10%的随机波动
        double randomFactor = 0.9 + Math.random() * 0.2;
        return (long) (productionSpeed * randomFactor);
    }

    // 获取消费延迟（随机化以避免同步）
    private long getConsumptionDelay() {
        // 在消费速度的基础上，增加±10%的随机波动
        double randomFactor = 0.9 + Math.random() * 0.2;
        return (long) (consumptionSpeed * randomFactor);
    }

    // 判断系统是否正在运行
    public boolean isRunning() {
        return isRunning;
    }

    // 缓冲区大小的setter方法
    public synchronized void setBufferSize(int bufferSize) {
        if (bufferSize < 1 || bufferSize > 20) {
            throw new IllegalArgumentException("缓冲区大小必须在1到20之间");
        }
        this.bufferSize = bufferSize;
        addLog("缓冲区大小已设置为: " + bufferSize);
    }

    // 缓冲区大小的getter方法
    public int getBufferSize() {
        return bufferSize;
    }
}