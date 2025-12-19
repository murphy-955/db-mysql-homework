<template>
  <!-- 单独上传表单 -->
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
      <button type="button" class="btn btn-cancel" @click="cancel">取消</button>
      <button type="submit" class="btn btn-primary" :disabled="loading">{{ loading ? '提交中...' : '提交' }}</button>
    </div>
  </form>
</template>

<script>
import axios from 'axios';

export default {
  name: 'BillAdd',
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
      loading: false
    };
  },
  computed: {
    today() {
      return this.getTodayDate();
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
        const token = localStorage.getItem('token') || 'mock_token';
        const requestData = {
          token,
          amount: this.billForm.amount,
          recordEnum: this.billForm.type === '收入' ? 'INCOME' : 'EXPENDITURE',
          type: this.billForm.category,
          date: this.billForm.date,
          account: this.billForm.account,
          remarks: this.billForm.remark
        };

        const response = await axios.post('http://localhost:8080/api/bill/addBill', requestData);

        if (response.data.statusCode === 200) {
          this.$emit('success');
        } else {
          this.$emit('error', response.data.message || '操作失败');
        }
      } catch (error) {
        console.error('添加账单失败:', error);
        this.$emit('error', '操作失败，请重试');
      } finally {
        this.loading = false;
      }
    },
    cancel() {
      this.$emit('cancel');
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
    }
  }
};
</script>

<style scoped>
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
  color: #333;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 12px 16px;
  font-size: 16px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
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
  color: #666;
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
  accent-color: #1890ff;
}

.type-text {
  font-size: 16px;
  color: #333;
}

/* 文本域样式 */
.form-textarea {
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.char-count {
  font-size: 12px;
  color: #999;
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
  background-color: #1890ff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #40a9ff;
  transform: translateY(-1px);
}

.btn-primary:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.btn-cancel {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #d9d9d9;
}

.btn-cancel:hover {
  background-color: #e5e5f5;
}

/* 错误消息样式 */
.error-message {
  font-size: 14px;
  color: #ff4d4f;
  margin-top: 4px;
}
</style>
