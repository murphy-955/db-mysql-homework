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
              <div v-else-if="expenseCategories.length === 0" class="chart-empty">暂无支出数据</div>
              <div v-else class="pie-chart-container">
                <div class="pie-chart">
                  <!-- 动态生成饼图 -->
                  <div class="pie-chart-simplified" :style="{ background: getPieChartGradient() }">
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
    </main>
  </div>
</template>

<script>
import axios from 'axios';
import Sidebar from './Sidebar.vue';

export default {
  name: 'StatisticsPage',
  components: {
    Sidebar
  },
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
      accountsData: [],
      bills: []
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
        const token = localStorage.getItem('token');
        const currentRequestBody = {
          token: token,
          startDate: this.getDateRangeStart(),
          endDate: this.getDateRangeEnd(),
          page: 1,
          limit: 10000 // 增加limit以获取所有数据
        };

        console.log('请求当前期统计数据:', currentRequestBody); // 调试日志
        const currentResponse = await axios.post('http://localhost:8080/api/query/getBillList?searchType=DATE_RANGE', currentRequestBody);
        console.log('接收到的当前期数据:', currentResponse.data); // 调试日志

        this.bills = currentResponse.data.data || [];
        console.log('当前期账单数量:', this.bills.length); // 调试日志

        // 获取上期数据用于计算趋势
        const previousRequestBody = {
          token: token,
          startDate: this.getPreviousPeriodStart(),
          endDate: this.getPreviousPeriodEnd(),
          page: 1,
          limit: 10000
        };

        console.log('请求上期统计数据:', previousRequestBody); // 调试日志
        const previousResponse = await axios.post('http://localhost:8080/api/query/getBillList?searchType=DATE_RANGE', previousRequestBody);
        console.log('接收到的上期数据:', previousResponse.data); // 调试日志

        const previousBills = previousResponse.data.data || [];
        console.log('上期账单数量:', previousBills.length); // 调试日志

        this.calculateStatistics(previousBills);
        this.updateTrendChart();
      } catch (error) {
        console.error('加载统计数据失败:', error);
        this.bills = [];
        this.calculateStatistics([]);
        this.updateTrendChart();
      } finally {
        this.loading = false;
      }
    },
    getDateRangeStart() {
      const today = new Date();
      switch (this.dateRange) {
        case 'week':
          return new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
        case 'month':
          return new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]; // 修复：最近一个月应该是30天前
        case 'quarter':
          return new Date(today.getTime() - 90 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]; // 修复：最近三个月应该是90天前
        case 'halfYear':
          return new Date(today.getTime() - 180 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]; // 修复：最近半年应该是180天前
        case 'year':
          return new Date(today.getTime() - 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]; // 修复：最近一年应该是365天前
        case 'custom':
          return this.startDate;
        default:
          return new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0];
      }
    },
    getDateRangeEnd() {
      const today = new Date();
      switch (this.dateRange) {
        case 'custom':
          return this.endDate;
        default:
          return today.toISOString().split('T')[0];
      }
    },
    getPreviousPeriodStart() {
      const today = new Date();
      const periodLength = this.getPeriodLengthInDays();
      const endDate = new Date(this.getDateRangeStart());
      endDate.setDate(endDate.getDate() - 1); // 上期结束日期为当前期开始前一天
      const startDate = new Date(endDate);
      startDate.setDate(endDate.getDate() - periodLength + 1);
      return startDate.toISOString().split('T')[0];
    },
    getPreviousPeriodEnd() {
      const startDate = new Date(this.getDateRangeStart());
      startDate.setDate(startDate.getDate() - 1);
      return startDate.toISOString().split('T')[0];
    },
    getPeriodLengthInDays() {
      switch (this.dateRange) {
        case 'week':
          return 7;
        case 'month':
          return 30;
        case 'quarter':
          return 90;
        case 'halfYear':
          return 180;
        case 'year':
          return 365;
        case 'custom':
          // 对于自定义，计算天数差
          const start = new Date(this.startDate);
          const end = new Date(this.endDate);
          return Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
        default:
          return 30;
      }
    },
    calculateStatistics(previousBills = []) {
      let totalExpense = 0;
      let totalIncome = 0;
      let expenseCount = 0;
      let incomeCount = 0;
      const categoryData = {};
      const accountData = {};

      this.bills.forEach(bill => {
        if (bill.type === '支出') {
          totalExpense += bill.amount;
          expenseCount++;
          if (!categoryData[bill.category]) categoryData[bill.category] = 0;
          categoryData[bill.category] += bill.amount;
        } else if (bill.type === '收入') {
          totalIncome += bill.amount;
          incomeCount++;
        }

        if (!accountData[bill.account]) {
          accountData[bill.account] = { totalExpense: 0, expenseCount: 0, totalIncome: 0, incomeCount: 0, totalCount: 0 };
        }
        accountData[bill.account].totalCount++;
        if (bill.type === '支出') {
          accountData[bill.account].totalExpense += bill.amount;
          accountData[bill.account].expenseCount++;
        } else if (bill.type === '收入') {
          accountData[bill.account].totalIncome += bill.amount;
          accountData[bill.account].incomeCount++;
        }
      });

      // 计算上期统计
      let previousExpense = 0;
      let previousIncome = 0;
      previousBills.forEach(bill => {
        if (bill.type === '支出') {
          previousExpense += bill.amount;
        } else if (bill.type === '收入') {
          previousIncome += bill.amount;
        }
      });

      // 计算趋势
      const expenseTrend = previousExpense > 0 ? ((totalExpense - previousExpense) / previousExpense * 100) : 0;
      const incomeTrend = previousIncome > 0 ? ((totalIncome - previousIncome) / previousIncome * 100) : 0;

      this.statistics = {
        totalExpense,
        totalIncome,
        balance: totalIncome - totalExpense,
        expenseCount,
        incomeCount,
        totalCount: expenseCount + incomeCount,
        expenseTrend: parseFloat(expenseTrend.toFixed(1)),
        incomeTrend: parseFloat(incomeTrend.toFixed(1))
      };

      // 计算支出分类
      const totalExpenseAmount = totalExpense;
      const colors = ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40', '#8AC926', '#6C757D'];
      let colorIndex = 0;
      this.expenseCategories = Object.keys(categoryData).map(category => ({
        name: category,
        amount: categoryData[category],
        percentage: totalExpenseAmount > 0 ? (categoryData[category] / totalExpenseAmount * 100).toFixed(1) : 0,
        color: colors[colorIndex++ % colors.length]
      })).sort((a, b) => b.amount - a.amount);

      // 计算账户数据
      this.accountsData = Object.keys(accountData).map(account => ({
        name: account,
        ...accountData[account]
      }));
    },
    updateTrendChart() {
      const labels = [];
      const data = [];
      const today = new Date();

      // 根据选择的日期范围确定显示的天数
      let daysToShow = 7; // 默认7天
      if (this.dateRange === 'month') {
        daysToShow = 30; // 一个月显示30天
      } else if (this.dateRange === 'quarter') {
        daysToShow = 90; // 三个月显示90天
      } else if (this.dateRange === 'halfYear') {
        daysToShow = 180; // 半年显示180天
      } else if (this.dateRange === 'year') {
        daysToShow = 365; // 一年显示365天
      }

      if (this.trendChartType === 'daily') {
        // 显示最近 daysToShow 天的每日数据
        for (let i = daysToShow - 1; i >= 0; i--) {
          const date = new Date(today);
          date.setDate(today.getDate() - i);
          const dateStr = date.toISOString().split('T')[0];
          labels.push(`${date.getMonth() + 1}/${date.getDate()}`);

          const dayExpense = this.bills.filter(bill => bill.date.startsWith(dateStr) && bill.type === '支出').reduce((sum, bill) => sum + bill.amount, 0);
          const dayIncome = this.bills.filter(bill => bill.date.startsWith(dateStr) && bill.type === '收入').reduce((sum, bill) => sum + bill.amount, 0);

          data.push({
            expense: dayExpense,
            income: dayIncome
          });
        }
      } else if (this.trendChartType === 'weekly') {
        // 最近4周
        for (let i = 3; i >= 0; i--) {
          const weekStart = new Date(today);
          weekStart.setDate(today.getDate() - (i * 7 + 6));
          const weekEnd = new Date(today);
          weekEnd.setDate(today.getDate() - (i * 7));

          labels.push(`第${4 - i}周`);

          const weekExpense = this.bills.filter(bill => {
            const billDate = new Date(bill.date);
            return billDate >= weekStart && billDate <= weekEnd && bill.type === '支出';
          }).reduce((sum, bill) => sum + bill.amount, 0);

          const weekIncome = this.bills.filter(bill => {
            const billDate = new Date(bill.date);
            return billDate >= weekStart && billDate <= weekEnd && bill.type === '收入';
          }).reduce((sum, bill) => sum + bill.amount, 0);

          data.push({
            expense: weekExpense,
            income: weekIncome
          });
        }
      } else if (this.trendChartType === 'monthly') {
        // 最近6个月
        for (let i = 5; i >= 0; i--) {
          const monthStart = new Date(today.getFullYear(), today.getMonth() - i, 1);
          const monthEnd = new Date(today.getFullYear(), today.getMonth() - i + 1, 0);

          labels.push(`${monthStart.getMonth() + 1}月`);

          const monthExpense = this.bills.filter(bill => {
            const billDate = new Date(bill.date);
            return billDate >= monthStart && billDate <= monthEnd && bill.type === '支出';
          }).reduce((sum, bill) => sum + bill.amount, 0);

          const monthIncome = this.bills.filter(bill => {
            const billDate = new Date(bill.date);
            return billDate >= monthStart && billDate <= monthEnd && bill.type === '收入';
          }).reduce((sum, bill) => sum + bill.amount, 0);

          data.push({
            expense: monthExpense,
            income: monthIncome
          });
        }
      }

      this.trendData = { labels, data };
    },
    getBarHeight(value) {
      const maxValue = Math.max(...this.trendData.data.map(item => Math.max(item.expense, item.income)), 1); // 防止除以0
      if (maxValue === 0) return '0%';
      return `${(value / maxValue) * 100}%`;
    },
    getPieChartGradient() {
      if (this.expenseCategories.length === 0) return 'transparent';

      let gradientParts = [];
      let currentPercentage = 0;

      this.expenseCategories.forEach((category, index) => {
        const startPercent = currentPercentage;
        currentPercentage += parseFloat(category.percentage);
        const endPercent = currentPercentage;

        gradientParts.push(`${category.color} ${startPercent}% ${endPercent}%`);
      });

      return `conic-gradient(${gradientParts.join(', ')})`;
    },
    getPieSliceRotation(index) {
      // 这个方法现在不需要了，但保留以防万一
      let rotation = 0;
      for (let i = 0; i < index; i++) {
        rotation += parseFloat(this.expenseCategories[i].percentage) * 3.6;
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
