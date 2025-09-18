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
    // 替换原来的 ConcurrentLinkedQueue，实现基于槽位的缓冲区
    private BufferItem[] bufferSlots;      // 固定大小的槽位数组，槽位永远存在（不会为 null）
    private final Object lock = new Object();

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
    // itemCount 表示当前处于 "已完成" 或 "消费中" 或 "生产中" 的有效物品计数（这里我们将它维护为：生产完成时 +1，消费完成时 -1）
    private AtomicInteger itemCount = new AtomicInteger(0);
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

        // 初始化槽位（初始为 "空"）
        bufferSlots = new BufferItem[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            BufferItem slot = new BufferItem();
            // 初始化槽位状态为 "空"
            slot.setState("空");
            // 其它字段保持默认（value=0, producerId=null 等）
            bufferSlots[i] = slot;
        }

        // 初始化其他数据结构
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
        
        // 清空生产者和消费者索引映射
        producerCurrentSlotIndex.clear();
        consumerCurrentSlotIndex.clear();

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
        
        // 如果已有槽位，先为每个槽位加锁并重置
        if (bufferSlots != null) {
            for (BufferItem slot : bufferSlots) {
                if (slot != null) {
                    synchronized (slot.getSlotLock()) {
                        // 确保槽位被正确清理
                    }
                }
            }
        }
        
        // 保持 bufferSize 原来的设置，仅重置槽位为 "空"
        if (bufferSize > 0) {
            bufferSlots = new BufferItem[bufferSize];
            for (int i = 0; i < bufferSize; i++) {
                BufferItem slot = new BufferItem();
                slot.setState("空");
                bufferSlots[i] = slot;
            }
        } else {
            bufferSlots = new BufferItem[0];
        }

        producers = new ArrayList<>();
        consumers = new ArrayList<>();
        nextItemValue.set(1);
        isRunning = false;
        simulationSpeed = 1000;
        productionSpeed = 5000;  // 默认生产速度5秒
        consumptionSpeed = 5000; // 默认消费速度5秒
        operationLogs = Collections.synchronizedList(new ArrayList<>());
        totalProduced.set(0);
        totalConsumed.set(0);
        bufferFullCount.set(0);
        bufferEmptyCount.set(0);
        itemCount.set(0);
        consumedItemsHistory.clear(); // 清空历史记录
        producerCurrentSlotIndex.clear(); // 清空生产者槽位索引映射，确保重置后从第一个槽位开始
    }

    // 生产者线程的运行方法（占位式生产）
    private void runProducer(Producer producer) {
        while (isRunning) {
            try {
                // 尝试找到可用于生产的槽位（"空" 或 "已消费"），使用循环选择策略
                BufferItem slot = findSlotForProduction(producer);
                if (slot == null) {
                    // 没有可用槽位，记录等待并短暂休眠
                    producer.setWaiting(true);
                    addLog(producer.getId() + " 因为没有可用槽位进入等待状态");
                    bufferFullCount.incrementAndGet();
                    Thread.sleep(100);
                    continue;
                }

                producer.setWaiting(false);

                // 在findSlotForProduction中已设置槽位状态和生产者信息，此处不再重复

                // 再次检查槽位状态，确保没有被其他生产者抢占
                synchronized (slot.getSlotLock()) {
                    if (!"生产中".equals(slot.getState()) || !producer.getId().equals(slot.getProducerId())) {
                        continue;
                    }
                    
                    // 分配物品值
                    int itemValue = nextItemValue.getAndIncrement();
                    slot.setValue(itemValue);
                }
                
                // 获取槽位索引用于后续日志
                int slotIndex = getSlotIndex(slot);
                
                // 模拟生产时长（生产期间占据槽位）
                Thread.sleep(getProductionDelay());

                // 生产完成，标记为 "已完成" 并统计
                synchronized (slot.getSlotLock()) {
                    if (slot.getProducerId() != null && producer.getId().equals(slot.getProducerId()) && "生产中".equals(slot.getState())) {
                        int finalItemValue = slot.getValue(); // 获取最终的物品值
                        slot.setState("已完成");
                        // 生产完成时，视为一个可被消费的物品，计数 +1
                        itemCount.incrementAndGet();
                        
                        // 获取槽位索引用于日志
                        addLog(producer.getId() + " 在槽位 " + slotIndex + " 完成了物品 " + finalItemValue);
                    }
                }

                totalProduced.incrementAndGet();
                synchronized (producer) {
                    producer.setItemsProduced(producer.getItemsProduced() + 1);
                }

                // 生产者循环继续（不强制休眠，避免占用过高 CPU；如果需要节拍可加短 sleep）
                Thread.yield();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                // 任何异常记录日志，继续循环或退出视情况
                addLog("生产者线程异常: " + ex.getMessage());
            }
        }

        // 线程结束时设置为等待状态
        producer.setWaiting(true);
    }

    // 消费者线程的运行方法
    private void runConsumer(Consumer consumer) {
        while (isRunning) {
            try {
                // 查找已完成的槽位
                BufferItem slot = findCompletedSlot(consumer);
                if (slot == null) {
                    consumer.setWaiting(true);
                    addLog(consumer.getId() + " 因为没有已完成的物品进入等待状态");
                    bufferEmptyCount.incrementAndGet();
                    Thread.sleep(100);
                    continue;
                }

                consumer.setWaiting(false);

                synchronized (slot.getSlotLock()) {
                    // 再次检查槽位状态，确保没有被其他消费者抢占
                    if (!"已完成".equals(slot.getState())) {
                        continue;
                    }

                    // 标记为消费中并记录消费者 id
                    slot.setState("消费中");
                    slot.setConsumerId(consumer.getId());

                    // 获取槽位索引用于日志
                    int slotIndex = getSlotIndex(slot);
                    addLog(consumer.getId() + " 在槽位 " + slotIndex + " 开始消费物品 " + slot.getValue());
                }

                // 模拟消费时长（消费中保留槽位但状态为 "消费中"）
                Thread.sleep(getConsumptionDelay());

                synchronized (slot.getSlotLock()) {
                    // 再次检查槽位状态
                    if (slot.getConsumerId() != null && "消费中".equals(slot.getState())) {
                        // 消费完成后标记为 "已消费"（不清空槽位，等待下次生产覆盖）
                        slot.setConsumed(true);
                        slot.setWaitTime(java.time.Duration.between(slot.getTimestamp(), LocalDateTime.now()).toMillis());
                        slot.setState("已消费");
                        // 项目已被消费，从"已完成"池中移除，计数 -1
                        itemCount.decrementAndGet();
                    }

                    // 获取槽位索引用于日志
                    int slotIndex = getSlotIndex(slot);
                    addLog(consumer.getId() + " 在槽位 " + slotIndex + " 消费完成物品 " + slot.getValue());
                }

                // 保存到已消费历史（需要创建副本，避免引用冲突）
                synchronized (lock) {
                    BufferItem consumedItemCopy = new BufferItem();
                    consumedItemCopy.setValue(slot.getValue());
                    consumedItemCopy.setProducerId(slot.getProducerId());
                    consumedItemCopy.setConsumerId(slot.getConsumerId());
                    consumedItemCopy.setTimestamp(slot.getTimestamp());
                    consumedItemCopy.setWaitTime(slot.getWaitTime());
                    consumedItemCopy.setConsumed(true);
                    consumedItemCopy.setState("已消费");
                    
                    consumedItemsHistory.add(consumedItemCopy);
                    if (consumedItemsHistory.size() > 50) {
                        consumedItemsHistory.remove(0);
                    }
                }

                totalConsumed.incrementAndGet();
                synchronized (consumer) {
                    consumer.setItemsConsumed(consumer.getItemsConsumed() + 1);
                }

                Thread.yield();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                addLog("消费者线程异常: " + ex.getMessage());
            }
        }

        consumer.setWaiting(true);
    }
    
    // 获取槽位在数组中的索引位置
    private int getSlotIndex(BufferItem slot) {
        for (int i = 0; i < bufferSlots.length; i++) {
            if (bufferSlots[i] == slot) {
                return i;
            }
        }
        return -1; // 不应该发生
    }

    // 开始模拟
    public synchronized void startSimulation() {
        if (isRunning) return;

        isRunning = true;

        // 创建线程池
        producerThreadPool = Executors.newFixedThreadPool(Math.max(1, producers.size()));
        consumerThreadPool = Executors.newFixedThreadPool(Math.max(1, consumers.size()));

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

        // 尝试优雅关闭线程池（不强制中断正在执行的任务）
        if (producerThreadPool != null) {
            producerThreadPool.shutdown();
        }

        if (consumerThreadPool != null) {
            consumerThreadPool.shutdown();
        }

        // 设置所有生产者和消费者为等待状态
        if (producers != null) {
            for (Producer producer : producers) {
                producer.setWaiting(true);
            }
        }

        if (consumers != null) {
            for (Consumer consumer : consumers) {
                consumer.setWaiting(true);
            }
        }

        addLog("模拟已停止");
    }

    // 继续模拟 - 恢复停止时的状态
    public synchronized void continueSimulation() {
        if (isRunning) return;

        isRunning = true;

        // 创建新的线程池
        producerThreadPool = Executors.newFixedThreadPool(Math.max(1, producers.size()));
        consumerThreadPool = Executors.newFixedThreadPool(Math.max(1, consumers.size()));

        // 使用一个集合来跟踪已经为其创建线程的生产者
        Set<String> processedProducers = new HashSet<>();
        
        // 首先完成正在生产中的物品
        for (int i = 0; i < bufferSlots.length; i++) {
            final int slotIndex = i;
            BufferItem slot = bufferSlots[i];
            
            synchronized (slot.getSlotLock()) {
                if (slot != null && "生产中".equals(slot.getState()) && slot.getProducerId() != null) {
                    // 找到对应的生产者
                    for (Producer producer : producers) {
                        if (producer.getId().equals(slot.getProducerId())) {
                            final Producer assignedProducer = producer;
                            final int itemValue = slot.getValue();
                            
                            // 标记此生产者已处理，避免重复创建线程
                            processedProducers.add(producer.getId());
                            
                            // 提交到线程池继续完成生产，完成后自动进入正常生产循环
                            producerThreadPool.submit(() -> {
                                try {
                                    // 继续完成生产
                                    Thread.sleep(getProductionDelay());
                                    
                                    synchronized (slot.getSlotLock()) {
                                        // 再次检查状态，确保槽位未被重置
                                        if (slot.getProducerId() != null && assignedProducer.getId().equals(slot.getProducerId()) && "生产中".equals(slot.getState())) {
                                            slot.setState("已完成");
                                            itemCount.incrementAndGet();
                                        }
                                    }
                                    
                                    totalProduced.incrementAndGet();
                                    synchronized (assignedProducer) {
                                        assignedProducer.setItemsProduced(assignedProducer.getItemsProduced() + 1);
                                    }
                                    
                                    addLog(assignedProducer.getId() + " 在槽位 " + slotIndex + " 完成了之前未完成的物品 " + itemValue);
                                    
                                    // 完成后继续正常生产循环
                                    runProducer(assignedProducer);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }

        // 然后为尚未处理的生产者启动正常的生产线程
        for (Producer producer : producers) {
            if (!processedProducers.contains(producer.getId())) {
                producerThreadPool.submit(() -> runProducer(producer));
            }
        }

        // 启动消费者线程
        for (Consumer consumer : consumers) {
            consumerThreadPool.submit(() -> runConsumer(consumer));
        }

        addLog("模拟已继续");
    }

    // 获取当前状态
    public synchronized ProducerConsumerStatusDTO getStatus() {
        // 创建槽位快照（为避免返回内部引用，做浅拷贝）
        List<BufferItem> bufferSnapshot = new ArrayList<>();
        synchronized (lock) {
            for (int i = 0; i < bufferSlots.length; i++) {
                BufferItem s = bufferSlots[i];
                if (s == null) {
                    BufferItem empty = new BufferItem();
                    empty.setState("空");
                    bufferSnapshot.add(empty);
                } else {
                    // 尝试深拷贝关键字段到新对象（避免前端修改内部数据）
                    BufferItem copy = new BufferItem();
                    copy.setValue(s.getValue());
                    copy.setProducerId(s.getProducerId());
                    copy.setConsumerId(s.getConsumerId());
                    copy.setTimestamp(s.getTimestamp());
                    copy.setConsumed(s.isConsumed());
                    copy.setWaitTime(s.getWaitTime());
                    copy.setState(s.getState());
                    bufferSnapshot.add(copy);
                }
            }
        }

        List<Producer> producerSnapshot = new ArrayList<>(producers);
        List<Consumer> consumerSnapshot = new ArrayList<>(consumers);
        List<String> logSnapshot = new ArrayList<>(operationLogs);

        ProducerConsumerStatusDTO.Stats stats = new ProducerConsumerStatusDTO.Stats(
                totalProduced.get(), totalConsumed.get(), bufferFullCount.get(), bufferEmptyCount.get()
        );

        // headPointer 不再有意义，tailPointer 使用 bufferSize 表示总槽位数
        return new ProducerConsumerStatusDTO(
                bufferSnapshot,
                0,
                bufferSnapshot.size(),
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
        // 返回历史快照（浅拷贝）
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

    // 获取生产延迟
    private long getProductionDelay() {
        // 按用户要求，不使用随机因子
        return productionSpeed;
    }

    // 获取消费延迟
    private long getConsumptionDelay() {
        // 按用户要求，不使用随机因子
        return consumptionSpeed;
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
        // 重新初始化槽位（保留原行为：调整大小时重新创建槽位并初始化为 "空"）
        bufferSlots = new BufferItem[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            BufferItem slot = new BufferItem();
            slot.setState("空");
            bufferSlots[i] = slot;
        }
        addLog("缓冲区大小已设置为: " + bufferSize);
    }

    // 缓冲区大小的getter方法
    public int getBufferSize() {
        return bufferSize;
    }

    // 为每个生产者维护当前槽位索引，用于循环选择槽位
    private final Map<String, Integer> producerCurrentSlotIndex = new ConcurrentHashMap<>();
    
    // 为每个消费者维护当前槽位索引，用于循环选择槽位
    private final Map<String, Integer> consumerCurrentSlotIndex = new ConcurrentHashMap<>();

    // 查找可用于生产的槽位（"空" 或 "已消费"）并尝试锁定
    private BufferItem findSlotForProduction(Producer producer) {
        // 初始化当前生产者的起始索引（如果不存在）
        int startIndex = producerCurrentSlotIndex.getOrDefault(producer.getId(), 0);
        int currentIndex = startIndex;
        int checkedSlots = 0;
        
        // 遍历所有槽位，从上次使用的下一个位置开始
        while (checkedSlots < bufferSlots.length) {
            BufferItem slot = bufferSlots[currentIndex];
            synchronized (slot.getSlotLock()) {
                if ("空".equals(slot.getState()) || "已消费".equals(slot.getState())) {
                    // 在返回槽位前就标记为生产中，防止其他生产者抢占
                    slot.setProducerId(producer.getId());
                    slot.setTimestamp(LocalDateTime.now());
                    slot.setConsumed(false);
                    slot.setWaitTime(0);
                    slot.setState("生产中");
                    slot.setConsumerId(null);
                    
                    // 获取槽位索引用于日志
                    int slotIndex = currentIndex;
                    addLog(producer.getId() + " 在槽位 " + slotIndex + " 开始生产");
                    
                    // 更新当前生产者的下一个起始索引
                    producerCurrentSlotIndex.put(producer.getId(), (currentIndex + 1) % bufferSlots.length);
                    
                    // 二次确认，确保槽位确实被当前生产者锁定
                    if ("生产中".equals(slot.getState()) && producer.getId().equals(slot.getProducerId())) {
                        return slot;
                    }
                }
            }
            
            // 移动到下一个槽位
            currentIndex = (currentIndex + 1) % bufferSlots.length;
            checkedSlots++;
        }
        
        // 如果没有找到可用槽位，也更新索引以便下次从新位置开始
        producerCurrentSlotIndex.put(producer.getId(), (startIndex + 1) % bufferSlots.length);
        return null;
    }
     
    // 查找已完成的槽位（"已完成"）并尝试锁定
    private BufferItem findCompletedSlot(Consumer consumer) {
        // 初始化当前消费者的起始索引（如果不存在）
        int startIndex = consumerCurrentSlotIndex.getOrDefault(consumer.getId(), 0);
        int currentIndex = startIndex;
        int checkedSlots = 0;
        
        // 遍历所有槽位，从上次使用的下一个位置开始
        while (checkedSlots < bufferSlots.length) {
            BufferItem slot = bufferSlots[currentIndex];
            synchronized (slot.getSlotLock()) {
                if ("已完成".equals(slot.getState())) {
                    // 更新当前消费者的下一个起始索引
                    consumerCurrentSlotIndex.put(consumer.getId(), (currentIndex + 1) % bufferSlots.length);
                    // 发现已完成的槽位，返回它
                    return slot;
                }
            }
            
            // 移动到下一个槽位
            currentIndex = (currentIndex + 1) % bufferSlots.length;
            checkedSlots++;
        }
        
        // 如果没有找到可用槽位，也更新索引以便下次从新位置开始
        consumerCurrentSlotIndex.put(consumer.getId(), (startIndex + 1) % bufferSlots.length);
        return null;
    }


}
