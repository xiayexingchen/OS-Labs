<template>
  <div class="producer-consumer-container">
    <h1>生产者-消费者问题演示</h1>
    
    <!-- 参数设置区域 -->
    <div class="settings-section">
      <div class="setting-item">
        <label for="buffer-size">缓冲区大小:</label>
        <input type="number" id="buffer-size" v-model.number="bufferSize" min="1" max="20">
      </div>
      <div class="setting-item">
        <label for="producer-count">生产者数量:</label>
        <input type="number" id="producer-count" v-model.number="producerCount" min="1" max="10">
      </div>
      <div class="setting-item">
        <label for="consumer-count">消费者数量:</label>
        <input type="number" id="consumer-count" v-model.number="consumerCount" min="1" max="10">
      </div>
      <div class="setting-item">
        <label for="simulation-speed">模拟速度:</label>
        <input type="range" id="simulation-speed" v-model.number="simulationSpeed" min="200" max="2000" step="100">
        <span>{{ simulationSpeed }}ms</span>
      </div>
      <div class="setting-item">
        <label for="production-speed">生产速度:</label>
        <input type="range" id="production-speed" v-model.number="productionSpeed" min="1000" max="5000" step="500">
        <span>{{ productionSpeed / 1000 }}秒</span>
      </div>
      <div class="setting-item">
        <label for="consumption-speed">消费速度:</label>
        <input type="range" id="consumption-speed" v-model.number="consumptionSpeed" min="1000" max="5000" step="500">
        <span>{{ consumptionSpeed / 1000 }}秒</span>
      </div>
      <button @click="startSimulation" :disabled="isRunning" class="btn btn-start">
        <i class="icon">▶</i> 开始模拟
      </button>
      <button @click="stopSimulation" :disabled="!isRunning" class="btn btn-stop">
        <i class="icon">⏸</i> 停止模拟
      </button>
      <button @click="resetSimulation" class="btn btn-reset">
        <i class="icon">🔄</i> 重置
      </button>
    </div>
    
    <!-- 主内容区域 -->
    <div class="main-content">
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
          <p>队头指针(H): {{ headPointer }} | 队尾指针(T): {{ tailPointer }} | 元素数量: {{ itemCount }}/{{ bufferSize }}</p>
        </div>
        
        <!-- 缓冲区使用百分比进度条 -->
        <div class="buffer-progress-container">
          <div class="buffer-progress-bar">
            <div class="buffer-progress-fill" :style="{ width: bufferUsage + '%' }"></div>
          </div>
          <span class="buffer-usage-text">{{ bufferUsage }}% 使用率</span>
        </div>
      </div>
      
      <!-- 生产者和消费者状态区域 -->
      <div class="entities-status">
        <!-- 生产者状态 -->
        <div class="entity-group">
          <h3>生产者状态</h3>
          <div class="entity-container">
            <div 
              v-for="producer in producers" 
              :key="producer.id"
              class="entity-item"
              :class="{
                'entity-active': !producer.waiting,
                'entity-waiting': producer.waiting,
                'producer': true
              }"
            >
              <span class="entity-id">{{ producer.id }}</span>
              <span class="entity-status">
                {{ producer.waiting ? '等待中' : '活跃' }}
              </span>
            </div>
          </div>
        </div>
        
        <!-- 消费者状态 -->
        <div class="entity-group">
          <h3>消费者状态</h3>
          <div class="entity-container">
            <div 
              v-for="consumer in consumers" 
              :key="consumer.id"
              class="entity-item"
              :class="{
                'entity-active': !consumer.waiting,
                'entity-waiting': consumer.waiting,
                'consumer': true
              }"
            >
              <span class="entity-id">{{ consumer.id }}</span>
              <span class="entity-status">
                {{ consumer.waiting ? '等待中' : '活跃' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 统计信息区域 -->
    <div class="stats-section">
      <h2>统计信息</h2>
      <div class="stats-container">
        <div class="stat-item">
          <span class="stat-icon">📦</span>
          <span class="stat-label">已生产物品</span>
          <span class="stat-value">{{ stats.totalProduced }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-icon">🍽️</span>
          <span class="stat-label">已消费物品</span>
          <span class="stat-value">{{ stats.totalConsumed }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-icon">⚠️</span>
          <span class="stat-label">缓冲区已满次数</span>
          <span class="stat-value">{{ stats.bufferFullCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-icon">📭</span>
          <span class="stat-label">缓冲区为空次数</span>
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
        <div v-if="operationLogs.length === 0" class="log-empty">
          暂无操作日志
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
      headPointer: 0,
      tailPointer: 0,
      itemCount: 0,
      operationLogs: [],
      isRunning: false,
      simulationSpeed: 1000,
      productionSpeed: 3000, // 默认生产速度3秒
      consumptionSpeed: 3000, // 默认消费速度3秒
      producers: [],
      consumers: [],
      stats: {
        totalProduced: 0,
        totalConsumed: 0,
        bufferFullCount: 0,
        bufferEmptyCount: 0
      },
      statusPollingInterval: null,
      pollingInterval: 1000 // 每1000ms获取一次状态，与后端模拟速度相匹配
    }
  },
  created() {
    this.initializeSimulation();
  },
  beforeUnmount() {
    this.stopStatusPolling();
  },
  computed: {
    // 计算缓冲区使用率百分比
    bufferUsage() {
      return Math.round((this.itemCount / this.bufferSize) * 100);
    }
  },
  methods: {
    async initializeSimulation() {
      try {
        const response = await this.$axios.post('/api/producer-consumer/init', {
          bufferSize: this.bufferSize,
          producerCount: this.producerCount,
          consumerCount: this.consumerCount,
          simulationSpeed: this.simulationSpeed,
          productionSpeed: this.productionSpeed,
          consumptionSpeed: this.consumptionSpeed
        });
        this.updateFromStatus(response.data);
      } catch (error) {
        console.error('初始化失败:', error);
        alert('后端服务未启动或连接失败，请确保Spring Boot应用正在运行');
        // 使用本地模拟作为备用
        this.useLocalSimulation();
      }
    },

    useLocalSimulation() {
      console.log('使用本地模拟模式');
      this.buffer = Array(this.bufferSize).fill(null);
      this.headPointer = 0;
      this.tailPointer = 0;
      this.itemCount = 0;
      this.producers = Array(this.producerCount).fill().map((_, i) => ({ id: `P${i+1}`, waiting: false }));
      this.consumers = Array(this.consumerCount).fill().map((_, i) => ({ id: `C${i+1}`, waiting: false }));
      this.operationLogs = ['[本地模式] 系统已初始化'];
      this.stats = {
        totalProduced: 0,
        totalConsumed: 0,
        bufferFullCount: 0,
        bufferEmptyCount: 0
      };
      this.localSimulationInterval = null;
    },
    
    // 启动本地模拟
    startLocalSimulation() {
      if (this.isRunning) return;
      
      this.isRunning = true;
      this.addLog(`本地模拟开始，速度: ${this.simulationSpeed}ms`);
      
      // 清除之前的定时器
      if (this.localSimulationInterval) {
        clearInterval(this.localSimulationInterval);
      }
      
      // 设置新的模拟间隔，根据simulationSpeed调整速度
      this.localSimulationInterval = setInterval(() => {
        this.runLocalSimulationStep();
      }, this.simulationSpeed);
    },
    
    // 停止本地模拟
    stopLocalSimulation() {
      if (!this.isRunning) return;
      
      this.isRunning = false;
      if (this.localSimulationInterval) {
        clearInterval(this.localSimulationInterval);
        this.localSimulationInterval = null;
      }
      if (this.operationLogs.length > 0) {
        this.addLog('本地模拟已暂停');
      }
    },
    
    // 运行本地模拟的一步
    runLocalSimulationStep() {
      // 随机选择生产或消费操作，使变化更平滑
      const operationType = Math.random() > 0.5 ? 'produce' : 'consume';
      
      if (operationType === 'produce') {
        this.tryProduce();
      } else {
        this.tryConsume();
      }
    },
    
    // 尝试生产
    tryProduce() {
      // 优先选择非等待状态的生产者，避免重复选择同一生产者
      let availableProducers = this.producers.filter(p => !p.waiting);
      let producer;
      
      // 如果所有生产者都在等待，则随机选择一个
      if (availableProducers.length === 0) {
        availableProducers = this.producers;
      }
      
      // 随机选择一个可用生产者
      const producerIndex = Math.floor(Math.random() * availableProducers.length);
      producer = availableProducers[producerIndex];
      
      // 重置所有生产者状态为非等待（避免状态混乱）
      this.producers.forEach(p => {
        if (this.itemCount < this.bufferSize) {
          p.waiting = false;
        }
      });
      
      if (this.itemCount < this.bufferSize) {
        // 缓冲区未满，可以生产
        const value = Math.floor(Math.random() * 100) + 1; // 生成1-100的随机数
        this.buffer[this.tailPointer] = { value };
        
        // 更新队尾指针
        this.tailPointer = (this.tailPointer + 1) % this.bufferSize;
        this.itemCount++;
        
        // 更新统计信息
        this.stats.totalProduced++;
        
        // 更新生产者状态
        producer.waiting = false;
        
        this.addLog(`生产者 ${producer.id} 生产了物品 ${value}`);
      } else {
        // 缓冲区已满，所有生产者等待
        this.producers.forEach(p => p.waiting = true);
        this.stats.bufferFullCount++;
        this.addLog(`缓冲区已满，所有生产者等待`);
      }
    },
    
    // 尝试消费
    tryConsume() {
      // 优先选择非等待状态的消费者，避免重复选择同一消费者
      let availableConsumers = this.consumers.filter(c => !c.waiting);
      let consumer;
      
      // 如果所有消费者都在等待，则随机选择一个
      if (availableConsumers.length === 0) {
        availableConsumers = this.consumers;
      }
      
      // 随机选择一个可用消费者
      const consumerIndex = Math.floor(Math.random() * availableConsumers.length);
      consumer = availableConsumers[consumerIndex];
      
      // 重置所有消费者状态为非等待（避免状态混乱）
      this.consumers.forEach(c => {
        if (this.itemCount > 0) {
          c.waiting = false;
        }
      });
      
      if (this.itemCount > 0) {
        // 缓冲区非空，可以消费
        const consumedItem = this.buffer[this.headPointer];
        this.buffer[this.headPointer] = null;
        
        // 更新队头指针
        this.headPointer = (this.headPointer + 1) % this.bufferSize;
        this.itemCount--;
        
        // 更新统计信息
        this.stats.totalConsumed++;
        
        // 更新消费者状态
        consumer.waiting = false;
        
        this.addLog(`消费者 ${consumer.id} 消费了物品 ${consumedItem.value}`);
      } else {
        // 缓冲区为空，所有消费者等待
        this.consumers.forEach(c => c.waiting = true);
        this.stats.bufferEmptyCount++;
        this.addLog(`缓冲区为空，所有消费者等待`);
      }
    },

    async resetSimulation() {
      try {
        await this.stopSimulation();
        await this.$axios.post('/api/producer-consumer/reset');
        await this.initializeSimulation();
        this.addLog('模拟已重置');
      } catch (error) {
        console.error('重置失败:', error);
        // 本地模式下的重置
        this.useLocalSimulation();
      }
    },
    
    async startSimulation() {
      if (this.isRunning) return;
      
      try {
        const response = await this.$axios.post('/api/producer-consumer/start');
        this.isRunning = true;
        this.updateFromStatus(response.data);
        this.startStatusPolling();
        this.addLog(`模拟开始，速度: ${this.simulationSpeed}ms`);
      } catch (error) {
        console.error('开始模拟失败:', error);
        // 使用本地模拟
        this.startLocalSimulation();
      }
    },
    
    async stopSimulation() {
      if (!this.isRunning) return;
      
      try {
        const response = await this.$axios.post('/api/producer-consumer/stop');
        this.isRunning = false;
        this.updateFromStatus(response.data);
        this.stopStatusPolling();
        if (this.operationLogs.length > 0) {
          this.addLog('模拟已暂停');
        }
      } catch (error) {
        console.error('停止模拟失败:', error);
        this.isRunning = false;
        this.stopStatusPolling();
        this.stopLocalSimulation();
      }
    },

    startStatusPolling() {
      this.stopStatusPolling(); // 确保之前的轮询已停止
      this.statusPollingInterval = setInterval(() => {
        this.fetchStatus();
      }, this.pollingInterval);
    },

    stopStatusPolling() {
      if (this.statusPollingInterval) {
        clearInterval(this.statusPollingInterval);
        this.statusPollingInterval = null;
      }
    },

    async fetchStatus() {
      try {
        const response = await this.$axios.get('/api/producer-consumer/status');
        this.updateFromStatus(response.data);
      } catch (error) {
        console.error('获取状态失败:', error);
        this.isRunning = false;
        this.stopStatusPolling();
      }
    },

    updateFromStatus(status) {
      if (!status) return;
      
      // 更新缓冲区和指针信息
      this.buffer = status.buffer || [];
      this.headPointer = status.headPointer || 0;
      this.tailPointer = status.tailPointer || 0;
      this.itemCount = status.itemCount || 0;
      this.bufferSize = status.bufferSize || this.bufferSize;
      
      // 更新生产者和消费者信息，完全使用后端返回的真实状态数据
      // 后端API已确认返回包含waiting属性的生产者和消费者数据
      let producersFromBackend = status.producers || [];
      let consumersFromBackend = status.consumers || [];
      
      // 处理生产者数据 - 完全依赖后端返回的waiting属性
      this.producers = producersFromBackend.map(producer => {
        return {
          ...producer,
          // 确保waiting属性存在，默认为false（活跃状态）
          waiting: producer.waiting !== undefined ? producer.waiting : false
        };
      });
      
      // 处理消费者数据 - 完全依赖后端返回的waiting属性
      this.consumers = consumersFromBackend.map(consumer => {
        return {
          ...consumer,
          // 确保waiting属性存在，默认为false（活跃状态）
          waiting: consumer.waiting !== undefined ? consumer.waiting : false
        };
      });
      
      // 更新统计信息
      if (status.stats) {
        this.stats = status.stats;
      }
      
      // 更新日志（避免重复）
      if (status.logs && status.logs.length > 0) {
        this.operationLogs = status.logs;
      }
    },
    
    addLog(message) {
      const timestamp = new Date().toLocaleTimeString();
      this.operationLogs.unshift(`[${timestamp}] ${message}`);
      
      // 限制日志数量
      if (this.operationLogs.length > 20) {
        this.operationLogs.pop();
      }
    }
  }
}
</script>

<style scoped>
/* 全局样式 */
.producer-consumer-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  color: #333;
}

.producer-consumer-container > * {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  padding: 24px;
  margin-bottom: 24px;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.producer-consumer-container h1 {
  color: #2c3e50;
  margin: -32px auto 0;
  text-align: center;
  font-size: 2.2rem;
  background: white;
  padding: 0 30px;
  border-radius: 25px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  width: fit-content;
  position: relative;
}

/* 设置区域样式 */
.settings-section {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  justify-content: center;
  align-items: center;
  padding: 25px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.setting-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: white;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.setting-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.setting-item label {
  font-weight: 600;
  color: #4a5568;
  min-width: 120px;
}

input[type="number"], input[type="range"] {
  padding: 8px 12px;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  font-size: 16px;
  transition: border-color 0.3s;
}

input[type="number"] {
  width: 80px;
}

input[type="range"] {
  width: 120px;
}

input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.btn {
  padding: 12px 24px;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 120px;
  justify-content: center;
}

.btn i.icon {
  font-size: 18px;
}

.btn-start {
  background: linear-gradient(135deg, #4CAF50, #45a049);
}

.btn-stop {
  background: linear-gradient(135deg, #f44336, #da190b);
}

.btn-reset {
  background: linear-gradient(135deg, #2196F3, #0b7dda);
}

.btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.15);
}

.btn:active:not(:disabled) {
  transform: translateY(0);
}

.btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 主内容区域布局 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

/* 缓冲区显示样式 */
.buffer-display {
  background: white;
}

.buffer-display h2 {
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 1.5rem;
  display: flex;
  align-items: center;
  gap: 10px;
}

.buffer-container {
  display: flex;
  gap: 12px;
  margin: 20px 0;
  justify-content: center;
  flex-wrap: wrap;
}

.buffer-item {
  width: 70px;
  height: 70px;
  border: 3px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  background-color: #f9f9f9;
  border-radius: 8px;
  transition: all 0.3s;
  position: relative;
  font-size: 18px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.buffer-item-filled {
  background: linear-gradient(135deg, #c8e6c9, #a5d6a7);
  border-color: #4CAF50;
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(76, 175, 80, 0.2);
}

.buffer-item-head {
  border: 3px solid #4CAF50;
  background: rgba(76, 175, 80, 0.1);
}

.buffer-item-tail {
  border: 3px solid #FF9800;
  background: rgba(255, 152, 0, 0.1);
}

.pointer-marker {
  position: absolute;
  top: -25px;
  font-size: 14px;
  font-weight: bold;
  padding: 4px 10px;
  border-radius: 15px;
  color: white;
  box-shadow: 0 2px 6px rgba(0,0,0,0.15);
}

.head-marker {
  background: linear-gradient(135deg, #4CAF50, #45a049);
  left: 50%;
  transform: translateX(-50%);
}

.tail-marker {
  background: linear-gradient(135deg, #FF9800, #f57c00);
  left: 50%;
  transform: translateX(-50%);
  top: auto;
  bottom: -25px;
}

.pointer-info {
  margin-top: 30px;
  font-weight: bold;
  text-align: center;
  color: #4a5568;
  font-size: 16px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

/* 缓冲区进度条 */
.buffer-progress-container {
  margin-top: 20px;
}

.buffer-progress-bar {
  width: 100%;
  height: 20px;
  background-color: #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.05);
}

.buffer-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #45a049);
  transition: width 0.3s ease;
  border-radius: 10px;
}

.buffer-usage-text {
  display: block;
  text-align: center;
  margin-top: 8px;
  font-weight: 600;
  color: #4a5568;
}

/* 生产者和消费者状态样式 */
.entities-status {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.entity-group {
  background: #f8fafc;
  border-radius: 10px;
  padding: 20px;
  border: 1px solid #e2e8f0;
}

.entity-group h3 {
  color: #2c3e50;
  margin-bottom: 15px;
  font-size: 1.3rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.entity-container {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.entity-item {
  padding: 10px 16px;
  border-radius: 8px;
  font-weight: 600;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 70px;
  transition: all 0.3s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.entity-id {
  font-size: 20px;
  font-weight: bold;
}

.entity-status {
  font-size: 12px;
  opacity: 0.8;
}

.entity-active.producer {
  background: linear-gradient(135deg, #4CAF50, #45a049);
  color: white;
}

.entity-waiting.producer {
  background: #ccc;
  color: #666;
  opacity: 0.7;
}

.entity-active.consumer {
  background: linear-gradient(135deg, #2196F3, #0b7dda);
  color: white;
}

.entity-waiting.consumer {
  background: #ccc;
  color: #666;
  opacity: 0.7;
}

.entity-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}

/* 统计信息样式 */
.stats-section h2 {
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 1.5rem;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.stat-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.stat-icon {
  font-size: 32px;
  margin-bottom: 10px;
  display: block;
}

.stat-label {
  display: block;
  font-weight: 600;
  margin-bottom: 10px;
  color: #4a5568;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 2.5em;
  color: #667eea;
  font-weight: bold;
  line-height: 1;
}

/* 操作日志样式 */
.operation-log h2 {
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 1.5rem;
  display: flex;
  align-items: center;
  gap: 10px;
}

.log-container {
  max-height: 300px;
  overflow-y: auto;
  border: 2px solid #e2e8f0;
  padding: 15px;
  background: #f8fafc;
  border-radius: 8px;
}

.log-container::-webkit-scrollbar {
  width: 8px;
}

.log-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.log-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.log-container::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

.log-item {
  padding: 10px 12px;
  border-bottom: 1px solid #e2e8f0;
  transition: background-color 0.2s;
  font-size: 14px;
  line-height: 1.5;
}

.log-item:hover {
  background: rgba(102, 126, 234, 0.1);
  border-left: 3px solid #667eea;
  padding-left: 10px;
}

.log-empty {
  text-align: center;
  color: #a0aec0;
  padding: 20px;
  font-style: italic;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .producer-consumer-container {
    padding: 10px;
  }
  
  .main-content {
    grid-template-columns: 1fr;
  }
  
  .settings-section {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .setting-item {
    justify-content: space-between;
  }
  
  .btn {
    width: 100%;
    margin-left: 0;
    margin-top: 10px;
  }
  
  .stats-container {
    grid-template-columns: 1fr;
  }
  
  .buffer-item {
    width: 60px;
    height: 60px;
    font-size: 16px;
  }
}
</style>