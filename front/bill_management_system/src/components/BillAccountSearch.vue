<template>
  <div class="page-layout">
    <!-- 左侧侧边栏 -->
    <Sidebar />

    <!-- 右侧主内容区 -->
    <main class="main-content">
      <div class="content-wrapper">
        <!-- 顶部头部 -->
        <div class="page-header">
          <div class="header-title">
            <h2>账户流水查询</h2>
            <span class="subtitle">按账户查看收支明细</span>
          </div>
        </div>

        <!-- 查询区域 -->
        <div class="query-section">
          <!-- 账户选择区域 -->
          <div class="account-selector">
            <label class="selector-label">选择账户：</label>
            <select v-model="selectedAccountId" class="account-select" @change="handleAccountChange">
              <option value="">请选择账户</option>
              <option v-for="account in accountList" :key="account.id" :value="account.id">
                {{ account.account }} (余额: {{ formatBalance(account.balance) }})
              </option>
            </select>
            <button class="btn btn-primary" @click="searchBills" :disabled="!selectedAccountId">
              查询流水
            </button>
          </div>

          <!-- 账户信息卡片 -->
          <div v-if="selectedAccount" class="account-info-card">
            <div class="account-header">
              <div class="account-icon">💳</div>
              <div class="account-details">
                <h4 class="account-name">{{ selectedAccount.account }}</h4>
                <span class="account-id">账户ID: {{ selectedAccount.id }}</span>
              </div>
            </div>
            <div class="account-balance">
              <span class="balance-label">当前余额</span>
              <span class="balance-value" :class="balanceClass">
                ¥{{ formatBalance(selectedAccount.balance) }}
              </span>
            </div>
          </div>

          <!-- 无账户提示 -->
          <div v-if="accountList.length === 0 && !accountLoading" class="no-account-tip">
            <div class="tip-icon">📭</div>
            <p>暂无可用账户</p>
            <span class="tip-text">请先添加账户后再进行账户查询</span>
          </div>

          <!-- 加载账户状态 -->
          <div v-if="accountLoading" class="loading-state">
            <div class="loading-spinner"></div>
            <span>加载账户列表...</span>
          </div>
        </div>

        <!-- 流水结果区域 -->
        <div class="results-section" v-if="hasSearched">
          <div class="section-header">
            <h3>流水记录</h3>
            <span class="result-count">共 {{ totalCount }} 条记录</span>
          </div>

          <!-- 加载流水状态 -->
          <div v-if="billLoading" class="loading-state">
            <div class="loading-spinner"></div>
            <span>加载流水记录...</span>
          </div>

          <!-- 无数据 -->
          <div v-else-if="bills.length === 0" class="no-data">
            <div class="empty-icon">📭</div>
            <p>该账户暂无流水记录</p>
          </div>

          <!-- 流水列表 -->
          <table v-else class="bills-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>类型</th>
                <th>金额</th>
                <th>日期</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="bill in bills" :key="bill.id">
                <td>{{ bill.id }}</td>
                <td>
                  <span class="type-tag" :class="getRecordTypeClass(bill.recordEnum)">
                    {{ getRecordTypeName(bill.recordEnum) }}
                  </span>
                </td>
                <td :class="bill.recordEnum?.toUpperCase() === 'INCOME' ? 'income-amount' : 'expenditure-amount'">
                  {{ bill.recordEnum?.toUpperCase() === 'INCOME' ? '+' : '-' }}{{ formatBalance(bill.amount) }}
                </td>
                <td>{{ bill.date }}</td>
                <td>
                  <button class="btn btn-small" @click="viewDetail(bill)">详情</button>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- 分页 -->
          <div class="pagination" v-if="bills.length > 0">
            <button class="btn btn-small" :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
            <span>第 {{ page }} 页 / 共 {{ totalPages }} 页</span>
            <button class="btn btn-small" :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
          </div>
        </div>
      </div>
    </main>

    <!-- 详情弹窗 -->
    <BillDetailWindow
      v-if="showDetailModal"
      :bill="selectedBill"
      :loading="detailLoading"
      :error-msg="detailError"
      @close="closeDetailModal"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import Sidebar from './Sidebar.vue';
import BillDetailWindow from './BillDetailWindow.vue';

// 账户相关
const accountList = ref([]);
const selectedAccountId = ref('');
const accountLoading = ref(false);

// 流水相关
const bills = ref([]);
const billLoading = ref(false);
const hasSearched = ref(false);
const page = ref(1);
const limit = ref(10);
const totalCount = ref(0);

// 详情弹窗相关
const showDetailModal = ref(false);
const selectedBill = ref(null);
const detailLoading = ref(false);
const detailError = ref('');

// 计算属性
const selectedAccount = computed(() => {
  if (!selectedAccountId.value) return null;
  return accountList.value.find(a => String(a.id) === String(selectedAccountId.value));
});

const balanceClass = computed(() => {
  if (!selectedAccount.value) return '';
  const balance = Number(selectedAccount.value.balance);
  if (balance > 0) return 'positive';
  if (balance < 0) return 'negative';
  return '';
});

const totalPages = computed(() => {
  return Math.ceil(totalCount.value / limit.value) || 1;
});

// 格式化余额
const formatBalance = (balance) => {
  if (balance === null || balance === undefined || isNaN(Number(balance))) return '0.00';
  return Number(balance).toFixed(2);
};

// 获取记录类型名称
const getRecordTypeName = (recordEnum) => {
  const enumMap = {
    'INCOME': '收入',
    'EXPENDITURE': '支出',
    'TRANSFER': '转账'
  };
  return enumMap[recordEnum?.toUpperCase()] || recordEnum;
};

// 获取记录类型样式类
const getRecordTypeClass = (recordEnum) => {
  const type = recordEnum?.toUpperCase();
  if (type === 'INCOME') return 'income';
  if (type === 'EXPENDITURE') return 'expense';
  return '';
};

// 获取用户账户列表
const fetchUserAccounts = async () => {
  accountLoading.value = true;
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      console.warn('未找到token，无法获取账户列表');
      accountList.value = [];
      return;
    }

    const response = await axios.post('http://localhost:8080/api/user/getUserAccount', {
      token
    });

    if (response.data.statusCode === 200) {
      accountList.value = response.data.data || [];
      console.log('获取到账户列表:', accountList.value);
      
      // 如果有账户，默认选中第一个
      if (accountList.value.length > 0) {
        selectedAccountId.value = accountList.value[0].id;
      }
    } else {
      console.error('获取账户列表失败:', response.data.message);
      accountList.value = [];
    }
  } catch (error) {
    console.error('获取账户列表异常:', error);
    accountList.value = [];
  } finally {
    accountLoading.value = false;
  }
};

// 处理账户选择变化
const handleAccountChange = () => {
  // 切换账户时清空之前的查询结果
  bills.value = [];
  hasSearched.value = false;
  page.value = 1;
  totalCount.value = 0;
};

// 根据账户查询流水
const searchBills = async () => {
  if (!selectedAccountId.value) {
    alert('请先选择账户');
    return;
  }

  billLoading.value = true;
  hasSearched.value = true;

  try {
    const token = localStorage.getItem('token');
    const response = await axios.post('http://localhost:8080/api/query/getBillList?searchType=ACCOUNT', {
      token,
      searchType: 'ACCOUNT',
      accountId: selectedAccountId.value,
      page: page.value,
      limit: limit.value
    });

    if (response.data.statusCode === 200) {
      bills.value = response.data.data || [];
      // 如果后端返回了 total 字段
      if (typeof response.data.total === 'number') {
        totalCount.value = response.data.total;
      } else {
        // 估算总数
        totalCount.value = bills.value.length < limit.value ? 
          (page.value - 1) * limit.value + bills.value.length : 
          page.value * limit.value + 1;
      }
      console.log('查询到流水记录:', bills.value);
    } else {
      console.error('查询流水失败:', response.data.message);
      alert('查询失败: ' + response.data.message);
      bills.value = [];
    }
  } catch (error) {
    console.error('查询流水异常:', error);
    alert('查询失败，请稍后重试');
    bills.value = [];
  } finally {
    billLoading.value = false;
  }
};

// 切换页码
const changePage = async (newPage) => {
  if (newPage < 1 || newPage > totalPages.value) return;
  page.value = newPage;
  await searchBills();
};

// 查看详情
const viewDetail = async (billData) => {
  if (!billData || !billData.id) {
    alert('缺少账单ID，无法查看详情');
    return;
  }

  showDetailModal.value = true;
  detailError.value = '';
  detailLoading.value = true;
  selectedBill.value = null;

  try {
    const token = localStorage.getItem('token');
    const response = await axios.post('http://localhost:8080/api/bill/getBillDetail', {
      token,
      id: billData.id
    });

    if (response.data.statusCode === 200 && response.data.data) {
      selectedBill.value = response.data.data;
    } else {
      detailError.value = response.data.message || '未找到该账单';
    }
  } catch (error) {
    console.error('获取账单详情失败:', error);
    detailError.value = '获取账单详情失败，请稍后重试';
  } finally {
    detailLoading.value = false;
  }
};

// 关闭详情弹窗
const closeDetailModal = () => {
  showDetailModal.value = false;
  selectedBill.value = null;
  detailError.value = '';
  detailLoading.value = false;
};

// 组件挂载时加载账户列表
onMounted(() => {
  fetchUserAccounts();
});
</script>

<style scoped>
/* 页面布局 */
.page-layout {
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
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 页面头部 */
.page-header {
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

/* 查询区域 */
.query-section,
.results-section {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e8e8e8;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.result-count {
  color: #999;
  font-size: 14px;
}

/* 账户选择器 */
.account-selector {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.selector-label {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  font-weight: 500;
}

.account-select {
  flex: 1;
  max-width: 300px;
  padding: 10px 14px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  color: #333;
  background-color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.account-select:hover {
  border-color: #40a9ff;
}

.account-select:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* 账户信息卡片 */
.account-info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  color: white;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  max-width: 350px;
}

.account-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}

.account-icon {
  font-size: 32px;
  background: rgba(255, 255, 255, 0.2);
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.account-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.account-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.account-id {
  font-size: 12px;
  opacity: 0.8;
}

.account-balance {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.balance-label {
  font-size: 12px;
  opacity: 0.8;
}

.balance-value {
  font-size: 28px;
  font-weight: 700;
}

.balance-value.positive {
  color: #b7eb8f;
}

.balance-value.negative {
  color: #ffccc7;
}

/* 无账户提示 */
.no-account-tip {
  text-align: center;
  padding: 30px 20px;
  background: #fafafa;
  border-radius: 12px;
  border: 1px dashed #d9d9d9;
}

.tip-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.no-account-tip p {
  margin: 0 0 8px;
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.tip-text {
  font-size: 14px;
  color: #999;
}

/* 无数据 */
.no-data {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.no-data p {
  margin: 0;
  font-size: 14px;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
  color: #999;
}

.loading-spinner {
  width: 28px;
  height: 28px;
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

/* 按钮样式 */
.btn {
  padding: 8px 20px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  font-size: 14px;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background-color: #1890ff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #40a9ff;
}

.btn-small {
  padding: 4px 12px;
  font-size: 12px;
}

/* 表格样式 */
.bills-table {
  width: 100%;
  border-collapse: collapse;
}

.bills-table th,
.bills-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.bills-table th {
  background-color: #fafafa;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.bills-table tbody tr:hover {
  background-color: #fafafa;
}

.type-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.type-tag.income {
  background-color: #f6ffed;
  color: #52c41a;
}

.type-tag.expense {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.income-amount {
  color: #52c41a;
  font-weight: 500;
}

.expenditure-amount {
  color: #ff4d4f;
  font-weight: 500;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  margin-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.pagination span {
  color: #666;
  font-size: 14px;
}
</style>
