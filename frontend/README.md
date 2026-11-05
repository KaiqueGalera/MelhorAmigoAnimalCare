# VetClínica — Front-end

React 19 + Vite 8 + React Router 7. Sem dependências extras.

```bash
npm install
cp .env.example .env     # ajuste VITE_BACKEND_URL se o Spring não estiver em :8080
npm run dev              # http://localhost:5173 (proxy /api -> back-end)
npm run lint && npm run build
```

Rotas do back-end: todas em `src/api/endpoints.js`. DTOs de login/registro: `src/api/authApi.js`.
