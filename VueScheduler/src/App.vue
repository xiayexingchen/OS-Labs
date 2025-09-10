<template>
  <div id="app">
    <Login v-if="!loggedIn" @loggedIn="handleLoggedIn" />
    <Layout v-else @experiment-change="handleExperimentChange">
      <div class="app-container" v-if="currentExperiment === 'scheduler'">

    <!-- 标题 -->
    <h1>进程调度模拟系统</h1>
    
    <!-- 进程输入区 -->
    <section class="section-block">
      <ProcessInputTable
        :processes="processes"
        @add-process="addProcess"
        @remove-process="removeProcess"
        @update-process="updateProcess"
        @batch-add-processes="handleBatchAddProcesses"
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
      <ProducerConsumer v-if="currentExperiment === 'producer-consumer'" />
    </Layout>
  </div>
</template>

<script>
import axios from 'axios'
import ProcessInputTable from './components/ProcessInputTable.vue'
import SchedulerControl from './components/SchedulerControl.vue'
import SchedulerStatus from './components/SchedulerStatus.vue'
import AlgorithmSelector from './components/AlgorithmSelector.vue'
import PerformanceStats from './components/PerformanceStats.vue'
import Layout from './components/Layout.vue'
import Login from './components/Login.vue'
import ProducerConsumer from './components/ProducerConsumer.vue'

export default {
  components: {
    ProcessInputTable,
    SchedulerControl,
    SchedulerStatus,
    AlgorithmSelector,
    PerformanceStats,
    Layout,
    Login,
    ProducerConsumer,
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
      loggedIn: false,
      currentExperiment: 'scheduler',
    }
  },
  methods: {
    handleExperimentChange(experiment) {
      this.currentExperiment = experiment;
    },
    addProcess() {
      this.processes.push({
        pid: this.processes.length + 1,
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
    },
    handleBatchAddProcesses(batchProcessData) {
      const { count, defaultArrivalTime, defaultBurstTime, defaultJobSize, defaultPriority, incrementArrivalTime } = batchProcessData;
      
      // 获取当前最大进程ID
      const maxProcessId = this.processes.length > 0 
        ? Math.max(...this.processes.map(p => p.pid)) 
        : 0;
      
      // 生成新进程数组
      const newProcesses = [];
      for (let i = 0; i < count; i++) {
        const processId = maxProcessId + i + 1;
        const processArrivalTime = incrementArrivalTime 
          ? defaultArrivalTime + i 
          : defaultArrivalTime;
        
        newProcesses.push({
          pid: processId,
          arrivalTime: processArrivalTime,
          burstTime: defaultBurstTime,
          jobSize: defaultJobSize,
          priority: defaultPriority
        });
      }
      
      // 将新进程添加到现有进程列表
      this.processes = [...this.processes, ...newProcesses];
      alert(`成功添加 ${count} 个进程！`);
    },
    handleLoggedIn() {
      this.loggedIn = true;
    }
  }
}
</script>

<style>
* {
  box-sizing: border-box;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  font-size: 14px;
  line-height: 1.5;
}

#app {
  height: 100%;
}

.app-container {
  width: 100%;
  padding: 1em;
}

.section-block {
  margin-bottom: 1em;
  padding: 1em;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.input-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 0.5em;
}

.input-table th, .input-table td {
  border: 1px solid #ccc;
  padding: 0.5em;
  text-align: center;
}

h1 {
  color: #2c3e50;
  text-align: center;
  margin-top: 0;
  margin-bottom: 1em;
  font-size: 1.8rem;
}

h2 {
  margin-top: 0;
  margin-bottom: 1em;
  font-size: 1.3rem;
}

button {
  margin-right: 0.5em;
  font-size: 14px;
  padding: 8px 12px;
}

/* 优化滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .app-container {
    padding: 0.5em;
  }
  
  .section-block {
    padding: 0.8em;
  }
  
  h1 {
    font-size: 1.5rem;
  }
  
  h2 {
    font-size: 1.1rem;
  }
  
  button {
    padding: 6px 10px;
    font-size: 13px;
  }
}
</style>
