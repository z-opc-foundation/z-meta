import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    resolve: {
        preserveSymlinks: true,
    },
    server: {
        port: 3000,
        proxy: {
            '/meta': {
                target: 'http://localhost:8080',
                changeOrigin: true,
            }
        }
    }
})