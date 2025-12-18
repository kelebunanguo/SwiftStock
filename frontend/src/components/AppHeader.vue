<template>
  <header class="header">
    <div class="header-left">
      <h1>SwiftStock电商仓库管理系统</h1>
    </div>
    <div class="header-right">
      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <el-icon><User /></el-icon>
          管理员
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, ArrowDown } from '@element-plus/icons-vue'

export default {
  name: 'AppHeader',
  components: {
    User,
    ArrowDown
  },
  setup() {
    const router = useRouter()
    
    // 处理命令
    const handleCommand = (command) => {
      if (command === 'logout') {
        localStorage.removeItem('token')
        ElMessage.success('已退出登录')
        router.push('/login')
      }
    }
    
    return {
      handleCommand
    }
  }
}
</script>

<style scoped>
.header {
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left h1 {
  color: #1890ff;
  font-size: 24px;
  margin: 0;
  display: inline-block;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #333;
}
</style>
