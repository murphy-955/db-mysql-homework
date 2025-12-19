// Mock数据拦截器 - 用于本地测试
import axios from 'axios';

// 加载mock数据
let mockBills = [];
let mockAccounts = [];
let mockEnums = {};
// token -> accountId[] 映射，用于模拟不同用户有不同账户
const tokenAccountMap = {};

// 初始化加载mock数据
const initMockData = async () => {
  try {
    const [billsResp, accountsResp, enumsResp] = await Promise.all([
      fetch('/mock/bills.json').then(r => r.json()),
      fetch('/mock/accounts.json').then(r => r.json()),
      fetch('/mock/enums.json').then(r => r.json())
    ]);
    mockBills = billsResp || [];
    mockAccounts = accountsResp || [];
    mockEnums = enumsResp || {};
    console.log('Mock数据加载成功:', { bills: mockBills.length, accounts: mockAccounts.length });
  } catch (error) {
    console.error('加载mock数据失败:', error);
  }
  // 初始化一个默认的映射示例（基于 accounts id）
  try {
    const ids = (mockAccounts || []).map(a => a.id);
    // 简单示例：第一个账号分配给 user1，第二个给 user2，admin 拥有全部
    if (ids.length > 0) tokenAccountMap['token-user1'] = [ids[0]];
    if (ids.length > 1) tokenAccountMap['token-user2'] = [ids[1]];
    if (ids.length > 0) tokenAccountMap['token-admin'] = ids.slice();
  } catch (e) {
    // ignore
  }
};

// 过滤账单的辅助函数
const filterBills = (bills, params) => {
  let filtered = bills.slice();
  
  // 日期范围过滤
  if (params.startDate) {
    filtered = filtered.filter(b => b.date >= params.startDate);
  }
  if (params.endDate) {
    filtered = filtered.filter(b => b.date <= params.endDate);
  }
  
  // 账户过滤
  if (params.accountId) {
    filtered = filtered.filter(b => b.accountId === params.accountId);
  }
  
  // 类型过滤
  if (params.type) {
    filtered = filtered.filter(b => (b.type || '').toUpperCase() === params.type.toUpperCase());
  }
  
  // 关键字过滤
  if (params.keyword) {
    const kw = params.keyword.toLowerCase();
    filtered = filtered.filter(b => 
      (b.description || '').toLowerCase().includes(kw) ||
      (b.type || '').toLowerCase().includes(kw) ||
      String(b.id).includes(kw)
    );
  }
  
  // 金额范围过滤
  if (params.minAmount != null) {
    filtered = filtered.filter(b => b.amount >= params.minAmount);
  }
  if (params.maxAmount != null) {
    filtered = filtered.filter(b => b.amount <= params.maxAmount);
  }
  
  // 按日期倒序排序
  filtered.sort((a, b) => new Date(b.date) - new Date(a.date));
  
  return filtered;
};

// 获取Mock响应数据
const getMockResponse = (url, data) => {
  console.log('🔄 处理Mock请求:', url, data);
  
  // 获取类型枚举
  if (url.includes('/api/bill/getTypeList')) {
    return {
      statusCode: 200,
      message: '获取成功',
      data: mockEnums.typeList || {}
    };
  }
  
  // 获取记录类型枚举
  if (url.includes('/api/bill/getRecordType')) {
    return {
      statusCode: 200,
      message: '获取成功',
      data: mockEnums.recordTypeList || {}
    };
  }
  
  // 获取用户账户列表
  if (url.includes('/api/user/getUserAccount')) {
    const token = data.token;
    if (token && tokenAccountMap[token]) {
      const accIds = tokenAccountMap[token] || [];
      const accounts = mockAccounts.filter(a => accIds.includes(a.id));
      return {
        statusCode: 200,
        message: '获取成功',
        data: accounts
      };
    }
    return {
      statusCode: 200,
      message: '获取成功',
      data: mockAccounts
    };
  }

  // 模拟登录
  if (url.includes('/api/user/login')) {
    const username = String(data.username || '').trim();
    const token = `token-${username || 'guest'}`;
    
    if (username === 'user1') {
      tokenAccountMap[token] = mockAccounts.length > 0 ? [mockAccounts[0].id] : [];
    } else if (username === 'user2') {
      tokenAccountMap[token] = mockAccounts.length > 1 ? [mockAccounts[1].id] : [];
    } else if (username === 'admin') {
      tokenAccountMap[token] = mockAccounts.map(a => a.id);
    } else {
      tokenAccountMap[token] = mockAccounts.map(a => a.id);
    }

    return {
      statusCode: 200,
      message: '登录成功',
      data: { token }
    };
  }
  
  // 获取报表统计数据
  if (url.includes('/api/query/getReport')) {
    const filtered = filterBills(mockBills, {
      startDate: data.startDate,
      endDate: data.endDate
    });
    
    let totalExpenditure = 0;
    let totalIncome = 0;
    
    filtered.forEach(bill => {
      if (bill.recordEnum === 'EXPENDITURE') {
        totalExpenditure += bill.amount;
      } else if (bill.recordEnum === 'INCOME') {
        totalIncome += bill.amount;
      }
    });
    
    console.log(`✅ Mock getReport: 收入${totalIncome}, 支出${totalExpenditure}`);
    
    return {
      statusCode: 200,
      message: '成功',
      generalExpenditure: -totalExpenditure,
      generalIncome: totalIncome,
      generalBalance: totalIncome - totalExpenditure,
      data: []
    };
  }
  
  // 查询账单列表
  if (url.includes('/api/query/getBillList')) {
    const searchType = new URL(url, 'http://dummy').searchParams.get('searchType');
    const page = parseInt(data.page || 1);
    const limit = parseInt(data.limit || 10);
    
    const filtered = filterBills(mockBills, {
      startDate: data.startDate,
      endDate: data.endDate,
      accountId: data.accountId,
      type: data.type,
      keyword: data.keyword,
      minAmount: data.minAmount,
      maxAmount: data.maxAmount
    });
    
    const start = (page - 1) * limit;
    const pageData = filtered.slice(start, start + limit);
    
    console.log(`✅ Mock返回: 第${page}页, ${pageData.length}/${filtered.length}条`);
    
    return {
      data: pageData,
      message: '成功',
      statusCode: 200
    };
  }
  
  // 获取账单详情
  if (url.includes('/api/bill/getBillDetail')) {
    const bill = mockBills.find(b => b.id === data.id);
    return {
      statusCode: 200,
      message: '获取成功',
      data: bill || null
    };
  }
  
  // 删除账单
  if (url.includes('/api/bill') && data.id) {
    const index = mockBills.findIndex(b => b.id === data.id);
    if (index > -1) {
      mockBills.splice(index, 1);
    }
    return {
      statusCode: 200,
      message: '删除成功',
      data: null
    };
  }
  
  // 添加账单
  if (url.includes('/api/bill/addBill')) {
    const newBill = {
      id: Math.max(...mockBills.map(b => b.id), 0) + 1,
      recordEnum: data.recordEnum,
      amount: data.amount,
      accountId: data.account,
      type: data.type,
      date: data.date,
      description: data.remarks || ''
    };
    mockBills.unshift(newBill);
    return {
      statusCode: 200,
      message: '添加成功',
      data: newBill
    };
  }
  
  // 批量添加账单
  if (url.includes('/api/bill/addBillList')) {
    const newBills = (data.data || []).map((item, index) => ({
      id: Math.max(...mockBills.map(b => b.id), 0) + index + 1,
      recordEnum: item.recordEnum,
      amount: item.amount,
      accountId: item.account,
      type: item.type,
      date: item.date,
      description: item.remarks || ''
    }));
    mockBills.unshift(...newBills);
    return {
      statusCode: 200,
      message: '批量添加成功',
      data: null
    };
  }
  
  // 默认返回
  console.warn('⚠️ 未匹配的mock接口:', url);
  return {
    statusCode: 200,
    message: 'Mock响应',
    data: null
  };
};

// 安装mock拦截器
export const setupMockInterceptor = async () => {
  await initMockData();
  
  // 请求拦截器 - 直接返回mock数据
  axios.interceptors.request.use(
    config => {
      const url = config.url || '';
      
      // 拦截所有以 /api 开头的接口请求（开发模式下使用mock）
      if (url.startsWith('/api')) {
        console.log('🔄 Mock拦截请求:', url);
        
        // 使用adapter直接返回mock数据
        config.adapter = async (cfg) => {
          const data = cfg.data ? JSON.parse(cfg.data) : {};
          return Promise.resolve({
            data: getMockResponse(url, data),
            status: 200,
            statusText: 'OK',
            headers: {},
            config: cfg
          });
        };
      }
      
      return config;
    },
    error => Promise.reject(error)
  );
  
  console.log('✅ Mock拦截器已启用');
};
