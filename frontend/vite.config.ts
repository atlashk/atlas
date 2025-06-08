import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  define: {
    // Fix for SockJS/STOMP WebSocket libraries in browser environment
    // Maps 'global' to 'globalThis' so libraries can access browser APIs like WebSocket
    // Without this, SockJS fails with "All transports failed" error
    global: 'globalThis',
  },
  server: {
    port: 9000, // or any port you prefer
    open: true, // open browser on server start
  },
  resolve: {
    alias: {
      '@': '/src', // allows import like '@/components/Example.vue'
    },
  },
});
