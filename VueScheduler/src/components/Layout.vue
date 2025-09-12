<template>
  <div class="layout">
    <aside class="sidebar">
      <!-- 侧边栏头部 -->
      <div class="sidebar-header">
        <h2 class="sidebar-title">实验列表</h2>
      </div>
      
      <!-- 侧边栏菜单 -->
      <nav class="sidebar-nav">
        <ul class="sidebar-menu">
          <li class="menu-item">
            <a 
              href="#" 
              :class="{ active: activeExperiment === 'scheduler' }"
              @click.prevent="switchExperiment('scheduler')"
              class="menu-link"
            >
              <span class="menu-icon">⚙️</span>
              <span class="menu-text">进程调度</span>
            </a>
          </li>
          <li class="menu-divider"></li>
          <li class="menu-item">
            <a 
              href="#" 
              :class="{ active: activeExperiment === 'producer-consumer' }"
              @click.prevent="switchExperiment('producer-consumer')"
              class="menu-link"
            >
              <span class="menu-icon">🔄</span>
              <span class="menu-text">生产者-消费者</span>
            </a>
          </li>
        </ul>
      </nav>
      
      <!-- 侧边栏底部 -->
      <div class="sidebar-footer">
        <div class="version-info">
          <span class="version-text">v1.0.0</span>
        </div>
      </div>
    </aside>
    
    <!-- 主内容区域 -->
    <main class="main-content">
      <slot></slot>
    </main>
  </div>
</template>

<script>
export default {
  name: 'AppLayout',
  data() {
    return {
      activeExperiment: 'scheduler'
    }
  },
  methods: {
    switchExperiment(experiment) {
      this.activeExperiment = experiment;
      this.$emit('experiment-change', experiment);
    }
  }
}
</script>

<style scoped>
/* 整体布局 */
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* 侧边栏样式 */
.sidebar {
  width: 240px;
  min-width: 240px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

/* 侧边栏头部 */
.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-title {
  margin: 0;
  font-size: 1.4rem;
  color: #ffffff;
  text-align: center;
  font-weight: 600;
  letter-spacing: 0.5px;
}

/* 侧边栏导航 */
.sidebar-nav {
  flex: 1;
  padding: 20px 0;
}

.sidebar-menu {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

/* 菜单项 */
.menu-item {
  margin: 0;
  padding: 0;
}

.menu-link {
  display: flex;
  align-items: center;
  padding: 14px 25px;
  color: #ecf0f1;
  text-decoration: none;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
  position: relative;
  overflow: hidden;
}

.menu-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  transform: translateX(5px);
}

.menu-link.active {
  background: linear-gradient(90deg, rgba(52, 152, 219, 0.2), rgba(52, 152, 219, 0.1));
  border-left-color: #3498db;
  color: #ffffff;
  font-weight: 500;
}

.menu-link.active::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 100%;
  background: linear-gradient(90deg, rgba(52, 152, 219, 0.15), transparent);
  animation: glow 2s infinite alternate;
}

@keyframes glow {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 菜单图标和文字 */
.menu-icon {
  font-size: 1.2rem;
  margin-right: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
}

.menu-text {
  font-size: 1rem;
  transition: all 0.3s ease;
}

/* 分隔线 */
.menu-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
  margin: 5px 0;
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.1);
}

.version-info {
  text-align: center;
}

.version-text {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
}

/* 主内容区域 */
.main-content {
  flex-grow: 1;
  padding: 20px;
  background-color: #ecf0f1;
  overflow-y: auto;
  transition: all 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 220px;
    min-width: 220px;
  }
  
  .menu-link {
    padding: 12px 20px;
  }
  
  .menu-icon {
    font-size: 1.1rem;
    margin-right: 10px;
  }
  
  .menu-text {
    font-size: 0.95rem;
  }
}
</style>