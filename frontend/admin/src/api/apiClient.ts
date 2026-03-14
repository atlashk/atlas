import { clearAuthCookies, getCookie, isValidToken, setCookie } from '@/utils/cookies';
import axios, { AxiosError } from "axios";
import { API_BASE_URL } from '@/config/env.config';

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  metadata?: Metadata;
  errorCode?: string;
  errorMessage?: string;
}

export interface Metadata {
  currentPage: number;
  pageSize: number;
  totalPages: number;
  totalRecords: number;
}

// Enhanced token refresh state management
let isRefreshing = false;
let refreshPromise: Promise<string> | null = null;
let failedQueue: Array<{
  resolve: (value: string | null) => void;
  reject: (reason: Error) => void;
}> = [];

// Maximum retry attempts for token refresh
const MAX_REFRESH_RETRIES = 3;
let refreshRetryCount = 0;

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error);
    } else {
      resolve(token);
    }
  });

  failedQueue = [];
};

// Token validation is now imported from auth hook

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000, // 30 seconds timeout
  withCredentials: true, // Add withCredentials to handle CORS
});

// Helper function to get token from cookies
const getAccessTokenFromCookies = (): string | null => {
  return getCookie('accessToken');
};

const getRefreshTokenFromCookies = (): string | null => {
  return getCookie('refreshToken');
};

const shouldSkipAuthRedirect = (requestUrl?: string): boolean => {
  if (!requestUrl) return false;
  return requestUrl.includes('/services/identity/api/users/profile');
};

const isUnauthorizedRefreshError = (error: unknown): boolean => {
  if (!axios.isAxiosError(error)) return false;

  const status = error.response?.status;
  const apiErrorCode = error.response?.data?.errorCode;

  return status === 401 || apiErrorCode === '401' || apiErrorCode === 401;
};

apiClient.interceptors.request.use(
  (config) => {
    config.headers = config.headers || {};

    if (!(config.data instanceof FormData)) {
      config.headers["Content-Type"] = "application/json";
    }

    const accessToken = getAccessTokenFromCookies();
    if (accessToken) {
      config.headers["Authorization"] = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const originalRequest = error.config as typeof error.config & {
      _retry?: boolean;
      _retryCount?: number;
    };

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing && refreshPromise) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers && token) {
              originalRequest.headers["Authorization"] = `Bearer ${token}`;
            }
            return apiClient(originalRequest);
          })
          .catch((err) => {
            return Promise.reject(err);
          });
      }

      originalRequest._retry = true;
      originalRequest._retryCount = (originalRequest._retryCount || 0) + 1;

      if (originalRequest._retryCount > MAX_REFRESH_RETRIES) {
        clearAuthCookies();
        if (!shouldSkipAuthRedirect(originalRequest.url)) {
          redirectToLogin();
        }
        return Promise.reject(new Error('Maximum refresh retries exceeded'));
      }

      const refreshToken = getRefreshTokenFromCookies();

      if (!refreshToken) {
        clearAuthCookies();
        if (!shouldSkipAuthRedirect(originalRequest.url)) {
          redirectToLogin();
        }
        return Promise.reject(error);
      }

      isRefreshing = true;
      refreshPromise = performTokenRefresh(refreshToken);

      try {
        const newAccessToken = await refreshPromise;
        processQueue(null, newAccessToken);
        
        if (originalRequest.headers) {
          originalRequest.headers["Authorization"] = `Bearer ${newAccessToken}`;
        }
        
        refreshRetryCount = 0;
        return apiClient(originalRequest);
      } catch (refreshError) {
        if (isUnauthorizedRefreshError(refreshError)) {
          processQueue(refreshError as Error, null);
          clearAuthCookies();
          if (!shouldSkipAuthRedirect(originalRequest.url)) {
            redirectToLogin();
          }
          refreshRetryCount = 0;
          return Promise.reject(refreshError);
        }

        refreshRetryCount++;
        processQueue(refreshError as Error, null);
        
        if (refreshRetryCount >= MAX_REFRESH_RETRIES) {
          clearAuthCookies();
          if (!shouldSkipAuthRedirect(originalRequest.url)) {
            redirectToLogin();
          }
          refreshRetryCount = 0;
        }
        
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
        refreshPromise = null;
      }
    }

    return Promise.reject(error);
  }
);

// Helper functions
function redirectToLogin(): void {
  if (typeof window === 'undefined') {
    return;
  }

  if (window.location.pathname !== '/login') {
    const currentPath = window.location.pathname + window.location.search;
    const loginUrl = `/login?redirect=${encodeURIComponent(currentPath)}`;
    window.location.href = loginUrl;
  }
}

async function performTokenRefresh(refreshToken: string): Promise<string> {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/services/identity/api/authentication/refresh-token`,
      { refreshToken },
      {
        timeout: 10000,
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );

    const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data.data;
    
    if (!newAccessToken || !newRefreshToken) {
      throw new Error('Invalid refresh response: missing tokens');
    }
    
    setCookie('accessToken', newAccessToken);
    setCookie('refreshToken', newRefreshToken);
    
    return newAccessToken;
  } catch (error) {
    if (isUnauthorizedRefreshError(error)) {
      clearAuthCookies();
      redirectToLogin();
    }

    console.error('Token refresh failed:', error);
    throw error;
  }
}

// Export utility functions for external use
export { isValidToken };
export default apiClient;
