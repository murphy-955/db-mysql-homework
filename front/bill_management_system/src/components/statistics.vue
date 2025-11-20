<template>
  <div class="statistics-page">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="nav-left">
        <h2 class="logo">来福记账本</h2>
      </div>
      <nav class="nav-center">
        <a href="/" class="nav-link">主页</a>
        <a href="/bill-query" class="nav-link">账单查询</a>
        <a href="/bill-add" class="nav-link">添加账单</a>
        <a href="/statistics" class="nav-link active">统计分析</a>
        <a href="#" class="nav-link">计划</a>
        <a href="#" class="nav-link">帮助</a>
        <a href="#" class="nav-link">邀请</a>
        <a href="#" class="nav-link">账户</a>
      </nav>
      <div class="nav-right">
        <button class="icon-btn">🌐</button>
        <button class="icon-btn">🌙</button>
        <button class="icon-btn">👤</button>
      </div>
    </header>

    <!-- 主内容区域 -->
    <div class="container">
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
              <div class="chart-x-axis">
                <span v-for="(item, index) in trendData.labels" :key="index" class="x-axis-label">{{ item }}</span>
              </div>
              <div class="chart-bars">
                <div v-for="(item, index) in trendData.data" :key="index" class="bar-group">
                  <div class="expense-bar" :style="{ height: getBarHeight(item.expense) }" :title="`支出: ¥${item.expense}`"></div>
                  <div class="income-bar" :style="{ height: getBarHeight(item.income) }" :title="`收入: ¥${item.income}`"></div>
                </div>
              </div>
              <div class="chart-legend">
                <div class="legend-item">
                  <span class="legend-color expense"></span>
                  <span class="legend-text">支出</span>
                </div>
                <div class="legend-item">
                  <span class="legend-color income"></span>
                  <span class="legend-text">收入</span>
                </div>
              </div>
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
            <div v-else class="pie-chart-container">
              <div class="pie-chart">
                <!-- 简化的饼图表示 -->
                <div class="pie-chart-simplified">
                  <div v-for="(category, index) in expenseCategories" :key="index" 
                       class="pie-slice" 
                       :style="{ backgroundColor: category.color, transform: `rotate(${getPieSliceRotation(index)})` }" 
                       :title="`${category.name}: ¥${category.amount} (${category.percentage}%)`">
                  </div>
                </div>
              </div>
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
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'StatisticsPage',
  data() {
    return {
      dateRange: 'month',
      startDate: '',
      endDate: '',
      trendChartType: 'daily',
      loading: false,
      statistics: {
        totalExpense: 0,
        totalIncome: 0,
        balance: 0,
        expenseCount: 0,
        incomeCount: 0,
        totalCount: 0,
        expenseTrend: 0,
        incomeTrend: 0
      },
      trendData: {
        labels: [],
        data: []
      },
      expenseCategories: [],
      accountsData: []
    };
  },
  mounted() {
    this.setDefaultDateRange();
    this.loadStatistics();
  },
  methods: {
    setDefaultDateRange() {
      const today = new Date();
      const monthAgo = new Date();
      monthAgo.setMonth(today.getMonth() - 1);
      
      this.startDate = monthAgo.toISOString().split('T')[0];
      this.endDate = today.toISOString().split('T')[0];
    },
    async loadStatistics() {
      this.loading = true;
      
      try {
        // 获取日期参数
        let dateParams = {};
        if (this.dateRange === 'custom') {
          dateParams = {
            startDate: this.startDate,
            endDate: this.endDate
          };
        } else {
          dateParams = {
            dateRange: this.dateRange
          };
        }
        
        // 实际项目中应调用真实API
        // const response = await axios.get('/api/query/getReport', { params: dateParams });
        
        // 使用模拟数据
        this.generateMockData();
        this.updateTrendChart();
      } catch (error) {
        console.error('加载统计数据失败:', error);
      } finally {
        this.loading = false;
      }
    },
    generateMockData() {
      // 模拟统计概览数据
      this.statistics = {
        totalExpense: 12580.50,
        totalIncome: 28000.00,
        balance: 15419.50,
        expenseCount: 48,
        incomeCount: 5,
        totalCount: 53,
        expenseTrend: -12.5,
        incomeTrend: 5.2
      };
      
      // 模拟支出分类数据
      this.expenseCategories = [
        { name: '餐饮', amount: 3800.00, percentage: 30.2, color: '#FF6384' },
        { name: '交通', amount: 1200.00, percentage: 9.5, color: '#36A2EB' },
        { name: '购物', amount: 2500.00, percentage: 19.9, color: '#FFCE56' },
        { name: '娱乐', amount: 1800.00, percentage: 14.3, color: '#4BC0C0' },
        { name: '医疗', amount: 850.50, percentage: 6.8, color: '#9966FF' },
        { name: '教育', amount: 1200.00, percentage: 9.5, color: '#FF9F40' },
        { name: '居住', amount: 800.00, percentage: 6.4, color: '#8AC926' },
        { name: '其他', amount: 430.00, percentage: 3.4, color: '#6C757D' }
      ];
      
      // 模拟账户数据
      this.accountsData = [
        { name: '微信', totalExpense: 4500.00, expenseCount: 20, totalIncome: 5000.00, incomeCount: 2, totalCount: 22 },
        { name: '支付宝', totalExpense: 5200.00, expenseCount: 18, totalIncome: 15000.00, incomeCount: 2, totalCount: 20 },
        { name: '银行卡', totalExpense: 2000.00, expenseCount: 6, totalIncome: 8000.00, incomeCount: 1, totalCount: 7 },
        { name: '现金', totalExpense: 880.50, expenseCount: 4, totalIncome: 0, incomeCount: 0, totalCount: 4 }
      ];
    },
    updateTrendChart() {
      // 生成模拟趋势数据
      const labels = [];
      const data = [];
      const today = new Date();
      
      if (this.trendChartType === 'daily') {
        // 最近7天
        for (let i = 6; i >= 0; i--) {
          const date = new Date(today);
          date.setDate(today.getDate() - i);
          labels.push(`${date.getMonth() + 1}/${date.getDate()}`);
          data.push({
            expense: Math.floor(Math.random() * 1000) + 800,
            income: i === 0 ? 8000 : 0 // 模拟每月1号发工资
          });
        }
      } else if (this.trendChartType === 'weekly') {
        // 最近4周
        for (let i = 3; i >= 0; i--) {
          labels.push(`第${4 - i}周`);
          data.push({
            expense: Math.floor(Math.random() * 2000) + 3000,
            income: i === 0 ? 8000 : 0
          });
        }
      } else if (this.trendChartType === 'monthly') {
        // 最近6个月
        for (let i = 5; i >= 0; i--) {
          const date = new Date(today);
          date.setMonth(today.getMonth() - i);
          labels.push(`${date.getMonth() + 1}月`);
          data.push({
            expense: Math.floor(Math.random() * 3000) + 10000,
            income: 28000
          });
        }
      }
      
      this.trendData = { labels, data };
    },
    getBarHeight(value) {
      const maxValue = Math.max(...this.trendData.data.map(item => Math.max(item.expense, item.income)));
      return `${(value / maxValue) * 100}%`;
    },
    getPieSliceRotation(index) {
      let rotation = 0;
      for (let i = 0; i < index; i++) {
        rotation += this.expenseCategories[i].percentage * 3.6; // 360度 / 100%
      }
      return `${rotation}deg`;
    },
    async exportReport() {
      try {
        // 实际项目中应调用真实API
        // const response = await axios.post('/api/query/exportReport', {
        //   dateRange: this.dateRange,
        //   startDate: this.startDate,
        //   endDate: this.endDate
        // }, {
        //   responseType: 'blob'
        // });
        
        alert('报表导出功能已触发（模拟）');
      } catch (error) {
        console.error('导出报表失败:', error);
        alert('导出失败，请重试');
      }
    }
  }
};
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  --bgcolor: rgba(0, 81, 255, 0.47);
  --primary-color: #d9534f;
  --income-color: #5cb85c;
  --balance-positive: #5cb85c;
  --balance-negative: #d9534f;
  --border-color: #ddd;
  --text-color: #333;
  --text-secondary: #666;
}

html, body {
  height: 100%;
  overflow: hidden;
}

.statistics-page {
  min-height: 100vh;
  height: 100vh;
  background-color: var(--bgcolor);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  display: flex;
  flex-direction: column;
}

/* 导航栏样式 */
.navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  background-color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 40px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.nav-left .logo {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.nav-center {
  display: flex;
  gap: 30px;
}

.nav-link {
  text-decoration: none;
  color: #666;
  font-size: 15px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s;
}

.nav-link.active {
  color: var(--primary-color);
  font-weight: 500;
}

.nav-link:hover {
  background-color: #f5f5f5;
}

.nav-right {
  display: flex;
  gap: 10px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border: none;
  background-color: #f5f5f5;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  transition: background-color 0.3s;
}

.icon-btn:hover {
  background-color: #e5e5e5;
}

/* 容器样式 */
.container {
  flex: 1;
  max-width: 100%;
  width: 100%;
  margin: 0;
  padding: 30px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
  min-height: 0; /* 允许flex子元素收缩 */
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
  gap: 20px;
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
  background: conic-gradient(
    #FF6384 0% 30.2%,
    #36A2EB 30.2% 39.7%,
    #FFCE56 39.7% 59.6%,
    #4BC0C0 59.6% 73.9%,
    #9966FF 73.9% 80.7%,
    #FF9F40 80.7% 90.2%,
    #8AC926 90.2% 96.6%,
    #6C757D 96.6% 100%
  );
  position: relative;
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