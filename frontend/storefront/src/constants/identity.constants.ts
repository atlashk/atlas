export const ROLES = [
  "USER",
  "ADMIN"
] as const;

export type Role = typeof ROLES[number];

export const AUTH_STORAGE_KEYS = {
  USER_STORE: 'user-store',
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
} as const;
