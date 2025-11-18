<template>
  <div class="login-page">
    <div class="background">
      <div class="login-container">
        <h1>来福记账本</h1>
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="acc_name">账号</label>
            <input
              id="acc_name"
              v-model="acc_name"
              type="text"
              required
              placeholder="请输入您的账号"
            />
          </div>
          <div class="form-group">
            <label for="password">密码</label>
            <input
              id="password"
              v-model="password"
              type="password"
              required
              placeholder="请输入您的密码"
            />
          </div>
          <button type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>
        <button @click="navigateToDashboard" class="debug-button">直接进入主界面</button>
        <div class="links">
          <a href="#">没有账户？立即注册</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'LoginPage',
  data() {
    return {
      acc_name: '',
      password: '',
      loading: false,
      error: null,
    };
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
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #800000;
  background-image: url('/public/background-pattern.svg');
  background-size: cover;
}

.login-container {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  text-align: center;
  width: 400px;
}

h1 {
  font-size: 32px;
  margin-bottom: 25px;
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
  padding: 15px;
  background-color: #d9534f;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
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
</style>
