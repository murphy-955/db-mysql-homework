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
            <label for="searchType">查询方式：</label>
            <select id="searchType" v-model="queryParams.searchType" @change="onSearchTypeChange">
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
            <div v-if="queryParams.searchType === 'DATE'" class="condition-group">
              <label>开始日期：</label>
              <input type="date" v-model="queryParams.startDate" />
              <label>结束日期：</label>
              <input type="date" v-model="queryParams.endDate" />
            </div>

            <!-- 类型查询 -->
            <div v-if="queryParams.searchType === 'USAGE_TYPE'" class="condition-group">
              <label>账单类型：</label>
              <select v-model="queryParams.type">
                <option value="">请选择类型</option>
                <option v-for="(label, value) in typeList" :key="value" :value="value">{{ label }}</option>
              </select>
            </div>

            <!-- 关键字查询 -->
            <div v-if="queryParams.searchType === 'KEYWORD'" class="condition-group">
              <label>关键字：</label>
              <input type="text" v-model="queryParams.keyword" placeholder="请输入关键字" />
            </div>

            <!-- 金额范围查询 -->
            <div v-if="queryParams.searchType === 'AMOUNT_RANGE'" class="condition-group">
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
            <!-- 临时测试按钮（已注释）
            <button class="btn btn-secondary" @click="generateMockAccountData">生成 accountId 假数据</button>
            -->
          </div>
        </div>

        <!-- 结果展示区域 -->
        <div class="results-section">
          <div class="section-header">
            <h3>查询结果</h3>
            <span class="result-count">共 {{ totalCount }} 条记录</span>
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
            <button class="btn btn-small" :disabled="queryParams.page >= totalPages" @click="changePage(queryParams.page + 1)">下一页</button>
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

<script>
import axios from 'axios';
import BillAddWindow from './BillAddWindow.vue';
import Sidebar from './Sidebar.vue';

export default {
  name: 'BillQueryPage',
  components: {
    BillAddWindow,
    Sidebar
  },
  data() {
    return {
      // 查询参数
      queryParams: {
        searchType: '',
        page: 1,
        limit: 10,
        startDate: '',
        endDate: '',
        type: '',
        keyword: '',
        minAmount: null,
        maxAmount: null,
        accountId: ''
      },
      // 账单数据
      bills: [],
      totalCount: 0,
      loading: false,
      // 类型枚举数据
      typeList: {},
      recordTypeList: {},
      // 弹窗控制
      showAddModal: false
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.totalCount / this.queryParams.limit);
    },
    // 新增统计计算属性
    currentIncome() {
      return this.bills
        .filter(b => b.recordEnum === 'INCOME')
        .reduce((sum, b) => sum + b.amount, 0);
    },
    currentExpenditure() {
      return this.bills
        .filter(b => b.recordEnum === 'EXPENDITURE')
        .reduce((sum, b) => sum + b.amount, 0);
    },
    currentBalance() {
      return this.currentIncome - this.currentExpenditure;
    }
  },
  async mounted() {
    // 获取类型枚举数据
    await this.fetchTypeLists();
    
    // 获取用户accountId
    await this.fetchUserAccountId();

    // 检查路由参数，如果是从仪表盘跳转过来的添加操作，则自动打开弹窗
    if (this.$route.query.action === 'add') {
      this.showAddModal = true;
      this.$router.replace({ query: {} });
    }
  },
  methods: {
    // 获取类型枚举列表
    async fetchTypeLists() {
      try {
        // 获取账单类型枚举
        const typeResponse = await axios.get('http://localhost:8080/api/bill/getTypeList');
        if (typeResponse.data.statusCode === 200) {
          this.typeList = typeResponse.data.data;
        }

        // 获取记录类型枚举
        const recordTypeResponse = await axios.get('http://localhost:8080/api/bill/getRecordType');
        if (recordTypeResponse.data.statusCode === 200) {
          this.recordTypeList = recordTypeResponse.data.data;
        }
      } catch (error) {
        console.error('获取类型枚举失败:', error);
      }
    },

    // 获取用户accountId
    async fetchUserAccountId() {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          console.error('未找到token，无法获取accountId');
          return;
        }

        const response = await axios.post('http://localhost:8080/api/user/getUserAccount', {
          token
        });

        if (response.data.statusCode === 200) {
          this.queryParams.accountId = response.data.data.accountId || '';
          console.log('获取到accountId:', this.queryParams.accountId);
        } else {
          console.error('获取accountId失败:', response.data.message);
        }
      } catch (error) {
        console.error('获取accountId异常:', error);
      }
    },

    // 查询类型改变时的处理
    onSearchTypeChange() {
      // 重置其他查询条件
      this.resetQueryConditions();
    },

    // 重置查询条件
    resetQueryConditions() {
      this.queryParams.startDate = '';
      this.queryParams.endDate = '';
      this.queryParams.type = '';
      this.queryParams.keyword = '';
      this.queryParams.minAmount = null;
      this.queryParams.maxAmount = null;
      this.queryParams.accountId = '';
      this.queryParams.page = 1;
    },

    // 重置所有查询
    resetQuery() {
      this.queryParams.searchType = '';
      this.resetQueryConditions();
      this.bills = [];
      this.totalCount = 0;
    },

    // 打开添加账单弹窗
    openAddModal() {
      this.showAddModal = true;
    },

    // 关闭添加账单弹窗
    closeAddModal() {
      this.showAddModal = false;
    },

    // 添加成功回调 (统一处理单次和批量)
    handleAddSuccess() {
      this.closeAddModal();
      // 如果当前有查询条件，刷新当前查询；否则重置查询显示最新数据
      if (this.queryParams.searchType) {
        this.searchBills();
      } else {
        // 默认查询最近的账单
        this.queryParams.searchType = 'DATE_RANGE';
        // 设置默认日期范围为当月
        const now = new Date();
        const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
        const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0);

        this.queryParams.startDate = firstDay.toISOString().split('T')[0];
        this.queryParams.endDate = lastDay.toISOString().split('T')[0];
        this.searchBills();
      }
    },

    // 查询账单
    async searchBills() {
      if (!this.queryParams.searchType) {
        alert('请选择查询方式');
        return;
      }

      this.loading = true;
      try {
        const token = localStorage.getItem('token') || 'mock_token';
        const requestBody = {
          token,
          ...this.queryParams
        };

        const response = await axios.post(
          `http://localhost:8080/api/query/getBillList?searchType=${this.queryParams.searchType}`,
          requestBody
        );

        if (response.data.statusCode === 200) {
          this.bills = response.data.data || [];
          // 模拟总条数（实际应该从接口返回）
          this.totalCount = response.data.data ? response.data.data.length : 0;
        } else {
          alert('查询失败: ' + response.data.message);
        }
      } catch (error) {
        console.error('查询账单失败:', error);
        // 提供模拟数据用于展示
        this.provideMockData();
      } finally {
        this.loading = false;
      }
    },

    // 查看详情
    viewDetail(id) {
      // 这里可以实现查看详情的逻辑，暂时用alert提示
      alert(`查看账单ID: ${id} 的详情`);
    },

    // 删除账单
    deleteBill(billId) {
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
            this.searchBills(); // 刷新列表
          } else {
            alert('删除失败: ' + data.message);
          }
        })
        .catch(error => {
          console.error('Error:', error);
          alert('删除失败');
        });
      }
    },

    // 切换页码
    changePage(page) {
      this.queryParams.page = page;
      this.searchBills();
    },

    // 获取记录类型名称
    getRecordTypeName(recordEnum) {
      const enumMap = {
        'INCOME': '收入',
        'EXPENDITURE': '支出',
        'TRANSFER': '转账'
      };
      return enumMap[recordEnum] || recordEnum;
    }
  }
};
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
