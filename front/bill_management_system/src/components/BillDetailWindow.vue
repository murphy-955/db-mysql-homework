<template>
  <div class="modal-overlay" @click.self="handleClose">
    <div class="bill-detail-window">
      <div class="window-header">
        <h3 class="window-title">账单详情</h3>
        <button class="close-btn" @click="handleClose" title="关闭">×</button>
      </div>

      <!-- 内容区域 -->
      <div class="window-content">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>

        <!-- 错误状态 -->
        <div v-else-if="error" class="error-state">
          <span class="error-icon">⚠️</span>
          <p>{{ error }}</p>
          <button class="retry-btn" @click="fetchDetail">重试</button>
        </div>

        <!-- 详情内容 -->
        <div v-else-if="bill" class="detail-content">
          <!-- 金额区域 -->
          <div class="amount-section" :class="bill.recordEnum === 'INCOME' ? 'income' : 'expense'">
            <span class="amount-label">{{ bill.recordEnum === 'INCOME' ? '收入' : '支出' }}</span>
            <span class="amount-value">
              {{ bill.recordEnum === 'INCOME' ? '+' : '-' }}¥{{ bill.amount?.toFixed(2) }}
            </span>
          </div>

          <!-- 详情列表 -->
          <div class="detail-list">
            <div class="detail-item">
              <span class="item-label">📋 账单类型</span>
              <span class="item-value">{{ getTypeLabel(bill.typeEnum) }}</span>
            </div>
            
            <div class="detail-item">
              <span class="item-label">📝 描述</span>
              <span class="item-value">{{ bill.description || '无描述' }}</span>
            </div>
            
            <div class="detail-item">
              <span class="item-label">📅 日期</span>
              <span class="item-value">{{ formatDate(bill.date) }}</span>
            </div>
            
            <div class="detail-item">
              <span class="item-label">💳 账户</span>
              <span class="item-value">{{ bill.account || '未知账户' }}</span>
            </div>
            
            <div class="detail-item" v-if="bill.remark">
              <span class="item-label">💬 备注</span>
              <span class="item-value">{{ bill.remark }}</span>
            </div>
            
            <div class="detail-item">
              <span class="item-label">🔖 账单ID</span>
              <span class="item-value id-value">{{ bill.id }}</span>
            </div>
          </div>
        </div>

        <!-- 无数据状态 -->
        <div v-else class="empty-state">
          <span class="empty-icon">📭</span>
          <p>暂无账单数据</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import axios from 'axios';

const props = defineProps({
  billId: {
    type: [String, Number],
    required: true
  },
  typeList: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['close']);

const bill = ref(null);
const loading = ref(false);
const error = ref('');

// 获取类型标签
const getTypeLabel = (typeEnum) => {
  if (!typeEnum) return '未知类型';
  return props.typeList[typeEnum] || typeEnum;
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '未知日期';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    weekday: 'short'
  });
};

// 获取账单详情
const fetchDetail = async () => {
  if (!props.billId) {
    error.value = '账单ID无效';
    return;
  }

  loading.value = true;
  error.value = '';
  bill.value = null;

  try {
    const token = localStorage.getItem('token');
    if (!token) {
      error.value = '未登录，请先登录';
      return;
    }

    const response = await axios.post('http://localhost:8080/api/bill/getBillDetail', {
      token,
      id: props.billId
    });

    if (response.data.statusCode === 200) {
      bill.value = response.data.data;
    } else {
      error.value = response.data.message || '获取详情失败';
    }
  } catch (err) {
    console.error('获取账单详情失败:', err);
    error.value = '网络错误，请稍后重试';
  } finally {
    loading.value = false;
  }
};

// 关闭弹窗
const handleClose = () => {
  emit('close');
};

// 监听 billId 变化
watch(() => props.billId, (newId) => {
  if (newId) {
    fetchDetail();
  }
}, { immediate: true });

// 组件挂载时获取详情
onMounted(() => {
  fetchDetail();
});
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
  max-width: 480px;
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

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  color: #999;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f0f0f0;
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 错误状态 */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  color: #ff4d4f;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-state p {
  margin: 0 0 16px;
}

.retry-btn {
  padding: 8px 24px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
}

.retry-btn:hover {
  background: #40a9ff;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 详情内容 */
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 金额区域 */
.amount-section {
  text-align: center;
  padding: 24px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
}

.amount-section.income {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
}

.amount-section.expense {
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
}

.amount-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.amount-value {
  font-size: 36px;
  font-weight: 700;
}

.amount-section.income .amount-value {
  color: #4caf50;
}

.amount-section.expense .amount-value {
  color: #f44336;
}

/* 详情列表 */
.detail-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f9f9f9;
  border-radius: 8px;
}

.item-label {
  color: #666;
  font-size: 14px;
}

.item-value {
  color: #333;
  font-weight: 500;
  font-size: 14px;
  text-align: right;
  max-width: 60%;
  word-break: break-all;
}

.id-value {
  font-family: monospace;
  font-size: 12px;
  color: #999;
}
</style>
