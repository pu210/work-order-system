import axios from "axios";
import { clearAuth, getToken } from '@/utils/auth.js'

const instance = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080'
});

instance.interceptors.request.use((config) => {
    const token = getToken()
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

instance.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error.response?.status
        if (status === 401 && !error.config?.skipAuthRedirect) {
            clearAuth()
            const returnUrl = `${window.location.pathname}${window.location.search}`
            window.location.assign(`/auth/login?returnUrl=${encodeURIComponent(returnUrl)}`)
        } else if (status === 403) {
            window.location.assign('/forbidden')
        }
        return Promise.reject(error)
    }
)


export default instance;
