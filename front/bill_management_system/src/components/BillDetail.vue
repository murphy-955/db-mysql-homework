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

        <!-- 账单详情卡片 -->
        <div v-if="bill" class="detail-card">
          <!-- 金额展示区 -->
          <div class="amount-section" :class="bill.recordEnum === 'INCOME' ? 'income' : 'expense'">
            <div class="amount-icon">
              {{ bill.recordEnum === 'INCOME' ? '💰' : '💸' }}
            </div>
            <div class="amount-info">
              <span class="amount-label">{{ bill.recordEnum === 'INCOME' ? '收入' : '支出' }}</span>
              <span class="amount-value">
                {{ bill.recordEnum === 'INCOME' ? '+' : '-' }}¥{{ formatAmount(bill.amount) }}
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

          <!-- 操作区 -->
          <div class="action-section">
            <button class="btn btn-outline btn-danger" @click="handleDelete">
              <span class="icon">🗑️</span> 删除账单
            </button>
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
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import Sidebar from './Sidebar.vue';

const router = useRouter();

// 直接从路由 state 获取账单数据（由 BillDashboard 传入）
const bill = ref(history.state?.bill || null);

// 格式化金额
const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '0.00';
  return Number(amount).toFixed(2);
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '未知日期';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  });
};

// 返回账单查询列表（优先回退历史记录，否则导航到 /bill-query）
const goBack = () => {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push({ path: '/bill-query' });
  }
};

// 删除账单
const handleDelete = async () => {
  if (!confirm('确定要删除这条账单吗？此操作不可恢复。')) {
    return;
  }

  try {
    const token = localStorage.getItem('token');
    const response = await axios.post('http://localhost:8080/api/bill', {
      token,
      id: bill.value.id
    });

    if (response.data.statusCode === 200) {
      alert('删除成功');
      goBack();
    } else {
      alert('删除失败: ' + response.data.message);
    }
  } catch (error) {
    console.error('删除账单失败:', error);
    alert('删除失败，请稍后重试');
  }
};
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
.action-section {
  padding: 24px 32px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

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