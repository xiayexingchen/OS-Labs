<template>
  <div class="layout">
    <aside class="sidebar">
      <!-- Sidebar content will go here -->
      <h2>实验列表</h2>
      <ul>
        <li class="active">进程调度</li>
        <!-- More experiments can be added here -->
      </ul>
    </aside>
    <main class="main-content">
      <slot></slot>
    </main>
  </div>
</template>

<script>
export default {
  name: 'AppLayout',
}
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 200px;
  background-color: #2c3e50;
  color: white;
  padding: 20px;
}

.sidebar h2 {
  margin-top: 0;
}

.sidebar ul {
  list-style-type: none;
  padding: 0;
}

.sidebar li {
  padding: 10px;
  cursor: pointer;
}

.sidebar li.active {
  background-color: #34495e;
}

.main-content {
  flex-grow: 1;
  padding: 20px;
  background-color: #ecf0f1;
  overflow-y: auto;
}
</style>