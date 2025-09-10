import { createApp } from 'vue'
import axios from 'axios'
import App from './App.vue'

const app = createApp(App)

// 配置axios基础URL
axios.defaults.baseURL = 'http://localhost:8080'

// 配置axios跨域请求
axios.defaults.withCredentials = true

// 设置请求超时
axios.defaults.timeout = 10000

// 添加axios到全局属性
app.config.globalProperties.$axios = axios

app.mount('#app')
