<template>
  <section class="section-block">
    <h2>进程输入</h2>
    <table class="input-table">
      <thead>
        <tr>
          <th>进程ID</th>
          <th>到达时间</th>
          <th>消耗时间</th>
          <th>工作大小</th>
          <th>优先级</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(process, idx) in processes" :key="process.pid">
          <td>
            <input v-model="process.pid" @input="onUpdate(idx, 'pid', process.pid)" />
          </td>
          <td>
            <input type="number" v-model.number="process.arrivalTime" @input="onUpdate(idx, 'arrivalTime', process.arrivalTime)" min="0" />
          </td>
          <td>
            <input type="number" v-model.number="process.burstTime" @input="onUpdate(idx, 'burstTime', process.burstTime)" min="1" />
          </td>
          <td>
            <input type="number" v-model.number="process.jobSize" @input="onUpdate(idx, 'jobSize', process.jobSize)" min="1" />
          </td>
          <td>
            <input type="number" v-model.number="process.priority" @input="onUpdate(idx, 'priority', process.priority)" min="1" />
          </td>
          <td>
            <button @click="$emit('remove-process', idx)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <button @click="$emit('add-process')">添加进程</button>
  </section>
</template>

<script>
export default {
  name: 'ProcessInputTable',
  props: {
    processes: {
      type: Array,
      required: true
    }
  },
  methods: {
    onUpdate(idx, field, value) {
      // 通知父组件更新数据
      this.$emit('update-process', idx, field, value)
    }
  }
}
</script>

<style scoped>
.input-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1em;
}

thead th {
  background-color: #f2f2f2;
  padding: 10px;
  text-align: left;
}

tbody td {
  padding: 10px;
  border-bottom: 1px solid #ddd;
}

input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.add-btn, .remove-btn {
  padding: 5px 10px;
  margin-top: 10px;
  border: none;
  border-radius: 4px;
  color: white;
  cursor: pointer;
}

.add-btn {
  background-color: #2ecc71;
}

.add-btn:hover {
  background-color: #27ae60;
}

.remove-btn {
  background-color: #e74c3c;
}

.remove-btn:hover {
  background-color: #c0392b;
}

.section-block {
  margin-bottom: 2em;
  padding: 1em;
  background: #f8f9fa;
  border-radius: 8px;
}
.input-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1em;
}
.input-table th, .input-table td {
  border: 1px solid #ccc;
  padding: 0.5em;
  text-align: center;
}
button {
  margin-right: 0.5em;
}
</style>