<template>
  <div class="dashboard-layout">
    <!-- 左侧侧边栏 -->
    <Sidebar />

    <!-- 右侧主内容区 -->
    <main class="main-content">
      <div class="content-wrapper">
        <!--
        测试用：生成假数据按钮（临时）
        <div class="test-controls">
          <button class="btn-generate" @click="generateMockDashboardData">生成假数据（仅测试）</button>
        </div>
        -->
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
                <h4>近一个月收支趋势</h4>
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
                  <div class="bill-amount" :class="bill.recordEnum === 'EXPENDITURE' ? 'text-expense' : 'text-income'">
                    {{ bill.recordEnum === 'EXPENDITURE' ? '-' : '+' }}{{ bill.amount }}
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

// ==================== 假数据对象 ====================
const mockData = {
  "data": [
    { "id": "7584710", "recordEnum": "EXPENDITURE", "amount": 1774.58, "date": "2025-12-19" },
    { "id": "7582984", "recordEnum": "EXPENDITURE", "amount": 2502.37, "date": "2025-12-19" },
    { "id": "7580002", "recordEnum": "EXPENDITURE", "amount": 2046.76, "date": "2025-12-19" },
    { "id": "7571061", "recordEnum": "EXPENDITURE", "amount": 2100.85, "date": "2025-12-19" },
    { "id": "7545383", "recordEnum": "EXPENDITURE", "amount": 9532.53, "date": "2025-12-19" },
    { "id": "7517033", "recordEnum": "INCOME", "amount": 30958.50, "date": "2025-12-19" },
    { "id": "7417915", "recordEnum": "EXPENDITURE", "amount": 886.64, "date": "2025-12-19" },
    { "id": "7340218", "recordEnum": "EXPENDITURE", "amount": 6339.41, "date": "2025-12-19" },
    { "id": "7300732", "recordEnum": "EXPENDITURE", "amount": 3773.36, "date": "2025-12-19" },
    { "id": "7291540", "recordEnum": "INCOME", "amount": 35033.08, "date": "2025-12-19" },
    { "id": "7261398", "recordEnum": "INCOME", "amount": 16326.13, "date": "2025-12-19" },
    { "id": "7248998", "recordEnum": "INCOME", "amount": 8007.79, "date": "2025-12-19" },
    { "id": "7244685", "recordEnum": "INCOME", "amount": 49508.95, "date": "2025-12-19" },
    { "id": "7244290", "recordEnum": "INCOME", "amount": 39875.87, "date": "2025-12-19" },
    { "id": "7211091", "recordEnum": "INCOME", "amount": 49962.18, "date": "2025-12-19" },
    { "id": "7200758", "recordEnum": "EXPENDITURE", "amount": 6846.57, "date": "2025-12-19" },
    { "id": "7200255", "recordEnum": "EXPENDITURE", "amount": 1271.87, "date": "2025-12-19" },
    { "id": "7197581", "recordEnum": "INCOME", "amount": 38640.31, "date": "2025-12-19" },
    { "id": "7187978", "recordEnum": "INCOME", "amount": 34285.82, "date": "2025-12-19" },
    { "id": "7140163", "recordEnum": "EXPENDITURE", "amount": 8301.00, "date": "2025-12-19" },
    { "id": "7125911", "recordEnum": "EXPENDITURE", "amount": 2479.97, "date": "2025-12-19" },
    { "id": "7093824", "recordEnum": "EXPENDITURE", "amount": 3719.62, "date": "2025-12-19" },
    { "id": "7081707", "recordEnum": "INCOME", "amount": 14239.78, "date": "2025-12-19" },
    { "id": "7070099", "recordEnum": "EXPENDITURE", "amount": 2334.78, "date": "2025-12-19" },
    { "id": "7042857", "recordEnum": "EXPENDITURE", "amount": 8769.87, "date": "2025-12-19" },
    { "id": "7021601", "recordEnum": "EXPENDITURE", "amount": 6009.26, "date": "2025-12-19" },
    { "id": "7007766", "recordEnum": "INCOME", "amount": 4133.36, "date": "2025-12-19" },
    { "id": "6907835", "recordEnum": "INCOME", "amount": 34234.16, "date": "2025-12-19" },
    { "id": "6881568", "recordEnum": "INCOME", "amount": 17533.36, "date": "2025-12-19" },
    { "id": "6859702", "recordEnum": "EXPENDITURE", "amount": 6539.62, "date": "2025-12-19" },
    { "id": "6791222", "recordEnum": "EXPENDITURE", "amount": 2965.20, "date": "2025-12-19" },
    { "id": "6749399", "recordEnum": "INCOME", "amount": 42277.22, "date": "2025-12-19" },
    { "id": "6723661", "recordEnum": "EXPENDITURE", "amount": 2962.48, "date": "2025-12-19" },
    { "id": "6704977", "recordEnum": "EXPENDITURE", "amount": 1179.23, "date": "2025-12-19" },
    { "id": "6682923", "recordEnum": "INCOME", "amount": 2823.67, "date": "2025-12-19" },
    { "id": "6680269", "recordEnum": "EXPENDITURE", "amount": 5703.21, "date": "2025-12-19" },
    { "id": "6674477", "recordEnum": "EXPENDITURE", "amount": 8072.03, "date": "2025-12-19" },
    { "id": "6636761", "recordEnum": "EXPENDITURE", "amount": 5495.47, "date": "2025-12-19" },
    { "id": "6632424", "recordEnum": "INCOME", "amount": 22318.32, "date": "2025-12-19" },
    { "id": "6598181", "recordEnum": "INCOME", "amount": 18834.15, "date": "2025-12-19" },
    { "id": "6552751", "recordEnum": "INCOME", "amount": 44783.79, "date": "2025-12-19" },
    { "id": "6550381", "recordEnum": "EXPENDITURE", "amount": 3452.22, "date": "2025-12-19" },
    { "id": "6549569", "recordEnum": "EXPENDITURE", "amount": 9743.64, "date": "2025-12-19" },
    { "id": "6538094", "recordEnum": "EXPENDITURE", "amount": 7299.47, "date": "2025-12-19" },
    { "id": "6456327", "recordEnum": "EXPENDITURE", "amount": 2463.05, "date": "2025-12-19" },
    { "id": "6448557", "recordEnum": "INCOME", "amount": 29446.83, "date": "2025-12-19" },
    { "id": "6415629", "recordEnum": "INCOME", "amount": 6754.92, "date": "2025-12-19" },
    { "id": "6332575", "recordEnum": "INCOME", "amount": 41148.52, "date": "2025-12-19" },
    { "id": "6291842", "recordEnum": "INCOME", "amount": 49819.61, "date": "2025-12-19" },
    { "id": "6238708", "recordEnum": "EXPENDITURE", "amount": 894.58, "date": "2025-12-19" },
    { "id": "6152372", "recordEnum": "INCOME", "amount": 49487.00, "date": "2025-12-19" },
    { "id": "6147573", "recordEnum": "INCOME", "amount": 46910.03, "date": "2025-12-19" },
    { "id": "6146485", "recordEnum": "INCOME", "amount": 23026.88, "date": "2025-12-19" },
    { "id": "6128775", "recordEnum": "EXPENDITURE", "amount": 9056.39, "date": "2025-12-19" },
    { "id": "6095420", "recordEnum": "INCOME", "amount": 37158.77, "date": "2025-12-19" },
    { "id": "6060918", "recordEnum": "EXPENDITURE", "amount": 4609.87, "date": "2025-12-19" },
    { "id": "6015221", "recordEnum": "INCOME", "amount": 19949.32, "date": "2025-12-19" },
    { "id": "6005016", "recordEnum": "INCOME", "amount": 26409.36, "date": "2025-12-19" },
    { "id": "5932318", "recordEnum": "EXPENDITURE", "amount": 5410.21, "date": "2025-12-19" },
    { "id": "5853149", "recordEnum": "INCOME", "amount": 10547.24, "date": "2025-12-19" },
    { "id": "5780323", "recordEnum": "INCOME", "amount": 4343.49, "date": "2025-12-19" },
    { "id": "5779334", "recordEnum": "INCOME", "amount": 39274.05, "date": "2025-12-19" },
    { "id": "5728097", "recordEnum": "INCOME", "amount": 14072.09, "date": "2025-12-19" },
    { "id": "5644455", "recordEnum": "EXPENDITURE", "amount": 9275.10, "date": "2025-12-19" },
    { "id": "5630010", "recordEnum": "EXPENDITURE", "amount": 7087.36, "date": "2025-12-19" },
    { "id": "5626541", "recordEnum": "INCOME", "amount": 48255.93, "date": "2025-12-19" },
    { "id": "5609189", "recordEnum": "EXPENDITURE", "amount": 5993.53, "date": "2025-12-19" },
    { "id": "5539128", "recordEnum": "INCOME", "amount": 563.40, "date": "2025-12-19" },
    { "id": "5482211", "recordEnum": "INCOME", "amount": 44416.50, "date": "2025-12-19" },
    { "id": "5473385", "recordEnum": "INCOME", "amount": 2191.92, "date": "2025-12-19" },
    { "id": "5413529", "recordEnum": "INCOME", "amount": 15263.00, "date": "2025-12-19" },
    { "id": "5394697", "recordEnum": "INCOME", "amount": 9222.47, "date": "2025-12-19" },
    { "id": "5350454", "recordEnum": "EXPENDITURE", "amount": 9612.86, "date": "2025-12-19" },
    { "id": "5300312", "recordEnum": "EXPENDITURE", "amount": 9422.38, "date": "2025-12-19" },
    { "id": "5263506", "recordEnum": "INCOME", "amount": 33414.55, "date": "2025-12-19" },
    { "id": "5257755", "recordEnum": "EXPENDITURE", "amount": 469.82, "date": "2025-12-19" },
    { "id": "5221455", "recordEnum": "INCOME", "amount": 15889.95, "date": "2025-12-19" },
    { "id": "5186565", "recordEnum": "EXPENDITURE", "amount": 486.58, "date": "2025-12-19" },
    { "id": "5168785", "recordEnum": "INCOME", "amount": 17920.81, "date": "2025-12-19" },
    { "id": "5166510", "recordEnum": "EXPENDITURE", "amount": 2948.18, "date": "2025-12-19" },
    { "id": "5136781", "recordEnum": "INCOME", "amount": 36780.32, "date": "2025-12-19" },
    { "id": "5074096", "recordEnum": "INCOME", "amount": 29106.76, "date": "2025-12-19" },
    { "id": "5023658", "recordEnum": "INCOME", "amount": 37853.79, "date": "2025-12-19" },
    { "id": "5007498", "recordEnum": "INCOME", "amount": 7339.78, "date": "2025-12-19" },
    { "id": "4986522", "recordEnum": "INCOME", "amount": 30504.59, "date": "2025-12-19" },
    { "id": "4891268", "recordEnum": "EXPENDITURE", "amount": 3156.91, "date": "2025-12-19" },
    { "id": "4880861", "recordEnum": "INCOME", "amount": 29211.71, "date": "2025-12-19" },
    { "id": "4765941", "recordEnum": "INCOME", "amount": 40736.65, "date": "2025-12-19" },
    { "id": "4751562", "recordEnum": "EXPENDITURE", "amount": 5502.75, "date": "2025-12-19" },
    { "id": "4680416", "recordEnum": "INCOME", "amount": 20495.21, "date": "2025-12-19" },
    { "id": "4573311", "recordEnum": "INCOME", "amount": 31507.64, "date": "2025-12-19" },
    { "id": "4560397", "recordEnum": "EXPENDITURE", "amount": 8055.77, "date": "2025-12-19" },
    { "id": "4480141", "recordEnum": "INCOME", "amount": 3216.76, "date": "2025-12-19" },
    { "id": "4421240", "recordEnum": "EXPENDITURE", "amount": 6868.68, "date": "2025-12-19" },
    { "id": "4404335", "recordEnum": "EXPENDITURE", "amount": 5331.51, "date": "2025-12-19" },
    { "id": "4384624", "recordEnum": "EXPENDITURE", "amount": 3586.43, "date": "2025-12-19" },
    { "id": "4360963", "recordEnum": "INCOME", "amount": 33202.09, "date": "2025-12-19" },
    { "id": "4302920", "recordEnum": "EXPENDITURE", "amount": 3773.20, "date": "2025-12-19" },
    { "id": "4277955", "recordEnum": "INCOME", "amount": 38494.14, "date": "2025-12-19" },
    { "id": "4256390", "recordEnum": "INCOME", "amount": 49164.92, "date": "2025-12-19" }
  ],
  "message": "成功",
  "statusCode": 200
};

// ==================== Dashboard 特有的响应式数据 ====================
const quotes = quotesData.quotes;
const selectedQuote = ref({ text: "", source: "" });
const bannerUrl = quoteBanner;
const showAddModal = ref(false);
const userName = localStorage.getItem('userName') || '用户';
const currentDate = new Date().toLocaleDateString();
const selectedDate = ref(new Date());

// ==================== 计算属性 ====================

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

  // 取最近30天的数据
  const dates = Object.keys(dailyData).sort().slice(-30);
  return dates.map(date => ({
    date: date,
    ...dailyData[date]
  }));
});

// 最近账单（最近10笔）
const recentBills = computed(() => {
  return [...allBills.value].sort((a, b) => new Date(b.date) - new Date(a.date)).slice(0, 10);
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
  // const now = new Date();
  // const startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
  // const endDate = now.toISOString().split('T')[0];

  // 传入30天内的数据
  // loadStatistics('month', startDate, endDate);
  loadStatistics('mock', '2025-12-19', '2025-12-19');
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

.bars-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  width: 100%;
  height: 180px;
  gap: 4px;
}

.bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  height: 100%;
}

.bar-column {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  height: 180px;
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

.test-controls {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.btn-generate {
  background: #ffb74d;
  border: none;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  color: #fff;
}

.btn-generate:hover { opacity: 0.95 }

/*
.test-controls {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.btn-generate {
  background: #ffb74d;
  border: none;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  color: #fff;
}

.btn-generate:hover { opacity: 0.95 }
*/

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
