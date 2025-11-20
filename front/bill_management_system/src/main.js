import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import { createRouter, createWebHistory } from 'vue-router';
import LoginPage from './components/login.vue';
import DashboardPage from './components/dashboard.vue';
import BillQueryPage from './components/billQuery.vue';
import BillAddPage from './components/billAdd.vue';
import StatisticsPage from './components/statistics.vue';

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: LoginPage },
  { path: '/dashboard', component: DashboardPage },
  { path: '/bill-query', component: BillQueryPage },
  { path: '/bill-add', component: BillAddPage },
  { path: '/statistics', component: StatisticsPage },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

const app = createApp(App);
app.use(router);
app.mount('#app');
