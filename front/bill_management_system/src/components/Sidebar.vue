<template>
  <aside :class="['sidebar', { collapsed: isCollapsed }]">
    <div class="sidebar-header">
      <transition name="fade">
        <h2 v-show="!isCollapsed" class="app-title">来福记账本</h2>
      </transition>
      <button class="collapse-btn" @click.stop="toggleCollapse" :title="isCollapsed ? '展开侧栏' : '收起侧栏'">
        <span class="collapse-icon">{{ isCollapsed ? '›' : '‹' }}</span>
      </button>
    </div>

    <nav class="sidebar-nav">
      <div class="nav-item" :class="{ active: currentPath === '/dashboard' }" @click="navigate('/dashboard')">
        <span class="nav-icon">🏠</span>
        <transition name="fade">
          <span v-show="!isCollapsed" class="nav-label">主页</span>
        </transition>
      </div>
      <div class="nav-item" :class="{ active: currentPath === '/statistics' }" @click="navigate('/statistics')">
        <span class="nav-icon">📊</span>
        <transition name="fade">
          <span v-show="!isCollapsed" class="nav-label">财务概览</span>
        </transition>
      </div>
      <div class="nav-item" :class="{ active: currentPath === '/bill-query' }" @click="navigate('/bill-query')">
        <span class="nav-icon">📋</span>
        <transition name="fade">
          <span v-show="!isCollapsed" class="nav-label">账单管理</span>
        </transition>
      </div>
      <div class="nav-item" :class="{ active: currentPath === '/account-search' }" @click="navigate('/account-search')">
        <span class="nav-icon">💳</span>
        <transition name="fade">
          <span v-show="!isCollapsed" class="nav-label">账户流水</span>
        </transition>
      </div>
    </nav>

    <div class="sidebar-footer">
      <div class="nav-item logout-btn" @click="handleLogout">
        <span class="nav-icon">🚪</span>
        <transition name="fade">
          <span v-show="!isCollapsed">退出登录</span>
        </transition>
      </div>
    </div>
  </aside>
</template>

<script>
export default {
  name: 'MenuSidebar',
  data() {
    return {
      isCollapsed: false
    }
  },
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
    },
    toggleCollapse() {
      this.isCollapsed = !this.isCollapsed;
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
  flex-shrink: 0;
  z-index: 10;
  box-shadow: 2px 0 8px rgba(0,0,0,0.05);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
}

.sidebar.collapsed .sidebar-header {
  justify-content: center;
  padding: 0;
}

.app-title {
  margin: 0;
  font-size: 20px;
  color: #1890ff;
  font-weight: bold;
  white-space: nowrap;
}

.collapse-btn {
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  cursor: pointer;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  flex-shrink: 0;
}

.collapse-btn:hover {
  background: #e6f7ff;
  border-color: #1890ff;
}

.collapse-btn:active {
  transform: scale(0.95);
}

.collapse-icon {
  font-size: 18px;
  color: #666;
  font-weight: bold;
  transition: transform 0.3s;
}

.collapse-btn:hover .collapse-icon {
  color: #1890ff;
}

.sidebar.collapsed .collapse-btn {
  margin: 0;
}

.sidebar-nav {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
  overflow-x: hidden;
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
  white-space: nowrap;
}

.sidebar.collapsed .nav-item {
  padding: 16px 20px;
  justify-content: center;
}

.nav-icon {
  font-size: 20px;
  margin-right: 12px;
  flex-shrink: 0;
  transition: transform 0.3s;
}

.sidebar.collapsed .nav-icon {
  margin-right: 0;
}

.nav-label {
  white-space: nowrap;
}

.nav-item:hover {
  background-color: #e6f7ff;
  color: #1890ff;
}

.nav-item:hover .nav-icon {
  transform: scale(1.1);
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

/* 淡入淡出动画 */
.fade-enter-active {
  transition: opacity 0.3s ease 0.1s;
}

.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
