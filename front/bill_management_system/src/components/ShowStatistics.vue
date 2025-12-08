<template>
  <div class="dashboard-layout">
    <!-- 左侧侧边栏 -->
    <Sidebar />

    <!-- 右侧主内容区 -->
    <main class="main-content">
      <div class="content-wrapper">
        <!-- 筛选区域 -->
        <div class="filter-section">
          <h3 class="section-title">统计筛选</h3>
          <div class="filter-controls">
            <div class="date-range-selector">
              <label class="filter-label">日期范围：</label>
              <select v-model="dateRange" class="date-range-input" @change="loadStatistics">
                <option value="week">最近一周</option>
                <option value="month">最近一个月</option>
                <option value="quarter">最近三个月</option>
                <option value="halfYear">最近半年</option>
                <option value="year">最近一年</option>
                <option value="custom">自定义</option>
              </select>
              <template v-if="dateRange === 'custom'">
                <input type="date" v-model="startDate" class="date-input" @change="loadStatistics">
                <span class="date-separator">至</span>
                <input type="date" v-model="endDate" class="date-input" @change="loadStatistics">
              </template>
            </div>
            <button class="btn btn-primary" @click="exportReport">导出报表</button>
          </div>
        </div>

        <!-- 统计概览卡片 -->
        <div class="overview-section">
          <div class="overview-card">
            <div class="card-header">
              <h4 class="card-title">支出总额</h4>
              <span class="card-icon">↓</span>
            </div>
            <div class="card-value expense">¥{{ statistics.totalExpense.toLocaleString() }}</div>
            <div class="card-compare" :class="statistics.expenseTrend > 0 ? 'up' : 'down'">
              <span>{{ statistics.expenseTrend > 0 ? '↑' : '↓' }}</span>
              {{ Math.abs(statistics.expenseTrend) }}% 较上期
            </div>
          </div>

          <div class="overview-card">
            <div class="card-header">
              <h4 class="card-title">收入总额</h4>
              <span class="card-icon">↑</span>
            </div>
            <div class="card-value income">¥{{ statistics.totalIncome.toLocaleString() }}</div>
            <div class="card-compare" :class="statistics.incomeTrend > 0 ? 'up' : 'down'">
              <span>{{ statistics.incomeTrend > 0 ? '↑' : '↓' }}</span>
              {{ Math.abs(statistics.incomeTrend) }}% 较上期
            </div>
          </div>

          <div class="overview-card">
            <div class="card-header">
              <h4 class="card-title">结余</h4>
              <span class="card-icon">💰</span>
            </div>
            <div class="card-value balance" :class="statistics.balance > 0 ? 'positive' : 'negative'">
              ¥{{ Math.abs(statistics.balance).toLocaleString() }}
            </div>
            <div class="card-desc">
              {{ statistics.balance > 0 ? '盈余' : '赤字' }}
            </div>
          </div>

          <div class="overview-card">
            <div class="card-header">
              <h4 class="card-title">账单数量</h4>
              <span class="card-icon">📋</span>
            </div>
            <div class="card-value total">
              <span class="expense-count">{{ statistics.expenseCount }}</span> 笔支出 /
              <span class="income-count">{{ statistics.incomeCount }}</span> 笔收入
            </div>
            <div class="card-desc">
              共 {{ statistics.totalCount }} 笔账单
            </div>
          </div>
        </div>

        <!-- 图表区域 -->
        <div class="charts-section">
          <!-- 收支趋势图 -->
          <div class="chart-card">
            <div class="chart-header">
              <h4 class="chart-title">收支趋势</h4>
              <select v-model="trendChartType" class="chart-type-selector" @change="updateTrendChart">
                <option value="daily">按日</option>
                <option value="weekly">按周</option>
                <option value="monthly">按月</option>
              </select>
            </div>
            <div class="chart-content">
              <div v-if="loading" class="chart-loading">加载中...</div>
              <div v-else class="trend-chart">
                <Bar :data="trendChartData" :options="trendChartOptions" />
              </div>
            </div>
          </div>

          <!-- 支出分类饼图 -->
          <div class="chart-card">
            <div class="chart-header">
              <h4 class="chart-title">支出分类占比</h4>
            </div>
            <div class="chart-content">
              <div v-if="loading" class="chart-loading">加载中...</div>
              <div v-else-if="expenseCategories.length === 0" class="chart-empty">暂无支出数据</div>
              <div v-else class="pie-chart-container">
                <Pie :data="pieChartData" :options="pieChartOptions" />
                <div class="category-list">
                  <div v-for="(category, index) in expenseCategories" :key="index" class="category-item">
                    <span class="category-color" :style="{ backgroundColor: category.color }"></span>
                    <span class="category-name">{{ category.name }}</span>
                    <span class="category-percentage">{{ category.percentage }}%</span>
                    <span class="category-amount">¥{{ category.amount.toLocaleString() }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 账户分析 -->
        <div class="accounts-section">
          <h3 class="section-title">账户分析</h3>
          <div class="accounts-table-container">
            <table class="accounts-table">
              <thead>
                <tr>
                  <th>账户</th>
                  <th>支出金额</th>
                  <th>支出笔数</th>
                  <th>收入金额</th>
                  <th>收入笔数</th>
                  <th>总交易笔数</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(account, index) in accountsData" :key="index">
                  <td>{{ account.name }}</td>
                  <td class="expense-amount">¥{{ account.totalExpense.toLocaleString() }}</td>
                  <td>{{ account.expenseCount }}</td>
                  <td class="income-amount">¥{{ account.totalIncome.toLocaleString() }}</td>
                  <td>{{ account.incomeCount }}</td>
                  <td>{{ account.totalCount }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import axios from 'axios';
import Sidebar from './Sidebar.vue';
import { ref, onMounted, computed } from 'vue';
import { Bar, Pie } from 'vue-chartjs';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Tooltip, Legend, ArcElement } from 'chart.js';
import { useStatistics } from '@/composables/useStatistics';

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend, ArcElement);

// ==================== 使用共享的统计逻辑 ====================
const {
  loading,
  statistics,
  expenseCategories,
  accountsData,
  allBills,
  loadStatistics: loadStatisticsCore
} = useStatistics();

// ==================== ShowStatistics 特有的响应式数据 ====================
const dateRange = ref('month');
const startDate = ref('');
const endDate = ref('');
const trendChartType = ref('monthly');
const trendData = ref({ labels: [], data: [] });

// ==================== Chart.js 配置 ====================
const trendChartData = computed(() => ({
  labels: trendData.value.labels,
  datasets: [
    {
      label: '支出',
      data: trendData.value.data.map(d => d.expense),
      backgroundColor: '#d9534f',
      borderRadius: 4
    },
    {
      label: '收入',
      data: trendData.value.data.map(d => d.income),
      backgroundColor: '#5cb85c',
      borderRadius: 4
    }
  ]
}));

const trendChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'top' },
    tooltip: {
      mode: 'index',
      intersect: false,
    }
  },
  scales: {
    y: {
      beginAtZero: true
    }
  }
};

const pieChartData = computed(() => ({
  labels: expenseCategories.value.map(cat => cat.name),
  datasets: [{
    data: expenseCategories.value.map(cat => cat.amount),
    backgroundColor: expenseCategories.value.map(cat => cat.color),
    borderWidth: 1
  }]
}));

const pieChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'bottom' }
  }
};

// ==================== 方法 ====================

/**
 * 更新趋势图数据
 */
const updateTrendChart = (billsInput) => {
  let bills = Array.isArray(billsInput) ? billsInput : allBills.value;

  if (!bills || bills.length === 0) {
    trendData.value.labels = [];
    trendData.value.data = [];
    return;
  }

  const groupedData = {};

  bills.forEach(bill => {
    const date = new Date(bill.date);
    let key = '';

    if (trendChartType.value === 'daily') {
      key = bill.date.split(' ')[0];
    } else if (trendChartType.value === 'monthly') {
      const month = String(date.getMonth() + 1).padStart(2, '0');
      key = `${date.getFullYear()}-${month}`;
    } else if (trendChartType.value === 'weekly') {
      const dayOfWeek = date.getDay() || 7;
      const weekStart = new Date(date);
      weekStart.setDate(date.getDate() - dayOfWeek + 1);
      key = weekStart.toISOString().split('T')[0];
    }

    if (!groupedData[key]) {
      groupedData[key] = { expense: 0, income: 0 };
    }

    if (bill.recordEnum === 'EXPENDITURE') {
      groupedData[key].expense += (bill.amount || 0);
    } else if (bill.recordEnum === 'INCOME') {
      groupedData[key].income += (bill.amount || 0);
    }
  });

  const sortedKeys = Object.keys(groupedData).sort();
  trendData.value.labels = sortedKeys;
  trendData.value.data = sortedKeys.map(key => groupedData[key]);
};

/**
 * 加载统计数据（包装共享逻辑）
 */
const loadStatistics = async () => {
  if (dateRange.value === 'custom') {
    if (!startDate.value || !endDate.value) {
      alert('请选择开始和结束日期!');
      return;
    }
    await loadStatisticsCore(dateRange.value, startDate.value, endDate.value);
  } else {
    await loadStatisticsCore(dateRange.value);
  }

  // 加载完成后更新趋势图
  updateTrendChart();
};

/**
 * 导出报表
 */
const exportReport = async () => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('请先登录');
      return;
    }

    let queryStartDate = '', queryEndDate = '';
    if (dateRange.value === 'custom') {
      if (!startDate.value || !endDate.value) {
        alert('请选择日期范围');
        return;
      }
      queryStartDate = startDate.value;
      queryEndDate = endDate.value;
    } else {
      const { getDateRange } = useStatistics();
      const dates = getDateRange(dateRange.value);
      queryStartDate = dates.startDate;
      queryEndDate = dates.endDate;
    }

    const response = await axios.post('/api/query/exportReport', {
      token,
      startDate: queryStartDate,
      endDate: queryEndDate
    }, { responseType: 'blob' });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `report_${queryStartDate}_${queryEndDate}.xlsx`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('导出失败:', error);
    alert('导出失败，请稍后重试');
  }
};

// ==================== 生命周期 ====================
onMounted(() => {
  loadStatistics();
});
</script>

<style scoped>
.dashboard-layout {
  display: flex;
  height: 100vh;
  width: 100vw; /* 确保占满全屏 */
  background-color: #f0f2f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  /* overflow: hidden; */ /* 移除此行以允许整个页面滚动 */
}

/* 右侧主内容区 */
.main-content {
  flex: 1;
  height: 100%;
  overflow-y: auto; /* 内容区滚动 */
  padding: 40px;
  background-color: #f0f2f5;
}

.content-wrapper {
  max-width: 1600px; /* 放宽最大宽度 */
  margin: 0 auto;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 40px; /* 增加section之间的距离 */
}

/* 筛选区域样式 */
.filter-section {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 20px;
}

.filter-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date-range-selector {
  display: flex;
  align-items: center;
  gap: 15px;
}

.filter-label {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color);
}

.date-range-input,
.date-input {
  padding: 8px 12px;
  font-size: 14px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
}

.date-separator {
  color: var(--text-secondary);
}

/* 按钮样式 */
.btn {
  padding: 8px 20px;
  font-size: 14px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
}

.btn-primary:hover {
  background-color: #c9302c;
}

/* 概览卡片样式 */
.overview-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30px; /* 增加概览卡片之间的距离 */
  flex-shrink: 0;
}

.overview-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-secondary);
}

.card-icon {
  font-size: 20px;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 10px;
}

.card-value.expense {
  color: var(--primary-color);
}

.card-value.income {
  color: var(--income-color);
}

.card-value.balance.positive {
  color: var(--balance-positive);
}

.card-value.balance.negative {
  color: var(--balance-negative);
}

.card-value.total {
  font-size: 18px;
  color: var(--text-color);
}

.expense-count {
  color: var(--primary-color);
}

.income-count {
  color: var(--income-color);
}

.card-compare {
  font-size: 14px;
  font-weight: 500;
}

.card-compare.up {
  color: var(--primary-color);
}

.card-compare.down {
  color: var(--income-color);
}

.card-desc {
  font-size: 14px;
  color: var(--text-secondary);
}

/* 图表区域样式 */
.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  flex: 1;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color);
}

.chart-type-selector {
  padding: 6px 12px;
  font-size: 14px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
}

.chart-content {
  height: 400px;
  min-height: 400px;
  position: relative;
}

.chart-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: var(--text-secondary);
  font-size: 16px;
}

/* 趋势图样式 */
.trend-chart {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-bars {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 0 10px;
  margin-bottom: 10px;
}

.bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  width: 40px;
}

.expense-bar,
.income-bar {
  width: 20px;
  border-radius: 3px 3px 0 0;
  transition: height 0.5s ease;
}

.expense-bar {
  background-color: var(--primary-color);
}

.income-bar {
  background-color: var(--income-color);
}

.chart-x-axis {
  display: flex;
  justify-content: space-between;
  padding: 0 10px;
  margin-top: 10px;
}

.x-axis-label {
  font-size: 12px;
  color: var(--text-secondary);
  text-align: center;
  width: 40px;
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.legend-color.expense {
  background-color: var(--primary-color);
}

.legend-color.income {
  background-color: var(--income-color);
}

.legend-text {
  font-size: 14px;
  color: var(--text-secondary);
}

/* 饼图样式 */
.pie-chart-container {
  height: 100%;
  display: flex;
  gap: 40px;
}

.pie-chart {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pie-chart-simplified {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  /* 移除硬编码的background，改为动态生成 */
  position: relative;
}

.chart-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-secondary);
  font-size: 16px;
}

.pie-slice {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  transform-origin: center;
}

.category-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  max-height: 100%;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.category-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  flex-shrink: 0;
}

.category-name {
  font-size: 14px;
  color: var(--text-color);
  flex: 1;
}

.category-percentage {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  min-width: 40px;
}

.category-amount {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color);
}

/* 账户分析表格样式 */
.accounts-section {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.accounts-table-container {
  overflow-x: auto;
}

.accounts-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.accounts-table th,
.accounts-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}

.accounts-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: var(--text-color);
  position: sticky;
  top: 0;
  z-index: 10;
}

.accounts-table tbody tr:hover {
  background-color: #f8f9fa;
}

.expense-amount {
  color: var(--primary-color);
  font-weight: 500;
}

.income-amount {
  color: var(--income-color);
  font-weight: 500;
}
</style>
