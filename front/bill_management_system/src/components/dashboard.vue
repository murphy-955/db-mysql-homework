<template>
  <div class="dashboard-layout">
    <!-- 左侧侧边栏 -->
    <Sidebar />

    <!-- 右侧主内容区 -->
    <main class="main-content">
      <div class="content-wrapper">
        <!-- 顶部名言金句区域 -->
        <div class="quotes-banner" :style="{ backgroundImage: `url(${bannerUrl})` }">
          <div class="quote-content">
            <h3>一粥一饭，当思来处不易</h3>
            <div class="quote-text">
              <div class="quote-line">{{ selectedQuote.text }} —— {{ selectedQuote.source }}</div>
            </div>
          </div>
        </div>

        <!-- 新增：7:3 分栏布局区域 -->
        <div class="dashboard-grid">

          <!-- 左侧：财务概览 (70%) -->
          <div class="left-panel">

            <!-- 1. 顶部数据卡片 -->
            <div class="stats-cards">
              <div class="card stat-item income">
                <div class="stat-label">本月收入</div>
                <div class="stat-value">¥ {{ totalIncome.toFixed(2) }}</div>
              </div>
              <div class="card stat-item expense">
                <div class="stat-label">本月支出</div>
                <div class="stat-value">¥ {{ totalExpense.toFixed(2) }}</div>
              </div>
              <div class="card stat-item balance">
                <div class="stat-label">结余</div>
                <div class="stat-value">¥ {{ (totalIncome - totalExpense).toFixed(2) }}</div>
              </div>
            </div>

            <!-- 2. 趋势图表 (使用 CSS 模拟简单的柱状图) -->
            <div class="card chart-section">
              <div class="card-header">
                <h4>近5日收支趋势</h4>
              </div>
              <div class="chart-container">
                <div v-if="chartData.length === 0" class="no-data">暂无数据</div>
                <div v-else class="bars-wrapper">
                  <div v-for="(item, index) in chartData" :key="index" class="bar-group">
                    <div class="bar-column">
                      <div class="bar income-bar" :style="{ height: getBarHeight(item.income) }" :title="'收入: ' + item.income"></div>
                      <div class="bar expense-bar" :style="{ height: getBarHeight(item.expense) }" :title="'支出: ' + item.expense"></div>
                    </div>
                    <div class="bar-date">{{ item.date.slice(5) }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 3. 最近账单列表 -->
            <div class="card recent-bills">
              <div class="card-header">
                <h4>最近账单</h4>
              </div>
              <ul class="bill-list">
                <li v-for="bill in recentBills" :key="bill.id" class="bill-item">
                  <div class="bill-info">
                    <span class="bill-category">{{ bill.category }}</span>
                    <span class="bill-desc">{{ bill.description }}</span>
                  </div>
                  <div class="bill-amount" :class="bill.type === '支出' ? 'text-expense' : 'text-income'">
                    {{ bill.type === '支出' ? '-' : '+' }}{{ bill.amount }}
                  </div>
                </li>
              </ul>
            </div>
          </div>

          <!-- 右侧：工具与日历 (30%) -->
          <div class="right-panel">

            <!-- 1. 日历/日期卡片 -->
            <div class="card calendar-section">
              <h4 class="calendar-subtitle">欢迎回来！今天是 {{ currentDate }}</h4>

              <Calendar @date-selected="handleDateSelected" :initialDate="selectedDate" />

              <button class="btn-bills-overview" @click="moveToStatistics">点击此查看历史账单情况</button>
            </div>

          </div>
        </div>
      </div>
    </main>

    <!-- 添加账单弹窗 -->
    <BillAddWindow v-if="showAddModal" @success="handleAddSuccess" @cancel="closeAddModal" />
  </div>
</template>

<script>
import axios from 'axios';
import quotesData from '@/assets/quotes/quote_zh.json';
import quoteBanner from '@/assets/quotes/quote_banner.png';
import Sidebar from './Sidebar.vue';
import BillAddWindow from './BillAddWindow.vue';
import Calendar from './Calendar.vue';

export default {
  name: 'DashboardPage',
  components: {
    Sidebar,
    BillAddWindow,
    Calendar
  },
  data() {
    return {
      quotes: quotesData.quotes,
      currentQuote: "",
      selectedQuote: {
        text: "",
        source: ""
      },
      bannerUrl: quoteBanner,
      chartData: [],
      totalExpense: 0,
      totalIncome: 0,
      loading: false,
      bills: [],
      showAddModal: false,
      userName: localStorage.getItem('userName') || '用户',
      currentDate: new Date().toLocaleDateString(),
      categoryStats: [],
      yearlyExpense: 0,
      yearlyIncome: 0,
      yearlyChartData: [],
      recentBills: [],
      yearlyBills: [],
      selectedDate: new Date()
    };
  },
  mounted() {
    // 随机选择一条名言
    const quote = this.quotes[Math.floor(Math.random() * this.quotes.length)];
    const text = quote.text;
    const periodIndex = text.lastIndexOf('。');
    if (periodIndex !== -1) {
      const spaces = ' '.repeat(periodIndex);
      this.currentQuote = text + '\n' + spaces + '—— ' + quote.source;
    } else {
      this.currentQuote = text + '\n—— ' + quote.source;
    }
    this.selectedQuote.text = quote.text;
    this.selectedQuote.source = quote.source;

    // 加载财务概览数据
    this.loadOverviewData();
    this.loadYearlyData();
  },
  methods: {
    async loadOverviewData() {
      this.loading = true;

      try {
        const token = localStorage.getItem('token');
        const requestBody = {
          token: token,
          searchType: 'DATE',
          startDate: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0],
          endDate: new Date().toISOString().split('T')[0],
          page: 1,
          limit: 100
        };

        const response = await axios.post('http://localhost:8080/api/query/getBillList', requestBody);
        this.bills = response.data.data || [];
        this.calculateStats();
      } catch (error) {
        console.error('加载账单数据失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async loadYearlyData() {
      try {
        const token = localStorage.getItem('token');
        const requestBody = {
          token: token,
          searchType: 'DATE',
          startDate: new Date(new Date().getFullYear(), 0, 1).toISOString().split('T')[0],
          endDate: new Date().toISOString().split('T')[0],
          page: 1,
          limit: 1000 // 假设足够全年数据
        };

        const response = await axios.post('http://localhost:8080/api/query/getBillList', requestBody);
        this.yearlyBills = response.data.data || [];
        this.calculateYearlyStats();
      } catch (error) {
        console.error('加载年度账单数据失败:', error);
      }
    },
    moveToStatistics() {
      this.$router.push({ name: 'statistics' });
    },
    calculateYearlyStats() {
      let totalExpense = 0;
      let totalIncome = 0;
      const monthlyData = {};

      this.yearlyBills.forEach(bill => {
        const month = new Date(bill.date).getMonth();
        if (!monthlyData[month]) {
          monthlyData[month] = { expense: 0, income: 0 };
        }
        if (bill.type === '支出') {
          monthlyData[month].expense += bill.amount;
          totalExpense += bill.amount;
        } else if (bill.type === '收入') {
          monthlyData[month].income += bill.amount;
          totalIncome += bill.amount;
        }
      });

      this.yearlyExpense = totalExpense;
      this.yearlyIncome = totalIncome;

      this.yearlyChartData = Array.from({ length: 12 }, (_, i) => monthlyData[i] || { expense: 0, income: 0 });
    },
    calculateStats() {
      let totalExpense = 0;
      let totalIncome = 0;
      const dailyData = {};
      let categoryMap = {};

      this.bills.forEach(bill => {
        const date = bill.date.split(' ')[0];
        if (!dailyData[date]) {
          dailyData[date] = { expense: 0, income: 0 };
        }
        if (bill.type === '支出') {
          dailyData[date].expense += bill.amount;
          totalExpense += bill.amount;

          const category = bill.category || '其他';
          if (!categoryMap[category]) categoryMap[category] = 0;
          categoryMap[category] += bill.amount;
        } else if (bill.type === '收入') {
          dailyData[date].income += bill.amount;
          totalIncome += bill.amount;
        }
      });

      this.totalExpense = totalExpense;
      this.totalIncome = totalIncome;

      // 取最近5天的数据
      const dates = Object.keys(dailyData).sort().slice(-5);
      this.chartData = dates.map(date => ({
        date: date,
        ...dailyData[date]
      }));

      // 计算类别统计
      this.categoryStats = Object.entries(categoryMap).map(([category, amount]) => ({
        category,
        amount,
        percentage: this.totalExpense > 0 ? (amount / this.totalExpense * 100).toFixed(1) : 0
      })).sort((a, b) => b.amount - a.amount);

      // 计算最近账单
      this.recentBills = this.bills.sort((a, b) => new Date(b.date) - new Date(a.date)).slice(0, 5);
    },
    getBarHeight(value) {
      if (this.chartData.length === 0) return '0%';
      const maxValue = Math.max(...this.chartData.flatMap(item => [item.expense, item.income]));
      if (maxValue === 0) return '0%';
      return `${(value / maxValue) * 100}%`;
    },
    getYearlyBarHeight(value) {
      if (this.yearlyChartData.length === 0) return '0%';
      const maxValue = Math.max(...this.yearlyChartData.flatMap(item => [item.expense, item.income]));
      return `${(value / maxValue) * 100}%`;
    },
    openAddModal() {
      this.showAddModal = true;
    },
    closeAddModal() {
      this.showAddModal = false;
    },
    handleAddSuccess() {
      this.closeAddModal();
      // 刷新财务概览数据
      this.loadOverviewData();
    }
  }
};
</script>

<style scoped>
.dashboard-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  /* 确保占满全屏 */
  background-color: #f0f2f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  overflow: hidden;
  /* 防止整体滚动，让main-content滚动 */
}

/* 右侧主内容区 */
.main-content {
  flex: 1;
  height: 100%;
  overflow-y: auto;
  /* 内容区滚动 */
  padding: 40px;
  background-color: #f0f2f5;
}

.content-wrapper {
  max-width: 1600px;
  /* 放宽最大宽度 */
  margin: 0 auto;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 名言横幅 */
.quotes-banner {
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  border-radius: 12px;
  padding: 30px;
  color: black;
  margin-bottom: 40px;
  box-shadow: 0 4px 12px rgba(118, 75, 162, 0.2);
  width: 100%;
}

.quote-content h3 {
  font-family: 'KaiTi', '楷体', serif;
  font-weight: bold;
  margin: 0 0 12px 0;
  font-size: 28px;
  opacity: 0.9;
  color: #ffff01;
  /* 棕色，适合暗黄背景 */
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
  /* 添加阴影模拟水墨效果 */
}

.quote-content p {
  margin: 0;
  font-size: 28px;
  /* 墛大名言字体 */
  font-style: italic;
  font-weight: 500;
  line-height: 1.4;
}

.quote-text {
  font-family: 'KaiTi', '楷体', serif;
  white-space: pre-wrap;
  font-size: 22px;
  color: #fffb00;
  /* 棕色 */
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.3);
  /* 水墨阴影 */
}

.quote-line {
  text-align: center;
}

.author-line {
  text-align: right;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 7fr 3fr;
  gap: 20px;
}

.left-panel, .right-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.calendar-subtitle {
  font-size: 16px;
  margin-bottom: 10px;
  color: #555;
  margin-top: -5px;
  margin-bottom: 20px;
}

.overview-card {
  text-align: center;
  margin-top: 20px;
  margin-bottom: 20px;
}

.btn-bills-overview {
  margin-top: 15px;
  background: linear-gradient(45deg, #2196f3, #21cbf3);
  border: none;
  color: #ffffff;
  padding:10px 20px;
  text-decoration: underline;
  cursor: pointer;
  margin-top: 30px;
  font-size: 14px;
  transition: background 0.3s ease;
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 60px;
}

.stats-cards {
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-label {
  font-size: 20px;
  color: #888;
}

.stat-value {
  font-size: 40px;
  font-weight: bold;
}

.chart-section {
  display: flex;
  flex-direction: column;
}

.chart-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 200px;
  margin-top: 40px;
}

.bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.bar-column {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  height: 100%;
  width: 20px;
}

.bar {
  width: 100%;
  border-radius: 4px;
}

.income-bar {
  background-color: #4caf50;
}

.expense-bar {
  background-color: #f44336;
}

.bar-date {
  margin-top: 5px;
  font-size: 12px;
  color: #888;
}

.no-data {
  text-align: center;
  color: #888;
}

.bill-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.bill-item {
  display: flex;
  justify-content: space-between;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.bill-info {
  display: flex;
  flex-direction: column;
}

.bill-category {
  font-weight: bold;
}

.bill-desc {
  font-size: 12px;
  color: #888;
}

.bill-amount {
  font-weight: bold;
}

.text-expense {
  color: #f44336;
}

.text-income {
  color: #4caf50;
}

.calendar-card {
  text-align: center;
}

.calendar-header {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}

.calendar-body {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  font-size: 24px;
}

.today-date {
  font-size: 36px;
  font-weight: bold;
}

.today-day {
  font-size: 18px;
  color: #888;
}

.current-full-date {
  margin-top: 10px;
  font-size: 14px;
  color: #888;
}

.action-card {
  text-align: center;
}

.btn-add-bill {
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 10px 20px;
  font-size: 16px;
  cursor: pointer;
}

.btn-add-bill .icon {
  font-size: 20px;
  margin-right: 5px;
}

.yearly-summary {
  font-size: 14px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}
</style>
