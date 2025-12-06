<template>
  <div class="modal-overlay">
    <div class="bill-modifier-window">
      <div class="window-header">
        <h3 class="window-title">添加账单记录</h3>
        <button class="close-btn" @click="handleCancel" title="关闭">×</button>
      </div>

      <!-- 模式切换按钮 -->
      <div class="mode-switcher">
        <button
          class="mode-btn"
          :class="{ active: currentMode === 'single' }"
          @click="currentMode = 'single'"
        >
          单独上传
        </button>
        <button
          class="mode-btn"
          :class="{ active: currentMode === 'batch' }"
          @click="currentMode = 'batch'"
        >
          批量上传
        </button>
      </div>

      <!-- 内容区域 -->
      <div class="window-content">
        <!-- 单独上传表单 -->
        <bill-add v-if="currentMode === 'single'" @success="handleSuccess" @cancel="handleCancel" />

        <!-- 批量上传组件 -->
        <bill-batch-add v-else @cancel="handleCancel" @success="handleSuccess" />
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
import BillAdd from './BillAdd.vue';
import BillBatchAdd from './BillBatchAdd.vue';

export default {
  name: 'BillAddWindow',
  components: {
    BillAdd,
    BillBatchAdd
  },
  data() {
    return {
      currentMode: 'single', // 默认选择单独上传
      showMessage: false,
      messageText: '',
      messageType: 'success'
    };
  },
  computed: {
    messageIcon() {
      return this.messageType === 'success' ? '✔' : '×';
    }
  },
  methods: {
    handleSuccess() {
      this.showMessageModal('success', '操作成功！');
      // 延迟触发成功事件，让用户看到成功提示
      setTimeout(() => {
        this.$emit('success');
      }, 1000);
    },
    handleCancel() {
      this.$emit('cancel');
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
/* 模态遮罩层 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* 半透明黑色背景 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px); /* 背景模糊效果 */
}

/* 窗口容器样式 */
.bill-modifier-window {
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 550px;
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 窗口头部 */
.window-header {
  padding: 24px 30px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.window-title {
  font-size: 22px;
  color: #333;
  font-weight: 600;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  padding: 0;
  line-height: 1;
  transition: color 0.3s;
}

.close-btn:hover {
  color: #333;
}

/* 内容区域 */
.window-content {
  padding: 0 30px 30px;
}

/* 模式切换按钮样式 */
.mode-switcher {
  display: flex;
  justify-content: center;
  gap: 0;
  margin: 0 30px 25px;
  background-color: #f5f5f5;
  padding: 4px;
  border-radius: 8px;
}

.mode-btn {
  flex: 1;
  padding: 8px 0;
  font-size: 15px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  background: transparent;
  color: #666;
  font-weight: 500;
}

.mode-btn.active {
  background-color: white;
  color: #1890ff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
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
  background-color: #52c41a;
}

.message-modal.error {
  background-color: #ff4d4f;
}

.message-icon {
  font-size: 24px;
  font-weight: bold;
}

.message-content p {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}
</style>
