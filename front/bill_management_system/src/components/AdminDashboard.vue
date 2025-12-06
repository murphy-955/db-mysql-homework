<template>
  <div class="admin-dashboard">
    <div class="content-wrapper">
      <!-- 顶部头部 Card -->
      <div class="card header-card">
        <div class="header-content">
          <div class="header-title">
            <h2>用户管理控制台</h2>
            <span class="subtitle">管理系统注册用户及权限</span>
          </div>
          <div class="header-actions">
            <div class="admin-info">
              <span class="avatar">lzy</span>
              <div class="info-text">
                <span class="name">管理员</span>
                <span class="role">系统管理员</span>
              </div>
            </div>
            <button class="btn btn-outline" @click="logout">退出登录</button>
          </div>
        </div>
      </div>

      <!-- 主体 Grid 布局 (类似 Dashboard 的分栏) -->
      <div class="dashboard-grid">

        <!-- 左侧：用户列表 (70%) -->
        <div class="left-panel">
          <div class="card table-card">
            <div class="card-header">
              <h3>用户列表</h3>
              <!-- 工具栏 -->
              <div class="toolbar">
                <div class="search-box">
                  <span class="search-icon">🔍</span>
                  <input
                    type="text"
                    v-model="searchQuery"
                    placeholder="搜索用户名..."
                    @input="handleSearch"
                  >
                </div>
                <button class="btn btn-primary" @click="refreshData">
                  刷新
                </button>
              </div>
            </div>

            <!-- 表格区域 -->
            <div class="table-container">
              <table class="data-table">
                <thead>
                  <tr>
                    <th width="80">ID</th>
                    <th>用户名</th>
                    <th width="100">状态</th>
                    <th width="150">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="loading">
                    <td colspan="4" class="text-center">加载中...</td>
                  </tr>
                  <tr v-else-if="filteredUsers.length === 0">
                    <td colspan="4" class="text-center">暂无数据</td>
                  </tr>
                  <tr v-for="user in filteredUsers" :key="user.id">
                    <td>#{{ user.id }}</td>
                    <td>
                      <div class="user-cell">
                        <div class="user-avatar">{{ user.username.charAt(0).toUpperCase() }}</div>
                        <span>{{ user.username }}</span>
                      </div>
                    </td>
                    <td>
                      <span :class="['status-badge', isUserDisabled(user) ? 'disabled' : 'active']">
                        {{ isUserDisabled(user) ? '已禁用' : '正常' }}
                      </span>
                    </td>
                    <td>
                      <div class="action-buttons">
                        <button
                          class="btn-icon"
                          :class="isUserDisabled(user) ? 'btn-enable' : 'btn-disable'"
                          @click="toggleUserStatus(user)"
                          :title="isUserDisabled(user) ? '解禁用户' : '禁用用户'"
                        >
                          {{ isUserDisabled(user) ? '🔓' : '🚫' }}
                        </button>
                        <button
                          class="btn-icon btn-delete"
                          @click="confirmDelete(user)"
                          title="删除用户"
                        >
                          🗑️
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- 分页 -->
            <div class="pagination">
              <span class="page-info">共 {{ totalCount }} 条</span>
              <div class="page-controls">
                <button
                  class="btn-page"
                  :disabled="currentPage === 1"
                  @click="changePage(currentPage - 1)"
                >
                  上一页
                </button>
                <span class="current-page">{{ currentPage }}</span>
                <button
                  class="btn-page"
                  :disabled="currentPage * limit >= totalCount"
                  @click="changePage(currentPage + 1)"
                >
                  下一页
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：统计与信息 (30%) -->
        <div class="right-panel">
          <!-- 统计卡片 -->
          <div class="card stat-card">
            <div class="stat-icon-wrapper">
              <span class="stat-icon">👥</span>
            </div>
            <div class="stat-content">
              <div class="stat-label">注册用户总数</div>
              <div class="stat-value">{{ totalCount }}</div>
            </div>
          </div>

          <!-- 系统状态 (模拟) -->
          <div class="card system-card">
            <h4>系统状态</h4>
            <div class="system-status">
              <div class="status-item">
                <span class="label">服务器状态</span>
                <span class="status-dot online"></span>
                <span class="value">运行中</span>
              </div>
              <div class="status-item">
                <span class="label">数据库连接</span>
                <span class="status-dot online"></span>
                <span class="value">正常</span>
              </div>
            </div>
          </div>

          <!-- 快速说明 -->
          <div class="card info-card">
            <h4>管理员指南</h4>
            <ul class="guide-list">
              <li>🚫 禁用用户将限制其登录权限</li>
              <li>🗑️ 删除用户操作不可恢复</li>
              <li>🔍 支持按用户名模糊搜索</li>
            </ul>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'AdminDashboard',
  data() {
    return {
      users: [],
      totalCount: 0,
      currentPage: 1,
      limit: 10,
      loading: false,
      searchQuery: '',
      token: ''
    };
  },
  computed: {
    // 前端搜索过滤（如果后端不支持搜索参数，则使用此方式）
    filteredUsers() {
      if (!this.searchQuery) return this.users;
      const query = this.searchQuery.toLowerCase();
      return this.users.filter(user =>
        user.username.toLowerCase().includes(query) ||
        String(user.id).includes(query)
      );
    }
  },
  async mounted() {
    // 尝试从localStorage获取token，实际项目中应从登录逻辑获取
    this.token = localStorage.getItem('adminToken') || '';
    this.token = 'testToken';
    if (!this.token) {
      alert('未检测到管理员登录信息，请先登录');
      this.$router.push('/admin-login');
      return;
    }
    await this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        await Promise.all([
          this.fetchUserCount(),
          this.fetchUsers()
        ]);
      } catch (error) {
        console.error('加载数据失败:', error);
      } finally {
        this.loading = false;
      }
    },

    async fetchUserCount() {
      try {
        const res = await axios.post(`/api/admin/getUserInfoCount?limit=${this.limit}`, {
          token: this.token
        });
        if (res.data.statusCode === 200) {
          this.totalCount = res.data.data;
        }
      } catch (error) {
        console.error('获取总数失败', error);
      }
    },

    async fetchUsers() {
      try {
        // 注意：如果后端支持搜索，应该在这里把 searchQuery 传过去
        const res = await axios.post(`/api/admin/getUserInfo?page=${this.currentPage}&limit=${this.limit}`, {
          token: this.token
        });

        if (res.data.statusCode === 200) {
          this.users = res.data.data;
        }
      } catch (error) {
        console.error('获取用户列表失败', error);
      }
    },

    isUserDisabled(user) {
      // 根据接口文档，"true" 表示禁用
      return user.whetherItIsDisabledOrNot === 'true';
    },

    async toggleUserStatus(user) {
      const isDisabled = this.isUserDisabled(user);
      const action = isDisabled ? 'enableUser' : 'disableUser';
      const confirmMsg = isDisabled ? `确定要解禁用户 ${user.username} 吗？` : `确定要禁用用户 ${user.username} 吗？`;

      if (!confirm(confirmMsg)) return;

      try {
        const res = await axios.post(`/api/admin/${action}`, {
          token: this.token,
          userId: user.id
        });

        if (res.data.statusCode === 200) {
          alert('操作成功');
          // 更新本地状态或重新加载
          await this.fetchUsers();
        } else {
          alert(res.data.message || '操作失败');
        }
      } catch (error) {
        alert('网络错误');
      }
    },

    async confirmDelete(user) {
      if (!confirm(`警告：确定要永久删除用户 ${user.username} 吗？此操作不可恢复！`)) return;

      try {
        const res = await axios.post('/api/admin/deleteUser', {
          token: this.token,
          userId: user.id
        });

        if (res.data.statusCode === 200) {
          alert('删除成功');
          await this.loadData(); // 重新加载总数和列表
        } else {
          alert(res.data.message || '删除失败');
        }
      } catch (error) {
        alert('网络错误');
      }
    },

    handleSearch() {
      // 如果是前端过滤，不需要做额外请求
      // 如果后端支持搜索，这里应该调用 fetchUsers() 并重置 page 为 1
      // this.currentPage = 1;
      // this.fetchUsers();
    },

    refreshData() {
      this.loadData();
    },

    changePage(page) {
      this.currentPage = page;
      this.fetchUsers();
    },

    logout() {
      localStorage.removeItem('adminToken');
      this.$router.push('/admin-login');
    }
  }
};
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 40px; /* 保持与 Dashboard 一致的大内边距 */
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.content-wrapper {
  max-width: 1600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 通用卡片样式 (复用 Dashboard 风格) */
.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 32px; /* 稍微调整 padding 以适应紧凑内容 */
}

/* 头部卡片 */
.header-card {
  padding: 24px 32px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title h2 {
  margin: 0;
  font-size: 24px;
  color: #1f1f1f;
  font-weight: 600;
}

.subtitle {
  color: #8c8c8c;
  font-size: 14px;
  margin-top: 6px;
  display: block;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 24px;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  background: #1890ff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}

.info-text {
  display: flex;
  flex-direction: column;
}

.info-text .name {
  font-weight: 500;
  color: #1f1f1f;
  font-size: 14px;
}

.info-text .role {
  font-size: 12px;
  color: #8c8c8c;
}

/* Grid 布局 */
.dashboard-grid {
  display: grid;
  grid-template-columns: 7fr 3fr; /* 7:3 分栏 */
  gap: 24px;
}

.left-panel, .right-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 表格卡片 */
.table-card {
  min-height: 600px;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1f1f1f;
}

.toolbar {
  display: flex;
  gap: 12px;
}

.search-box {
  position: relative;
  width: 240px;
}

.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #bfbfbf;
}

.search-box input {
  width: 100%;
  padding: 8px 12px 8px 32px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
}

.search-box input:focus {
  border-color: #1890ff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* 表格样式 */
.table-container {
  flex: 1;
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 16px;
  background: #fafafa;
  color: #595959;
  font-weight: 600;
  border-bottom: 1px solid #f0f0f0;
}

.data-table td {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  color: #262626;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 28px;
  height: 28px;
  background: #f56a00;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}

/* 状态徽章 */
.status-badge {
  padding: 4px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-badge.disabled {
  background: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffa39e;
}

/* 按钮 */
.btn {
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  border: none;
  transition: all 0.3s;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-primary:hover {
  background: #40a9ff;
}

.btn-outline {
  background: white;
  border: 1px solid #d9d9d9;
  color: #595959;
}

.btn-outline:hover {
  color: #1890ff;
  border-color: #1890ff;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-icon {
  width: 30px;
  height: 30px;
  border-radius: 6px;
  border: 1px solid #d9d9d9;
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.btn-icon:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.btn-delete:hover {
  border-color: #ff4d4f;
  color: #ff4d4f;
  background: #fff1f0;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.page-info {
  color: #8c8c8c;
  font-size: 14px;
}

.page-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-page {
  padding: 5px 12px;
  border: 1px solid #d9d9d9;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.btn-page:disabled {
  background: #f5f5f5;
  color: #bfbfbf;
  cursor: not-allowed;
}

.current-page {
  font-weight: 600;
  color: #1890ff;
  margin: 0 8px;
}

/* 右侧卡片样式 */
.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon-wrapper {
  width: 60px;
  height: 60px;
  background: #e6f7ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon {
  font-size: 30px;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-label {
  color: #8c8c8c;
  font-size: 14px;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #1890ff;
}

.system-card h4, .info-card h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #1f1f1f;
}

.system-status {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
}

.status-item .label {
  color: #595959;
}

.status-item .value {
  color: #1f1f1f;
  font-weight: 500;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #d9d9d9;
}

.status-dot.online {
  background: #52c41a;
}

.guide-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.guide-list li {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  color: #595959;
  font-size: 14px;
}

.guide-list li:last-child {
  border-bottom: none;
}

/* 响应式适配 */
@media screen and (max-width: 1024px) {
  .dashboard-grid {
    grid-template-columns: 1fr; /* 平板/手机改为单列 */
  }

  .admin-dashboard {
    padding: 20px;
  }
}
</style>
