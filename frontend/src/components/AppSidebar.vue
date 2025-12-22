<template>
  <aside class="sidebar">
    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      router
      background-color="#001529"
      text-color="#fff"
      active-text-color="#1890ff"
    >
      <el-menu-item index="/dashboard">
        <el-icon><House /></el-icon>
        <span>AI运营概览</span>
      </el-menu-item>
      <el-menu-item index="/ai/forecast">
        <el-icon><TrendCharts /></el-icon>
        <span>AI智能补货</span>
      </el-menu-item>
      <el-menu-item index="/products">
        <el-icon><Box /></el-icon>
        <span>商品管理</span>
      </el-menu-item>
      <el-menu-item index="/orders">
        <el-icon><Document /></el-icon>
        <span>订单管理</span>
      </el-menu-item>
      <el-menu-item index="/inventory">
        <el-icon><Grid /></el-icon>
        <span>库存管理</span>
      </el-menu-item>
      <el-menu-item index="/suppliers">
        <el-icon><Box /></el-icon>
        <span>供应商管理</span>
      </el-menu-item>
      <el-menu-item index="/reports/stock">
        <el-icon><TrendCharts /></el-icon>
        <span>报表统计</span>
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<script>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { 
  House, 
  Box, 
  Document, 
  Grid, 
  TrendCharts
} from '@element-plus/icons-vue'

export default {
  name: 'AppSidebar',
  components: {
    House,
    Box,
    Document,
    Grid,
    TrendCharts
  },
  setup() {
    const route = useRoute()
    
    // 根据当前路由计算激活的菜单项
    const activeMenu = computed(() => {
      return route.path
    })
    
    return {
      activeMenu
    }
  }
}
</script>

<style scoped>
.sidebar {
  width: 200px;
  background: #001529;
}

.sidebar-menu {
  border: none;
  height: 100%;
}
</style>
