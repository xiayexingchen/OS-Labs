<template>
  <section class="section-block">
    <h2>调度状态</h2>
    <div v-if="status" class="status-container">
      <!-- 当前时间卡片 -->
      <div class="status-card current-time-card">
        <div class="status-label">当前时间</div>
        <div class="status-value time-value">{{ status.currentTime }}</div>
      </div>

      <!-- 运行中进程卡片 -->
      <div class="status-card running-process-card">
        <div class="status-label">正在运行</div>
        <div class="process-list">
          <div v-if="status.cores && status.cores.length > 0">
            <div v-for="core in status.cores" :key="core.id" class="core-item">
              <span class="core-label">核心{{ core.id }}:</span>
              <span v-if="core.runningProcess" class="process-tag running-tag">
                {{ core.runningProcess.pid }}
              </span>
              <span v-else class="no-process">空闲</span>
            </div>
          </div>
          <div v-else class="no-process">无</div>
        </div>
      </div>

      <!-- 就绪队列卡片 -->
      <div class="status-card ready-queue-card">
        <div class="status-label">就绪队列</div>
        <div class="process-list">
          <div v-if="status.readyQueue.length" class="queue-items">
            <span v-for="p in status.readyQueue" :key="p.pid" class="process-tag ready-tag">
              {{ p.pid }}
              <span class="process-info">[优先级: {{ p.priority }}]</span>
            </span>
          </div>
          <div v-else class="no-process">无</div>
        </div>
      </div>

      <!-- 已完成进程卡片 -->
      <div class="status-card finished-queue-card">
        <div class="status-label">已完成</div>
        <div class="process-list">
          <div v-if="status.finishedQueue.length" class="queue-items">
            <span v-for="p in status.finishedQueue" :key="p.pid" class="process-tag finished-tag">
              {{ p.pid }}
              <span class="process-info">[完成时间: {{ p.finishTime }}]</span>
            </span>
          </div>
          <div v-else class="no-process">无</div>
        </div>
      </div>

      <!-- 时间线可视化 -->
      <div v-if="status.processTimeline && status.processTimeline.length" class="timeline-card">
        <div class="status-label">执行时间线</div>
        <div class="timeline-container">
          <div v-for="event in status.processTimeline" :key="event.id" :style="{ left: getTimelinePosition(event.time) + '%' }" 
               class="timeline-event" :class="getEventClass(event.type)">
            <div class="event-tooltip">
              {{ event.processId }} - {{ getEventTypeText(event.type) }}
              <br>时间: {{ event.time }}
            </div>
          </div>
          <div class="timeline-markers">
            <div v-for="i in 10" :key="i" :style="{ left: (i-1)*10 + '%' }" class="timeline-marker">{{ i-1 }}</div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="no-status">尚未初始化或无状态信息</div>
  </section>
</template>

<script>
export default {
  name: 'SchedulerStatus',
  props: {
    status: Object
  },
  methods: {
    getTimelinePosition(time) {
      // 假设时间线最大显示10个时间单位
      const maxTime = 10;
      return Math.min((time / maxTime) * 100, 100);
    },
    getEventClass(type) {
      const classMap = {
        'start': 'event-start',
        'end': 'event-end',
        'preempt': 'event-preempt'
      };
      return classMap[type] || 'event-default';
    },
    getEventTypeText(type) {
      const textMap = {
        'start': '开始运行',
        'end': '完成运行',
        'preempt': '被抢占'
      };
      return textMap[type] || type;
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

.status-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.status-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 10px;
  padding: 1.2em;
  transition: all 0.3s ease;
  border-left: 4px solid #667eea;
}

.status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.12);
}

.current-time-card {
  border-left-color: #4CAF50;
}

.running-process-card {
  border-left-color: #2196F3;
}

.ready-queue-card {
  border-left-color: #FF9800;
}

.finished-queue-card {
  border-left-color: #9C27B0;
}

.timeline-card {
  grid-column: 1 / -1;
  border-left-color: #F44336;
}

.status-label {
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 12px;
  font-size: 0.95rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-value {
  font-size: 2rem;
  font-weight: bold;
  color: #2c3e50;
}

.time-value {
  color: #4CAF50;
  font-family: 'Courier New', monospace;
}

.process-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.core-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 0;
}

.core-label {
  font-weight: 600;
  color: #4a5568;
  min-width: 60px;
}

.queue-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.process-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  color: white;
  transition: transform 0.2s ease;
}

.process-tag:hover {
  transform: scale(1.05);
}

.running-tag {
  background: linear-gradient(135deg, #2196F3, #0b7dda);
}

.ready-tag {
  background: linear-gradient(135deg, #FF9800, #f57c00);
}

.finished-tag {
  background: linear-gradient(135deg, #9C27B0, #7b1fa2);
}

.process-info {
  font-size: 0.8rem;
  opacity: 0.9;
}

.no-process {
  color: #718096;
  font-style: italic;
  padding: 8px 0;
}

.no-status {
  text-align: center;
  color: #718096;
  padding: 40px 20px;
  font-style: italic;
}

/* 时间线样式 */
.timeline-container {
  position: relative;
  height: 60px;
  background: white;
  border-radius: 8px;
  padding: 20px 0;
  margin-top: 10px;
  overflow: hidden;
}

.timeline-event {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 12px;
  height: 12px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
}

.timeline-event:hover {
  transform: translate(-50%, -50%) scale(1.5);
}

.event-start {
  background: #4CAF50;
}

.event-end {
  background: #f44336;
}

.event-preempt {
  background: #FF9800;
}

.event-tooltip {
  visibility: hidden;
  width: 120px;
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  text-align: center;
  border-radius: 6px;
  padding: 8px;
  position: absolute;
  z-index: 1;
  bottom: 125%;
  left: 50%;
  margin-left: -60px;
  opacity: 0;
  transition: opacity 0.3s;
  font-size: 0.8rem;
}

.timeline-event:hover .event-tooltip {
  visibility: visible;
  opacity: 1;
}

.timeline-markers {
  position: absolute;
  bottom: 5px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  font-size: 0.7rem;
  color: #718096;
}

.timeline-marker {
  position: absolute;
  text-align: center;
  width: 20px;
  margin-left: -10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .status-container {
    grid-template-columns: 1fr;
  }
  
  .timeline-card {
    grid-column: 1;
  }
}
</style>