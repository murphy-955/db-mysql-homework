<template>
  <div class="register-page" :style="{ backgroundImage: `url(${backgroundImage})` }">
    <div class="register-container">
      <h1>注册新账户</h1>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="acc_name">账号</label>
          <input id="acc_name" v-model="acc_name" type="text" required placeholder="请输入账号" />
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input id="password" v-model="password" type="password" required placeholder="请输入密码" />
        </div>
        <div class="form-group">
          <label for="confirm_password">确认密码</label>
          <input id="confirm_password" v-model="confirm_password" type="password" required placeholder="请再次输入密码" />
          <div v-if="passwordMismatch" class="error">密码不匹配</div>
        </div>
        <button type="submit">注册</button>
      </form>
      <div class="links">
        <router-link to="/login">已有账户？去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watchEffect } from 'vue';
import { useRouter } from 'vue-router';
import backgroundImage from '@/assets/register/background.jpg';

const acc_name = ref('');
const password = ref('');
const confirm_password = ref('');
const passwordMismatch = ref(false);
const router = useRouter();

watchEffect(() => {
  passwordMismatch.value = password.value !== confirm_password.value;
});

const handleRegister = () => {
  if (passwordMismatch.value) {
    return;
  }
  alert('注册成功，正在返回登录界面...');
  router.push('/login');
};
</script>

<style scoped>
.register-page {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: right;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding-left: 10%;
}

.register-container {
  background: rgba(255, 255, 255, 0.7);
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  text-align: center;
  width: 500px;
  height: 90vh;
  display: flex;
  flex-direction: column;
}

h1 {
  font-size: 28px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
  text-align: left;
}

label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
}

input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

button {
  width: 100%;
  padding: 10px;
  background: linear-gradient(to right, lightblue, #007bff);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 10px;
}

button:hover {
  background: #0056b3;
}

.error {
  color: red;
  font-size: 12px;
  margin-top: 5px;
}

.links {
  margin-top: 15px;
  font-size: 14px;
}
</style>
