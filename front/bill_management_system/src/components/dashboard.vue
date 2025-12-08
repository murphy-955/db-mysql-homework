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

        <div class="dashboard-grid">

          <div class="left-panel">

            <!-- 顶部数据卡片 -->
            <div class="stats-cards">
              <div class="card stat-item income">
                <div class="stat-label">本月收入</div>
                <div class="stat-value">¥ {{ statistics.totalIncome.toFixed(2) }}</div>
              </div>
              <div class="card stat-item expense">
                <div class="stat-label">本月支出</div>
                <div class="stat-value">¥ {{ statistics.totalExpense.toFixed(2) }}</div>
              </div>
              <div class="card stat-item balance">
                <div class="stat-label">结余</div>
                <div class="stat-value">¥ {{ (statistics.totalIncome - statistics.totalExpense).toFixed(2) }}</div>
              </div>
            </div>

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
                    <span class="bill-category">{{ bill.type }}</span>
                    <span class="bill-desc">{{ bill.remarks }}</span>
                  </div>
                  <div class="bill-amount" :class="bill.recordEnum === 'expenditure' ? 'text-expense' : 'text-income'">
                    {{ bill.recordEnum === 'expenditure' ? '-' : '+' }}{{ bill.amount }}
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

<script setup>
import quotesData from '@/assets/quotes/quote_zh.json';
import quoteBanner from '@/assets/quotes/quote_banner.png';
import Sidebar from './Sidebar.vue';
import BillAddWindow from './BillAddWindow.vue';
import Calendar from './Calendar.vue';
import { ref, onMounted, computed } from 'vue';
import { useStatistics } from '@/composables/useStatistics';

// ==================== 使用共享的统计逻辑 ====================
const { loading, statistics, allBills, loadStatistics } = useStatistics();

// ==================== Dashboard 特有的响应式数据 ====================
const quotes = quotesData.quotes;
const selectedQuote = ref({ text: "", source: "" });
const bannerUrl = quoteBanner;
const showAddModal = ref(false);
const userName = localStorage.getItem('userName') || '用户';
const currentDate = new Date().toLocaleDateString();
const selectedDate = ref(new Date());

// ==================== 计算属性 ====================
// 近5日趋势数据（从 allBills 计算）
const chartData = computed(() => {
  if (allBills.value.length === 0) return [];

  const dailyData = {};
  allBills.value.forEach(bill => {
    const date = bill.date.split(' ')[0];
    if (!dailyData[date]) {
      dailyData[date] = { expense: 0, income: 0 };
    }

    if (bill.recordEnum === 'EXPENDITURE') {
      dailyData[date].expense += bill.amount;
    } else if (bill.recordEnum === 'INCOME') {
      dailyData[date].income += bill.amount;
    }
  });

  // 取最近5天的数据
  const dates = Object.keys(dailyData).sort().slice(-5);
  return dates.map(date => ({
    date: date,
    ...dailyData[date]
  }));
});

// 最近账单（最近5笔）
const recentBills = computed(() => {
  return [...allBills.value].sort((a, b) => new Date(b.date) - new Date(a.date)).slice(0, 5);
});

// ==================== 业务逻辑方法 ====================

/**
 * 计算柱状图高度
 */
const getBarHeight = (value) => {
  if (chartData.value.length === 0) return '0%';
  const maxValue = Math.max(...chartData.value.flatMap(item => [item.expense, item.income]));
  if (maxValue === 0) return '0%';
  return `${(value / maxValue) * 100}%`;
};

/**
 * 跳转到统计页面
 */
const moveToStatistics = () => {
  window.location.href = '/statistics';
};

/**
 * 打开添加账单弹窗
 */
const openAddModal = () => {
  showAddModal.value = true;
};

/**
 * 关闭添加账单弹窗
 */
const closeAddModal = () => {
  showAddModal.value = false;
};

/**
 * 账单添加成功后的回调
 */
const handleAddSuccess = () => {
  closeAddModal();
  loadOverviewData();
};

/**
 * 日历日期选择回调
 */
const handleDateSelected = (date) => {
  selectedDate.value = date;
  console.log('选中日期:', date);
};

/**
 * 加载本月财务概览数据
 */
const loadOverviewData = () => {
  // 获取本月的日期范围
  const now = new Date();
  const startDate = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0];
  const endDate = now.toISOString().split('T')[0];

  // 调用共享的 loadStatistics 方法，传入自定义日期
  loadStatistics('month', startDate, endDate);
};

// ==================== 生命周期 ====================
onMounted(() => {
  // 随机选择一条名言
  const quote = quotes[Math.floor(Math.random() * quotes.length)];
  selectedQuote.value.text = quote.text;
  selectedQuote.value.source = quote.source;

  // 加载财务概览数据（调用共享逻辑）
  loadOverviewData();
});
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
