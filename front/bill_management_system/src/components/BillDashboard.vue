<template>
  <div class="dashboard-layout">
    <!-- 左侧侧边栏 -->
    <Sidebar />

    <!-- 右侧主内容区 -->
    <main class="main-content">
      <div class="content-wrapper">
        <!-- 顶部概览区域 -->
        <div class="dashboard-header">
          <div class="header-title">
            <h2>账单仪表盘</h2>
            <span class="subtitle">管理您的每一笔收支</span>
          </div>
          <button class="btn btn-primary btn-lg" @click="openAddModal">
            <span class="icon">+</span> 记一笔
          </button>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-cards">
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

        <!-- 查询区域 -->
        <div class="query-section">
          <div class="section-header">
            <h3>账单查询</h3>
          </div>

          <!-- 查询方式选择 -->
          <div class="query-type-selector">
            <label for="usageEnum">查询方式：</label>
            <select id="usageEnum" v-model="queryParams.usageEnum" @change="onSearchTypeChange">
              <option value="">请选择查询方式</option>
              <option value="DATE_RANGE">日期查询</option>
              <option value="ACCOUNT">账户查询</option>
              <option value="USAGE_TYPE">类型查询</option>
              <option value="KEYWORD">关键字查询</option>
              <option value="AMOUNT_RANGE">金额范围查询</option>
            </select>
          </div>

          <!-- 动态查询条件 -->
          <div class="query-conditions">
            <!-- 日期查询 -->
            <div v-if="queryParams.usageEnum === 'DATE_RANGE'" class="condition-group">
              <label>开始日期：</label>
              <input type="date" v-model="queryParams.startDate" />
              <label>结束日期：</label>
              <input type="date" v-model="queryParams.endDate" />
            </div>

            <!-- 类型查询 -->
            <div v-if="queryParams.usageEnum === 'USAGE_TYPE'" class="condition-group">
              <label>账单类型：</label>
              <select v-model="queryParams.type">
                <option value="">请选择类型</option>
                <option v-for="(label, value) in typeList" :key="value" :value="value">{{ label }}</option>
              </select>
            </div>

            <!-- 关键字查询 -->
            <div v-if="queryParams.usageEnum === 'KEYWORD'" class="condition-group">
              <label>关键字：</label>
              <input type="text" v-model="queryParams.keyword" placeholder="请输入关键字" />
            </div>

            <!-- 账户查询 -->
            <div v-if="queryParams.usageEnum === 'ACCOUNT'" class="condition-group">
              <label>选择账户：</label>
              <select v-model="queryParams.accountId">
                <option value="">请选择账户</option>
                <option v-for="account in accountList" :key="account.id" :value="account.id">
                  {{ account.account }} (余额: {{ account.balance }})
                </option>
              </select>
            </div>

            <!-- 金额范围查询 -->
            <div v-if="queryParams.usageEnum === 'AMOUNT_RANGE'" class="condition-group">
              <label>最小金额：</label>
              <input type="number" v-model.number="queryParams.minAmount" placeholder="请输入最小金额" />
              <label>最大金额：</label>
              <input type="number" v-model.number="queryParams.maxAmount" placeholder="请输入最大金额" />
            </div>
          </div>

          <!-- 分页设置 -->
          <div class="pagination-settings">
            <label>每页条数：</label>
            <select v-model.number="queryParams.limit">
              <option :value="10">10条</option>
              <option :value="20">20条</option>
              <option :value="50">50条</option>
            </select>
          </div>

          <!-- 查询按钮 -->
          <div class="query-actions">
            <button class="btn btn-primary" @click="searchBills">查询</button>
            <button class="btn btn-outline" @click="resetQuery">重置</button>
            
          </div>
        </div>

        <!-- 结果展示区域 -->
        <div class="results-section">
          <div class="section-header">
            <h3>查询结果</h3>
            <span class="result-count">共 {{ totalCount }} 条记录</span>
            <span v-if="isPageCapped" class="cap-note">（已封顶 {{ MAX_PAGES }} 页）</span>
          </div>

          <div v-if="loading" class="loading">加载中...</div>

          <div v-else-if="bills.length === 0" class="no-data">暂无数据</div>

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
                  <button class="btn btn-small" @click="viewDetail(bill.id)">详情</button>
                  <button class="btn btn-small btn-danger" @click="deleteBill(bill.id)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- 分页组件 -->
          <div class="pagination" v-if="bills.length > 0">
            <button class="btn btn-small" :disabled="queryParams.page <= 1" @click="changePage(queryParams.page - 1)">上一页</button>
            <span>第 {{ queryParams.page }} 页 / 共 {{ totalPages }} 页</span>
            <button class="btn btn-small" :disabled="queryParams.page >= totalPages || queryParams.page >= MAX_PAGES" @click="changePage(queryParams.page + 1)">下一页</button>
          </div>
        </div>
      </div>
    </main>

    <BillAddWindow
      v-if="showAddModal"
      @success="handleAddSuccess"
      @cancel="closeAddModal"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import BillAddWindow from './BillAddWindow.vue';
import Sidebar from './Sidebar.vue';

// 路由相关
const route = useRoute();
const router = useRouter();

// 查询参数
const queryParams = ref({
  usageEnum: '',
  page: 1,
  limit: 10,
  startDate: '',
  endDate: '',
  type: '',
  keyword: '',
  minAmount: null,
  maxAmount: null,
  accountId: ''
});

// 账单数据
const bills = ref([]);
const totalCount = ref(0);
const loading = ref(false);

// 游标式分页：记录上一页最后一条数据的日期，用于非日期查询的翻页
const lastEndDate = ref('');

const typeList = ref({});
const recordTypeList = ref({});

// 用户账户列表
const accountList = ref([]);

const showAddModal = ref(false);

// 计算属性
// 最大页数封顶
const MAX_PAGES = 50;
const isPageCapped = ref(false);

const totalPages = computed(() => {
  const pages = Math.ceil(totalCount.value / queryParams.value.limit);
  return Math.min(pages, MAX_PAGES);
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

// 获取类型枚举列表
const fetchTypeLists = async () => {
  try {
    // 获取账单类型枚举
    const typeResponse = await axios.get('http://localhost:8080/api/bill/getTypeList');
    if (typeResponse.data.statusCode === 200) {
      typeList.value = typeResponse.data.data;
    }

    // 获取记录类型枚举
    const recordTypeResponse = await axios.get('http://localhost:8080/api/bill/getRecordType');
    if (recordTypeResponse.data.statusCode === 200) {
      recordTypeList.value = recordTypeResponse.data.data;
    }
  } catch (error) {
    console.error('获取类型枚举失败:', error);
  }
};

// 获取用户账户列表
const fetchUserAccounts = async () => {
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
      // 后端返回的是账户列表
      accountList.value = response.data.data || [];
      console.log('获取到账户列表:', accountList.value);
      
      // 如果有账户，默认选中第一个（仅在账户列表不为空时）
      if (accountList.value.length > 0) {
        queryParams.value.accountId = accountList.value[0].id;
      } else {
        console.warn('当前用户没有可用账户');
        queryParams.value.accountId = '';
      }
    } else {
      console.error('获取账户列表失败:', response.data.message);
      accountList.value = [];
    }
  } catch (error) {
    console.error('获取账户列表异常:', error);
    accountList.value = [];
  }
};



// 查询类型改变时的处理
const onSearchTypeChange = () => {
  // 重置其他查询条件
  resetQueryConditions();
  
  // 如果切换到账户查询，默认选中第一个账户
  if (queryParams.value.usageEnum === 'ACCOUNT' && accountList.value.length > 0) {
    queryParams.value.accountId = accountList.value[0].id;
  }
};

// 重置查询条件
const resetQueryConditions = () => {
  queryParams.value.startDate = '';
  queryParams.value.endDate = '';
  queryParams.value.type = '';
  queryParams.value.keyword = '';
  queryParams.value.minAmount = null;
  queryParams.value.maxAmount = null;
  queryParams.value.page = 1;
  // 如果有账户列表，保留第一个账户为默认值
  if (accountList.value.length > 0) {
    queryParams.value.accountId = accountList.value[0].id;
  } else {
    queryParams.value.accountId = '';
  }
};

// 重置所有查询
const resetQuery = () => {
  queryParams.value.usageEnum = '';
  resetQueryConditions();
  bills.value = [];
  totalCount.value = 0;
};

// 打开添加账单弹窗
const openAddModal = () => {
  showAddModal.value = true;
};

// 关闭添加账单弹窗
const closeAddModal = () => {
  showAddModal.value = false;
};

// 添加成功回调 (统一处理单次和批量)
const handleAddSuccess = () => {
  closeAddModal();
  // 如果当前有查询条件，刷新当前查询；否则重置查询显示最新数据
  if (queryParams.value.usageEnum) {
    searchBills();
  } else {
    // 默认查询最近的账单
    queryParams.value.usageEnum = 'DATE_RANGE';
    // 设置默认日期范围为当月
    const now = new Date();
    const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
    const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0);

    queryParams.value.startDate = firstDay.toISOString().split('T')[0];
    queryParams.value.endDate = lastDay.toISOString().split('T')[0];
    searchBills();
  }
};

const buildRequestBody = (token, page = 1, limit) => {
  const base = {
    token,
    startDate: queryParams.value.startDate,
    endDate: queryParams.value.endDate
  }

  switch (queryParams.value.usageEnum) {
    case 'DATE_RANGE':
      return {
        ...base,
        page: page,
        limit: limit
      }
    
    case 'ACCOUNT':
      return {
        ...base,
        usageEnum: "ACCOUNT",
        accountId: queryParams.value.accountId,
        page: page,
        limit: limit
    }

    case 'USAGE_TYPE':
      return {
        ...base,
        usageEnum: queryParams.value.type.toUpperCase(),
        type: queryParams.value.type.toUpperCase(),
        page: page,
        limit: limit
      }
    
    case 'KEYWORD':
      return {
        ...base,
        keyword: queryParams.value.keyword,
        page: page,
        limit: limit
      };
    case 'AMOUNT_RANGE':
      return {
        ...base,
        usageEnum: "AMOUNT_RANGE",
        minAmount: queryParams.value.minAmount,
        maxAmount: queryParams.value.maxAmount,
        page: page,
        limit: limit
      };
    default:
      return base;
  }
};

// 获取第一页数据
const fetchFirstPage = async (token) => {
  const limit = Number(queryParams.value.limit) || 10;
  const requestBody = buildRequestBody(token, 1, limit);

  const response = await axios.post(
    `http://localhost:8080/api/query/getBillList?searchType=${queryParams.value.usageEnum}`,
    requestBody
  );

  if (response.data.statusCode === 200) {
    const pageData = response.data.data || [];
    bills.value = pageData;

    // 记录最后一条数据的日期，用于下一页查询
    if (pageData.length > 0) {
      lastEndDate.value = pageData[pageData.length - 1].date || '';
    }

    // 使用后端返回的 total
    if (typeof response.data.total === 'number') {
      totalCount.value = response.data.total;
    } else {
      // 如果后端没返回 total，估算一个较大值以允许翻页
      totalCount.value = pageData.length < limit ? pageData.length : limit * 10;
    }
    // 应用页数封顶（按每页条数 * MAX_PAGES）
    const maxTotal = Number(queryParams.value.limit) * MAX_PAGES;
    if (totalCount.value > maxTotal) {
      totalCount.value = maxTotal;
      isPageCapped.value = true;
    } else {
      isPageCapped.value = false;
    }

    console.log(`第一页查询完成：本页 ${pageData.length} 条，总计约 ${totalCount.value} 条`);
  } else {
    alert('查询失败: ' + response.data.message);
  }
};

// 获取下一页数据（游标式分页）
const fetchNextPage = async (token) => {
  const limit = Number(queryParams.value.limit) || 10;
  
  // 对于非日期查询，使用游标式分页：将 endDate 设为上一页最后一条的日期（向前翻页）
  if (queryParams.value.usageEnum !== 'DATE_RANGE' && lastEndDate.value) {
    queryParams.value.endDate = lastEndDate.value;
  }
  
  const requestBody = buildRequestBody(token, queryParams.value.page, limit);
  response = '';
  if (queryParams.value.usageEnum === "USAGE_TYPE") {
    response = await axios.post(
      `http://localhost:8080/api/query/getBillList?searchType=USAGE_TYPE`,
      requestBody
    );
  } else {
    response = await axios.post(
    `http://localhost:8080/api/query/getBillList?searchType=${queryParams.value.usageEnum}`,
    requestBody
    );
  }

  if (response.data.statusCode === 200) {
    const pageData = response.data.data || [];
    bills.value = pageData;

    // 更新游标
    if (pageData.length > 0) {
      lastEndDate.value = pageData[pageData.length - 1].date || '';
    }

    // 更新总数
    if (typeof response.data.total === 'number') {
      totalCount.value = response.data.total;
    } else if (pageData.length > 0) {
      // 如果还有数据，继续估算总数
      const currentEstimate = queryParams.value.page * limit;
      if (pageData.length >= limit) {
        totalCount.value = Math.max(totalCount.value, currentEstimate + limit);
      }
    }
    // 应用页数封顶
    const maxTotal = Number(queryParams.value.limit) * MAX_PAGES;
    if (totalCount.value > maxTotal) {
      totalCount.value = maxTotal;
      isPageCapped.value = true;
    } else {
      isPageCapped.value = false;
    }

    console.log(`第 ${queryParams.value.page} 页查询完成：本页 ${pageData.length} 条`);
  } else {
    alert('查询失败: ' + response.data.message);
  }
};

// 获取今天的日期字符串 (YYYY-MM-DD)
const getTodayDateStr = () => {
  const now = new Date();
  return now.toISOString().split('T')[0];
};

// 获取一年前的日期字符串 (YYYY-MM-DD)
const getOneYearAgoDateStr = () => {
  const now = new Date();
  now.setFullYear(now.getFullYear() - 1);
  return now.toISOString().split('T')[0];
};

// 查询账单
const searchBills = async () => {
  if (!queryParams.value.usageEnum) {
    alert('请选择查询方式');
    return;
  }

  // 验证账户查询需要选择账户：确保非占位且在 accountList 中存在
  if (queryParams.value.usageEnum === 'ACCOUNT') {
    if (accountList.value.length === 0) {
      alert('当前没有可用账户，请先添加账户');
      return;
    }
    // const accId = String(queryParams.value.accountId || '').trim();
    // const exists = accountList.value.some(a => String(a.id) === accId);
    // if (!accId || !exists) {
    //  alert('请选择有效的账户');
    //  return;
    // }
  }

  // 重置到第一页
  queryParams.value.page = 1;
  // 重置游标
  lastEndDate.value = '';

  // 非日期查询时，设置合理的日期范围：从一年前到今天
  if (queryParams.value.usageEnum !== 'DATE_RANGE') {
    queryParams.value.startDate = getOneYearAgoDateStr(); // 下界：一年前
    queryParams.value.endDate = getTodayDateStr(); // 上界：今天
  }

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

const viewDetail = (id) => {
  // TODO: 实现详细查看逻辑
  alert(`查看账单ID: ${id} 的详情`);
};

const deleteBill = (billId) => {
  if (confirm('确定要删除这条账单吗？')) {
    fetch('http://localhost:8080/api/bill', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        token: localStorage.getItem('token'),
        id: billId
      })
    })
    .then(response => response.json())
    .then(data => {
      if (data.statusCode === 200) {
        alert('删除成功');
        searchBills(); // 刷新列表
      } else {
        alert('删除失败: ' + data.message);
      }
    })
    .catch(error => {
      console.error('Error:', error);
      alert('删除失败');
    });
  }
};

// 切换页码
const changePage = async (page) => {
  // 阻止超出允许页码（包含 MAX_PAGES）
  if (page < 1 || page > totalPages.value || page > MAX_PAGES) return;
  
  // 如果是回到第一页，重新执行查询
  if (page === 1) {
    await searchBills();
    return;
  }
  
  queryParams.value.page = page;
  loading.value = true;
  
  try {
    const token = localStorage.getItem('token');
    // 翻页时使用游标式分页
    await fetchNextPage(token);
  } catch (error) {
    console.error('翻页失败:', error);
    alert('翻页失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const getRecordTypeName = (recordEnum) => {
  const enumMap = {
    'INCOME': '收入',
    'EXPENDITURE': '支出',
    'TRANSFER': '转账'
  };
  return enumMap[recordEnum] || recordEnum;
};

onMounted(async () => {
  await fetchTypeLists();
  await fetchUserAccounts();
  // 检查路由参数，如果是从仪表盘跳转过来的添加操作，则自动打开弹窗
  if (route.query.action === 'add') {
    showAddModal.value = true;
    router.replace({ query: {} });
  }
});
</script>

<style scoped>
/* 基础布局样式 */
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
  gap: 24px; /* 增加间距 */
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

.btn-lg {
  padding: 10px 24px;
  font-size: 16px;
  box-shadow: 0 4px 10px rgba(24, 144, 255, 0.3);
}

/* 统计卡片样式 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 8px;
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
  margin-bottom: 0; /* 由gap控制间距 */
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

.query-type-selector {
  margin-bottom: 20px;
}

.query-type-selector label {
  margin-right: 10px;
  color: #666;
}

.query-type-selector select {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  min-width: 200px;
}

.condition-group {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.condition-group label {
  color: #666;
  white-space: nowrap;
}

.condition-group input,
.condition-group select {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

.pagination-settings {
  margin-bottom: 20px;
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

.query-actions {
  display: flex;
  gap: 10px;
}

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

.btn-outline {
  background-color: white;
  color: #1890ff;
  border: 1px solid #1890ff;
}

.btn-outline:hover {
  background-color: #e6f7ff;
}

.btn-small {
  padding: 4px 12px;
  font-size: 12px;
}

.btn-danger {
  background-color: #ff4d4f;
  color: white;
}

.btn-danger:hover {
  background-color: #ff7875;
}

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

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.pagination-info {
  color: #999;
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 5px;
}

.page-item {
  display: inline-block;
}

.pagination-controls .btn-small.active {
  background-color: #1890ff;
  color: white;
}

.cap-note {
  color: #fa8c16;
  font-size: 12px;
  margin-left: 8px;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  background: transparent;
  padding: 20px;
}

/* 确保弹窗内的卡片样式适配 */
.modal-content :deep(.form-card) {
  margin-bottom: 0;
  max-height: calc(90vh - 40px);
  overflow-y: auto;
}
</style>
