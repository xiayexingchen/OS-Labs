<template>
  <section class="section-block">
    <h2>调度状态</h2>
    <div v-if="status">
      <div>当前时间：{{ status.currentTime }}</div>
      <div>
        <div>正在运行：
            <span v-if="status.cores && status.cores.length > 0">
            <span v-for="core in status.cores" :key="core.id">
                <span v-if="core.runningProcess">
                {{ core.runningProcess.pid }}
                </span>
            </span>
            </span>
            <span v-else>无</span>
        </div>
        </div>
      <div>
        <strong>就绪队列：</strong>
        <span v-if="status.readyQueue.length">
          <span v-for="p in status.readyQueue" :key="p.pid">{{ p.pid }} </span>
        </span>
        <span v-else>无</span>
      </div>
      <div>
        <strong>已完成：</strong>
        <span v-if="status.finishedQueue.length">
          <span v-for="p in status.finishedQueue" :key="p.pid">{{ p.pid }} </span>
        </span>
        <span v-else>无</span>
      </div>
    </div>
    <div v-else>尚未初始化或无状态信息</div>
  </section>
</template>

<script>
export default {
  name: 'SchedulerStatus',
  props: {
    status: Object
  }
}
</script>

<style scoped>
.status-container {
  padding: 1.5em;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 2em;
}

h3 {
  color: #2c3e50;
  margin-top: 0;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.status-item {
  background-color: #f8f9fa;
  padding: 10px;
  border-radius: 5px;
}

.status-item strong {
  color: #34495e;
}

.section-block {
  margin-bottom: 2em;
  padding: 1em;
  background: #f8f9fa;
  border-radius: 8px;
}
</style>