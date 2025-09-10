<template>
  <section class="section-block">
    <h2>控制操作</h2>
    <div class="control-container">
      <!-- 主控制按钮组 -->
      <div class="main-controls">
        <button 
          class="control-btn init-btn"
          @click="initialize"
          :disabled="inited"
          :class="{ disabled: inited }"
        >
          <div class="btn-icon">🚀</div>
          <div class="btn-text">初始化</div>
        </button>
        
        <button 
          class="control-btn step-btn"
          @click="step"
          :disabled="!inited"
          :class="{ disabled: !inited }"
        >
          <div class="btn-icon">▶️</div>
          <div class="btn-text">单步执行</div>
        </button>
        
        <button 
          class="control-btn run-btn"
          @click="run"
          :disabled="!inited"
          :class="{ disabled: !inited }"
        >
          <div class="btn-icon">⏭️</div>
          <div class="btn-text">运行到底</div>
        </button>
        
        <button 
          class="control-btn reset-btn"
          @click="reset"
          :disabled="!inited"
          :class="{ disabled: !inited }"
        >
          <div class="btn-icon">🔄</div>
          <div class="btn-text">重置</div>
        </button>
        
        <button 
          class="control-btn reset-all-btn"
          @click="resetAll"
        >
          <div class="btn-icon">🧹</div>
          <div class="btn-text">全部重置</div>
        </button>
      </div>
      
      <!-- 控制信息提示区 -->
      <div class="control-info" v-if="inited">
        <div class="info-badge active">
          <div class="badge-icon">✅</div>
          <div class="badge-text">调度器已初始化</div>
        </div>
        <div class="info-text">
          可以执行 <strong>单步执行</strong> 查看详细过程，或点击 <strong>运行到底</strong> 直接查看结果
        </div>
      </div>
      
      <!-- 未初始化提示 -->
      <div class="control-info" v-else>
        <div class="info-badge">
          <div class="badge-icon">ℹ️</div>
          <div class="badge-text">调度器就绪</div>
        </div>
        <div class="info-text">
          请先点击 <strong>初始化</strong> 开始调度过程
        </div>
      </div>
    </div>
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
  },
  methods: {
    initialize() {
      this.$emit('init');
    },
    step() {
      this.$emit('step');
    },
    run() {
      this.$emit('run');
    },
    reset() {
      this.$emit('reset');
    },
    resetAll() {
      this.$emit('reset-all');
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

.control-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.main-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.control-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 20px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 120px;
  font-weight: 600;
  color: white;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.control-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.control-btn:hover::before {
  left: 100%;
}

.control-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.2);
}

.control-btn:active {
  transform: translateY(0);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.control-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.control-btn.disabled:hover {
  transform: none;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.control-btn.disabled:hover::before {
  left: -100%;
}

.btn-icon {
  font-size: 1.8rem;
}

.btn-text {
  font-size: 0.9rem;
  letter-spacing: 0.5px;
}

/* 按钮颜色区分 */
.init-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.step-btn {
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
}

.run-btn {
  background: linear-gradient(135deg, #2196F3 0%, #0b7dda 100%);
}

.reset-btn {
  background: linear-gradient(135deg, #FF9800 0%, #f57c00 100%);
}

.reset-all-btn {
  background: linear-gradient(135deg, #f44336 0%, #d32f2f 100%);
}

/* 控制信息区域 */
.control-info {
  padding: 16px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-radius: 8px;
  border-left: 4px solid #2196F3;
}

.info-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 6px;
  width: fit-content;
}

.info-badge.active {
  border-left: 3px solid #4CAF50;
}

.badge-icon {
  font-size: 1.2rem;
}

.badge-text {
  font-weight: 600;
  color: #2c3e50;
}

.info-text {
  color: #4a5568;
  line-height: 1.6;
}

.info-text strong {
  color: #2196F3;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .control-btn {
    min-width: auto;
    flex-direction: row;
    justify-content: center;
  }
  
  .btn-icon {
    font-size: 1.5rem;
  }
}
</style>