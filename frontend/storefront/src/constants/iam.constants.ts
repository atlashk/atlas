export const ROLES = [
  "ADMIN",
  "USER"
] as const;

export type Role = typeof ROLES[number];

// Auth constants
export const AUTH_CONFIG = {
  PROFILE_CACHE_TTL: 5 * 60 * 1000, // 5 minutes
  MAX_REFRESH_RETRIES: 3,
  TOKEN_REFRESH_BUFFER: 30, // seconds
} as const;

export const AUTH_STORAGE_KEYS = {
  USER_STORE: 'user-store',
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
} as const;

export const AUTH_ENDPOINTS = {
  LOGIN: '/authentication/login',
  LOGOUT: '/authentication/logout',
  REFRESH: '/authentication/refresh-token',
  PROFILE: '/front/users/profile',
  REGISTER: '/front/users/register',
  CHANGE_PASSWORD: '/front/users/change-password',
} as const;

export const AUTH_ERRORS = {
  UNAUTHORIZED: 'UNAUTHORIZED',
  TOKEN_EXPIRED: 'TOKEN_EXPIRED',
  INVALID_CREDENTIALS: 'INVALID_CREDENTIALS',
  PROFILE_FETCH_FAILED: 'PROFILE_FETCH_FAILED',
  MAX_RETRIES_EXCEEDED: 'MAX_RETRIES_EXCEEDED',
} as const;

export type AuthError = typeof AUTH_ERRORS[keyof typeof AUTH_ERRORS];
