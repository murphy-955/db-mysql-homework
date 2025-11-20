<template>
  <div class="bill-query-page">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="nav-left">
        <h2 class="logo">来福记账本</h2>
      </div>
      <nav class="nav-center">
        <a href="/dashboard" class="nav-link">主页</a>
        <a href="/bill-query" class="nav-link active">账单查询</a>
        <a href="#" class="nav-link">添加账单</a>
        <a href="#" class="nav-link">统计分析</a>
        <a href="#" class="nav-link">账户管理</a>
      </nav>
      <div class="nav-right">
        <button class="icon-btn">👤</button>
      </div>
    </header>

    <!-- 查询区域 -->
    <div class="container">
      <div class="query-section">
        <div class="section-header">
          <h3>账单查询</h3>
        </div>

        <!-- 查询方式选择 -->
        <div class="query-type-selector">
          <label for="searchType">查询方式：</label>
          <select id="searchType" v-model="queryParams.searchType" @change="onSearchTypeChange">
            <option value="">请选择查询方式</option>
            <option value="DATE">日期查询</option>
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
              <td :class="bill.recordEnum === 'income' ? 'income-amount' : 'expenditure-amount'">
                {{ bill.recordEnum === 'income' ? '+' : '-' }}{{ bill.amount.toFixed(2) }}
              </td>
              <td>{{ bill.date }}</td>
              <td>
                <button class="btn btn-small" @click="viewDetail(bill.id)">详情</button>
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
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'BillQueryPage',
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
      recordTypeList: {}
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.totalCount / this.queryParams.limit);
    }
  },
  async mounted() {
    // 获取类型枚举数据
    await this.fetchTypeLists();
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

    // 切换页码
    changePage(page) {
      this.queryParams.page = page;
      this.searchBills();
    },

    // 获取记录类型名称
    getRecordTypeName(recordEnum) {
      const enumMap = {
        'income': '收入',
        'expenditure': '支出',
        'transfer': '转账'
      };
      return enumMap[recordEnum] || recordEnum;
    },

    // 提供模拟数据用于展示
    provideMockData() {
      this.bills = [
        {
          id: 1,
          recordEnum: 'income',
          amount: 1500,
          date: '2024-01-15',
          account: '支付宝',
          type: 'SALARY',
          remarks: '工资收入'
        },
        {
          id: 2,
          recordEnum: 'expenditure',
          amount: 200,
          date: '2024-01-16',
          account: '微信',
          type: 'FOOD',
          remarks: '午餐'
        },
        {
          id: 3,
          recordEnum: 'expenditure',
          amount: 150,
          date: '2024-01-16',
          account: '支付宝',
          type: 'TRANSPORTATION',
          remarks: '打车费'
        },
        {
          id: 4,
          recordEnum: 'income',
          amount: 500,
          date: '2024-01-17',
          account: '银行卡',
          type: 'BONUS',
          remarks: '项目奖金'
        },
        {
          id: 5,
          recordEnum: 'expenditure',
          amount: 300,
          date: '2024-01-18',
          account: '微信',
          type: 'SHOPPING',
          remarks: '日用品'
        }
      ];
      this.totalCount = 5;
    }
  }
};
</script>

<style scoped>
/* 基础样式 */
.bill-query-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  height: 60px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #1890ff;
}

.nav-center {
  display: flex;
  gap: 20px;
}

.nav-link {
  text-decoration: none;
  color: #666;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s;
}

.nav-link:hover {
  color: #1890ff;
}

.nav-link.active {
  color: #1890ff;
  background-color: #e6f7ff;
}

.nav-right {
  display: flex;
  gap: 10px;
}

.icon-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  padding: 5px;
}

.container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 30px;
  }



  .modal-content {
    position: relative;
    background-color: white;
    border-radius: 8px;
    padding: 0;
    width: 600px;
    max-height: 70vh;
    overflow-y: auto;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    z-index: 1001;
  }

.query-section,
.results-section {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
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

.btn-view {
  background-color: #52c41a;
  color: white;
}

.btn-view:hover {
  background-color: #73d13d;
}

.loading,
.no-data {
  text-align: center;
  padding: 40px;
  color: #999;
}

/* 表格样式优化 */
.results-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.table-responsive {
  overflow-x: auto;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}

.bills-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
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
  position: sticky;
  top: 0;
  z-index: 10;
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

.remarks-column {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 记录类型标签 */
.record-type-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.record-type-tag.income-tag {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.record-type-tag.expenditure-tag {
  background-color: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

.record-type-tag.transfer-tag {
  background-color: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

/* 分页组件样式 */
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

.page-jump {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-left: 10px;
  font-size: 14px;
}

.page-input {
  width: 50px;
  padding: 4px;
  text-align: center;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

/* 模态框样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.modal-header h4 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
  width: 30px;
  height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
  transition: all 0.3s;
}

.close-btn:hover {
  background-color: #f0f0f0;
  color: #333;
}

.modal-body {
  padding: 20px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.detail-item.full-width {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.detail-item label {
  width: 80px;
  color: #666;
  font-weight: 500;
  margin-right: 10px;
  flex-shrink: 0;
}

.detail-item span {
  color: #333;
  flex: 1;
}

.remarks-content {
  color: #333;
  margin-top: 5px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 15px 20px;
  border-top: 1px solid #e8e8e8;
  background-color: #fafafa;
}
</style>
