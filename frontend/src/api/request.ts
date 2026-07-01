import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

service.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResult<unknown> | unknown

    if (!res || typeof res !== 'object' || !('code' in res)) {
      return res as typeof response
    }

    const apiResult = res as ApiResult<unknown>

    if (apiResult.code !== 200) {
      const message = apiResult.message || '请求失败'

      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }

    return apiResult.data as typeof response
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络异常'

    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
)

const request = {
  get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config) as unknown as Promise<T>
  },

  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config) as unknown as Promise<T>
  },

  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config) as unknown as Promise<T>
  },

  delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config) as unknown as Promise<T>
  },
}

export default request
