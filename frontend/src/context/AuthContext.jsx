import { createContext, useContext, useState, useEffect } from 'react'
import { authApi } from '../services/api'

const AuthContext = createContext(null)

const getStoredToken = () => {
  return sessionStorage.getItem('careerlens_token') || localStorage.getItem('careerlens_token')
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(() => getStoredToken())
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = getStoredToken()
      if (storedToken) {
        try {
          const res = await authApi.getCurrentUser()
          if (res.success && res.data) {
            setUser(res.data)
            // Ensure current tab's sessionStorage retains token
            sessionStorage.setItem('careerlens_token', storedToken)
          } else {
            logout()
          }
        } catch {
          logout()
        }
      }
      setLoading(false)
    }
    initAuth()
  }, [])

  const saveAuthSession = (authToken, userData) => {
    setToken(authToken)
    setUser(userData)
    sessionStorage.setItem('careerlens_token', authToken)
    localStorage.setItem('careerlens_token', authToken)
  }

  const registerSendOtp = async (payload) => {
    return await authApi.registerSendOtp(payload)
  }

  const registerVerifyOtp = async (payload) => {
    const res = await authApi.registerVerifyOtp(payload)
    if (res.success && res.data?.token) {
      saveAuthSession(res.data.token, res.data.user)
    }
    return res
  }

  const loginSendOtp = async (payload) => {
    return await authApi.loginSendOtp(payload)
  }

  const loginVerifyOtp = async (payload) => {
    const res = await authApi.loginVerifyOtp(payload)
    if (res.success && res.data?.token) {
      saveAuthSession(res.data.token, res.data.user)
    }
    return res
  }

  const resendOtp = async (payload) => {
    return await authApi.resendOtp(payload)
  }

  const logout = () => {
    setToken(null)
    setUser(null)
    sessionStorage.removeItem('careerlens_token')
    sessionStorage.removeItem('careerlens_user')
    localStorage.removeItem('careerlens_token')
    localStorage.removeItem('careerlens_user')
  }

  const value = {
    user,
    token,
    role: user?.role || null,
    isAuthenticated: Boolean(user && token),
    loading,
    registerSendOtp,
    registerVerifyOtp,
    loginSendOtp,
    loginVerifyOtp,
    saveAuthSession,
    resendOtp,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
