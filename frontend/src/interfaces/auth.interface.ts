// Essential auth interfaces - cleaned up version

// Base types (used in useAuthRedirect and user.store)
export type UserRole = 'USER' | 'ADMIN' | 'MODERATOR';

// Login interfaces (used in API calls)
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
}

// Token refresh interfaces (used in API calls)
export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
}
