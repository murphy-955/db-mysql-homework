<template>
  <div class="bill-add-page">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="nav-left">
        <h2 class="logo">来福记账本</h2>
      </div>
      <nav class="nav-center">
        <a href="/" class="nav-link">主页</a>
        <a href="/bill-query" class="nav-link">账单查询</a>
        <a href="/bill-add" class="nav-link active">添加账单</a>
        <a href="/statistics" class="nav-link">统计分析</a>
        <a href="#" class="nav-link">计划</a>
        <a href="#" class="nav-link">帮助</a>
        <a href="#" class="nav-link">邀请</a>
        <a href="#" class="nav-link">账户</a>
      </nav>
      <div class="nav-right">
        <button class="icon-btn">🌐</button>
        <button class="icon-btn">🌙</button>
        <button class="icon-btn">👤</button>
      </div>
    </header>

    <!-- 主内容区域 -->
    <div class="container">
      <div class="form-card">
        <h3 class="form-title">添加账单记录</h3>
        
        <form @submit.prevent="submitBill" class="bill-form">
          <!-- 金额输入 -->
          <div class="form-group">
            <label class="form-label" for="amount">金额</label>
            <div class="amount-input-wrapper">
              <span class="currency-symbol">¥</span>
              <input 
                type="number" 
                id="amount" 
                v-model.number="billForm.amount" 
                class="form-input" 
                placeholder="请输入金额" 
                step="0.01" 
                min="0.01" 
                required
              >
            </div>
            <span v-if="errors.amount" class="error-message">{{ errors.amount }}</span>
          </div>

          <!-- 类型选择 -->
          <div class="form-group">
            <label class="form-label">类型</label>
            <div class="type-selector">
              <label class="type-option">
                <input type="radio" v-model="billForm.type" value="支出" required>
                <span class="type-text">支出</span>
              </label>
              <label class="type-option">
                <input type="radio" v-model="billForm.type" value="收入" required>
                <span class="type-text">收入</span>
              </label>
            </div>
          </div>

          <!-- 分类选择 -->
          <div class="form-group">
            <label class="form-label" for="category">分类</label>
            <select 
              id="category" 
              v-model="billForm.category" 
              class="form-select" 
              required
            >
              <option value="" disabled>请选择分类</option>
              <template v-if="billForm.type === '支出'">
                <option value="餐饮">餐饮</option>
                <option value="交通">交通</option>
                <option value="购物">购物</option>
                <option value="娱乐">娱乐</option>
                <option value="医疗">医疗</option>
                <option value="教育">教育</option>
                <option value="居住">居住</option>
                <option value="其他">其他</option>
              </template>
              <template v-else>
                <option value="工资">工资</option>
                <option value="奖金">奖金</option>
                <option value="投资">投资</option>
                <option value="兼职">兼职</option>
                <option value="礼金">礼金</option>
                <option value="其他">其他</option>
              </template>
            </select>
            <span v-if="errors.category" class="error-message">{{ errors.category }}</span>
          </div>

          <!-- 日期选择 -->
          <div class="form-group">
            <label class="form-label" for="date">日期</label>
            <input 
              type="date" 
              id="date" 
              v-model="billForm.date" 
              class="form-input" 
              max="{{ today }}" 
              required
            >
            <span v-if="errors.date" class="error-message">{{ errors.date }}</span>
          </div>

          <!-- 账户选择 -->
          <div class="form-group">
            <label class="form-label" for="account">账户</label>
            <select 
              id="account" 
              v-model="billForm.account" 
              class="form-select" 
              required
            >
              <option value="" disabled>请选择账户</option>
              <option value="现金">现金</option>
              <option value="银行卡">银行卡</option>
              <option value="支付宝">支付宝</option>
              <option value="微信">微信</option>
              <option value="其他">其他</option>
            </select>
          </div>

          <!-- 备注输入 -->
          <div class="form-group">
            <label class="form-label" for="remark">备注</label>
            <textarea 
              id="remark" 
              v-model="billForm.remark" 
              class="form-textarea" 
              placeholder="请输入备注信息（选填）"
              rows="3"
              maxlength="200"
            ></textarea>
            <span class="char-count">{{ billForm.remark.length }}/200</span>
          </div>

          <!-- 提交按钮 -->
          <div class="form-actions">
            <button type="button" class="btn btn-cancel" @click="resetForm">重置</button>
            <button type="submit" class="btn btn-primary" :disabled="loading">{{ loading ? '提交中...' : '提交' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 提示弹窗 -->
    <div v-if="showMessage" class="message-modal" :class="messageType">
      <div class="message-content">
        <span class="message-icon">{{ messageIcon }}</span>
        <p>{{ messageText }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'BillAddPage',
  data() {
    return {
      billForm: {
        amount: '',
        type: '支出',
        category: '',
        date: this.getTodayDate(),
        account: '现金',
        remark: ''
      },
      errors: {},
      loading: false,
      showMessage: false,
      messageText: '',
      messageType: 'success' // success, error
    };
  },
  computed: {
    today() {
      return this.getTodayDate();
    },
    messageIcon() {
      return this.messageType === 'success' ? '✓' : '✗';
    }
  },
  methods: {
    getTodayDate() {
      const today = new Date();
      return today.toISOString().split('T')[0];
    },
    validateForm() {
      this.errors = {};
      
      // 验证金额
      if (!this.billForm.amount || this.billForm.amount <= 0) {
        this.errors.amount = '请输入有效的金额';
      }
      
      // 验证分类
      if (!this.billForm.category) {
        this.errors.category = '请选择分类';
      }
      
      // 验证日期
      if (!this.billForm.date) {
        this.errors.date = '请选择日期';
      }
      
      return Object.keys(this.errors).length === 0;
    },
    async submitBill() {
      if (!this.validateForm()) {
        return;
      }
      
      this.loading = true;
      
      try {
        // 这里使用模拟数据，实际项目中应调用真实API
        const response = await axios.post('/api/bill/add', {
          amount: this.billForm.amount,
          type: this.billForm.type,
          category: this.billForm.category,
          date: this.billForm.date,
          account: this.billForm.account,
          remark: this.billForm.remark
        });
        
        // 模拟成功响应
        // 实际项目中应该检查response.data.statusCode === 200
        this.showMessageModal('success', '账单添加成功！');
        this.resetForm();
      } catch (error) {
        console.error('添加账单失败:', error);
        this.showMessageModal('error', '添加失败，请重试');
      } finally {
        this.loading = false;
      }
    },
    resetForm() {
      this.billForm = {
        amount: '',
        type: '支出',
        category: '',
        date: this.getTodayDate(),
        account: '现金',
        remark: ''
      };
      this.errors = {};
    },
    showMessageModal(type, text) {
      this.messageType = type;
      this.messageText = text;
      this.showMessage = true;
      
      setTimeout(() => {
        this.showMessage = false;
      }, 3000);
    }
  }
};
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  --bgcolor: rgba(0, 81, 255, 0.47);
  --primary-color: #d9534f;
  --secondary-color: #5bc0de;
  --success-color: #5cb85c;
  --error-color: #d9534f;
  --border-color: #ddd;
  --text-color: #333;
  --text-secondary: #666;
}

.bill-add-page {
  min-height: 100vh;
  background-color: var(--bgcolor);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  padding-top: 70px; /* 添加顶部内边距，避免被导航栏遮挡 */
}

/* 导航栏样式 */
.navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  background-color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 40px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.nav-left .logo {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.nav-center {
  display: flex;
  gap: 30px;
}

.nav-link {
  text-decoration: none;
  color: #666;
  font-size: 15px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s;
}

.nav-link.active {
  color: var(--primary-color);
  font-weight: 500;
}

.nav-link:hover {
  background-color: #f5f5f5;
}

.nav-right {
  display: flex;
  gap: 10px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border: none;
  background-color: #f5f5f5;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  transition: background-color 0.3s;
}

.icon-btn:hover {
  background-color: #e5e5e5;
}

/* 容器样式 */
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px;
  min-height: calc(100vh - 70px); /* 确保容器至少占据剩余高度 */
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

/* 表单卡片样式 */
.form-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.form-title {
  font-size: 24px;
  color: var(--text-color);
  margin-bottom: 30px;
  text-align: center;
  font-weight: 600;
}

/* 表单样式 */
.bill-form {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color);
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 12px 16px;
  font-size: 16px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(217, 83, 79, 0.1);
}

/* 金额输入特殊样式 */
.amount-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.currency-symbol {
  position: absolute;
  left: 16px;
  font-size: 16px;
  color: var(--text-secondary);
  font-weight: 500;
}

.amount-input-wrapper .form-input {
  padding-left: 35px;
}

/* 类型选择器样式 */
.type-selector {
  display: flex;
  gap: 20px;
}

.type-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.type-option input[type="radio"] {
  width: 20px;
  height: 20px;
  accent-color: var(--primary-color);
}

.type-text {
  font-size: 16px;
  color: var(--text-color);
}

/* 文本域样式 */
.form-textarea {
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.char-count {
  font-size: 12px;
  color: var(--text-secondary);
  text-align: right;
  margin-top: 4px;
}

/* 操作按钮样式 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 20px;
}

.btn {
  padding: 12px 30px;
  font-size: 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #c9302c;
  transform: translateY(-1px);
}

.btn-primary:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.btn-cancel {
  background-color: #f5f5f5;
  color: var(--text-color);
  border: 1px solid var(--border-color);
}

.btn-cancel:hover {
  background-color: #e5e5e5;
}

/* 错误消息样式 */
.error-message {
  font-size: 14px;
  color: var(--error-color);
  margin-top: 4px;
}

/* 消息弹窗样式 */
.message-modal {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 20px 30px;
  border-radius: 8px;
  color: white;
  z-index: 2000;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 300px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.message-modal.success {
  background-color: var(--success-color);
}

.message-modal.error {
  background-color: var(--error-color);
}

.message-icon {
  font-size: 24px;
  font-weight: bold;
}

.message-content p {
  font-size: 16px;
  margin: 0;
}
</style>