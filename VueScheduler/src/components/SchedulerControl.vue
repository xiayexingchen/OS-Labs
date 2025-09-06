<template>
  <section class="section-block">
    <button @click="$emit('init')">初始化</button>
    <button @click="$emit('step')" :disabled="!inited">单步执行</button>
    <button @click="$emit('run')" :disabled="!inited">运行到底</button>
    <button @click="$emit('reset')">重置</button>
    <button @click="$emit('reset-all')">全部重置</button>
  </section>
</template>

<script>
export default {
  name: 'SchedulerControl',
  props: {
    inited: {
      type: Boolean,
      required: true
    }
  }
}
</script>

<style scoped>
.control-buttons {
  text-align: center;
  margin-bottom: 2em;
}

button {
  padding: 10px 20px;
  margin: 5px;
  border: none;
  border-radius: 5px;
  background-color: #3498db;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover {
  background-color: #2980b9;
}

button:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
}

.reset-all {
  background-color: #e74c3c;
}

.reset-all:hover {
  background-color: #c0392b;
}

.section-block {
  margin-bottom: 2em;
  padding: 1em;
  background: #f8f9fa;
  border-radius: 8px;
}
button {
  margin-right: 0.5em;
}
</style>