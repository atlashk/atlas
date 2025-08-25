import axios, { AxiosError } from "axios";
import { getCookie, isValidToken, clearAuthCookies, setCookie } from '@/utils/cookies';

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

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

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

// Enhanced request interceptor
apiClient.interceptors.request.use(
  (config) => {
    config.headers = config.headers || {};

    // Conditionally set Content-Type header
    // If data is FormData, let axios set the Content-Type with the correct boundary
    if (!(config.data instanceof FormData)) {
      config.headers["Content-Type"] = "application/json";
    }

    // Add access token with validation from cookies
    const accessToken = getAccessTokenFromCookies();
    if (isValidToken(accessToken)) {
      config.headers["Authorization"] = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Enhanced response interceptor with better race condition handling
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const originalRequest = error.config as typeof error.config & {
      _retry?: boolean;
      _retryCount?: number;
    };

    // Handle 401 errors (unauthorized)
    if (error.response?.status === 401 && !originalRequest._retry) {
      // If already refreshing, queue the request
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

      // Prevent infinite retry loops
      if (originalRequest._retryCount > MAX_REFRESH_RETRIES) {
        clearAuthCookies();
        redirectToLogin();
        return Promise.reject(new Error('Maximum refresh retries exceeded'));
      }

      const refreshToken = getRefreshTokenFromCookies();

      if (!refreshToken || !isValidToken(refreshToken)) {
        clearAuthCookies();
        redirectToLogin();
        return Promise.reject(error);
      }

      // Start refresh process
      isRefreshing = true;
      refreshPromise = performTokenRefresh(refreshToken);

      try {
        const newAccessToken = await refreshPromise;
        processQueue(null, newAccessToken);
        
        if (originalRequest.headers) {
          originalRequest.headers["Authorization"] = `Bearer ${newAccessToken}`;
        }
        
        refreshRetryCount = 0; // Reset retry count on success
        return apiClient(originalRequest);
      } catch (refreshError) {
        refreshRetryCount++;
        processQueue(refreshError as Error, null);
        
        // If we've exceeded max retries, clear tokens and redirect
        if (refreshRetryCount >= MAX_REFRESH_RETRIES) {
          clearAuthCookies();
          redirectToLogin();
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
  // Avoid redirect loops
  if (window.location.pathname !== '/login') {
    const currentPath = window.location.pathname + window.location.search;
    const loginUrl = `/login?redirect=${encodeURIComponent(currentPath)}`;
    window.location.href = loginUrl;
  }
}

// Centralized token refresh function
async function performTokenRefresh(refreshToken: string): Promise<string> {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/api/auth/refresh-token`,
      { refreshToken },
      {
        timeout: 10000, // 10 second timeout for refresh requests
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );

    const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data.data;
    
    if (!newAccessToken || !newRefreshToken) {
      throw new Error('Invalid refresh response: missing tokens');
    }
    
    // Validate new tokens before storing
    if (!isValidToken(newAccessToken)) {
      throw new Error('Received invalid access token from refresh');
    }
    
    // Store tokens in cookies using auth utilities
    setCookie('accessToken', newAccessToken);
    setCookie('refreshToken', newRefreshToken);
    
    return newAccessToken;
  } catch (error) {
    // Log refresh errors for debugging
    console.error('Token refresh failed:', error);
    throw error;
  }
}

// Export utility functions for external use
export { isValidToken };
export default apiClient;
