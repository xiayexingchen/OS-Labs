
<template>
  <section class="section-block">
    <h2>性能统计</h2>
    <div v-if="performance">
      <div>平均等待时间：{{ performance.avgWaitingTime }}</div>
      <div>平均周转时间：{{ performance.avgTurnaroundTime }}</div>
      <div>平均带权周转时间：{{ performance.avgWeightedTurnaroundTime }}</div>
    </div>
    <div v-else>暂无统计信息</div>
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
}
}

</script>

<style scoped>
.section-block {
  margin-bottom: 2em;
  padding: 1em;
  background: #f8f9fa;
  border-radius: 8px;
}
</style>


