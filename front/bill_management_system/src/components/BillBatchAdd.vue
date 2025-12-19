<template>
  <div class="bill-batch-add">
    <!-- 上传区域 -->
    <div
      class="upload-area"
      :class="{ 'has-file': file }"
      @click="triggerFileInput"
      @dragover.prevent
      @drop.prevent="handleFileDrop"
    >
      <div class="upload-content">
        <div class="upload-icon">{{ file ? '📄' : '📁' }}</div>
        <p class="upload-text">{{ file ? file.name : '点击或拖拽 Excel 文件到此处上传' }}</p>
        <p class="upload-hint" v-if="!file">支持 .xlsx 和 .xls 格式</p>
        <p class="upload-hint" v-else>点击可更换文件</p>
      </div>
      <input
        ref="fileInput"
        type="file"
        accept=".xlsx, .xls"
        style="display: none;"
        @change="handleFileChange"
      />
    </div>

    <!-- 操作按钮 -->
    <div class="form-actions">
      <button type="button" class="btn btn-cancel" @click="cancelUpload">取消</button>
      <button
        type="button"
        class="btn btn-primary"
        @click="uploadFile"
        :disabled="!file || uploading"
      >
        {{ uploading ? '上传中...' : '确定上传' }}
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import readXlsxFile from 'read-excel-file';

export default {
  name: 'BillBatchAdd',
  data() {
    return {
      file: null,
      fileName: '',
      uploading: false,
    };
  },
  methods: {
    // 触发隐藏的文件输入框
    triggerFileInput() {
      this.$refs.fileInput.click();
    },

    // 文件更改处理方法
    handleFileChange(event) {
      const selectedFile = event.target.files[0];
      if (selectedFile) {
        this.validateAndSetFile(selectedFile);
      }
      // 重置 input value，允许重复选择同一文件
      event.target.value = '';
    },

    // 拖拽文件操作方法
    handleFileDrop(event) {
      event.preventDefault();
      const droppedFile = event.dataTransfer.files[0];
      if (droppedFile) {
        this.validateAndSetFile(droppedFile);
      }
    },

    // 验证文件并设置
    validateAndSetFile(file) {
      const allowedTypes = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'application/vnd.ms-excel'];
      // 简单的后缀名检查作为补充
      const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls');

      if (!isExcel && !allowedTypes.includes(file.type)) {
        alert('请选择有效的 Excel 文件 (.xlsx 或 .xls)');
        return;
      }
      this.file = file;
      this.fileName = file.name;
    },

    // 上传文件
    async uploadFile() {
      if (!this.file) return;

      this.uploading = true;
      try {
        // 解析 Excel 文件
        const bills = await this.parseExcel(this.file);

        // 构建请求数据
        const token = localStorage.getItem('token') || 'mock_token';
        const requestData = {
          token,
          data: bills,
        };

        // 发送请求
        const response = await axios.post('http://localhost:8080/api/bill/adddBillList', requestData);

        if (response.data.statusCode === 200) {
          this.$emit('success', response.data); // 传递上传结果
        } else {
          throw new Error(response.data.message);
        }
      } catch (error) {
        console.error('上传失败:', error);
        this.$emit('error', error.message || '上传失败，请重试');
        alert('上传失败: ' + (error.message || '请重试'));
      } finally {
        this.uploading = false;
      }
    },

    // 基于 read-excel-file 解析 Excel 文件
    async parseExcel(file) {
      try {
        const rows = await readXlsxFile(file);

        // 跳过表头，从第二行开始
        const bills = rows.slice(1).map(row => ({
          recordEnum: row[0] === '收入' ? 'INCOME' : 'EXPENDITURE', // 映射记录类型
          amount: parseFloat(row[1]), // 金额
          account: row[2], // 账户
          type: row[3], // 类型
          date: this.formatDate(row[4]), // 日期处理
          remarks: row[5] || '', // 备注
        })).filter(bill => bill.amount && bill.account && bill.type && bill.date); // 过滤无效行

        return bills;
      } catch (error) {
        console.error('Excel 解析错误:', error);
        throw new Error('Excel 文件解析失败，请检查格式');
      }
    },

    formatDate(dateVal) {
      if (dateVal instanceof Date) {
        return dateVal.toISOString().split('T')[0];
      }
      return dateVal;
    },

    // 取消上传
    cancelUpload() {
      this.file = null;
      this.fileName = '';
      this.$emit('cancel'); // 通知父组件
    },
  },
}
</script>

<style scoped>
.bill-batch-add {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.upload-area {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fafafa;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-area:hover {
  border-color: #1890ff;
  background-color: #f0f8ff;
}

.upload-area.has-file {
  border-color: #52c41a;
  background-color: #f6ffed;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}

.upload-hint {
  font-size: 14px;
  color: #999;
}

/* 操作按钮样式 - 与 billAdd 保持一致 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 10px;
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
</style>
