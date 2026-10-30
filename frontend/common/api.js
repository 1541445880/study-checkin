import request from './request.js'

// 认证
export const login = (data) => request({ url: '/api/auth/login', method: 'POST', data })
export const register = (data) => request({ url: '/api/auth/register', method: 'POST', data })

// 用户资料
export const getProfile = () => request({ url: '/api/user/profile' })
export const updateProfile = (data) => request({ url: '/api/user/profile', method: 'PUT', data })
export const updatePassword = (data) => request({ url: '/api/user/password', method: 'PUT', data })
export const updateAvatar = (data) => request({ url: '/api/user/avatar', method: 'PUT', data })
export const updateBg = (data) => request({ url: '/api/user/bg', method: 'PUT', data })

// 打卡
export const doCheckIn = (data) => request({ url: '/api/checkin', method: 'POST', data })
export const getCheckIn = (id) => request({ url: `/api/checkin/${id}` })
export const updateCheckIn = (id, data) => request({ url: `/api/checkin/${id}`, method: 'PUT', data })
export const getToday = () => request({ url: '/api/checkin/today' })
export const getList = (page, size) => request({ url: `/api/checkin/list?page=${page}&size=${size}` })
export const getStats = () => request({ url: '/api/checkin/stats' })
export const deleteCheckIn = (id) => request({ url: `/api/checkin/${id}`, method: 'DELETE' })

// 每日一句
export const getQuote = () => request({ url: '/api/quote/random' })
