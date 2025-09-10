<template>
  <section class="section-block">
    <h2>进程输入</h2>
    
    <!-- 操作工具栏 -->
    <div class="process-toolbar">
      <div class="toolbar-buttons">
        <button 
          class="add-btn"
          @click="$emit('add-process')"
        >
          <span class="add-icon">➕</span>
          <span>添加进程</span>
        </button>
        <button 
          class="batch-add-btn"
          @click="showBatchModal = true"
        >
          <span class="batch-icon">📋</span>
          <span>批量添加</span>
        </button>
      </div>
      <div class="process-count">
        <span class="count-icon">📊</span>
        <span>当前进程数: <strong>{{ processes.length }}</strong></span>
      </div>
    </div>

    <!-- 批量添加模态框 -->
    <div v-if="showBatchModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>批量添加进程</h3>
          <button class="close-btn" @click="closeModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>添加数量</label>
            <input 
              type="number" 
              v-model.number="batchConfig.count" 
              min="1" 
              max="50" 
              placeholder="输入要添加的进程数量"
              class="modal-input"
            />
          </div>
          <div class="form-group">
            <label>默认到达时间</label>
            <input 
              type="number" 
              v-model.number="batchConfig.defaultArrivalTime" 
              min="0" 
              placeholder="0"
              class="modal-input"
            />
          </div>
          <div class="form-group">
            <label>默认消耗时间</label>
            <input 
              type="number" 
              v-model.number="batchConfig.defaultBurstTime" 
              min="1" 
              placeholder="1"
              class="modal-input"
            />
          </div>
          <div class="form-group">
            <label>默认工作大小</label>
            <input 
              type="number" 
              v-model.number="batchConfig.defaultJobSize" 
              min="1" 
              placeholder="10"
              class="modal-input"
            />
          </div>
          <div class="form-group">
            <label>默认优先级</label>
            <input 
              type="number" 
              v-model.number="batchConfig.defaultPriority" 
              min="1" 
              max="10" 
              placeholder="5"
              class="modal-input"
            />
          </div>
          <div class="form-group checkbox-group">
            <input 
              type="checkbox" 
              id="incrementArrivalTime" 
              v-model="batchConfig.incrementArrivalTime"
              class="modal-checkbox"
            />
            <label for="incrementArrivalTime">按序递增到达时间</label>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeModal">取消</button>
          <button class="confirm-btn" @click="handleBatchAdd">确认添加</button>
        </div>
      </div>
    </div>
    
    <!-- 进程表格卡片 -->
    <div class="process-table-card">
      <div class="table-wrapper">
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
            <tr v-for="(process, idx) in processes" :key="process.pid" class="process-row">
              <td>
                <input 
                  v-model="process.pid" 
                  @input="onUpdate(idx, 'pid', process.pid)" 
                  placeholder="P1"
                  class="process-input"
                />
              </td>
              <td>
                <input 
                  type="number" 
                  v-model.number="process.arrivalTime" 
                  @input="onUpdate(idx, 'arrivalTime', process.arrivalTime)" 
                  min="0" 
                  placeholder="0"
                  class="process-input"
                />
              </td>
              <td>
                <input 
                  type="number" 
                  v-model.number="process.burstTime" 
                  @input="onUpdate(idx, 'burstTime', process.burstTime)" 
                  min="1" 
                  placeholder="1"
                  class="process-input"
                />
              </td>
              <td>
                <input 
                  type="number" 
                  v-model.number="process.jobSize" 
                  @input="onUpdate(idx, 'jobSize', process.jobSize)" 
                  min="1" 
                  placeholder="10"
                  class="process-input"
                />
              </td>
              <td>
                <input 
                  type="number" 
                  v-model.number="process.priority" 
                  @input="onUpdate(idx, 'priority', process.priority)" 
                  min="1" 
                  max="10"
                  placeholder="5"
                  class="process-input"
                />
              </td>
              <td>
                <button 
                  class="remove-btn"
                  @click="$emit('remove-process', idx)"
                  title="删除进程"
                >
                  🗑️
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <!-- 提示信息 -->
    <div v-if="processes.length === 0" class="empty-state">
      <div class="empty-icon">📋</div>
      <div class="empty-text">暂无进程数据</div>
      <div class="empty-subtext">点击 "添加进程" 按钮开始配置进程</div>
    </div>
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
  data() {
    return {
      showBatchModal: false,
      batchConfig: {
        count: 5,
        defaultArrivalTime: 0,
        defaultBurstTime: 1,
        defaultJobSize: 10,
        defaultPriority: 5,
        incrementArrivalTime: true
      }
    }
  },
  methods: {
    onUpdate(idx, field, value) {
      // 通知父组件更新数据
      this.$emit('update-process', idx, field, value)
    },
    closeModal() {
      this.showBatchModal = false
    },
    handleBatchAdd() {
      // 验证数据
      if (!this.batchConfig.count || this.batchConfig.count < 1 || this.batchConfig.count > 50) {
        alert('请输入有效的进程数量（1-50）')
        return
      }
      
      // 发送批量添加事件
      this.$emit('batch-add-processes', this.batchConfig)
      
      // 关闭模态框
      this.closeModal()
    }
  }
}</script>

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

/* 工具栏样式 */
.process-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 15px;
}

.toolbar-buttons {
  display: flex;
  gap: 10px;
}

.batch-add-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #28a745 0%, #218838 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(40, 167, 69, 0.3);
}

.batch-add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(40, 167, 69, 0.4);
}

.batch-add-btn:active {
  transform: translateY(0);
  box-shadow: 0 4px 6px rgba(40, 167, 69, 0.3);
}

.batch-icon {
  font-size: 1.2rem;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease-out;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 440px;
  max-height: 75vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.3rem;
  cursor: pointer;
  color: #718096;
  padding: 0;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background-color: #f7fafc;
  color: #4a5568;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: #4a5568;
  font-weight: 500;
  font-size: 0.9rem;
}

.modal-input {
  width: 100%;
  padding: 8px 12px;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  font-size: 13px;
  transition: all 0.3s ease;
  background-color: white;
}

.modal-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.modal-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e2e8f0;
  background-color: #f7fafc;
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
}

.cancel-btn {
  padding: 10px 20px;
  background-color: #e2e8f0;
  color: #4a5568;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background-color: #cbd5e0;
}

.confirm-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(102, 126, 234, 0.3);
}

.confirm-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 12px rgba(102, 126, 234, 0.4);
}

.confirm-btn:active {
  transform: translateY(0);
  box-shadow: 0 4px 6px rgba(102, 126, 234, 0.3);
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(102, 126, 234, 0.3);
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(102, 126, 234, 0.4);
}

.add-btn:active {
  transform: translateY(0);
  box-shadow: 0 4px 6px rgba(102, 126, 234, 0.3);
}

.add-icon {
  font-size: 1.2rem;
}

.process-count {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f8f9fa;
  border-radius: 6px;
  font-size: 0.9rem;
}

.count-icon {
  font-size: 1.2rem;
}

.process-count strong {
  color: #667eea;
  font-weight: bold;
}

/* 进程表格卡片 */
.process-table-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.table-wrapper {
  overflow-x: auto;
}

.input-table {
  width: 100%;
  border-collapse: collapse;
}

.input-table th {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4eaf2 100%);
  padding: 14px 16px;
  text-align: left;
  font-weight: 600;
  color: #4a5568;
  font-size: 0.95rem;
  border-bottom: 2px solid #e2e8f0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.process-row {
  transition: all 0.3s ease;
  border-bottom: 1px solid #f1f1f1;
}

.process-row:hover {
  background-color: rgba(102, 126, 234, 0.05);
  transform: translateX(3px);
}

.process-row:last-child {
  border-bottom: none;
}

.input-table td {
  padding: 12px 16px;
  text-align: left;
  border: 1px solid #f1f1f1;
}

/* 输入框样式 */
.process-input {
  width: 100%;
  padding: 8px 12px;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s ease;
  background-color: white;
}

.process-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.process-input::placeholder {
  color: #a0aec0;
}

/* 删除按钮样式 */
.remove-btn {
  padding: 8px 12px;
  background: linear-gradient(135deg, #f44336 0%, #d32f2f 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
}

.remove-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 8px rgba(244, 67, 54, 0.3);
}

.remove-btn:active {
  transform: scale(0.95);
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #718096;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 16px;
  opacity: 0.7;
}

.empty-text {
  font-size: 1.2rem;
  font-weight: 500;
  margin-bottom: 8px;
  color: #4a5568;
}

.empty-subtext {
  font-size: 0.95rem;
  opacity: 0.8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .process-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .add-btn {
    justify-content: center;
  }
  
  .process-count {
    justify-content: center;
  }
  
  .input-table th, .input-table td {
    padding: 10px 8px;
  }
  
  .process-input {
    padding: 6px 8px;
    font-size: 13px;
  }
  
  .remove-btn {
    padding: 6px 10px;
    min-width: 36px;
    font-size: 1rem;
  }
}
</style>