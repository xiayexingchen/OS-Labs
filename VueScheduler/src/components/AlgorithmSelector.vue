<template>
  <section class="section-block">
    <h2>算法选择</h2>
    <div class="selector-container">
      <!-- 算法选择卡片 -->
      <div class="algorithm-selection-card">
        <div class="selection-header">
          <h3>调度算法</h3>
          <div class="algorithm-icon">
            <div v-if="localAlgorithm === 'fcfs'" class="icon-fcfs">⚡</div>
            <div v-else-if="localAlgorithm === 'sjf'" class="icon-sjf">⏱️</div>
            <div v-else-if="localAlgorithm === 'priority'" class="icon-priority">⭐</div>
            <div v-else-if="localAlgorithm === 'rr'" class="icon-rr">🔄</div>
            <div v-else-if="localAlgorithm === 'srtf'" class="icon-srtf">💡</div>
          </div>
        </div>
        <div class="selection-body">
          <div class="algorithm-dropdown-container">
            <select 
              v-model="localAlgorithm" 
              @change="onAlgorithmChange"
              class="algorithm-dropdown"
            >
              <option 
                v-for="algo in algorithms" 
                :key="algo.value" 
                :value="algo.value"
              >
                {{ algo.name }} ({{ algo.short }})
              </option>
            </select>
            <div class="selected-algorithm-info">
              <div class="algorithm-name">{{ getSelectedAlgorithm().name }} ({{ getSelectedAlgorithm().short }})</div>
              <div class="algorithm-description">{{ getSelectedAlgorithm().description }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 时间片设置 (仅在RR算法下显示) -->
      <div class="time-slice-card" v-if="localAlgorithm === 'rr'">
        <div class="slice-header">
          <h3>时间片设置</h3>
          <div class="slice-icon">🎯</div>
        </div>
        <div class="slice-body">
          <div class="slider-container">
            <input 
              type="range" 
              v-model="localTimeSlice" 
              @input="onTimeSliceChange"
              min="1"
              max="10"
              step="1"
              class="time-slice-slider"
            >
            <div class="slider-value">{{ localTimeSlice }}</div>
          </div>
          <div class="slider-labels">
            <span>1</span>
            <span>5</span>
            <span>10</span>
          </div>
          <div class="time-slice-info">
            <div class="info-item">
              <div class="info-label">当前时间片:</div>
              <div class="info-value highlight">{{ localTimeSlice }} 单位时间</div>
            </div>
            <div class="info-item" v-if="localTimeSlice <= 3">
              <div class="info-icon">🚀</div>
              <div class="info-text">小时间片：响应更快，但上下文切换开销增加</div>
            </div>
            <div class="info-item" v-else-if="localTimeSlice <= 7">
              <div class="info-icon">⚖️</div>
              <div class="info-text">中时间片：平衡响应时间与上下文切换开销</div>
            </div>
            <div class="info-item" v-else>
              <div class="info-icon">💡</div>
              <div class="info-text">大时间片：减少上下文切换，但响应时间可能增加</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: 'AlgorithmSelector',
  props: {
    algorithm: {
      type: String,
      required: true
    },
    timeSlice: {
      type: Number,
      default: 2
    }
  },
  data() {
    return {
      localAlgorithm: this.algorithm,
      localTimeSlice: this.timeSlice,
      algorithms: [
        {
          value: 'fcfs',
          name: '先来先服务',
          short: 'FCFS',
          description: '按照进程到达的先后顺序进行调度'
        },
        {
          value: 'sjf',
          name: '短作业优先',
          short: 'SJF',
          description: '优先调度估计运行时间最短的进程'
        },
        {
          value: 'priority',
          name: '优先级调度',
          short: 'Priority',
          description: '根据进程优先级进行调度，优先级高的先执行'
        },
        {
          value: 'rr',
          name: '时间片轮转',
          short: 'RR',
          description: '按时间片轮流调度各个就绪进程'
        },
        {
          value: 'srtf',
          name: '高响应比优先',
          short: 'HRN',
          description: '综合考虑进程等待时间和服务时间的调度算法'
        }
      ]
    }
  },
  watch: {
    algorithm(newVal) {
      this.localAlgorithm = newVal;
    },
    timeSlice(newVal) {
      this.localTimeSlice = newVal;
    }
  },
  methods: {
    onAlgorithmChange() {
      this.$emit('update:algorithm', this.localAlgorithm);
      // 如果切换到rr，自动触发时间片同步
      if (this.localAlgorithm === 'rr') {
        this.$emit('update:timeSlice', this.localTimeSlice);
      }
    },
    onTimeSliceChange() {
      this.$emit('update:timeSlice', this.localTimeSlice);
    },
    getSelectedAlgorithm() {
      return this.algorithms.find(algo => algo.value === this.localAlgorithm) || this.algorithms[0];
    }
  }
}
</script>

<style scoped>
.section-block {
  margin-bottom: 2em;
  padding: 1.5em;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-block h2 {
  color: #2c3e50;
  margin-top: 0;
  margin-bottom: 1.5em;
  font-size: 1.5rem;
  display: flex;
  align-items: center;
  gap: 10px;
}

.selector-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 算法选择卡片样式 */
.algorithm-selection-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4eaf2 100%);
  border-radius: 12px;
  padding: 1.5em;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.algorithm-selection-card:hover {
  box-shadow: 0 6px 16px rgba(0,0,0,0.1);
}

.selection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.2em;
}

.selection-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.algorithm-icon {
  font-size: 2rem;
}

/* 算法下拉选择容器 */
.algorithm-dropdown-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 算法下拉选择框 */
.algorithm-dropdown {
  width: 100%;
  padding: 12px 16px;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  color: #2c3e50;
  cursor: pointer;
  transition: all 0.3s ease;
  appearance: none;
  background-image: url('data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="%23667eea"%3E%3Cpath fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd"/%3E%3C/svg%3E');
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 16px;
  padding-right: 40px;
}

.algorithm-dropdown:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.algorithm-dropdown:hover {
  border-color: #667eea;
}

/* 选中算法信息显示 */
.selected-algorithm-info {
  background: rgba(255, 255, 255, 0.8);
  padding: 12px;
  border-radius: 8px;
  border-left: 3px solid #667eea;
}

.algorithm-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.algorithm-description {
  font-size: 0.9rem;
  color: #718096;
  margin-top: 4px;
}

/* 时间片设置卡片样式 */
.time-slice-card {
  background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
  border-radius: 12px;
  padding: 1.5em;
  border: 1px solid #ffecb3;
  transition: all 0.3s ease;
  animation: slideIn 0.4s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.time-slice-card:hover {
  box-shadow: 0 6px 16px rgba(255, 152, 0, 0.15);
}

.slice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.2em;
}

.slice-header h3 {
  margin: 0;
  color: #e65100;
  font-size: 1.2rem;
}

.slice-icon {
  font-size: 1.8rem;
}

.slider-container {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.time-slice-slider {
  -webkit-appearance: none;
  width: 100%;
  height: 8px;
  border-radius: 4px;
  background: #ffecb3;
  outline: none;
  -webkit-transition: .2s;
  transition: all .2s;
}

.time-slice-slider:hover {
  background: #ffe082;
}

.time-slice-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #ff9800;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(255, 152, 0, 0.4);
  transition: all 0.2s ease;
}

.time-slice-slider::-webkit-slider-thumb:hover {
  transform: scale(1.2);
  box-shadow: 0 3px 8px rgba(255, 152, 0, 0.6);
}

.time-slice-slider::-moz-range-thumb {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #ff9800;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(255, 152, 0, 0.4);
}

.slider-value {
  min-width: 30px;
  padding: 8px 12px;
  background: #ff9800;
  color: white;
  border-radius: 6px;
  font-weight: bold;
  text-align: center;
  box-shadow: 0 2px 6px rgba(255, 152, 0, 0.4);
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  color: #718096;
  font-size: 0.85rem;
  margin-bottom: 20px;
}

.time-slice-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 6px;
  border-left: 3px solid #ff9800;
}

.info-label {
  font-weight: 600;
  color: #e65100;
}

.info-value {
  color: #2c3e50;
}

.info-value.highlight {
  font-weight: bold;
  color: #ff9800;
}

.info-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
}

.info-text {
  color: #5d4037;
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .algorithm-options {
    gap: 10px;
  }
  
  .option-radio {
    padding: 10px;
  }
}
</style>