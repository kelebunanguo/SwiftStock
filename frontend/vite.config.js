import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 8080,
    proxy: {
      '/swiftstock': {
        target: 'http://localhost:9090', // 后端服务地址
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/api/, '') // 如果后端API没有/api前缀，可以移除
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets'
  }
})
