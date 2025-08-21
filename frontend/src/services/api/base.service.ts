import type { ApiResponse } from "@/interfaces/api.interface";
import apiClient from "@/services/api/apiClient";
import type { AxiosResponse } from "axios";

export abstract class BaseService {
  protected readonly baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  protected async get<T>(endpoint: string): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<ApiResponse<T>> = await apiClient.get(
        `${this.baseUrl}${endpoint}`
      );
      return response.data;
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  protected async post<T, K = unknown>(
    endpoint: string,
    data?: K
  ): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<ApiResponse<T>> = await apiClient.post(
        `${this.baseUrl}${endpoint}`,
        data
      );
      return response.data;
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  protected async put<T, K = unknown>(
    endpoint: string,
    data?: K
  ): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<ApiResponse<T>> = await apiClient.put(
        `${this.baseUrl}${endpoint}`,
        data
      );
      return response.data;
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  protected async delete<T>(endpoint: string): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<ApiResponse<T>> = await apiClient.delete(
        `${this.baseUrl}${endpoint}`
      );
      return response.data;
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  private handleError<T>(error: unknown): ApiResponse<T> {
    console.error("Service error:", error);
    return {
      success: false,
      data: null as T,
      errorMessage:
        error instanceof Error ? error.message : "An error occurred",
    };
  }
}
