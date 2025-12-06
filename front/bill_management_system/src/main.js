import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import { createRouter, createWebHistory } from 'vue-router';
import LoginPage from './components/Login.vue';
import DashboardPage from './components/Dashboard.vue';
import BillDashboardPage from './components/BillDashboard.vue';
import StatisticsPage from './components/Statistics.vue';
import AdminLoginPage from './components/AdminLogin.vue';
import AdminDashboardPage from './components/AdminDashboard.vue';
import RegisterPage from './components/RegisterPage.vue';
import BillAddWindow from './components/BillAddWindow.vue';

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: LoginPage, meta: { title: '登录' } },
  { path: '/dashboard', component: DashboardPage, meta: { title: '仪表板' } },
  { path: '/bill-query', component: BillDashboardPage, meta: { title: '账单查询' } },
  { path: '/bill-add', component: BillAddWindow, meta: { title: '添加账单' } },
  { path: '/statistics', component: StatisticsPage, meta: { title: '统计' } },
  { path: '/admin-login', component: AdminLoginPage, meta: { title: '管理员登录' } },
  { path: '/admin-dashboard', component: AdminDashboardPage, meta: { title: '管理员控制台' } },
  { path: '/register', component: RegisterPage, meta: { title: '注册' } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.afterEach((to) => {
  document.title = '来福记账本 - ' + (to.meta.title || '首页');
});

const app = createApp(App);
app.use(router);
app.mount('#app');
