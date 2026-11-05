import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// O proxy encaminha /api para o Spring Boot durante o desenvolvimento.
// Assim o navegador enxerga front e back na mesma origem e não é preciso
// liberar CORS no back-end só para rodar localmente.
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '')
  const backendUrl = env.VITE_BACKEND_URL || 'http://localhost:8080'

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: backendUrl,
          changeOrigin: true,
        },
      },
    },
  }
})
