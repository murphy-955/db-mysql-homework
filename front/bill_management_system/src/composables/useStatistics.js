import { ref } from 'vue';
import axios from 'axios';

/**
 * 统计数据的可复用组合式函数
 * 提供数据获取、处理和计算的核心逻辑
 */
export function useStatistics() {
  // ==================== 响应式数据 ====================
  const loading = ref(false);
  const BILL_LIMIT = 100;

  const statistics = ref({
    totalExpense: 0,
    totalIncome: 0,
    balance: 0,
    expenseTrend: 0,
    incomeTrend: 0,
    expenseCount: 0,
    incomeCount: 0,
    totalCount: 0,
    countTruncated: false
  });
  const expenseCategories = ref([]);
  const accountsData = ref([]);
  const allBills = ref([]);

  // ==================== 枚举和常量 ====================
  const CATEGORY_COLORS = {
    'FOOD': '#d9534f',
    'SHOPPING': '#5bc0de',
    'TRANSPORTATION': '#5cb85c',
    'HOUSING': '#f0ad4e',
    'HEALTH': '#337ab7',
    'FINANCE': '#9c27b0',
    'INSURANCE': '#ff9800',
    'CLOTHING': '#e91e63',
    'ELECTRONIC': '#00bcd4',
    'FURNITURE': '#795548',
    'HOUSEHOLD': '#607d8b',
    'OTHERS': '#9e9e9e'
  };

  const TYPE_ENUM = {
    "FOOD": "食品",
    "SHOPPING": "购物",
    "TRANSPORTATION": "交通",
    "HOUSING": "住房",
    "HEALTH": "健康",
    "FINANCE": "金融",
    "INSURANCE": "保险",
    "CLOTHING": "服装",
    "ELECTRONIC": "电子",
    "FURNITURE": "家具",
    "HOUSEHOLD": "家庭",
    "OTHERS": "其他"
  };

  // ==================== 辅助函数 ====================

  /**
   * 计算日期范围
   * @param {string} rangeType - 日期范围类型
   * @returns {Object} { startDate, endDate }
   */
  const getDateRange = (rangeType) => {
    const start = new Date();
    const end = new Date();

    switch(rangeType) {
      case 'week':
        start.setDate(end.getDate() - 7);
        break;
      case 'month':
        start.setDate(end.getDate() - 30);
        break;
      case 'quarter':
        start.setDate(end.getDate() - 90);
        break;
      case 'halfYear':
        start.setDate(end.getDate() - 180);
        break;
      case 'year':
        start.setDate(end.getDate() - 365);
        break;
      default:
        console.warn('未知的日期范围类型:', rangeType);
        return { startDate: null, endDate: null };
    }

    const formatDate = (date) => {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    };

    return { startDate: formatDate(start), endDate: formatDate(end) };
  };

  /**
   * 计算趋势百分比
   * @param {string} token - 用户 token
   * @param {string} queryStartDate - 查询起始日期
   * @param {string} queryEndDate - 查询结束日期
   * @param {number} currentExpense - 当前期支出
   * @param {number} currentIncome - 当前期收入
   * @returns {Promise<Object>} { expenseTrend, incomeTrend }
   */
  const getTrends = async (token, queryStartDate, queryEndDate, currentExpense, currentIncome) => {
    const periodDays = Math.ceil(
      (new Date(queryEndDate) - new Date(queryStartDate)) / (1000 * 60 * 60 * 24)
    );
    const prevEndDate = new Date(queryStartDate);
    prevEndDate.setDate(prevEndDate.getDate() - 1);
    const prevStartDate = new Date(prevEndDate);
    prevStartDate.setDate(prevStartDate.getDate() - periodDays);

    try {
      const prevResponse = await axios.post('/api/query/getReport', {
        token,
        startDate: prevStartDate.toISOString().split('T')[0],
        endDate: prevEndDate.toISOString().split('T')[0]
      });

      if (prevResponse.data.statusCode === 200) {
        const prevExpense = Math.abs(prevResponse.data.generalExpenditure || 0);
        const prevIncome = prevResponse.data.generalIncome || 0;

        const expenseTrend = prevExpense > 0 ? Number((((currentExpense - prevExpense) / prevExpense) * 100).toFixed(1)) : 0;
        const incomeTrend = prevIncome > 0 ? Number((((currentIncome - prevIncome) / prevIncome) * 100).toFixed(1)) : 0;

        return { expenseTrend, incomeTrend };
      }
    } catch (error) {
      console.warn('获取上期数据失败，趋势对比将不可用:', error);
    }
    return { expenseTrend: 0, incomeTrend: 0 };
  };

  /**
   * 处理支出分类数据
   * @param {Object} reportData - 报表数据
   * @returns {Array} 支出分类数组
   */
  const getExpenseCategories = (reportData) => {
    if (reportData.data && Array.isArray(reportData.data)) {
      const expenseData = reportData.data.filter(item =>
        item.expenditure && item.expenditure > 0
      );

      const totalExpense = expenseData.reduce((sum, item) =>
        sum + (item.expenditure || 0), 0
      );

      const categories = expenseData.map(item => ({
        name: TYPE_ENUM[item.type] || item.type || '其他',
        amount: item.expenditure,
        percentage: totalExpense > 0 ?
          Number(((item.expenditure / totalExpense) * 100).toFixed(1)) : 0,
        color: CATEGORY_COLORS[item.type] || '#9e9e9e'
      }));

      categories.sort((a, b) => b.amount - a.amount);
      return categories;
    }
    return [];
  };

  /**
   * 获取账单数据（分页循环获取所有账单）
   * @param {string} token - 用户 token
   * @param {string} queryStartDate - 查询起始日期
   * @param {string} queryEndDate - 查询结束日期
   * @returns {Promise<Object>} 账单统计数据
   */
  const getBillNum = async (token, queryStartDate, queryEndDate) => {
    try {
      const limit = BILL_LIMIT;
      const page = 1;
      const billsResponse = await axios.post(
        `/api/query/getBillList?searchType=DATE_RANGE&page=${page}&limit=${limit}`,
        {
          token,
          page,
          limit,
          startDate: queryStartDate,
          endDate: queryEndDate
        }
      );

      if (billsResponse.data.statusCode !== 200 || !Array.isArray(billsResponse.data.data)) {
        return { expenseCount: 0, incomeCount: 0, totalCount: 0, accountsData: [], bills: [], countTruncated: false };
      }

      let bills = billsResponse.data.data;
      // 规范 recordEnum 为大写，避免不同组件对大小写判断不一致
      bills = bills.map(b => ({
        ...b,
        recordEnum: (b.recordEnum || '').toString().toUpperCase()
      }));
      const expenseCount = bills.filter(bill => bill.recordEnum === 'EXPENDITURE').length;
      const incomeCount = bills.filter(bill => bill.recordEnum === 'INCOME').length;
      const totalCount = bills.length;

      const countTruncated = bills.length >= limit;

      const accountMap = new Map();
      bills.forEach(bill => {
        const accountId = bill.account || 'unknown';
        if (!accountMap.has(accountId)) {
          accountMap.set(accountId, {
            name: accountId,
            totalExpense: 0,
            expenseCount: 0,
            totalIncome: 0,
            incomeCount: 0,
            totalCount: 0
          });
        }
        const account = accountMap.get(accountId);
        account.totalCount++;
        if (bill.recordEnum === 'EXPENDITURE') {
          account.totalExpense += (bill.amount || 0);
          account.expenseCount++;
        } else if (bill.recordEnum === 'INCOME') {
          account.totalIncome += (bill.amount || 0);
          account.incomeCount++;
        }
      });

      const accountsDataResult = Array.from(accountMap.values()).sort((a, b) => b.totalCount - a.totalCount);

      return { expenseCount, incomeCount, totalCount, accountsData: accountsDataResult, bills, countTruncated };
    } catch (error) {
      console.error('获取账单列表失败:', error);
      return { expenseCount: 0, incomeCount: 0, totalCount: 0, accountsData: [], bills: [], countTruncated: false };
    }
  };

  /**
   * 加载统计数据（核心方法）
   * @param {string} dateRangeType - 日期范围类型（如 'month', 'week'）
   * @param {string} customStartDate - 自定义开始日期（可选）
   * @param {string} customEndDate - 自定义结束日期（可选）
   */
  const loadStatistics = async (dateRangeType = 'month', customStartDate = null, customEndDate = null) => {
    try {
      loading.value = true;

      // 确定查询的日期范围
      let queryStartDate = '', queryEndDate = '';

      if (customStartDate && customEndDate) {
        queryStartDate = customStartDate;
        queryEndDate = customEndDate;
      } else {
        const dates = getDateRange(dateRangeType);
        if (!dates || !dates.startDate) {
          loading.value = false;
          return;
        }
        queryStartDate = dates.startDate;
        queryEndDate = dates.endDate;
      }

      // 获取用户token
      const token = localStorage.getItem('token');
      if (!token) {
        console.error('未找到用户token');
        loading.value = false;
        return;
      }

      // 调用 getReport 接口
      const response = await axios.post('/api/query/getReport', {
        token,
        startDate: queryStartDate,
        endDate: queryEndDate
      });

      if (response.data.statusCode !== 200) {
        console.error('获取统计数据失败:', response.data.message);
        loading.value = false;
        return;
      }

      const reportData = response.data;

      // 填充概览卡片数据
      statistics.value.totalExpense = Math.abs(reportData.generalExpenditure || 0);
      statistics.value.totalIncome = reportData.generalIncome || 0;
      statistics.value.balance = reportData.generalBalance || 0;

      // 计算趋势
      const trends = await getTrends(token, queryStartDate, queryEndDate, statistics.value.totalExpense, statistics.value.totalIncome);
      statistics.value.expenseTrend = trends.expenseTrend;
      statistics.value.incomeTrend = trends.incomeTrend;

      // 处理支出分类数据
      expenseCategories.value = getExpenseCategories(reportData);

      // 获取账单统计和账户数据
      const billStats = await getBillNum(token, queryStartDate, queryEndDate);
      statistics.value.expenseCount = billStats.expenseCount;
      statistics.value.incomeCount = billStats.incomeCount;
      statistics.value.totalCount = billStats.totalCount;
      statistics.value.countTruncated = billStats.countTruncated;
      accountsData.value = billStats.accountsData;
      allBills.value = billStats.bills;

    } catch (error) {
      console.error('加载统计数据失败:', error);
    } finally {
      loading.value = false;
    }
  };

  // 返回所有需要的数据和方法
  return {
    // 响应式数据
    loading,
    statistics,
    expenseCategories,
    accountsData,
    allBills,

    // 常量
    CATEGORY_COLORS,
    TYPE_ENUM,
    BILL_LIMIT,

    // 方法
    getDateRange,
    getTrends,
    getExpenseCategories,
    getBillNum,
    loadStatistics
  };
}
