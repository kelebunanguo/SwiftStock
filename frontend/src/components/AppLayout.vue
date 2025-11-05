<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <AppHeader />
    
    <div class="main-layout">
      <!-- 侧边栏 -->
      <AppSidebar />
      
      <!-- 主内容区 -->
      <main class="main-content">
        <slot />
      </main>
    </div>
    
    <!-- 底部 -->
    <AppFooter />
  </div>
</template>

<script>
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
import AppFooter from './AppFooter.vue'

export default {
  name: 'AppLayout',
  components: {
    AppHeader,
    AppSidebar,
    AppFooter
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.main-layout {
  display: flex;
  flex: 1;
  min-height: calc(100vh - 60px - 60px); /* 减去header和footer的高度 */
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-layout {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    height: auto;
  }
  
  .main-content {
    padding: 10px;
  }
}
</style>
