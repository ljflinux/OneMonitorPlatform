import axios from './axios'

// API服务配置
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1'
const CMDB_SERVICE_URL = import.meta.env.VITE_CMDB_SERVICE_URL || '/api/v1/cmdb'

// 创建专门的CMDB服务API客户端
const cmdbApi = axios.create({
  baseURL: CMDB_SERVICE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 复制axios实例的拦截器
const instanceInterceptors = axios.interceptors
cmdbApi.interceptors.request.use(...instanceInterceptors.request.handlers)
cmdbApi.interceptors.response.use(...instanceInterceptors.response.handlers)

// 导出API客户端
export { axios as default, cmdbApi }

// 导出API服务类
export class CMDBService {
  // CI类型相关接口
  static async getCITypes(params = {}) {
    return cmdbApi.get('/ci-types', { params })
  }

  static async getCITypeById(id) {
    return cmdbApi.get(`/ci-types/${id}`)
  }

  static async createCIType(data) {
    return cmdbApi.post('/ci-types', data)
  }

  static async updateCIType(id, data) {
    return cmdbApi.put(`/ci-types/${id}`, data)
  }

  static async deleteCIType(id) {
    return cmdbApi.delete(`/ci-types/${id}`)
  }

  // CI属性相关接口
  static async getCIAttributes(params = {}) {
    return cmdbApi.get('/ci-attributes', { params })
  }

  static async createCIAttribute(data) {
    return cmdbApi.post('/ci-attributes', data)
  }

  // CI相关接口
  static async getCIs(params = {}) {
    return cmdbApi.get('/cis', { params })
  }

  static async getCIById(id) {
    return cmdbApi.get(`/cis/${id}`)
  }

  static async getCIWithRelations(id) {
    return cmdbApi.get(`/cis/${id}/with-relations`)
  }

  static async createCI(data) {
    return cmdbApi.post('/cis', data)
  }

  static async updateCI(id, data) {
    return cmdbApi.put(`/cis/${id}`, data)
  }

  static async deleteCI(id) {
    return cmdbApi.delete(`/cis/${id}`)
  }

  static async searchCIs(params = {}) {
    return cmdbApi.get('/cis/search', { params })
  }

  // CI关系相关接口
  static async getCIRelations(params = {}) {
    return cmdbApi.get('/relations', { params })
  }

  static async createCIRelation(data) {
    return cmdbApi.post('/relations', data)
  }

  static async updateCIRelation(id, data) {
    return cmdbApi.put(`/relations/${id}`, data)
  }

  static async deleteCIRelation(id) {
    return cmdbApi.delete(`/relations/${id}`)
  }
}
