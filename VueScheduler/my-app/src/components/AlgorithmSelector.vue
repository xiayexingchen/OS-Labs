<template>
  <section class="section-block">
    <h2>算法选择</h2>
    <select v-model="localAlgorithm" @change="onAlgorithmChange">
      <option value="fcfs">先来先服务（FCFS）</option>
      <option value="sjf">短作业优先（SJF）</option>
      <option value="priority">优先级调度（Priority）</option>
      <option value="rr">时间片轮转（RR）</option>
      <option value="srtf">高响应比优先（HRN）</option>
    </select>
    <span v-if="localAlgorithm === 'rr'">
      时间片大小：
      <input type="number" v-model.number="localTimeSlice" min="1" @input="onTimeSliceChange" />
    </span>
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
      localTimeSlice: this.timeSlice
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
select, input {
  margin-right: 1em;
}
</style>