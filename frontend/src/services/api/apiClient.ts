import type { ApiResponse } from '@/interfaces/api.interface';
import router from '@/router/index.ts';
import { generateDeviceId } from '@/utils/deviceIdGenerator.ts';
import axios, { AxiosError } from 'axios';
import { toast } from 'vue3-toastify';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const DEVICE_ID_HEADER = 'X-Device-Id';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000, // 30 seconds timeout
  withCredentials: true, // Add withCredentials to handle CORS
});

// Request interceptor
apiClient.interceptors.request.use(
  (config) => {
    config.headers = config.headers || {};

    // Conditionally set Content-Type header
    // If data is FormData, let axios set the Content-Type with the correct boundary
    if (!(config.data instanceof FormData)) {
      config.headers['Content-Type'] = 'application/json';
    }

    // Add deviceId header
    const deviceId = getOrCreateDeviceId();
    config.headers[DEVICE_ID_HEADER] = deviceId;

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
    const originalRequest = error.config;

    // Handle 401 Unauthorized
    if (error.response?.status === 401) {
      // If refresh token exists, try to refresh
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken && originalRequest) {
        try {
          // Call your refresh token endpoint
          const refreshTokenResponse = await axios.post(`${API_BASE_URL}/api/auth/refresh-token`, {
            refreshToken
          }, {
            headers: {
              [DEVICE_ID_HEADER]: getOrCreateDeviceId()
            }
          });

          if (refreshTokenResponse.data.success) {
            // Update tokens
            localStorage.setItem('accessToken', refreshTokenResponse.data.data.accessToken);
            localStorage.setItem('refreshToken', refreshTokenResponse.data.data.refreshToken);

            // Retry original request
            originalRequest.headers['Authorization'] = `Bearer ${refreshTokenResponse.data.data.accessToken}`;
            return apiClient(originalRequest);
          }
        } catch (refreshError) {
          // If refresh fails, logout user
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          router.push({ name: 'login' });
          toast.error('Session expired. Please login again.');
        }
      } else {
        // No refresh token, redirect to login
        router.push({ name: 'login' });
        toast.error('Please login to continue.');
      }
    }

    // Get error message from response if available
    const errorMessage = error.response?.data?.errorMessage || error.message;
    return Promise.reject({ ...error, message: errorMessage });
  }
);

function getOrCreateDeviceId(): string {
  let deviceId = localStorage.getItem(DEVICE_ID_HEADER);
  if (!deviceId) {
    deviceId = generateDeviceId();
    localStorage.setItem(DEVICE_ID_HEADER, deviceId);
  }
  return deviceId;
}

export default apiClient;
