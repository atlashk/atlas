import type { ApiResponse } from '@/interfaces/api.interface';
import { generateDeviceId } from '@/utils/deviceIdGenerator.ts';
import { performanceMonitor } from '@/utils/performance';
import axios, { AxiosError } from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const DEVICE_ID_HEADER = 'X-Device-Id';

// Token refresh state management
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value: any) => void;
  reject: (reason: any) => void;
}> = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error);
    } else {
      resolve(token);
    }
  });

  failedQueue = [];
};

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000, // 30 seconds timeout
  withCredentials: true, // Add withCredentials to handle CORS
});

// Cache for device ID to avoid repeated localStorage access
let cachedDeviceId: string | null = null;

function getOrCreateDeviceId(): string {
  if (cachedDeviceId) {
    return cachedDeviceId;
  }

  let deviceId = localStorage.getItem(DEVICE_ID_HEADER);
  if (!deviceId) {
    deviceId = generateDeviceId();
    localStorage.setItem(DEVICE_ID_HEADER, deviceId);
  }

  cachedDeviceId = deviceId;
  return deviceId;
}

// Request interceptor
apiClient.interceptors.request.use(
  (config) => {
    config.headers = config.headers || {};

    // Conditionally set Content-Type header
    // If data is FormData, let axios set the Content-Type with the correct boundary
    if (!(config.data instanceof FormData)) {
      config.headers['Content-Type'] = 'application/json';
    }

    // Add deviceId header (cached)
    config.headers[DEVICE_ID_HEADER] = getOrCreateDeviceId();

    // Add access token
    const accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
      config.headers['Authorization'] = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<any>>) => {
    const originalRequest = error.config as any;

    // Handle 401 Unauthorized
    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      const refreshToken = localStorage.getItem('refreshToken');

      if (!refreshToken) {
        // No refresh token, clear auth state immediately
        clearAuthTokens();
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // If already refreshing, queue this request
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(token => {
          originalRequest.headers['Authorization'] = `Bearer ${token}`;
          return apiClient(originalRequest);
        }).catch(err => {
          return Promise.reject(err);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        performanceMonitor.trackTokenRefresh()

        const refreshTokenResponse = await axios.post(`${API_BASE_URL}/api/auth/refresh-token`, {
          refreshToken
        }, {
          headers: {
            [DEVICE_ID_HEADER]: getOrCreateDeviceId()
          }
        });

        if (refreshTokenResponse.data.success) {
          const newAccessToken = refreshTokenResponse.data.data.accessToken;
          const newRefreshToken = refreshTokenResponse.data.data.refreshToken;

          // Update tokens
          localStorage.setItem('accessToken', newAccessToken);
          localStorage.setItem('refreshToken', newRefreshToken);

          // Process queued requests
          processQueue(null, newAccessToken);

          // Retry original request
          originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
          return apiClient(originalRequest);
        } else {
          throw new Error('Token refresh failed');
        }
      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError);
        clearAuthTokens();
        processQueue(refreshError, null);
        return Promise.reject(error);
      } finally {
        isRefreshing = false;
      }
    }

    // Get error message from response if available
    const errorMessage = error.response?.data?.errorMessage || error.message;
    return Promise.reject({ ...error, message: errorMessage });
  }
);

function clearAuthTokens(): void {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
}

export default apiClient;
