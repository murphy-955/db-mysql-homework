<template>
  <div class="dashboard-page">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="nav-left">
        <h2 class="logo">来福记账本</h2>
      </div>
      <nav class="nav-center">
        <a href="#" class="nav-link active">主页</a>
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

    <!-- 功能区域 -->
    <div class="container">
      <div class="features-section">
        <div class="section-header">
          <span class="section-icon">🚀</span>
          <h3>快速开始</h3>
        </div>
        <div class="download-section">
          <button class="btn btn-primary">下载客户端</button>
          <button class="btn btn-outline">复制订阅链接</button>
          <button class="btn btn-outline">扫码订阅</button>
        </div>
        <div class="platform-tabs">
          <button class="tab-btn">iOS</button>
          <button class="tab-btn">Android</button>
          <button class="tab-btn">Mac OS</button>
          <button class="tab-btn active">Windows</button>
        </div>
        <div class="import-card">
          <span class="import-icon">📦</span>
          <span>一键导入到 Clash</span>
        </div>
      </div>

      <!-- 功能卡片网格 -->
      <div class="card-grid">
        <div class="feature-card">
          <div class="card-icon-wrapper pink">
            <span class="card-emoji">❓</span>
          </div>
          <h4>帮助中心</h4>
          <p>指南和常见问题</p>
          <span class="card-arrow">→</span>
        </div>

        <div class="feature-card">
          <div class="card-icon-wrapper purple">
            <span class="card-emoji">🔄</span>
          </div>
          <h4>订阅转换器</h4>
          <p>自定义订阅和节点</p>
          <span class="card-arrow">→</span>
        </div>

        <div class="feature-card">
          <div class="card-icon-wrapper green">
            <span class="card-emoji">🎁</span>
          </div>
          <h4>推荐奖励</h4>
          <p>邀请新用户并赚取佣金</p>
          <span class="card-arrow">→</span>
        </div>

        <div class="feature-card">
          <div class="card-icon-wrapper yellow">
            <span class="card-emoji">💬</span>
          </div>
          <h4>在线支持</h4>
          <p>获取在线客服帮助</p>
          <span class="card-arrow">→</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DashboardPage',
};
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  --bgcolor: rgba(0, 81, 255, 0.47);
}

.dashboard-page {
  min-height: 100vh;
  background-color: var(--bgcolor);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 导航栏 */
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
  color: #d9534f;
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

/* 容器 */
.container {
  width: 100vw;
  margin: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  padding: 50px;
  box-sizing: border-box;
}

/* 功能区域 */
.features-section {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  /* 移除 margin-bottom，因为现在是并排布局 */
}

/* 功能卡片网格 */
.card-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.feature-card {
  background: white;
  border-radius: 7px;
  padding: 20px; /* 从25px减少到20px，缩小卡片大小 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  width: 300px;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.card-icon-wrapper {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  font-size: 24px;
}

.card-icon-wrapper.pink {
  background-color: #ffe0e6;
}

.card-icon-wrapper.purple {
  background-color: #e6e0ff;
}

.card-icon-wrapper.green {
  background-color: #d4f4dd;
}

.card-icon-wrapper.yellow {
  background-color: #fff4d9;
}

.feature-card h4 {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
}

.feature-card p {
  font-size: 14px;
  color: #999;
  margin-bottom: 15px;
}

.card-arrow {
  position: absolute;
  top: 25px;
  right: 25px;
  font-size: 20px;
  color: #ddd;
}
</style>
