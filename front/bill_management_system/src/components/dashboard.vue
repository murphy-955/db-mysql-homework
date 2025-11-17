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
}

.dashboard-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 导航栏 */
.navbar {
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
  max-width: 1200px;
  margin: 50px auto;
  padding: 0 20px;
}

/* 功能区域 */
.features-section {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.section-icon {
  font-size: 20px;
}

.section-header h3 {
  font-size: 18px;
  color: #333;
}

.download-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.platform-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.tab-btn {
  padding: 8px 20px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.tab-btn.active {
  background-color: #d9534f;
  color: white;
  border-color: #d9534f;
}

.tab-btn:hover:not(.active) {
  background-color: #f5f5f5;
}

.import-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.import-card:hover {
  background-color: #e9ecef;
}

.import-icon {
  font-size: 20px;
}

/* 按钮样式 */
.btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  font-weight: 500;
}

.btn-primary {
  background-color: #d9534f;
  color: white;
}

.btn-primary:hover {
  background-color: #c9302c;
}

.btn-outline {
  background-color: white;
  color: #d9534f;
  border: 1px solid #d9534f;
}

.btn-outline:hover {
  background-color: #fff5f5;
}

/* 功能卡片网格 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.feature-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
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
