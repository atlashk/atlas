export const APP_CONFIG = {
  API_TIMEOUT: 30000,
  TOAST_DURATION: 3000,
  STORAGE_KEYS: {
    ACCESS_TOKEN: 'accessToken',
    REFRESH_TOKEN: 'refreshToken',
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
