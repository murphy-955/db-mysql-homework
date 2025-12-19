<template>
  <div class="dashboard-layout">
    <!-- 左侧侧边栏 -->
    <Sidebar />

    <!-- 右侧主内容区 -->
    <main class="main-content">
      <div class="content-wrapper">
        <!-- 顶部标题 -->
        <div class="dashboard-header">
          <div class="header-title">
            <h2>账户查询</h2>
            <span class="subtitle">按账户查看流水记录</span>
          </div>
        </div>

        <!-- 账户选择区域 -->
        <div class="query-section">
          <div class="section-header">
            <h3>选择账户</h3>
          </div>

          <div v-if="accountLoading" class="loading">加载账户列表...</div>

          <div v-else-if="accountList.length === 0" class="no-data">
            暂无可用账户，请先添加账户
          </div>

          <div v-else class="account-cards">
            <div
              v-for="account in accountList"
              :key="account.id"
              class="account-card"
              :class="{ active: selectedAccountId === account.id }"
              @click="selectAccount(account)"
            >
              <div class="account-icon">💳</div>
              <div class="account-info">
                <span class="account-name">{{ account.account }}</span>
                <span class="account-balance">余额: ¥{{ formatAmount(account.balance) }}</span>
                <span v-if="account.description" class="account-desc">{{ account.description }}</span>
              </div>
              <div v-if="selectedAccountId === account.id" class="check-icon">✓</div>
            </div>
          </div>

          <!-- 分页设置 -->
          <div v-if="selectedAccountId" class="pagination-settings">
            <label>每页条数：</label>
            <select v-model.number="queryParams.limit" @change="searchBills">
              <option :value="10">10条</option>
              <option :value="20">20条</option>
              <option :value="50">50条</option>
            </select>
          </div>
        </div>

        <!-- 统计卡片 -->
        <div v-if="bills.length > 0" class="stats-cards">
          <div class="stat-card income">
            <div class="stat-icon">💰</div>
            <div class="stat-info">
              <span class="label">本页收入</span>
              <span class="value">+{{ currentIncome.toFixed(2) }}</span>
            </div>
          </div>
          <div class="stat-card expense">
            <div class="stat-icon">💸</div>
            <div class="stat-info">
              <span class="label">本页支出</span>
              <span class="value">-{{ currentExpenditure.toFixed(2) }}</span>
            </div>
          </div>
          <div class="stat-card balance">
            <div class="stat-icon">⚖️</div>
            <div class="stat-info">
              <span class="label">本页结余</span>
              <span class="value">{{ currentBalance >= 0 ? '+' : '' }}{{ currentBalance.toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <!-- 结果展示区域 -->
        <div v-if="selectedAccountId" class="results-section">
          <div class="section-header">
            <h3>账单列表</h3>
            <span class="result-count">共 {{ totalCount }} 条记录</span>
          </div>

          <div v-if="loading" class="loading">加载中...</div>

          <div v-else-if="bills.length === 0" class="no-data">该账户暂无账单记录</div>

          <table v-else class="bills-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>记录类型</th>
                <th>金额</th>
                <th>日期</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="bill in bills" :key="bill.id">
                <td>{{ bill.id }}</td>
                <td>{{ getRecordTypeName(bill.recordEnum) }}</td>
                <td :class="bill.recordEnum === 'INCOME' ? 'income-amount' : 'expenditure-amount'">
                  {{ bill.recordEnum === 'INCOME' ? '+' : '-' }}{{ bill.amount.toFixed(2) }}
                </td>
                <td>{{ bill.date }}</td>
                <td>
                  <button class="btn btn-small" @click="viewDetail(bill)">详情</button>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- 分页组件 -->
          <div class="pagination" v-if="bills.length > 0">
            <button class="btn btn-small" :disabled="queryParams.page <= 1" @click="changePage(queryParams.page - 1)">上一页</button>
            <span>第 {{ queryParams.page }} 页 / 共 {{ totalPages }} 页</span>
            <button class="btn btn-small" :disabled="queryParams.page >= totalPages" @click="changePage(queryParams.page + 1)">下一页</button>
          </div>
        </div>
      </div>
    </main>

    <!-- 账单详情弹窗 -->
    <BillDetailWindow
      v-if="showDetailModal"
      :billId="selectedBillId"
      :typeList="typeList"
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
const accountLoading = ref(true);
const selectedAccountId = ref('');

// 查询参数
const queryParams = ref({
  page: 1,
  limit: 10,
  startDate: '',
  endDate: ''
});

// 账单数据
const bills = ref([]);
const totalCount = ref(0);
const loading = ref(false);

// 游标式分页
const lastEndDate = ref('');

// 详情弹窗
const showDetailModal = ref(false);
const selectedBillId = ref(null);

// 类型列表（供弹窗使用）
const typeList = ref({});

// 计算属性
const totalPages = computed(() => {
  return Math.ceil(totalCount.value / queryParams.value.limit) || 1;
});

const currentIncome = computed(() => {
  return bills.value
    .filter(b => b.recordEnum === 'INCOME')
    .reduce((sum, b) => sum + b.amount, 0);
});

const currentExpenditure = computed(() => {
  return bills.value
    .filter(b => b.recordEnum === 'EXPENDITURE')
    .reduce((sum, b) => sum + b.amount, 0);
});

const currentBalance = computed(() => {
  return currentIncome.value - currentExpenditure.value;
});

// 格式化金额
const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '0.00';
  return Number(amount).toFixed(2);
};

// 获取记录类型名称
const getRecordTypeName = (recordEnum) => {
  const enumMap = {
    'INCOME': '收入',
    'EXPENDITURE': '支出',
    'TRANSFER': '转账'
  };
  return enumMap[recordEnum] || recordEnum;
};

// 获取日期范围（一年前到今天）
const getTodayDateStr = () => {
  const now = new Date();
  return now.toISOString().split('T')[0];
};

const getOneYearAgoDateStr = () => {
  const now = new Date();
  now.setFullYear(now.getFullYear() - 1);
  return now.toISOString().split('T')[0];
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

// 选择账户
const selectAccount = (account) => {
  selectedAccountId.value = account.id;
  // 重置查询状态
  queryParams.value.page = 1;
  bills.value = [];
  totalCount.value = 0;
  selectedBill.value = null;
  detailError.value = '';
  // 自动查询
  searchBills();
};

// 构建请求体
const buildRequestBody = (token, page, limit) => {
  return {
    token,
    startDate: queryParams.value.startDate,
    endDate: queryParams.value.endDate,
    usageEnum: 'ACCOUNT',
    accountId: selectedAccountId.value,
    page,
    limit
  };
};

// 查询账单
const searchBills = async () => {
  if (!selectedAccountId.value) return;

  // 清空详情
  selectedBill.value = null;
  detailError.value = '';
  detailLoading.value = false;

  // 设置日期范围
  queryParams.value.startDate = getOneYearAgoDateStr();
  queryParams.value.endDate = getTodayDateStr();
  queryParams.value.page = 1;
  lastEndDate.value = '';

  loading.value = true;
  try {
    const token = localStorage.getItem('token');
    await fetchFirstPage(token);
  } catch (error) {
    console.error('查询账单失败:', error);
    alert('查询失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 获取第一页数据
const fetchFirstPage = async (token) => {
  const limit = Number(queryParams.value.limit) || 10;
  const requestBody = buildRequestBody(token, 1, limit);

  const response = await axios.post(
    'http://localhost:8080/api/query/getBillList?searchType=ACCOUNT',
    requestBody
  );

  if (response.data.statusCode === 200) {
    const pageData = response.data.data || [];
    bills.value = pageData;

    if (pageData.length > 0) {
      lastEndDate.value = pageData[pageData.length - 1].date || '';
    }

    if (typeof response.data.total === 'number') {
      totalCount.value = response.data.total;
    } else {
      totalCount.value = pageData.length < limit ? pageData.length : limit * 10;
    }

    console.log(`第一页查询完成：本页 ${pageData.length} 条，总计约 ${totalCount.value} 条`);
  } else {
    alert('查询失败: ' + response.data.message);
  }
};

// 获取下一页数据
const fetchNextPage = async (token) => {
  const limit = Number(queryParams.value.limit) || 10;

  if (lastEndDate.value) {
    queryParams.value.endDate = lastEndDate.value;
  }

  const requestBody = buildRequestBody(token, queryParams.value.page, limit);

  const response = await axios.post(
    'http://localhost:8080/api/query/getBillList?searchType=ACCOUNT',
    requestBody
  );

  if (response.data.statusCode === 200) {
    const pageData = response.data.data || [];
    bills.value = pageData;

    if (pageData.length > 0) {
      lastEndDate.value = pageData[pageData.length - 1].date || '';
    }

    if (typeof response.data.total === 'number') {
      totalCount.value = response.data.total;
    }

    console.log(`第 ${queryParams.value.page} 页查询完成：本页 ${pageData.length} 条`);
  } else {
    alert('查询失败: ' + response.data.message);
  }
};

// 切换页码
const changePage = async (page) => {
  if (page < 1 || page > totalPages.value) return;

  if (page === 1) {
    await searchBills();
    return;
  }

  queryParams.value.page = page;
  loading.value = true;

  try {
    const token = localStorage.getItem('token');
    await fetchNextPage(token);
  } catch (error) {
    console.error('翻页失败:', error);
    alert('翻页失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 查看详情
const viewDetail = (billData) => {
  if (!billData || !billData.id) {
    alert('缺少账单ID，无法查看详情');
    return;
  }
  selectedBillId.value = billData.id;
  showDetailModal.value = true;
};

// 关闭详情弹窗
const closeDetailModal = () => {
  showDetailModal.value = false;
  selectedBillId.value = null;
};

onMounted(() => {
  fetchUserAccounts();
});
</script>

<style scoped>
/* 基础布局样式 */
.dashboard-layout {
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
  max-width: 1600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 顶部头部样式 */
.dashboard-header {
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

/* 账户卡片 */
.account-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.account-card {
  background: #fafafa;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
}

.account-card:hover {
  border-color: #1890ff;
  background: #e6f7ff;
}

.account-card.active {
  border-color: #1890ff;
  background: #e6f7ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.account-icon {
  font-size: 32px;
  width: 56px;
  height: 56px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0,0,0,0.06);
}

.account-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.account-name {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.account-balance {
  font-size: 14px;
  color: #52c41a;
  font-weight: 500;
}

.account-desc {
  font-size: 12px;
  color: #8c8c8c;
}

.check-icon {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 24px;
  height: 24px;
  background: #1890ff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

/* 统计卡片样式 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  transition: transform 0.3s, box-shadow 0.3s;
  border: 1px solid #f0f0f0;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.stat-icon {
  font-size: 32px;
  margin-right: 20px;
  background: #f5f5f5;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-info .label {
  color: #8c8c8c;
  font-size: 14px;
  margin-bottom: 4px;
}

.stat-info .value {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
}

.stat-card.income .stat-icon { background: #f6ffed; }
.stat-card.income .value { color: #52c41a; }

.stat-card.expense .stat-icon { background: #fff1f0; }
.stat-card.expense .value { color: #ff4d4f; }

.stat-card.balance .stat-icon { background: #e6f7ff; }
.stat-card.balance .value { color: #1890ff; }

/* 查询和结果区域样式 */
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

.pagination-settings {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.pagination-settings label {
  margin-right: 10px;
  color: #666;
}

.pagination-settings select {
  padding: 6px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

/* 按钮样式 */
.btn {
  padding: 8px 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  font-size: 14px;
}

.btn-primary {
  background-color: #1890ff;
  color: white;
}

.btn-primary:hover {
  background-color: #40a9ff;
}

.btn-small {
  padding: 4px 12px;
  font-size: 12px;
}

.btn-small:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 表格样式 */
.loading,
.no-data {
  text-align: center;
  padding: 40px;
  color: #999;
}

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
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  margin-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* 内联详情 */
.inline-detail {
  margin-top: 24px;
}

.detail-card-inline {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-amount {
  padding: 16px;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-amount.income {
  background: #f6ffed;
  color: #52c41a;
}

.detail-amount.expense {
  background: #fff1f0;
  color: #ff4d4f;
}

.detail-amount .amount-main {
  font-size: 24px;
  font-weight: 700;
}

.detail-amount .amount-meta {
  font-size: 13px;
  color: #8c8c8c;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.detail-item {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item .label {
  font-size: 12px;
  color: #8c8c8c;
}

.detail-item .value {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
}

.detail-remark {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-remark .label {
  font-size: 12px;
  color: #8c8c8c;
}

.detail-remark .value {
  font-size: 14px;
  color: #595959;
  line-height: 1.6;
  margin: 0;
}

.error-text {
  color: #ff4d4f;
  padding: 12px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .main-content {
    padding: 20px;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .account-cards {
    grid-template-columns: 1fr;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
