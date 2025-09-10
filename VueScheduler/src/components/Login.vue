<template>
  <div class="login-container">
    <!-- 装饰元素 -->
    <div class="login-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>
    
    <div class="login-box">
      <!-- 登录标题与图标 -->
      <div class="login-header">
        <div class="login-icon">🔐</div>
        <h2 class="login-title">系统登录</h2>
        <p class="login-subtitle">请输入您的用户名和密码</p>
      </div>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <!-- 用户名输入 -->
        <div class="input-group">
          <div class="input-wrapper">
            <span class="input-icon">👤</span>
            <input 
              type="text" 
              id="username" 
              v-model="username" 
              placeholder="请输入用户名"
              required
              class="form-input"
              autocomplete="username"
            >
          </div>
        </div>
        
        <!-- 密码输入 -->
        <div class="input-group">
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input 
              type="password" 
              id="password" 
              v-model="password" 
              placeholder="请输入密码"
              required
              class="form-input"
              autocomplete="current-password"
            >
          </div>
        </div>
        
        <!-- 记住密码和忘记密码 -->
        <div class="login-options">
          <label class="remember-me">
            <input type="checkbox" v-model="rememberMe">
            <span>记住密码</span>
          </label>
          <a href="#" class="forgot-password">忘记密码？</a>
        </div>
        
        <!-- 登录按钮 -->
        <button type="submit" class="login-button">
          <span class="button-text">登录</span>
          <span class="button-icon">→</span>
        </button>
      </form>
      
      <!-- 登录提示 -->
      <div class="login-footer">
        <p class="login-note">测试账号：admin / 123456</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserLogin',
  data() {
    return {
      username: '',
      password: '',
      rememberMe: false
    };
  },
  methods: {
    handleLogin() {
      // 简单的登录验证
      if (this.username && this.password) {
        // 添加表单提交动画
        this.$emit('loggedIn');
      }
    }
  },
  mounted() {
    // 尝试从localStorage恢复记住的用户名
    const savedUsername = localStorage.getItem('rememberedUsername');
    if (savedUsername) {
      this.username = savedUsername;
      this.rememberMe = true;
    }
  },
  watch: {
    rememberMe(newValue) {
      // 记住用户名到localStorage
      if (newValue && this.username) {
        localStorage.setItem('rememberedUsername', this.username);
      } else {
        localStorage.removeItem('rememberedUsername');
      }
    }
  }
};
</script>

<style scoped>
/* 全局动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* 登录容器 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* 装饰元素 */
.login-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 8s ease-in-out infinite;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  right: -50px;
  animation-delay: 2s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 10%;
  animation-delay: 4s;
}

/* 登录框 */
.login-box {
  padding: 40px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  width: 100%;
  max-width: 400px;
  z-index: 10;
  animation: fadeIn 0.6s ease-out;
  transition: transform 0.3s ease;
}

.login-box:hover {
  transform: translateY(-5px);
}

/* 登录头部 */
.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-icon {
  font-size: 4rem;
  margin-bottom: 15px;
  animation: pulse 2s ease-in-out infinite;
}

.login-title {
  font-size: 2rem;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 8px;
}

.login-subtitle {
  color: #7f8c8d;
  font-size: 1rem;
}

/* 登录表单 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 输入组 */
.input-group {
  position: relative;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border-radius: 12px;
  padding: 0 15px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-icon {
  font-size: 1.2rem;
  margin-right: 10px;
  color: #7f8c8d;
}

.form-input {
  width: 100%;
  padding: 14px 0;
  border: none;
  background: transparent;
  font-size: 1rem;
  outline: none;
}

.form-input::placeholder {
  color: #95a5a6;
}

/* 登录选项 */
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: -10px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #5d6d7e;
  font-size: 0.9rem;
}

.remember-me input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.forgot-password {
  color: #667eea;
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #764ba2;
  text-decoration: underline;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.5);
}

.login-button:active {
  transform: translateY(0);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.button-icon {
  transition: transform 0.3s ease;
}

.login-button:hover .button-icon {
  transform: translateX(5px);
}

/* 登录底部 */
.login-footer {
  text-align: center;
  margin-top: 25px;
}

.login-note {
  color: #7f8c8d;
  font-size: 0.85rem;
  background: #f8f9fa;
  padding: 8px 16px;
  border-radius: 8px;
  display: inline-block;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-box {
    margin: 0 20px;
    padding: 30px 25px;
  }
  
  .login-title {
    font-size: 1.6rem;
  }
  
  .login-icon {
    font-size: 3rem;
  }
  
  .form-input {
    font-size: 0.95rem;
  }
  
  .login-options {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .forgot-password {
    align-self: flex-end;
  }
}
</style>