// Enhanced auth interfaces with better type safety

// Base types
export type UserRole = 'USER' | 'ADMIN' | 'MODERATOR';
export type AuthStatus = 'idle' | 'loading' | 'authenticated' | 'unauthenticated' | 'error';

// JWT Token payload interface
export interface JWTPayload {
  sub: string; // User ID
  username: string;
  roles: UserRole[];
  exp: number; // Expiration timestamp
  iat: number; // Issued at timestamp
  jti?: string; // JWT ID
}

// Login interfaces
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user?: {
    id: string;
    username: string;
    email?: string;
    fullName?: string;
    roles: UserRole[];
  };
}

// Token refresh interfaces
export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
}

// Auth state interface
export interface AuthState {
  status: AuthStatus;
  user: AuthUser | null;
  accessToken: string | null;
  refreshToken: string | null;
  error: string | null;
  isLoading: boolean;
}

// User interface for auth context
export interface AuthUser {
  id: string;
  username: string;
  email?: string;
  fullName?: string;
  roles: UserRole[];
  isActive: boolean;
  lastLoginAt?: Date;
  createdAt?: Date;
  updatedAt?: Date;
}

// Auth error types
export interface AuthError {
  code: string;
  message: string;
  details?: Record<string, unknown>;
}

// Auth validation schemas (for runtime validation)
export interface LoginValidationSchema {
  username: {
    required: boolean;
    minLength: number;
    maxLength: number;
    pattern?: RegExp;
  };
  password: {
    required: boolean;
    minLength: number;
    maxLength: number;
    pattern?: RegExp;
  };
}

// Auth configuration
export interface AuthConfig {
  tokenStorageKey: string;
  refreshTokenStorageKey: string;
  tokenRefreshThreshold: number; // seconds before expiry to refresh
  maxRefreshRetries: number;
  loginRedirectPath: string;
  logoutRedirectPath: string;
  unauthorizedRedirectPath: string;
}

// Auth event types
export type AuthEventType = 
  | 'login_success'
  | 'login_failure'
  | 'logout'
  | 'token_refresh_success'
  | 'token_refresh_failure'
  | 'session_expired'
  | 'unauthorized_access';

export interface AuthEvent {
  type: AuthEventType;
  timestamp: Date;
  data?: Record<string, unknown>;
}

// Type guards
export const isValidUserRole = (role: string): role is UserRole => {
  return ['USER', 'ADMIN', 'MODERATOR'].includes(role);
};

export const isAuthenticatedUser = (user: unknown): user is AuthUser => {
  return (
    typeof user === 'object' &&
    user !== null &&
    'id' in user &&
    'username' in user &&
    'roles' in user &&
    Array.isArray((user as AuthUser).roles)
  );
};

export const isValidJWTPayload = (payload: unknown): payload is JWTPayload => {
  return (
    typeof payload === 'object' &&
    payload !== null &&
    'sub' in payload &&
    'username' in payload &&
    'roles' in payload &&
    'exp' in payload &&
    'iat' in payload &&
    Array.isArray((payload as JWTPayload).roles)
  );
};

// Default auth configuration
export const DEFAULT_AUTH_CONFIG: AuthConfig = {
  tokenStorageKey: 'accessToken',
  refreshTokenStorageKey: 'refreshToken',
  tokenRefreshThreshold: 300, // 5 minutes
  maxRefreshRetries: 3,
  loginRedirectPath: '/login',
  logoutRedirectPath: '/',
  unauthorizedRedirectPath: '/'
};

// Validation schema
export const LOGIN_VALIDATION_SCHEMA: LoginValidationSchema = {
  username: {
    required: true,
    minLength: 3,
    maxLength: 50,
    pattern: /^[a-zA-Z0-9._-]+$/
  },
  password: {
    required: true,
    minLength: 6,
    maxLength: 128
  }
};
