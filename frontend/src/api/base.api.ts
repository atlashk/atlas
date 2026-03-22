import apiClient, { ApiResponse } from "@/api/apiClient";
import type { AxiosError, AxiosResponse } from "axios";

export abstract class BaseApi {
  protected baseUrl: string;

  protected constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  protected async get<T>(endpoint: string): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<ApiResponse<T>> = await apiClient.get(
        `${this.baseUrl}${endpoint}`,
      );
      return response.data;
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  protected async post<T, K = unknown>(
    endpoint: string,
    data?: K,
  ): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<ApiResponse<T>> = await apiClient.post(
        `${this.baseUrl}${endpoint}`,
        data,
      );
      return response.data;
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  protected async put<T, K = unknown>(
    endpoint: string,
    data?: K,
  ): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<ApiResponse<T>> = await apiClient.put(
        `${this.baseUrl}${endpoint}`,
        data,
      );
      return response.data;
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  protected async delete<T>(endpoint: string): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<ApiResponse<T>> = await apiClient.delete(
        `${this.baseUrl}${endpoint}`,
      );
      return response.data;
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  protected async getBlob(endpoint: string): Promise<AxiosResponse<Blob>> {
    return await apiClient.get(`${this.baseUrl}${endpoint}`, {
      responseType: "blob",
    });
  }

  protected async postBlob<K = unknown>(
    endpoint: string,
    data?: K,
  ): Promise<AxiosResponse<Blob>> {
    return await apiClient.post(`${this.baseUrl}${endpoint}`, data, {
      responseType: "blob",
    });
  }

  private handleError<T>(error: unknown): ApiResponse<T> {
    console.error("Service error:", error);
    const fallbackMessage =
      error instanceof Error ? error.message : "An error occurred";

    if (
      typeof error === "object" &&
      error !== null &&
      "isAxiosError" in error &&
      (error as { isAxiosError?: boolean }).isAxiosError
    ) {
      const axiosError = error as AxiosError<unknown>;
      const statusCode = axiosError.response?.status;
      const responseData = axiosError.response?.data;

      if (responseData && typeof responseData === "object") {
        const apiResponseData = responseData as Partial<ApiResponse<unknown>>;

        const errorMessage = apiResponseData.errorMessage || fallbackMessage;

        const errorCode =
          apiResponseData.errorCode ??
          (statusCode !== undefined ? String(statusCode) : undefined);

        return {
          success: false,
          data: null as T,
          errorCode: errorCode !== undefined ? String(errorCode) : undefined,
          errorMessage,
        };
      }

      if (typeof responseData === "string") {
        return {
          success: false,
          data: null as T,
          errorCode: statusCode !== undefined ? String(statusCode) : undefined,
          errorMessage: responseData,
        };
      }

      return {
        success: false,
        data: null as T,
        errorCode: statusCode !== undefined ? String(statusCode) : undefined,
        errorMessage: fallbackMessage,
      };
    }

    return {
      success: false,
      data: null as T,
      errorMessage: fallbackMessage,
    };
  }
}
