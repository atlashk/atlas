import { Role } from "@/constants";

// Base types (used in useAuthRedirect and user.store)
export type UserRole = 'USER' | 'ADMIN'

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

export interface User {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  role: Role;
}

export interface ListUserFilters {
  id?: string;
  keyword?: string;
  role?: Role;
  page: number;
  size: number;
}

export interface RegisterRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
}
