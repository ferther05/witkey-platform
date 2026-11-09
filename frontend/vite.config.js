import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5174,
    // 编辑器/工具写入时的临时目录不参与监听，否则 Windows 下会 EBUSY 崩溃
    watch: {
      ignored: ['**/.*.tmpdir/**', '**/*.tmp', '**/*.tmpdir/**']
    },
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8082',
        changeOrigin: true
      }
    }
  }
})
