<template>
  <div class="producer-consumer-container">
    <h1>生产者-消费者问题演示</h1>
    
    <!-- 参数设置区域 -->
    <div class="settings-section">
      <div class="setting-item">
        <label for="buffer-size">缓冲区大小 (a):</label>
        <input type="number" id="buffer-size" v-model.number="bufferSize" min="1" max="20">
      </div>
      <div class="setting-item">
        <label for="producer-count">生产者数量 (b):</label>
        <input type="number" id="producer-count" v-model.number="producerCount" min="1" max="10">
      </div>
      <div class="setting-item">
        <label for="consumer-count">消费者数量 (c):</label>
        <input type="number" id="consumer-count" v-model.number="consumerCount" min="1" max="10">
      </div>
      <div class="setting-item">
        <label for="simulation-speed">模拟速度 (ms):</label>
        <input type="range" id="simulation-speed" v-model.number="simulationSpeed" min="200" max="2000" step="100">
        <span>{{ simulationSpeed }}ms</span>
      </div>
      <button @click="startSimulation" :disabled="isRunning" class="btn btn-start">开始模拟</button>
      <button @click="stopSimulation" :disabled="!isRunning" class="btn btn-stop">停止模拟</button>
      <button @click="resetSimulation" class="btn btn-reset">重置</button>
    </div>
    
    <!-- 缓冲区显示区域 -->
    <div class="buffer-display">
      <h2>缓冲区状态</h2>
      <div class="buffer-container">
        <div 
          v-for="(item, index) in buffer" 
          :key="index" 
          class="buffer-item"
          :class="{
            'buffer-item-filled': item !== null,
            'buffer-item-head': index === headPointer,
            'buffer-item-tail': index === tailPointer
          }"
        >
          <span v-if="item !== null">{{ item.value }}</span>
          <span v-else>空</span>
          <div v-if="index === headPointer" class="pointer-marker head-marker">H</div>
          <div v-if="index === tailPointer" class="pointer-marker tail-marker">T</div>
        </div>
      </div>
      <div class="pointer-info">
        <p>队头指针(H): {{ headPointer }} | 队尾指针(T): {{ tailPointer }} | 元素数量: {{ itemCount }}</p>
      </div>
    </div>
    
    <!-- 统计信息区域 -->
    <div class="stats-section">
      <h2>统计信息</h2>
      <div class="stats-container">
        <div class="stat-item">
          <span class="stat-label">已生产物品:</span>
          <span class="stat-value">{{ stats.totalProduced }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已消费物品:</span>
          <span class="stat-value">{{ stats.totalConsumed }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">缓冲区已满次数:</span>
          <span class="stat-value">{{ stats.bufferFullCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">缓冲区为空次数:</span>
          <span class="stat-value">{{ stats.bufferEmptyCount }}</span>
        </div>
      </div>
    </div>
    
    <!-- 操作日志区域 -->
    <div class="operation-log">
      <h2>操作日志</h2>
      <div class="log-container">
        <div v-for="(log, index) in operationLogs" :key="index" class="log-item">
          {{ log }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProducerConsumerDemo',
  data() {
    return {
      bufferSize: 5,
      producerCount: 2,
      consumerCount: 2,
      buffer: [],
      headPointer: 0, // 队头指针，消费者从这里取出元素
      tailPointer: 0, // 队尾指针，生产者从这里放入元素
      itemCount: 0, // 当前缓冲区中的元素数量
      operationLogs: [],
      isRunning: false,
      simulationInterval: null,
      producers: [],
      consumers: [],
      nextItemValue: 1,
      simulationSpeed: 1000,
      stats: {
        totalProduced: 0,
        totalConsumed: 0,
        bufferFullCount: 0,
        bufferEmptyCount: 0
      }
    }
  },
  created() {
    this.resetSimulation();
  },
  methods: {
    resetSimulation() {
      this.stopSimulation();
      this.buffer = Array(this.bufferSize).fill(null);
      this.headPointer = 0;
      this.tailPointer = 0;
      this.itemCount = 0;
      this.operationLogs = [];
      this.nextItemValue = 1;
      this.producers = Array(this.producerCount).fill().map((_, i) => ({ id: `P${i+1}`, waiting: false }));
      this.consumers = Array(this.consumerCount).fill().map((_, i) => ({ id: `C${i+1}`, waiting: false }));
      
      // 重置统计数据
      this.stats = {
        totalProduced: 0,
        totalConsumed: 0,
        bufferFullCount: 0,
        bufferEmptyCount: 0
      };
      
      this.addLog('模拟已重置');
    },
    
    startSimulation() {
      if (this.isRunning) return;
      
      this.isRunning = true;
      this.simulationStep(); // 立即开始第一步
      
      this.addLog(`模拟开始，速度: ${this.simulationSpeed}ms`);
    },
    
    stopSimulation() {
      if (this.simulationTimeout) {
        clearTimeout(this.simulationTimeout);
        this.simulationTimeout = null;
      }
      this.isRunning = false;
      
      if (this.operationLogs.length > 0) {
        this.addLog('模拟已暂停');
      }
    },
    
    simulationStep() {
      if (!this.isRunning) return;
      
      // 计算缓冲区使用率 - 使用元素计数
      const bufferUsageRatio = this.itemCount / this.bufferSize;
      
      // 根据缓冲区使用率动态调整生产和消费的概率
      let produceChance;
      
      if (bufferUsageRatio > 0.8) {
        // 缓冲区较满，降低生产概率
        produceChance = 0.3;
      } else if (bufferUsageRatio < 0.2) {
        // 缓冲区较空，提高生产概率
        produceChance = 0.7;
      } else {
        // 缓冲区使用率适中，平衡生产和消费
        produceChance = 0.5;
      }
      
      // 随机决定是生产还是消费
      if (Math.random() < produceChance) {
        this.produceItem();
      } else {
        this.consumeItem();
      }
      
      // 安排下一步
      this.simulationTimeout = setTimeout(() => {
        this.simulationStep();
      }, this.simulationSpeed);
    },
    
    produceItem() {
      // 检查缓冲区是否已满 - 使用元素计数判断
      const isFull = this.itemCount >= this.bufferSize;
      
      if (isFull) {
        // 所有生产者等待
        this.producers.forEach(p => p.waiting = true);
        this.addLog('缓冲区已满，所有生产者等待');
        this.stats.bufferFullCount++;
        return;
      }
      
      // 随机选择一个生产者
      const availableProducers = this.producers.filter(p => !p.waiting);
      if (availableProducers.length === 0) return;
      
      const producerIndex = Math.floor(Math.random() * availableProducers.length);
      const producer = availableProducers[producerIndex];
      
      // 生产物品 - 只在队尾(tailPointer)添加
      const newItem = {
        value: this.nextItemValue++,
        producer: producer.id,
        timestamp: Date.now()
      };
      
      // 在队尾位置添加元素
      this.buffer[this.tailPointer] = newItem;
      
      // 更新队尾指针 - 循环移动
      this.tailPointer = (this.tailPointer + 1) % this.bufferSize;
      
      // 增加元素计数
      this.itemCount++;
      this.stats.totalProduced++;
      
      // 添加日志
      this.addLog(`${producer.id} 在队尾位置 ${(this.tailPointer - 1 + this.bufferSize) % this.bufferSize} 生产了物品 ${newItem.value}`);
      
      // 如果有等待的消费者，唤醒它们
      if (this.consumers.some(c => c.waiting)) {
        this.consumers.forEach(c => c.waiting = false);
        this.addLog('缓冲区有物品，唤醒等待的消费者');
      }
    },
    
    consumeItem() {
      // 检查缓冲区是否为空 - 使用元素计数判断
      const isEmpty = this.itemCount === 0;
      
      if (isEmpty) {
        // 所有消费者等待
        this.consumers.forEach(c => c.waiting = true);
        this.addLog('缓冲区为空，所有消费者等待');
        this.stats.bufferEmptyCount++;
        return;
      }
      
      // 随机选择一个消费者
      const availableConsumers = this.consumers.filter(c => !c.waiting);
      if (availableConsumers.length === 0) return;
      
      const consumerIndex = Math.floor(Math.random() * availableConsumers.length);
      const consumer = availableConsumers[consumerIndex];
      
      // 从队头(headPointer)取出元素 - 实现FIFO
      const consumedItem = this.buffer[this.headPointer];
      
      if (!consumedItem) return; // 安全检查
      
      const waitTime = Date.now() - consumedItem.timestamp;
      
      // 清空队头位置
      this.buffer[this.headPointer] = null;
      
      // 更新队头指针 - 循环移动
      this.headPointer = (this.headPointer + 1) % this.bufferSize;
      
      // 减少元素计数
      this.itemCount--;
      this.stats.totalConsumed++;
      
      // 添加日志
      this.addLog(`${consumer.id} 从队头位置 ${(this.headPointer - 1 + this.bufferSize) % this.bufferSize} 消费了物品 ${consumedItem.value}，等待时间: ${waitTime}ms，由 ${consumedItem.producer} 生产`);
      
      // 如果有等待的生产者，唤醒它们
      if (this.producers.some(p => p.waiting)) {
        this.producers.forEach(p => p.waiting = false);
        this.addLog('缓冲区有空位，唤醒等待的生产者');
      }
    },
    
    addLog(message) {
      const timestamp = new Date().toLocaleTimeString();
      this.operationLogs.unshift(`[${timestamp}] ${message}`);
      
      // 限制日志数量
      if (this.operationLogs.length > 10) {
        this.operationLogs.pop();
      }
    }
  }
}
</script>

<style scoped>
.producer-consumer-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: Arial, sans-serif;
}

.producer-consumer-container h1 {
  color: #2c3e50;
  margin-bottom: 20px;
  text-align: center;
}

.settings-section {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.setting-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

input[type="number"], input[type="range"] {
  width: 80px;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.btn {
  padding: 10px 15px;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 10px;
  font-weight: bold;
  transition: background-color 0.3s;
}

.btn-start {
  background-color: #4CAF50;
}

.btn-stop {
  background-color: #f44336;
}

.btn-reset {
  background-color: #2196F3;
}

button:hover {
  opacity: 0.9;
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.buffer-display {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.buffer-container {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.buffer-item {
  width: 60px;
  height: 60px;
  border: 2px solid #333;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  background-color: #f9f9f9;
  border-radius: 4px;
  transition: all 0.3s;
  position: relative; /* 添加相对定位以支持指针标记的绝对定位 */
}

.buffer-item-filled {
  background-color: #c8e6c9;
  transform: scale(1.05);
}

.buffer-item-head {
  border: 2px solid #4CAF50;
  background-color: rgba(76, 175, 80, 0.1);
}

.buffer-item-tail {
  border: 2px solid #FF9800;
  background-color: rgba(255, 152, 0, 0.1);
}

.pointer-marker {
  position: absolute;
  top: -20px;
  font-size: 12px;
  font-weight: bold;
  padding: 2px 5px;
  border-radius: 50%;
  color: white;
}

.head-marker {
  background-color: #4CAF50;
  left: 5px;
}

.tail-marker {
  background-color: #FF9800;
  right: 5px;
}

.pointer-info {
  margin-top: 15px;
  font-weight: bold;
  text-align: center;
  color: #2c3e50;
}

.stats-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #e8f5e9;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.stats-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
  padding: 10px;
  background-color: white;
  border-radius: 4px;
  min-width: 150px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.stat-label {
  display: block;
  font-weight: bold;
  margin-bottom: 5px;
  color: #555;
}

.stat-value {
  font-size: 1.5em;
  color: #2c3e50;
}

.operation-log {
  margin-top: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.log-container {
  max-height: 250px;
  overflow-y: auto;
  border: 1px solid #ddd;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.log-item {
  padding: 8px 5px;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.log-item:hover {
  background-color: #f0f0f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-container {
    flex-direction: column;
    align-items: center;
  }
  
  .buffer-item {
    width: 50px;
    height: 50px;
  }
  
  .setting-item {
    width: 100%;
    justify-content: space-between;
  }
}
</style>