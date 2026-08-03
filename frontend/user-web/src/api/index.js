import http from './http'

export const authApi = {
  register: data => http.post('/api/auth/register', data),
  login: data => http.post('/api/auth/login', data),
  me: () => http.get('/api/auth/me'),
  updateProfile: data => http.put('/api/auth/profile', data),
  logout: refreshToken => http.post('/api/auth/logout', { refreshToken })
}

export const productApi = {
  categories: () => http.get('/api/product/categories'),
  list: params => http.get('/api/product/list', { params }),
  detail: id => http.get(`/api/product/${id}`),
  hot: () => http.get('/api/product/hot'),
  reviews: (id, params) => http.get(`/api/product/${id}/reviews`, { params }),
  addReview: (id, data) => http.post(`/api/product/${id}/reviews`, data)
}

export const orderApi = {
  cart: () => http.get('/api/order/cart'),
  addCart: data => http.post('/api/order/cart', data),
  updateCart: (id, data) => http.put(`/api/order/cart/${id}`, data),
  deleteCart: id => http.delete(`/api/order/cart/${id}`),
  create: data => http.post('/api/order/create', data),
  list: params => http.get('/api/order/list', { params }),
  detail: orderNo => http.get(`/api/order/${orderNo}`),
  cancel: orderNo => http.post(`/api/order/cancel/${orderNo}`),
  confirm: orderNo => http.post(`/api/order/confirm/${orderNo}`),
  address: (orderNo, data) => http.post(`/api/order/${orderNo}/address`, data)
}

export const payApi = {
  create: orderNo => http.post('/api/pay/create', { orderNo }),
  status: orderNo => http.get(`/api/pay/status/${orderNo}`)
}

export const seckillApi = {
  activities: params => http.get('/api/seckill/activities', { params }),
  detail: id => http.get(`/api/seckill/activities/${id}`),
  buy: (activityId, productId) => http.post(`/api/seckill/${activityId}/products/${productId}`)
}

export const logisticsApi = {
  track: orderNo => http.get(`/api/logistics/track/${orderNo}`)
}

export const notifyApi = {
  announcements: () => http.get('/api/notify/announcements'),
  messages: params => http.get('/api/notify/messages', { params }),
  unreadCount: () => http.get('/api/notify/messages/unread-count'),
  markRead: id => http.post(`/api/notify/messages/${id}/read`)
}
