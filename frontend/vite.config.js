import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    // Element Plus 按需引入：模板中的 el-* 组件自动注册并注入样式，去掉全量 866KB 主包
    Components({ resolvers: [ElementPlusResolver()], dts: false })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 5173,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path
      },
      '/uploads': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
})
