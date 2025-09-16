<template>
    <h1>生产者-消费者问题演示</h1>

  <div class="producer-consumer-container">
    
    <!-- 参数设置区域 -->
    <div class="settings-section">
      <!-- 第一行：基础设置 -->
      <div class="settings-grid">
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
      </div>
      
      <!-- 第二行：速度设置 -->
      <div class="speed-settings-grid" style="display: flex; gap: 20px; justify-content: center; margin-top: 15px;">
        <div class="setting-item">
          <label for="production-speed">生产时间:</label>
          <input type="range" id="production-speed" v-model.number="productionSpeed" min="1000" max="5000" step="100">
          <span>{{ productionSpeed / 1000 }}秒</span>
        </div>
        <div class="setting-item">
          <label for="consumption-speed">消费时间:</label>
          <input type="range" id="consumption-speed" v-model.number="consumptionSpeed" min="1000" max="5000" step="100">
          <span>{{ consumptionSpeed / 1000 }}秒</span>
        </div>
      </div>
      
      <!-- 控制按钮 - 另起一行 -->
      <div class="control-buttons-row" style="width:100%; display:flex; justify-content:center; gap:20px; margin-top:15px;">
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
    </div>
    
    <!-- 缓冲区状态区域 -->
    <div class="buffer-display">
      <h2>缓冲区状态</h2>
      
      <div class="buffer-content-layout">
        <!-- 左边：缓冲区显示 -->
        <div class="buffer-left-section">
          <div class="buffer-container">
            <div 
              v-for="(item, index) in buffer" 
              :key="index" 
              class="buffer-item"
              :class="{
                'buffer-item-filled': item !== null,
                'buffer-item-head': index === headPointer,
                'buffer-item-tail': index === tailPointer,
                'buffer-item-consumed': item !== null && item.isConsumed
              }"
            >
              <span v-if="item !== null">
                <div class="item-value">{{ item.value }}</div>
                <div class="item-producer">{{ item.producerId }}</div>
                <div v-if="item.isConsumed" class="item-consumer">{{ item.consumerId }}</div>
                <div v-if="item.isConsumed" class="item-wait-time">{{ item.waitTime }}ms</div>
              </span>
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

        <!-- 右边：统计信息和状态 -->
        <div class="buffer-right-section">
          <!-- 右边上部：统计信息 -->
          <div class="stats-in-buffer">
            <h3>统计信息</h3>
            <div class="buffer-stats-grid">
              <div class="buffer-stat-card">
                <div class="stat-icon">📦</div>
                <div class="stat-content">
                  <span class="stat-label">已生产物品</span>
                  <span class="stat-value">{{ stats.totalProduced }}</span>
                </div>
              </div>
              <div class="buffer-stat-card">
                <div class="stat-icon">🍽️</div>
                <div class="stat-content">
                  <span class="stat-label">已消费物品</span>
                  <span class="stat-value">{{ stats.totalConsumed }}</span>
                </div>
              </div>
              <div class="buffer-stat-card">
                <div class="stat-icon">⚠️</div>
                <div class="stat-content">
                  <span class="stat-label">缓冲区已满</span>
                  <span class="stat-value">{{ stats.bufferFullCount }}</span>
                </div>
              </div>
              <div class="buffer-stat-card">
                <div class="stat-icon">📭</div>
                <div class="stat-content">
                  <span class="stat-label">缓冲区为空</span>
                  <span class="stat-value">{{ stats.bufferEmptyCount }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 右边下部：生产者和消费者状态 -->
          <div class="entities-status-in-buffer">
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
      </div>
    </div>

    <!-- 移除原来的主内容区域 -->
        <!-- 已消费物品历史记录 -->
    <div class="consumed-history">
      <h2>已消费物品历史记录</h2>
      <div class="consumed-items-grid">
        <div 
          v-for="(item, index) in formattedConsumedItems" 
          :key="index"
          class="consumed-item"
          :class="{
            'consumed-item-filled': item !== null,
            'consumed-item-recent': index === 0 && item !== null
          }"
        >
          <span v-if="item !== null">
            <div class="item-value">{{ item.value }}</div>
            <div class="item-producer">{{ item.producerId }}</div>
            <div class="item-consumer">{{ item.consumerId }}</div>
          </span>
          <span v-else>空</span>
          <div v-if="index === 0" class="pointer-marker recent-marker">R</div>
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
      pollingInterval: 1000, // 每1000ms获取一次状态，与后端模拟速度相匹配
      consumedItemsHistory: [], // 存储已消费物品的历史记录
      historyFetchCounter: 0 // 用于控制历史记录获取频率的计数器
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
    },
    
    // 格式化已消费物品列表，固定显示20个方格
    formattedConsumedItems() {
      // 创建一个长度为20的新数组，初始值为null
      const formatted = new Array(20).fill(null);
      
      // 将已消费物品填充到新数组中，第一个位置是最近消费的
      // 从历史列表的后面获取数据，确保最新消费的物品显示在第一个位置
      const itemsToDisplay = this.consumedItemsHistory.slice(-20).reverse();
      itemsToDisplay.forEach((item, index) => {
        formatted[index] = item;
      });
      
      return formatted;
    }
  },
  methods: {
    // 获取已消费物品历史记录
    async fetchConsumedHistory() {
      try {
        const response = await this.$axios.get('/api/producer-consumer/consumed-history');
        this.consumedItemsHistory = response.data;
      } catch (error) {
        console.error('获取已消费物品历史记录失败:', error);
        // 本地模拟模式下，不做处理
      }
    },
    
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
        this.fetchConsumedHistory(); // 获取初始历史记录
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
        this.fetchConsumedHistory(); // 获取初始历史记录
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
      this.headPointer = status.headPointer || 0;
      this.tailPointer = status.tailPointer || 0;
      this.itemCount = status.itemCount || 0;
      this.bufferSize = status.bufferSize || this.bufferSize;
      
      // 获取后端返回的buffer数据
      const backendBuffer = status.buffer || [];
      
      // 创建一个长度等于bufferSize的新数组，初始值为null
      this.buffer = new Array(this.bufferSize).fill(null);
      
      // 将后端返回的数据填充到新数组中
      backendBuffer.forEach((item, index) => {
        if (index < this.bufferSize) {
          if (!item) {
            this.buffer[index] = null;
          } else {
            // 确保每个buffer项都有完整的属性
            this.buffer[index] = {
              ...item,
              // 使用后端返回的'consumed'字段，并设置'isConsumed'属性
              isConsumed: item.consumed !== undefined ? item.consumed : false,
              consumed: item.consumed !== undefined ? item.consumed : false,
              // 确保consumerId属性存在，未消费时为null
              consumerId: item.consumerId || null,
              // 确保waitTime属性存在
              waitTime: item.waitTime || 0
            };
          }
        }
      });
      
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
      
      // 每3次状态轮询获取一次历史记录，减少API调用频率
      if (!this.historyFetchCounter) {
        this.historyFetchCounter = 0;
      }
      this.historyFetchCounter++;
      if (this.historyFetchCounter >= 2) {
        this.historyFetchCounter = 0;
        this.fetchConsumedHistory();
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
  padding: 10px;
  max-width: 95%;
  margin: 0 auto;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  /* background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); */
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
  margin-bottom: 2em;
  padding: 1.5em;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  animation: fadeIn 0.3s ease-out;
}

.settings-section h2 {
  color: #2c3e50;
  margin-top: 0;
  margin-bottom: 1.5em;
  font-size: 1.5rem;
  display: flex;
  align-items: center;
  gap: 10px;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 15px;
}

/* 速度设置行样式 */
.speed-settings-grid {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin: 15px 0;
  flex-wrap: wrap;
}

.setting-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: white;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
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

/* 控制按钮行样式 */
.control-buttons-row {
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 15px;
  flex-wrap: wrap;
}



/* 缓冲区显示样式 - 白色背景 */
    .buffer-display {
      background: white;
      border-radius: 15px;
      padding: 30px;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
      margin-bottom: 30px;
      border: 1px solid #e2e8f0;
    }

    .buffer-display h2 {
      color: #2c3e50;
      text-align: center;
      margin-bottom: 25px;
      font-size: 28px;
      font-weight: 700;
    }

/* 新布局容器 - 7:3比例 */
    .buffer-content-layout {
      display: grid;
      grid-template-columns: 7fr 3fr;
      gap: 30px;
      align-items: start;
    }

.buffer-left-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.buffer-right-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 右边上部：统计信息 - 白色背景适配 */
    .stats-in-buffer {
      background: #f8fafc;
      border-radius: 12px;
      padding: 20px;
      border: 1px solid #e2e8f0;
    }

    .stats-in-buffer h3 {
      color: #2c3e50;
      margin-bottom: 15px;
      font-size: 20px;
      font-weight: 600;
    }

/* 右边下部：生产者和消费者状态 - 白色背景适配 */
    .entities-status-in-buffer {
      background: #f8fafc;
      border-radius: 12px;
      padding: 20px;
      border: 1px solid #e2e8f0;
    }

/* 缓冲区统计信息网格 - 白色背景适配 */
    .buffer-stats-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 10px;
    }

    .buffer-stat-card {
      background: linear-gradient(135deg, #ffffff, #f8fafc);
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 12px;
      display: flex;
      align-items: center;
      gap: 8px;
      transition: all 0.3s ease;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    }

    .buffer-stat-card:hover {
      transform: translateY(-1px);
      box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
      border-color: #cbd5e0;
    }

    .stat-icon {
      font-size: 20px;
      flex-shrink: 0;
    }

    .stat-content {
      display: flex;
      flex-direction: column;
    }

    .stat-label {
      color: #64748b;
      font-size: 11px;
      font-weight: 500;
    }

    .stat-value {
      color: #1e293b;
      font-size: 16px;
      font-weight: 700;
    }

.buffer-container {
  display: flex;
  gap: 12px;
  margin: 20px 0;
  justify-content: center;
  flex-wrap: wrap;
}

.buffer-item {
  width: 100px;
  height: 100px;
  border: 3px solid #e2e8f0;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  background-color: #f9f9f9;
  position: relative;
  transition: all 0.3s;
  padding: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.buffer-item-filled {
  background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%);
  color: white;
  border-color: #4CAF50;
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(76, 175, 80, 0.2);
}

.buffer-item-head {
  border-color: #f97316;
  box-shadow: 0 0 0 2px rgba(249, 115, 22, 0.3);
}

.buffer-item-tail {
  border-color: #0ea5e9;
  box-shadow: 0 0 0 2px rgba(14, 165, 233, 0.3);
}

.buffer-item-consumed {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  border-color: #3b82f6;
}
.item-value {
  font-size: 24px;
  margin-bottom: 8px;
  font-weight: bold;
}

.item-producer,
.item-consumer,
.item-wait-time {
  font-size: 11px;
  opacity: 0.9;
  text-align: center;
  line-height: 1.3;
  margin: 1px 0;
}

.item-wait-time {
  font-size: 10px;
  opacity: 0.8;
  margin-top: 2px;
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

/* 生产者和消费者状态区域样式 - 白色背景适配 */
.entities-status-in-buffer {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.entities-status-in-buffer .entity-group {
  margin-bottom: 0;
}

.entities-status-in-buffer .entity-group h3 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 18px;
  font-weight: 600;
}

.entities-status-in-buffer .entity-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.entities-status-in-buffer .entity-item {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 6px 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  font-size: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.entities-status-in-buffer .entity-item.entity-active {
  background: #f0fdf4;
  border-color: #4caf50;
  color: #166534;
}

.entities-status-in-buffer .entity-item.entity-waiting {
  background: #fffbeb;
  border-color: #ff9800;
  color: #92400e;
}

.entities-status-in-buffer .entity-item.producer {
  border-left: 2px solid #4caf50;
}

.entities-status-in-buffer .entity-item.consumer {
  border-left: 2px solid #ff9800;
}

.entities-status-in-buffer .entity-id {
  font-weight: 600;
  color: #1e293b;
  font-size: 13px;
  font-family: 'Courier New', monospace;
  min-width: 25px;
  text-align: center;
  display: inline-block;
}

.entities-status-in-buffer .entity-status {
  color: #475569;
  font-size: 11px;
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

/* 已消费物品历史记录样式 */
.consumed-history {
  /* background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); */
  border-radius: 14px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
}

.consumed-history h2 {
  margin-bottom: 18px;
  font-size: 1.5rem;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 已消费物品网格布局 - 固定20个方格，分为两排 */
.consumed-items-grid {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 8px;
  margin: 20px 0;
  justify-content: center;
}

/* 已消费物品方格样式 - 调整大小 */
.consumed-item {
  width: 90px;
  height: 90px;
  border: 3px solid #e2e8f0;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  background-color: #f9f9f9;
  position: relative;
  transition: all 0.3s;
  padding: 6px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  font-size: 16px;
}

.consumed-item-filled {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  color: white;
  border-color: #3b82f6;
}

.consumed-item-recent {
  background: linear-gradient(135deg, #f43f5e 0%, #e11d48 100%);
  border-color: #e11d48;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

/* 已消费物品内容样式调整 */
.consumed-item .item-value {
  font-size: 20px;
  margin-bottom: 4px;
  font-weight: bold;
}

.consumed-item .item-producer,
.consumed-item .item-consumer {
  font-size: 12px;
  opacity: 0.9;
  text-align: center;
  line-height: 1.2;
  margin: 1px 0;
}

/* 最近消费标记样式 */
.recent-marker {
  background: linear-gradient(135deg, #f43f5e, #e11d48);
  left: 50%;
  transform: translateX(-50%);
  top: -22px;
}

.consumed-history .pointer-info {
  margin-top: 30px;
  text-align: center;
  font-size: 0.9rem;
  color: #64748b;
  font-weight: bold;
  padding: 8px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

h3 {
  margin-top: 0;
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
  
  .consumed-items-container {
    grid-template-columns: 1fr;
  }
  
  .buffer-item {
    width: 60px;
    height: 60px;
    font-size: 16px;
  }
}
</style>
