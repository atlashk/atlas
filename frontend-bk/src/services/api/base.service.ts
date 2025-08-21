import type { ApiResponse } from '@/interfaces/api.interface'
import apiClient from '@/services/api/apiClient'
import type { AxiosResponse } from 'axios'

export abstract class BaseService {
  protected readonly baseUrl: string

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl
  }

  protected async get<T>(endpoint: string): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<ApiResponse<T>> = await apiClient.get(`${this.baseUrl}${endpoint}`)
      return response.data
    } catch (error) {
      return this.handleError(error)
    }
  }

  protected async post<T, K = any>(endpoint: string, data?: K): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<ApiResponse<T>> = await apiClient.post(`${this.baseUrl}${endpoint}`, data)
      return response.data
    } catch (error) {
      return this.handleError(error)
    }
  }

  protected async put<T, K = any>(endpoint: string, data?: K): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<ApiResponse<T>> = await apiClient.put(`${this.baseUrl}${endpoint}`, data)
      return response.data
    } catch (error) {
      return this.handleError(error)
    }
  }

  protected async delete<T>(endpoint: string): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<ApiResponse<T>> = await apiClient.delete(`${this.baseUrl}${endpoint}`)
      return response.data
    } catch (error) {
      return this.handleError(error)
    }
  }

  private handleError(error: any): ApiResponse<any> {
    console.error('Service error:', error)
    return {
      success: false,
      data: null,
      errorMessage: error?.message || 'An error occurred'
    }
  }
}
