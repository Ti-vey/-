import request from '@/utils/request'

/** 认证相关接口 */
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  me: () => request.get('/auth/me'),
  logout: () => request.post('/auth/logout')
}

/**
 * 基础数据通用接口工厂。
 * 后端七类基础数据（单位/分类/仓库/库位/供应商/客户/公告）采用统一的 RESTful 规范，
 * 因此这里用一个工厂函数生成对应的接口对象，避免重复定义。
 */
const buildCrud = (base) => ({
  page: (params) => request.get(`${base}/page`, { params }),
  list: () => request.get(`${base}/list`),
  detail: (id) => request.get(`${base}/${id}`),
  save: (data) => request.post(base, data),
  update: (data) => request.put(base, data),
  remove: (id) => request.delete(`${base}/${id}`)
})

export const unitApi = buildCrud('/unit')
export const categoryApi = buildCrud('/category')
export const warehouseApi = buildCrud('/warehouse')
export const locationApi = buildCrud('/location')
export const supplierApi = buildCrud('/supplier')
export const customerApi = buildCrud('/customer')
export const noticeApi = buildCrud('/notice')

/** 物料档案：查询条件较多，单独定义 */
export const materialApi = {
  page: (params) => request.get('/material/page', { params }),
  list: (params) => request.get('/material/list', { params }),
  detail: (id) => request.get(`/material/${id}`),
  save: (data) => request.post('/material', data),
  update: (data) => request.put('/material', data),
  remove: (id) => request.delete(`/material/${id}`)
}

/** 用户与角色 */
export const userApi = {
  page: (params) => request.get('/user/page', { params }),
  roles: () => request.get('/role/list'),
  save: (data, roleId) => request.post(`/user?roleId=${roleId || ''}`, data),
  update: (data, roleId) => request.put(`/user?roleId=${roleId || ''}`, data),
  resetPassword: (id) => request.put(`/user/${id}/reset-password`),
  changeStatus: (id, status) => request.put(`/user/${id}/status?status=${status}`),
  remove: (id) => request.delete(`/user/${id}`)
}

/** 入库单 */
export const inboundApi = {
  page: (params) => request.get('/inbound/page', { params }),
  detail: (id) => request.get(`/inbound/${id}`),
  save: (data) => request.post('/inbound', data),
  update: (data) => request.put('/inbound', data),
  submit: (id) => request.post(`/inbound/${id}/submit`),
  audit: (data) => request.post('/inbound/audit', data),
  remove: (id) => request.delete(`/inbound/${id}`)
}

/** 出库单 */
export const outboundApi = {
  page: (params) => request.get('/outbound/page', { params }),
  detail: (id) => request.get(`/outbound/${id}`),
  save: (data) => request.post('/outbound', data),
  update: (data) => request.put('/outbound', data),
  submit: (id) => request.post(`/outbound/${id}/submit`),
  audit: (data) => request.post('/outbound/audit', data),
  remove: (id) => request.delete(`/outbound/${id}`)
}

/** 调拨单 */
export const transferApi = {
  page: (params) => request.get('/transfer/page', { params }),
  detail: (id) => request.get(`/transfer/${id}`),
  save: (data) => request.post('/transfer', data),
  update: (data) => request.put('/transfer', data),
  submit: (id) => request.post(`/transfer/${id}/submit`),
  audit: (data) => request.post('/transfer/audit', data),
  remove: (id) => request.delete(`/transfer/${id}`)
}

/** 盘点单 */
export const stocktakingApi = {
  page: (params) => request.get('/stocktaking/page', { params }),
  detail: (id) => request.get(`/stocktaking/${id}`),
  create: (params) => request.post('/stocktaking/create', null, { params }),
  saveActual: (id, data) => request.put(`/stocktaking/${id}/actual`, data),
  finish: (id) => request.post(`/stocktaking/${id}/finish`)
}

/** 库存查询与分析 */
export const stockApi = {
  page: (params) => request.get('/stock/page', { params }),
  available: (params) => request.get('/stock/available', { params }),
  flowPage: (params) => request.get('/stock/flow/page', { params }),
  amountByWarehouse: () => request.get('/stock/amount-by-warehouse'),
  abc: () => request.get('/stock/abc'),
  turnover: (days) => request.get('/stock/turnover', { params: { days } }),
  // 库存多维度汇总
  summaryByMaterial: () => request.get('/stock/summary-by-material'),
  summaryByWarehouse: () => request.get('/stock/summary-by-warehouse')
}

/** 补货计划 */
export const replenishmentApi = {
  page: (params) => request.get('/replenishment/page', { params }),
  generate: () => request.post('/replenishment/generate'),
  changeStatus: (id, status, remark) =>
    request.put(`/replenishment/${id}/status`, null, { params: { status, remark } })
}

/** 库存预警 */
export const warningApi = {
  page: (params) => request.get('/warning/page', { params }),
  generate: () => request.post('/warning/generate'),
  handle: (id, remark) => request.put(`/warning/${id}/handle`, null, { params: { remark } })
}

/** 统计分析 */
export const statApi = {
  dashboard: () => request.get('/stat/dashboard'),
  trend: (days) => request.get('/stat/trend', { params: { days } }),
  topMaterials: (limit) => request.get('/stat/top-materials', { params: { limit } }),
  // 首页提醒：低库存清单与临期批次清单
  lowStock: (limit) => request.get('/stat/low-stock', { params: { limit } }),
  expiring: (limit) => request.get('/stat/expiring', { params: { limit } })
}
