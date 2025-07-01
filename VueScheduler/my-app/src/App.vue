<template>
  <div class="app-container">
    <!-- 标题 -->
    <h1>进程调度模拟系统</h1>
    
    <!-- 进程输入区 -->
    <section class="section-block">
      <ProcessInputTable
        :processes="processes"
        @add-process="addProcess"
        @remove-process="removeProcess"
        @update-process="updateProcess"
      />
    </section>

    <!-- 算法选择与时间片区 -->
    <AlgorithmSelector
      :algorithm="algorithm"
      :timeSlice="timeSlice"
      @algorithm-change="onAlgorithmChange"
      @timeSlice-change="onTimeSliceChange"
    />
    <!-- 控制按钮区 -->
    <SchedulerControl
        :inited="inited"
        @init="handleInit"
        @step="handleStep"
        @run="handleRun"
        @reset="handleReset"
        @reset-all="handleFullReset"
    />
    <!-- 调度状态展示区 -->
    <SchedulerStatus
      :status="status"
    />

    <!-- 性能统计区 -->
    <PerformanceStats
      :performance="performance"
    />
  </div>
</template>

<script>
import axios from 'axios'
import ProcessInputTable from './components/ProcessInputTable.vue'
import SchedulerControl from './components/SchedulerControl.vue'
import SchedulerStatus from './components/SchedulerStatus.vue'
import AlgorithmSelector from './components/AlgorithmSelector.vue'
import PerformanceStats from './components/PerformanceStats.vue'

export default {
  components :{
    ProcessInputTable,
    SchedulerControl,
    SchedulerStatus,
    AlgorithmSelector,
    PerformanceStats,
  },
  name: 'App',
  data() {
    return {
      // 进程列表
      processes: [
        { pid: 1,
          arrivalTime: 0, 
          burstTime: 3, 
          jobSize:5,
          priority: 1 },
      ],
      algorithm: 'fcfs',
      timeSlice: 2,
      status: null,
      performance: null,
      inited: false,
    }
  },
  methods: {
    addProcess() {
      this.processes.push({
        pid: `${this.processes.length + 1}`,
        arrivalTime: 0,
        burstTime: 1,
        jobSize: 1,
        priority: 1,
      })
    },
    removeProcess(idx) {
      this.processes.splice(idx, 1)
    },
    updateProcess(idx, field, value) {
      // 保证响应式数据
      this.processes[idx][field] = value;
    },    
    async handleInit() {
    // 打印要发送的数据
    console.log('init payload', {
    algorithm: this.algorithm,
    processes: this.processes,
    timeSlice: this.algorithm === 'rr' ? this.timeSlice : undefined
    });
    try {
      const res = await axios.post('/api/scheduler/init', {
      algorithm: this.algorithm,
      processes: this.processes,
      timeSlice: this.algorithm === 'rr' ? this.timeSlice : undefined
    });
    console.log('init response', res.data);
    this.inited = true;
    await this.refreshStatus();
    
  } catch (e) {
    console.error('init error', e);
    alert('初始化失败，请检查数据');
  }
  },
    async handleStep() {
      try {
        const res = await axios.post('/api/scheduler/step');
        console.log('单步后端返回:', res.data);
        this.status = res.data; // 直接用后端返回的调度状态

        await this.refreshPerformance();
        console.log('this.status 刷新后:', this.status);
        console.log('this.performance 刷新后:', this.performance);
      } catch (e) {
        alert('单步执行失败');
      }
    },
    async handleRun() {
      try {
        const res = await axios.post('/api/scheduler/run');
        this.status = res.data;
        await this.refreshPerformance();
      } catch (e) {
        alert('运行到底失败');
      }
    },
    handleFullReset() {
      this.processes = [
        { pid: 1, arrivalTime: 0, burstTime: 3, jobSize: 5, priority: 1 }
      ];
      this.algorithm = 'fcfs';
      this.timeSlice = 2;
      this.status = null;
      this.performance = null;
      this.inited = false;
      axios.post('/api/scheduler/reset');
    },
    async handleReset() {
      try {
        await axios.post('/api/scheduler/reset')
        this.inited = false
        this.status = null
        this.performance = null
      } catch (e) {
        alert('重置失败')
      }
    },
    async refreshStatus() {
      try {
        const res = await axios.get('/api/scheduler/status')
        this.status = res.data
      } catch (e) {
        this.status = null
      }
    },    
    async refreshPerformance() {
      try {
        const res = await axios.get('/api/scheduler/performance');
        console.log('performance接口返回:', res.data);
        this.performance = res.data;
      } catch (e) {
        this.performance = null;
        console.error('performance刷新异常:', e);
      }
    }
  }
}
</script>

<style scoped>
.app-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 2em;
  font-family: "Microsoft YaHei", Arial, sans-serif;
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
