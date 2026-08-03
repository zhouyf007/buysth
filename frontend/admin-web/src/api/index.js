import http from './http'

export const authApi = {
  login: data => http.post('/api/auth/login', data),
  me: () => http.get('/api/auth/me')
}

export const categoryApi = {
  list: () => http.get('/api/product/categories'),
  create: data => http.post('/api/admin/categories', data),
  update: (id, data) => http.put(`/api/admin/categories/${id}`, data),
  remove: id => http.delete(`/api/admin/categories/${id}`)
}

export const productApi = {
  page: params => http.get('/api/admin/products', { params }),
  hot: () => http.get('/api/product/hot'),
  categorySales: () => http.get('/api/admin/products/category-sales'),
  detail: id => http.get(`/api/admin/products/${id}`),
  create: data => http.post('/api/admin/products', data),
  update: (id, data) => http.put(`/api/admin/products/${id}`, data),
  remove: id => http.delete(`/api/admin/products/${id}`),
  status: (id, status) => http.put(`/api/admin/products/${id}/status`, { status }),
  upload: (form, type = 'product') => http.post('/api/admin/upload', form, { params: { type } })
}

export const orderApi = {
  page: params => http.get('/api/admin/orders', { params }),
  monthly: () => http.get('/api/admin/orders/monthly-stats'),
  statusStats: () => http.get('/api/admin/orders/status-stats'),
  detail: orderNo => http.get(`/api/admin/orders/${orderNo}`),
  cancel: orderNo => http.post(`/api/admin/orders/${orderNo}/cancel`),
  deleteOrder: orderNo => http.delete(`/api/admin/orders/${orderNo}`),
  batchDelete: orderNos => http.post('/api/admin/orders/batch-delete', { orderNos })
}

export const paymentApi = {
  page: params => http.get('/api/admin/payments', { params }),
  deletePayment: id => http.delete(`/api/admin/payments/${id}`),
  batchDelete: ids => http.post('/api/admin/payments/batch-delete', { ids })
}

export const activityApi = {
  page: params => http.get('/api/admin/activities', { params }),
  detail: id => http.get(`/api/admin/activities/${id}`),
  create: data => http.post('/api/admin/activities', data),
  update: (id, data) => http.put(`/api/admin/activities/${id}`, data),
  remove: id => http.delete(`/api/admin/activities/${id}`),
  status: (id, status) => http.put(`/api/admin/activities/${id}/status`, { status }),
  preload: id => http.post(`/api/admin/activities/${id}/preload`),
  products: params => http.get('/api/admin/seckill-products', { params })
}

export const logisticsApi = {
  page: params => http.get('/api/admin/shipments', { params }),
  create: orderNo => http.post('/api/admin/shipments', { orderNo }),
  track: (id, data) => http.post(`/api/admin/shipments/${id}/track`, data),
  status: (id, status) => http.put(`/api/admin/shipments/${id}/status`, { status }),
  deleteShipment: id => http.delete(`/api/admin/shipments/${id}`),
  batchDelete: ids => http.post('/api/admin/shipments/batch-delete', { ids })
}

export const userApi = {
  page: params => http.get('/api/admin/users', { params }),
  status: (id, status) => http.put(`/api/admin/users/${id}/status`, { status }),
  role: (id, roleIds) => http.put(`/api/admin/users/${id}/role`, { roleIds }),
  roles: id => http.get(`/api/admin/users/${id}/roles`)
}

export const roleApi = {
  list: () => http.get('/api/admin/roles'),
  create: data => http.post('/api/admin/roles', data),
  update: (id, data) => http.put(`/api/admin/roles/${id}`, data),
  remove: id => http.delete(`/api/admin/roles/${id}`),
  menus: () => http.get('/api/admin/menus/tree'),
  saveMenu: data => http.post('/api/admin/menus', data),
  updateMenu: (id, data) => http.put(`/api/admin/menus/${id}`, data),
  removeMenu: id => http.delete(`/api/admin/menus/${id}`)
}

export const announceApi = {
  page: params => http.get('/api/admin/announcements', { params }),
  create: data => http.post('/api/admin/announcements', data),
  update: (id, data) => http.put(`/api/admin/announcements/${id}`, data),
  remove: id => http.delete(`/api/admin/announcements/${id}`)
}

export const messageApi = {
  page: params => http.get('/api/admin/messages', { params }),
  deleteMessage: id => http.delete(`/api/admin/messages/${id}`),
  batchDelete: ids => http.post('/api/admin/messages/batch-delete', { ids })
}
