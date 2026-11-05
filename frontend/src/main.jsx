import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import './styles/index.css'

// StrictMode executa efeitos duas vezes em desenvolvimento para revelar
// efeitos sem cleanup (ex.: requisições sem AbortController).
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
