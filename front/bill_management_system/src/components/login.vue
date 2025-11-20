<template>
  <!-- 普通用户登录界面-->
  <div class="login-page" :style="{ backgroundImage: `url(${selectedBackground})` }">
    <div class="background">
      <div class="login-container">
        <h1>来福记账本</h1>
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="acc_name">账号</label>
            <input id="acc_name" v-model="acc_name" type="text" required placeholder="请输入您的账号" />
          </div>
          <div class="form-group">
            <label for="password">密码</label>
            <input id="password" v-model="password" type="password" required placeholder="请输入您的密码" />
          </div>
          <button type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>
        <hr class="divider" />
        <button v-if="!showMoreOptions" @click="showMoreOptions = true" class="more-options-btn">查看更多选项</button>
        <div v-if="showMoreOptions" class="more-options">
          <button @click="navigateToDashboard" class="debug-button">直接进入主界面</button>
          <button @click="$router.push('/admin-login')" class="admin-option-btn">管理员登录</button>
        </div>
        <div class="links">
          <router-link to="/register">没有账户？立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import beach from '@/assets/login/beach.jpg';
import dandelion from '@/assets/login/dandelion.jpg';
import sunset from '@/assets/login/sunset.jpg';
import castle from '@/assets/login/castle.jpg';
import greatwall from '@/assets/login/greatwall.jpg';

export default {
  name: 'LoginPage',
  data() {
    return {
      acc_name: '',
      password: '',
      loading: false,
      error: null,
      showMoreOptions: false,
      selectedBackground: '',
      backgroundImages: [beach, dandelion, sunset, castle, greatwall],
    };
  },
  mounted() {
    this.selectedBackground = this.backgroundImages[Math.floor(Math.random() * this.backgroundImages.length)];
  },
  methods: {
    async handleLogin() {
      this.loading = true;
      this.error = null;

      const loginData = {
        username: this.acc_name,
        password: this.password,
      };

      try {
        const response = await axios.post('http://localhost:8080/api/user/login', loginData);

        if (response.data.statusCode === 200) {
          localStorage.setItem('token', response.data.data.token);
          this.$router.push('/dashboard');
        } else {
          this.error = response.data.message;
        }
      } catch (err) {
        this.error = '登录失败，请重试';
        console.error(err);
      } finally {
        this.loading = false;
      }
    },
    navigateToDashboard() {
      this.$router.push('/dashboard');
    },
  },
};
</script>

<style scoped>

.login-page {
  --background-color: #930b0b;
  --login-container-bg: white;
  --button-primary: #0c14ff;
  --button-primary-light: #cce7ff;
  --button-admin: #ff6b35;
  --button-admin-hover: #e55a2b;
  --text-color: white;
  --shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

/* 页面背景 */
.login-page {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;  /* 改为顶部对齐 */
  padding-left: 10%;
  padding-top: 3%;  /* 添加顶部距离 */
}

/* 登录窗口设置 */
.login-container {
  background: rgba(255, 255, 255, 0.7);  /* 透明度设置，目前为 70% */
  padding: 30px;
  border-radius: 8px;
  box-shadow: var(--shadow);
  text-align: center;
  width: 500px;
  height: 90vh;
  display: flex;
  flex-direction: column;
}

h1 {
  font-size: 32px;
  margin-bottom: 50px;  /* 增加间隔 */
}

.form-group {
  margin-bottom: 20px;
  text-align: left;
}

label {
  display: block;
  margin-bottom: 8px;
  font-size: 16px;
}

input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

button {
  width: 100%;
  padding: 10px;  /* 放大按钮 */
  background: linear-gradient(to right, lightblue, #007bff);
  background-size: 200% 200%;
  color: var(--text-color);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 20px;
}

button:hover {
  animation: flow 2s infinite;
}

@keyframes flow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.debug-button {
  margin-top: 15px;
  background-color: #5bc0de;
}

.links {
  margin-top: 15px;
  font-size: 16px;
  text-align: center;
}

/* 新增样式 */
.divider {
  border: none;
  height: 1px;
  background-color: #000;
  margin-top: 30px;
  margin-bottom: 20px;
}

.more-options-btn {
  width: 100%;
  padding: 10px;  /* 统一padding */
  background-color: transparent;
  color: var(--button-primary);
  border: 1px solid var(--button-primary);
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-bottom: 10px;
}

.more-options {
  display: flex;
  flex-direction: column;
}

.admin-option-btn {
  padding: 10px;  /* 统一padding */
  background-color: var(--button-admin);
  color: var(--text-color);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.admin-option-btn:hover {
  background-color: var(--button-admin-hover);
}

.error {
  color: red;
  margin-top: 10px;
}
</style>
