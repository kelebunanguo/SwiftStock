<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1 class="eng-title">SwiftStock AI</h1>
        <h2 class="cn-title">电商仓库管理系统</h2>
        <p class="slogan">AI智能 · 高效管理 · 降本增效</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-button"
          >
            立即登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI } from '@/api'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const loginFormRef = ref()
    const loading = ref(false)
    
    const loginForm = reactive({
      username: '',
      password: ''
    })
    
    const loginRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ]
    }
    
    const handleLogin = async () => {
      if (!loginFormRef.value) return
      
      try {
        await loginFormRef.value.validate()
        loading.value = true
        
        const response = await authAPI.login(loginForm)

        if (response.success) {
          const token = response.data?.token
          if (!token) {
            ElMessage.error('服务器未返回 token')
            return
          }
          localStorage.setItem('token', token)
          ElMessage.success('登录成功')
          router.push('/dashboard')
        } else {
          ElMessage.error(response.message || '登录失败')
        }
      } catch (error) {
        console.error('登录错误:', error)
        ElMessage.error('登录失败，请检查用户名和密码')
      } finally {
        loading.value = false
      }
    }
    
    return {
      loginFormRef,
      loginForm,
      loginRules,
      loading,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  /* 背景图片 + 深色蒙版 */
  background: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.6)),
              url('/img/image.jpg') center/cover no-repeat fixed;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-box {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
  padding: 50px 40px;
  width: 100%;
  max-width: 440px;
  backdrop-filter: blur(10px); /* 毛玻璃效果，更高级 */
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.eng-title {
  color: #2c3e50;
  font-size: 42px;
  margin: 0 0 8px 0;
  font-weight: 800;
  letter-spacing: 1px;
}

.cn-title {
  color: #34495e;
  font-size: 28px;
  margin: 0 0 12px 0;
  font-weight: 600;
}

.slogan {
  color: #7f8c8d;
  font-size: 16px;
  margin: 0;
  font-weight: 500;
}

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 8px;
}
</style>