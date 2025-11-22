<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <h2 class="app-title">来福记账本</h2>
    </div>

    <nav class="sidebar-nav">
      <div class="nav-item" :class="{ active: currentPath === '/dashboard' }" @click="navigate('/dashboard')">
        <span>主页</span>
      </div>
      <div class="nav-item" :class="{ active: currentPath === '/statistics' }" @click="navigate('/statistics')">
        <span>财务概览</span>
      </div>
      <div class="nav-item" :class="{ active: currentPath === '/bill-query' }" @click="navigate('/bill-query')">
        <span>账单管理</span>
      </div>
      <div class="nav-item">
        <span>用户信息</span>
      </div>
    </nav>

    <div class="sidebar-footer">
      <div class="nav-item logout-btn" @click="handleLogout">
        <span>退出登录</span>
      </div>
    </div>
  </aside>
</template>

<script>
export default {
  name: 'MenuSidebar',
  computed: {
    currentPath() {
      return this.$route.path;
    }
  },
  methods: {
    navigate(path) {
      if (this.$route.path !== path) {
        this.$router.push(path);
      }
    },
    handleLogout() {
      if (confirm('确定要退出登录吗？')) {
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        this.$router.push('/login');
      }
    }
  }
};
</script>

<style scoped>
/* 左侧侧边栏 */
.sidebar {
  width: 240px;
  background: white;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  flex-shrink: 0; /* 防止侧边栏被压缩 */
  z-index: 10;
  box-shadow: 2px 0 8px rgba(0,0,0,0.05);
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #f0f0f0;
}

.app-title {
  margin: 0;
  font-size: 20px;
  color: #1890ff;
  font-weight: bold;
}

.sidebar-nav {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
}

.nav-item {
  padding: 16px 24px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  display: flex;
  align-items: center;
  font-size: 16px;
  margin-bottom: 4px;
  border-left: 3px solid transparent;
}

.nav-item:hover {
  background-color: #e6f7ff;
  color: #1890ff;
}

.nav-item.active {
  background-color: #e6f7ff;
  color: #1890ff;
  border-left-color: #1890ff;
  font-weight: 500;
}

.sidebar-footer {
  padding: 20px 0;
  border-top: 1px solid #f0f0f0;
}

.logout-btn {
  color: #ff4d4f;
}

.logout-btn:hover {
  background-color: #fff1f0;
  color: #ff4d4f;
}
</style>
