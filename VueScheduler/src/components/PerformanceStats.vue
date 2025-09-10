
<template>
  <section class="section-block">
    <h2>性能统计</h2>
    <div v-if="performance" class="performance-container">
      <!-- 统计卡片网格 -->
      <div class="stats-grid">
        <!-- 平均等待时间 -->
        <div class="stat-card waiting-time-card">
          <div class="stat-icon">⏱️</div>
          <div class="stat-info">
            <div class="stat-label">平均等待时间</div>
            <div class="stat-value">{{ formatNumber(performance.avgWaitingTime) }}</div>
            <div class="stat-unit">时间单位</div>
          </div>
          <div class="progress-bar-container">
            <div class="progress-bar">
              <div class="progress-fill waiting-fill" :style="{ width: getProgressWidth(performance.avgWaitingTime) + '%' }"></div>
            </div>
          </div>
        </div>

        <!-- 平均周转时间 -->
        <div class="stat-card turnaround-time-card">
          <div class="stat-icon">🔄</div>
          <div class="stat-info">
            <div class="stat-label">平均周转时间</div>
            <div class="stat-value">{{ formatNumber(performance.avgTurnaroundTime) }}</div>
            <div class="stat-unit">时间单位</div>
          </div>
          <div class="progress-bar-container">
            <div class="progress-bar">
              <div class="progress-fill turnaround-fill" :style="{ width: getProgressWidth(performance.avgTurnaroundTime) + '%' }"></div>
            </div>
          </div>
        </div>

        <!-- 平均带权周转时间 -->
        <div class="stat-card weighted-time-card">
          <div class="stat-icon">⚖️</div>
          <div class="stat-info">
            <div class="stat-label">平均带权周转时间</div>
            <div class="stat-value">{{ formatNumber(performance.avgWeightedTurnaroundTime) }}</div>
            <div class="stat-unit">比率</div>
          </div>
          <div class="progress-bar-container">
            <div class="progress-bar">
              <div class="progress-fill weighted-fill" :style="{ width: getProgressWidth(performance.avgWeightedTurnaroundTime) + '%' }"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 进程详细统计表格 -->
      <div v-if="performance.processStats" class="process-stats-table">
        <h3>进程详细统计</h3>
        <table>
          <thead>
            <tr>
              <th>进程ID</th>
              <th>到达时间</th>
              <th>服务时间</th>
              <th>完成时间</th>
              <th>等待时间</th>
              <th>周转时间</th>
              <th>带权周转时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="process in performance.processStats" :key="process.pid">
              <td>{{ process.pid }}</td>
              <td>{{ process.arrivalTime }}</td>
              <td>{{ process.burstTime }}</td>
              <td>{{ process.finishTime }}</td>
              <td>{{ process.waitingTime }}</td>
              <td>{{ process.turnaroundTime }}</td>
              <td>{{ formatNumber(process.weightedTurnaroundTime) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <div v-else class="no-stats">暂无统计信息</div>
  </section>
</template>

<script>
export default {
  name: 'PerformanceStats',
  props: {
    performance: {
      type: Object,
      required: false
    }
  },
  mounted() {
    console.log('PerformanceStats 挂载时收到 performance:', this.performance);
  },
  watch: {
    performance(newVal) {
      console.log('PerformanceStats 收到新 performance:', newVal);
    }
  },
  methods: {
    formatNumber(num) {
      // 格式化数字，保留2位小数
      if (num === null || num === undefined) return '0.00';
      return Number(num).toFixed(2);
    },
    getProgressWidth(value) {
      // 计算进度条宽度，最大100%，假设最大值为10
      const maxValue = 10;
      return Math.min((value / maxValue) * 100, 100);
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

.performance-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.stat-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 10px;
  padding: 1.2em;
  transition: all 0.3s ease;
  border-left: 4px solid #667eea;
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.12);
}

.waiting-time-card {
  border-left-color: #4CAF50;
}

.turnaround-time-card {
  border-left-color: #2196F3;
}

.weighted-time-card {
  border-left-color: #FF9800;
}

.stat-icon {
  font-size: 2rem;
  flex-shrink: 0;
  margin-top: 4px;
}

.stat-info {
  flex-grow: 1;
}

.stat-label {
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 4px;
  font-size: 0.95rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
  color: #2c3e50;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-unit {
  font-size: 0.85rem;
  color: #718096;
}

.progress-bar-container {
  margin-top: 12px;
  width: 100%;
}

.progress-bar {
  height: 8px;
  background-color: rgba(255, 255, 255, 0.6);
  border-radius: 4px;
  overflow: hidden;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.1);
}

.progress-fill {
  height: 100%;
  transition: width 0.5s ease;
  border-radius: 4px;
}

.waiting-fill {
  background: linear-gradient(90deg, #4CAF50, #45a049);
}

.turnaround-fill {
  background: linear-gradient(90deg, #2196F3, #0b7dda);
}

.weighted-fill {
  background: linear-gradient(90deg, #FF9800, #f57c00);
}

/* 进程详细统计表格样式 */
.process-stats-table {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  border: 1px solid #e2e8f0;
}

.process-stats-table h3 {
  margin: 0;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4eaf2 100%);
  color: #2c3e50;
  font-size: 1.2rem;
  border-bottom: 1px solid #e2e8f0;
}

.process-stats-table table {
  width: 100%;
  border-collapse: collapse;
}

.process-stats-table th {
  background-color: #f8fafc;
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #4a5568;
  border-bottom: 2px solid #e2e8f0;
  font-size: 0.9rem;
}

.process-stats-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #f1f1f1;
  color: #2d3748;
  transition: background-color 0.2s ease;
}

.process-stats-table tr:hover td {
  background-color: rgba(102, 126, 234, 0.05);
}

.process-stats-table tr:last-child td {
  border-bottom: none;
}

.no-stats {
  text-align: center;
  color: #718096;
  padding: 60px 20px;
  font-style: italic;
  font-size: 1.1rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .process-stats-table {
    overflow-x: auto;
  }
  
  .process-stats-table table {
    min-width: 600px;
  }
}
</style>


