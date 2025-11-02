"use client";

import { useAuth as useAuthContext } from '@/contexts/AuthContext';
import type { LoginRequest } from '@/interfaces/auth.interface';
import type { RegisterRequest, User } from '@/interfaces/user.interface';

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isLoading: boolean;
  error: string | null;
}

interface AuthActions {
  login: (credentials: LoginRequest) => Promise<{ success: boolean; errorMessage?: string }>;
  register: (userData: RegisterRequest) => Promise<{ success: boolean; errorMessage?: string }>;
  logout: () => void;
  clearError: () => void;
  isAuthenticated: boolean;
  isAdmin: boolean;
  hasRole: (role: string) => boolean;
  getFullName: () => string;
}

type UseAuthReturn = AuthState & AuthActions;

/**
 * useAuth hook that delegates to AuthContext to avoid duplicate API calls
 * This ensures we have a single source of truth for authentication state
 */
export function useAuth(): UseAuthReturn {
  const authContext = useAuthContext();
  
  return {
    // Map AuthContext state to useAuth interface
    user: authContext.user,
    accessToken: null, // AuthContext doesn't expose tokens directly for security
    refreshToken: null, // AuthContext doesn't expose tokens directly for security
    isLoading: authContext.isLoading,
    error: authContext.error,
    
    // Map AuthContext actions
    login: authContext.login,
    register: async (userData: RegisterRequest) => {
      // AuthContext doesn't have register, so we'll need to handle this differently
      // For now, throw an error to indicate this needs to be implemented
      throw new Error('Register functionality should be implemented through UserStore directly');
    },
    logout: authContext.logout,
    clearError: authContext.clearError,
    
    // Map AuthContext computed values
    isAuthenticated: authContext.isAuthenticated,
    isAdmin: authContext.isAdmin,
    hasRole: authContext.hasRole,
    getFullName: authContext.getFullName,
  };
}

// Re-export cookie utilities for backward compatibility
export { getCookie, setCookie, clearAuthCookies, isValidToken } from '@/utils/cookies';