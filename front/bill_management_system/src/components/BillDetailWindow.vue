<template>
  <div class="modal-overlay" @click.self="handleClose">
    <div class="bill-detail-window">
      <div class="window-header">
        <h3 class="window-title">账单详情</h3>
        <button class="close-btn" @click="handleClose" title="关闭">×</button>
      </div>

      <!-- 内容区域 -->
      <div class="window-content">
        <!-- 加载中 -->
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <span>加载中...</span>
        </div>

        <!-- 错误提示 -->
        <div v-else-if="errorMsg" class="error-container">
          <div class="error-icon">⚠️</div>
          <p class="error-text">{{ errorMsg }}</p>
        </div>

        <!-- 详情内容 -->
        <div v-else-if="bill" class="detail-content">
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

          <!-- 详细信息网格 -->
          <div class="info-grid">
            <div class="info-item">
              <div class="info-icon">🔢</div>
              <div class="info-content">
                <span class="info-label">账单ID</span>
                <span class="info-value">{{ bill.id }}</span>
              </div>
            </div>

            <div class="info-item">
              <div class="info-icon">📁</div>
              <div class="info-content">
                <span class="info-label">分类</span>
                <span class="info-value">{{ bill.type || '未分类' }}</span>
              </div>
            </div>

            <div class="info-item">
              <div class="info-icon">📅</div>
              <div class="info-content">
                <span class="info-label">日期</span>
                <span class="info-value">{{ formatDate(bill.date) }}</span>
              </div>
            </div>

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

        <!-- 未找到数据 -->
        <div v-else class="empty-container">
          <div class="empty-icon">📭</div>
          <p>未找到账单数据</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  bill: {
    type: Object,
    default: null
  },
  loading: {
    type: Boolean,
    default: false
  },
  errorMsg: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['close']);

const isIncome = computed(() => {
  const val = (props.bill?.recordEnum || '').toString().toUpperCase();
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

const handleClose = () => {
  emit('close');
};
</script>

<style scoped>
/* 模态遮罩层 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

/* 窗口容器样式 */
.bill-detail-window {
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 窗口头部 */
.window-header {
  padding: 24px 30px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.window-title {
  font-size: 22px;
  color: #333;
  font-weight: 600;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  padding: 0;
  line-height: 1;
  transition: color 0.3s;
}

.close-btn:hover {
  color: #333;
}

/* 内容区域 */
.window-content {
  padding: 0 30px 30px;
}

/* 加载中 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #999;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f0f0f0;
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 错误提示 */
.error-container {
  text-align: center;
  padding: 30px 0;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.error-text {
  color: #ff4d4f;
  font-size: 14px;
  margin: 0;
}

/* 空状态 */
.empty-container {
  text-align: center;
  padding: 30px 0;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

/* 详情内容 */
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 金额展示区 */
.amount-section {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 12px;
  gap: 16px;
}

.amount-section.income {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
}

.amount-section.expense {
  background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
}

.amount-icon {
  font-size: 40px;
  background: white;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.amount-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.amount-label {
  font-size: 14px;
  color: #8c8c8c;
}

.amount-value {
  font-size: 28px;
  font-weight: 700;
}

.amount-section.income .amount-value {
  color: #52c41a;
}

.amount-section.expense .amount-value {
  color: #ff4d4f;
}

/* 详细信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: #fafafa;
  padding: 14px;
  border-radius: 10px;
  transition: background 0.3s;
}

.info-item:hover {
  background: #f0f0f0;
}

.info-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.info-label {
  font-size: 12px;
  color: #8c8c8c;
}

.info-value {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
  word-break: break-word;
}

/* 备注区域 */
.remark-section {
  background: #fafafa;
  border-radius: 10px;
  padding: 16px;
}

.remark-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
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
  font-size: 14px;
  color: #595959;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
