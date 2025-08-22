import type { ApiResponse } from "@/interfaces/api.interface";
import axios, { AxiosError } from "axios";

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

// Enhanced token validation
const isTokenValid = (token: string | null): boolean => {
  if (!token) return false;
  
  try {
    // Basic JWT structure validation
    const parts = token.split('.');
    if (parts.length !== 3) return false;
    
    // Decode payload to check expiration
    const payload = JSON.parse(atob(parts[1]));
    const currentTime = Math.floor(Date.now() / 1000);
    
    // Check if token expires within next 30 seconds (buffer time)
    return payload.exp && payload.exp > (currentTime + 30);
  } catch {
    return false;
  }
};

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000, // 30 seconds timeout
  withCredentials: true, // Add withCredentials to handle CORS
});

// Enhanced request interceptor
apiClient.interceptors.request.use(
  (config) => {
    config.headers = config.headers || {};

    // Conditionally set Content-Type header
    // If data is FormData, let axios set the Content-Type with the correct boundary
    if (!(config.data instanceof FormData)) {
      config.headers["Content-Type"] = "application/json";
    }

    // Add access token with validation
    const accessToken = localStorage.getItem("accessToken");
    if (isTokenValid(accessToken)) {
      config.headers["Authorization"] = `Bearer ${accessToken}`;
    } else if (accessToken) {
      // Token exists but is invalid/expired, remove it
      localStorage.removeItem("accessToken");
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
        clearAuthTokens();
        redirectToLogin();
        return Promise.reject(new Error('Maximum refresh retries exceeded'));
      }

      const refreshToken = localStorage.getItem("refreshToken");

      if (!refreshToken || !isTokenValid(refreshToken)) {
        clearAuthTokens();
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
          clearAuthTokens();
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
function clearAuthTokens(): void {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
  
  // Clear any auth-related data from sessionStorage as well
  sessionStorage.removeItem("user");
  sessionStorage.removeItem("authState");
}

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
    if (!isTokenValid(newAccessToken)) {
      throw new Error('Received invalid access token from refresh');
    }
    
    localStorage.setItem("accessToken", newAccessToken);
    localStorage.setItem("refreshToken", newRefreshToken);
    
    return newAccessToken;
  } catch (error) {
    // Log refresh errors for debugging
    console.error('Token refresh failed:', error);
    throw error;
  }
}

// Export utility functions for external use
export { clearAuthTokens, isTokenValid };
export default apiClient;
