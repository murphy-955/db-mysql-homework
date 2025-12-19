<template>
  <div class="detail-layout">
    <!-- 左侧侧边栏 -->
    <Sidebar />

    <!-- 右侧主内容区 -->
    <main class="main-content">
      <div class="content-wrapper">
        <!-- 顶部头部 -->
        <div class="detail-header">
          <div class="header-title">
            <h2>账单详情</h2>
            <span class="subtitle">查看账单完整信息</span>
          </div>
          <button class="btn btn-outline" @click="goBack">
            <span class="icon">←</span> 返回列表
          </button>
        </div>

        <!-- 加载中 -->
        <div v-if="loading" class="loading-card">
          <div class="loading-spinner"></div>
          <span>加载中...</span>
        </div>

        <!-- 错误或未找到 -->
        <div v-else-if="errorMsg" class="empty-card">
          <div class="empty-icon">⚠️</div>
          <h3>无法获取账单</h3>
          <p>{{ errorMsg }}</p>
          <button class="btn btn-primary" @click="goBack">返回列表</button>
        </div>

        <!-- 账单详情卡片 -->
        <div v-else-if="bill" class="detail-card">
          <!-- 金额展示区 -->
          <div class="amount-section" :class="isIncome ? 'income' : 'expense'">
            <div class="amount-icon">
              {{ isIncome ? '💰' : '💸' }}
            </div>
            <div class="amount-info">
              <span class="amount-label">{{ recordLabel }}</span>
              <span class="amount-value">
                {{ amountPrefix }}¥{{ formatAmount(bill.amount) }}
              </span>
            </div>
          </div>

          <!-- 详细信息区 -->
          <div class="info-section">
            <div class="section-header">
              <h3>详细信息</h3>
            </div>

            <div class="info-grid">
              <!-- 账单ID -->
              <div class="info-item">
                <div class="info-icon">🔢</div>
                <div class="info-content">
                  <span class="info-label">账单ID</span>
                  <span class="info-value">{{ bill.id }}</span>
                </div>
              </div>

              <!-- 类型/分类 -->
              <div class="info-item">
                <div class="info-icon">📁</div>
                <div class="info-content">
                  <span class="info-label">分类</span>
                  <span class="info-value">{{ bill.type || '未分类' }}</span>
                </div>
              </div>

              <!-- 日期 -->
              <div class="info-item">
                <div class="info-icon">📅</div>
                <div class="info-content">
                  <span class="info-label">日期</span>
                  <span class="info-value">{{ formatDate(bill.date) }}</span>
                </div>
              </div>

              <!-- 账户 -->
              <div class="info-item">
                <div class="info-icon">💳</div>
                <div class="info-content">
                  <span class="info-label">账户</span>
                  <span class="info-value">{{ bill.account || '未指定' }}</span>
                </div>
              </div>
            </div>

            <!-- 备注区域 -->
            <div class="remark-section">
              <div class="remark-header">
                <span class="remark-icon">📝</span>
                <span class="remark-label">备注</span>
              </div>
              <div class="remark-content">
                {{ bill.remarks || '暂无备注' }}
              </div>
            </div>
          </div>

        </div>

        <!-- 未找到账单 -->
        <div v-else class="empty-card">
          <div class="empty-icon">📭</div>
          <h3>未找到账单</h3>
          <p>该账单数据不存在，请返回列表重新查看</p>
          <button class="btn btn-primary" @click="goBack">返回列表</button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import Sidebar from './Sidebar.vue';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const errorMsg = ref('');
const bill = ref(null);

const isIncome = computed(() => {
  const val = (bill.value?.recordEnum || '').toString().toUpperCase();
  return val === 'INCOME';
});

const recordLabel = computed(() => (isIncome.value ? '收入' : '支出'));
const amountPrefix = computed(() => (isIncome.value ? '+' : '-'));

// 格式化金额
const formatAmount = (amount) => {
  if (amount === null || amount === undefined || isNaN(Number(amount))) return '0.00';
  return Number(amount).toFixed(2);
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '未知日期';
  const date = new Date(dateStr);
  if (Number.isNaN(date.getTime())) return dateStr;
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  });
};

// 获取账单详情
const fetchBillDetail = async () => {
  const routeStateBill = history.state?.bill || null;
  const id = route.params.id || route.query.id || routeStateBill?.id;

  if (!id) {
    errorMsg.value = '缺少账单ID，无法查询详情';
    loading.value = false;
    return;
  }

  // 若路由 state 已有数据，先展示，再请求最新数据
  if (routeStateBill) {
    bill.value = routeStateBill;
  }

  try {
    const token = localStorage.getItem('token');
    const response = await axios.post('/api/bill/getBillDetail', {
      token,
      id
    });

    if (response.data.statusCode === 200 && response.data.data) {
      bill.value = response.data.data;
    } else {
      errorMsg.value = response.data.message || '未找到该账单';
    }
  } catch (error) {
    console.error('获取账单详情失败:', error);
    errorMsg.value = '获取账单详情失败，请稍后重试';
  } finally {
    loading.value = false;
  }
};

// 返回账单查询列表（优先回退历史记录，否则导航到 /bill-query）
const goBack = () => {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push({ path: '/bill-query' });
  }
};

onMounted(fetchBillDetail);
</script>

<style scoped>
/* 基础布局 */
.detail-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #f0f2f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  overflow: hidden;
}

.main-content {
  flex: 1;
  height: 100%;
  overflow-y: auto;
  padding: 40px;
  background-color: #f0f2f5;
}

.content-wrapper {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 头部样式 */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.header-title h2 {
  margin: 0;
  font-size: 24px;
  color: #1f1f1f;
}

.subtitle {
  color: #8c8c8c;
  font-size: 14px;
  margin-top: 4px;
  display: block;
}

/* 按钮样式 */
.btn {
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.btn-primary {
  background-color: #1890ff;
  color: white;
}

.btn-primary:hover {
  background-color: #40a9ff;
  transform: translateY(-1px);
}

.btn-outline {
  background-color: white;
  color: #1890ff;
  border: 1px solid #1890ff;
}

.btn-outline:hover {
  background-color: #e6f7ff;
}

.btn-danger {
  color: #ff4d4f;
  border-color: #ff4d4f;
}

.btn-danger:hover {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.icon {
  font-size: 14px;
}

/* 详情卡片 */
.detail-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
}

/* 金额区域 */
.amount-section {
  padding: 40px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.amount-section.income {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
}

.amount-section.expense {
  background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
}

.amount-icon {
  font-size: 48px;
  width: 80px;
  height: 80px;
  background: rgba(255,255,255,0.8);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.amount-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.amount-label {
  font-size: 16px;
  color: #595959;
  font-weight: 500;
}

.amount-value {
  font-size: 36px;
  font-weight: 700;
}

.amount-section.income .amount-value {
  color: #52c41a;
}

.amount-section.expense .amount-value {
  color: #ff4d4f;
}

/* 信息区域 */
.info-section {
  padding: 32px;
}

.section-header {
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 12px;
  transition: background 0.3s;
}

.info-item:hover {
  background: #f5f5f5;
}

.info-icon {
  font-size: 24px;
  width: 48px;
  height: 48px;
  background: white;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0,0,0,0.06);
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 13px;
  color: #8c8c8c;
}

.info-value {
  font-size: 16px;
  color: #262626;
  font-weight: 500;
}

/* 备注区域 */
.remark-section {
  background: #fafafa;
  border-radius: 12px;
  padding: 20px;
}

.remark-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.remark-icon {
  font-size: 18px;
}

.remark-label {
  font-size: 14px;
  color: #8c8c8c;
  font-weight: 500;
}

.remark-content {
  font-size: 15px;
  color: #595959;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 操作区域 */


/* 空状态 */
.empty-card {
  background: white;
  border-radius: 16px;
  padding: 60px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-card h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #262626;
}

.empty-card p {
  margin: 0 0 24px 0;
  color: #8c8c8c;
  font-size: 14px;
}

/* 加载状态 */
.loading-card {
  background: white;
  border-radius: 16px;
  padding: 60px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #8c8c8c;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f0f0f0;
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .main-content {
    padding: 20px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .amount-section {
    padding: 24px;
    flex-direction: column;
    text-align: center;
  }

  .amount-value {
    font-size: 28px;
  }

  .detail-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>