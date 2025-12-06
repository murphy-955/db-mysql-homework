<template>
  <!-- 管理员登录界面-->
  <div class="login-page" :style="{ backgroundImage: `url(${selectedBackground})` }">
    <div class="background">
      <div class="login-container">
        <h1><img src="@/assets/icons/记账本icon.png" alt="记账本" style="width: 92px; height: 92px; margin-right: 10px;">管理员模式</h1>
        <form @submit.prevent="handleAdminLogin">
          <div class="form-group">
            <label for="admin_name">账号</label>
            <input id="admin_name" v-model="admin_name" type="text" required placeholder="请输入管理员账号" />
          </div>
          <div class="form-group">
            <label for="admin_pwd">密码</label>
            <input id="admin_pwd" v-model="admin_pwd" type="password" required placeholder="请输入管理员密码" />
          </div>
          <button type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>
        <div class="links">
          <router-link to="/login">误触了？返回普通登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { sha256 } from '@/util/crypto';
import han from '@/assets/adminLogin/汉朝.jpg';
import carthage from '@/assets/adminLogin/迦太基.jpg';
import qajar from '@/assets/adminLogin/卡扎尔.jpg';
import ming from '@/assets/adminLogin/明朝.jpg';
import qing from '@/assets/adminLogin/清朝.jpg';
import spain from '@/assets/adminLogin/西班牙.jpg';
import siam from '@/assets/adminLogin/暹罗.jpg';
import silla from '@/assets/adminLogin/新罗.jpg';
import chola from '@/assets/adminLogin/朱罗.jpg';
import aksum from '@/assets/adminLogin/阿克苏姆.jpg';

export default {
  name: 'AdminLoginPage',
  data() {
    return {
      admin_name: '',
      admin_pwd: '',
      loading: false,
      error: null,
      selectedBackground: '',
      backgroundImages: [han, carthage, qajar, ming, qing, spain, siam, silla, chola, aksum],
    };
  },
  mounted() {
    this.selectedBackground = this.backgroundImages[Math.floor(Math.random() * this.backgroundImages.length)];
  },
  methods: {
    async handleAdminLogin() {
      this.loading = true;
      this.error = null;

      // 对密码进行SHA256加密
      const encryptedPassword = await sha256(this.admin_pwd);
      
      const loginData = {
        username: this.admin_name,
        password: encryptedPassword,
      };

      try {
        const response = await axios.post('http://localhost:8080/api/admin/login', loginData);

        if (response.data.statusCode === 200) {
          localStorage.setItem('adminToken', response.data.data.token);
          this.$router.push('/admin-dashboard');
        } else {
          this.error = response.data.message;
        }
      } catch (err) {
        this.error = '管理员登录失败，请重试';
        console.error(err);
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.login-page {
  --background-color: #930b0b;
  --login-container-bg: white;
  --button-primary: #007bff;
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
  background-color: var(--background-color);
  background-image: url('/public/background-pattern.svg');
  background-size: cover;
  background-position: center; /* 添加居中定位 */
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  padding-left: 10%;
  padding-top: 3%;
}

/* 登录容器 */
.login-container {
  background: rgba(255, 255, 255, 0.7);
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
  margin-bottom: 50px;
  display: flex;
  align-items: center;
  font-weight: bold;
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
  padding: 10px;
  background: linear-gradient(to right, red, darkred); /* 渐变红色背景 */
  background-size: 200% 200%; /* 支持流动动画 */
  color: var(--text-color);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  /* 可以调整按钮的顶部距离 */
  margin-top: 10px;
  margin-bottom: 20px;
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

links {
  margin-top: 15px;
  font-size: 16px;
  text-align: center;
}
</style>
