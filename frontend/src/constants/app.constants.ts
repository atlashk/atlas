export const APP_CONFIG = {
  API_TIMEOUT: 30000,
  TOAST_DURATION: 3000,
  DEVICE_ID_HEADER: 'X-Device-Id',
  STORAGE_KEYS: {
    ACCESS_TOKEN: 'accessToken',
    REFRESH_TOKEN: 'refreshToken',
    DEVICE_ID: 'X-Device-Id'
  }
} as const

export enum Role {
  ADMIN = 'ADMIN',
  USER = 'USER'
}

export enum OrderStatus {
  PROCESSING = 'PROCESSING',
  CONFIRMED = 'CONFIRMED',
  CANCELED = 'CANCELED'
}
