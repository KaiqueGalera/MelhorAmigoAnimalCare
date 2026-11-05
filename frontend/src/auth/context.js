import { createContext } from 'react'

// Em arquivo separado para que AuthContext.jsx exporte só componentes (Fast Refresh).
export const AuthContext = createContext(null)
