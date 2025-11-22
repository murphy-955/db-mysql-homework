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

        <!-- 仪表盘卡片区域 -->
        <div class="dashboard-grid">
          <!-- 财务概览卡片 -->
          <div class="card overview-card" @click="$router.push('/statistics')">
            <div class="card-header">
              <h4>财务概览</h4>
              <span class="more-link">查看详情 ></span>
            </div>
            <div class="chart-placeholder">
              <div v-if="loading" class="chart-loading">加载中...</div>
              <div v-else class="trend-chart">
                <div class="chart-bars">
                  <div v-for="(item, index) in chartData" :key="index" class="bar-group">
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
            <div class="card-stats">
              <div class="stat-item">
                <span class="label">本月支出</span>
                <span class="value expense">¥ {{ totalExpense.toLocaleString() }}</span>
              </div>
              <div class="stat-item">
                <span class="label">本月收入</span>
                <span class="value income">¥ {{ totalIncome.toLocaleString() }}</span>
              </div>
            </div>
          </div>

          <!-- 快速记账卡片 -->
          <div class="card action-card" @click="$router.push('/bill-add')">
            <div class="icon-wrapper blue">
              <span class="big-icon">✏️</span>
            </div>
            <h4>记一笔</h4>
            <p>快速记录今天的每一笔收支</p>
            <button class="action-btn">立即记录</button>
          </div>

          <!-- 账单查询卡片 -->
          <div class="card action-card" @click="$router.push('/bill-query')">
            <div class="icon-wrapper purple">
              <span class="big-icon">🔍</span>
            </div>
            <h4>查账单</h4>
            <p>回顾历史消费，分析支出去向</p>
            <button class="action-btn secondary">查看明细</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import axios from 'axios';
import quotesData from '@/assets/quotes/quote_zh.json';
import quoteBanner from '@/assets/quotes/quote_banner.png';
import Sidebar from './Sidebar.vue';

export default {
  name: 'DashboardPage',
  components: {
    Sidebar
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
      bills: []
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
        // 如果接口失败，使用模拟数据
        this.generateMockData();
      } finally {
        this.loading = false;
      }
    },
    calculateStats() {
      let totalExpense = 0;
      let totalIncome = 0;
      const dailyData = {};

      this.bills.forEach(bill => {
        const date = bill.date.split(' ')[0];
        if (!dailyData[date]) {
          dailyData[date] = { expense: 0, income: 0 };
        }
        if (bill.type === '支出') {
          dailyData[date].expense += bill.amount;
          totalExpense += bill.amount;
        } else if (bill.type === '收入') {
          dailyData[date].income += bill.amount;
          totalIncome += bill.amount;
        }
      });

      this.totalExpense = totalExpense;
      this.totalIncome = totalIncome;

      // 取最近5天的数据
      const dates = Object.keys(dailyData).sort().slice(-5);
      this.chartData = dates.map(date => dailyData[date]);
    },
    generateMockData() {
      // 模拟数据
      this.totalExpense = 2340;
      this.totalIncome = 8500;
      this.chartData = [
        { expense: 2340, income: 8500 },
        { expense: 1800, income: 9200 },
        { expense: 2100, income: 8700 },
        { expense: 2500, income: 8900 },
        { expense: 2000, income: 9100 }
      ];
    },
    getBarHeight(value) {
      if (this.chartData.length === 0) return '0%';
      const maxValue = Math.max(...this.chartData.flatMap(item => [item.expense, item.income]));
      return `${(value / maxValue) * 100}%`;
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
  overflow: hidden; /* 防止整体滚动，让main-content滚动 */
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
  color: #ffff01; /* 棕色，适合暗黄背景 */
  text-shadow: 1px 1px 2px rgba(0,0,0,0.3); /* 添加阴影模拟水墨效果 */
}

.quote-content p {
  margin: 0;
  font-size: 28px; /* 墛大名言字体 */
  font-style: italic;
  font-weight: 500;
  line-height: 1.4;
}

.quote-text {
  font-family: 'KaiTi', '楷体', serif;
  white-space: pre-wrap;
  font-size: 22px;
  color: #fffb00; /* 棕色 */
  text-shadow: 1px 1px 4px rgba(0,0,0,0.3); /* 水墨阴影 */
}

.quote-line {
  text-align: center;
}

.author-line {
  text-align: right;
}

/* 仪表盘网格 - 纯PC布局 */
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 固定三列 */
  gap: 32px;
  width: 100%;
}

/* 移除之前的媒体查询，强制PC布局 */

/* 财务概览卡片样式 */
.overview-card {
  grid-column: span 2; /* 占据两列 */
  min-height: 400px; /* 增加高度 */
}

.chart-placeholder {
  height: 200px; /* 墛大图表高度 */
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.chart-loading {
  font-size: 16px;
  color: #999;
}

.trend-chart {
  width: 100%;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 100%;
  width: 100%;
}

.bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.expense-bar {
  width: 20px;
  background-color: #ff4d4f;
  border-radius: 4px 4px 0 0;
  margin-bottom: 4px;
}

.income-bar {
  width: 20px;
  background-color: #52c41a;
  border-radius: 4px 4px 0 0;
}

.chart-legend {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  margin-right: 20px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 6px;
}

.legend-color.expense {
  background-color: #ff4d4f;
}

.legend-color.income {
  background-color: #52c41a;
}

.card-stats {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-item .label {
  font-size: 14px;
  color: #999;
  margin-bottom: 6px;
}

.stat-item .value {
  font-size: 24px; /* 墛大数值 */
  font-weight: bold;
}

.value.expense { color: #ff4d4f; }
.value.income { color: #52c41a; }

/* 动作卡片样式 */
.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  min-height: 300px; /* 墛大高度 */
  padding: 32px 24px; /* 墛大内边距 */
}

.icon-wrapper {
  width: 80px; /* 墛大图标容器 */
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.icon-wrapper.blue { background-color: #e6f7ff; color: #1890ff; }
.icon-wrapper.purple { background-color: #f9f0ff; color: #722ed1; }

.big-icon {
  font-size: 36px; /* 墛大图标 */
}

.action-card h4 {
  margin: 0 0 12px 0;
  font-size: 20px; /* 墛大标题 */
}

.action-card p {
  color: #999;
  font-size: 16px; /* 墛大描述 */
  margin: 0 0 24px 0;
  line-height: 1.5;
}

.action-btn {
  padding: 12px 32px; /* 墛大按钮 */
  border-radius: 24px;
  border: none;
  background-color: #1890ff;
  color: white;
  cursor: pointer;
  transition: background 0.3s;
  font-size: 16px; /* 墛大字体 */
}

.action-btn:hover {
  background-color: #40a9ff;
}

.action-btn.secondary {
  background-color: #f0f2f5;
  color: #666;
}

.action-btn.secondary:hover {
  background-color: #e6e6e6;
}

/* 钱包信息样式 */
.info-card {
  min-height: 300px;
  padding: 32px 24px;
}

.wallet-item {
  display: flex;
  align-items: center;
  padding: 16px 0; /* 墛大间距 */
  border-bottom: 1px solid #f5f5f5;
}

.wallet-item:last-child {
  border-bottom: none;
}

.wallet-icon {
  font-size: 28px; /* 墛大图标 */
  margin-right: 16px;
  background: #f5f5f5;
  width: 50px; /* 墛大容器 */
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.wallet-detail {
  display: flex;
  flex-direction: column;
}

.wallet-name {
  font-size: 16px;
  color: #666;
}

.wallet-balance {
  font-size: 18px; /* 墛大余额 */
  font-weight: bold;
  color: #333;
}

/* 卡片通用样式 */
.card {
  background: white;
  border-radius: 16px; /* 墛大圆角 */
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
  transition: transform 0.3s, box-shadow 0.3s;
  border: 1px solid #f0f0f0;
  cursor: pointer;
}

.card:hover {
  transform: translateY(-6px); /* 墛大悬停效果 */
  box-shadow: 0 12px 24px rgba(0,0,0,0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h4 {
  margin: 0;
  font-size: 18px; /* 墛大标题 */
  color: #333;
}

.more-link {
  font-size: 14px;
  color: #999;
}
</style>
