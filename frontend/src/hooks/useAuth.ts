"use client";

import { authApi, userApi } from '@/api';
import type { LoginRequest } from '@/interfaces/auth.interface';
import type { RegisterRequest, User } from '@/interfaces/user.interface';
import { getCookie, setCookie, clearAuthCookies, isValidToken } from '@/utils/cookies';
import { useCallback, useEffect, useState } from 'react';

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

// Cookie utilities are now imported from @/utils/cookies

export function useAuth(): UseAuthReturn {
  const [state, setState] = useState<AuthState>({
    user: null,
    accessToken: null,
    refreshToken: null,
    isLoading: true,
    error: null,
  });

  // Initialize auth state from cookies
  useEffect(() => {
    const initializeAuth = async () => {
      const accessToken = getCookie('accessToken');
      const refreshToken = getCookie('refreshToken');

      if (isValidToken(accessToken)) {
        setState(prev => ({ ...prev, accessToken, refreshToken }));
        
        // Fetch user profile
        try {
          const response = await userApi.getProfile();
          if (response.success && response.data) {
            setState(prev => ({ 
              ...prev, 
              user: response.data, 
              isLoading: false 
            }));
          } else {
            // Keep user authenticated even if profile fetch fails
            setState(prev => ({ ...prev, isLoading: false }));
          }
        } catch (error) {
          console.warn('Failed to fetch profile, keeping user authenticated:', error);
          setState(prev => ({ ...prev, isLoading: false }));
        }
      } else {
        // Clear invalid tokens
        clearAuthCookies();
        setState(prev => ({ ...prev, isLoading: false }));
      }
    };

    initializeAuth();
  }, []);

  const login = useCallback(async (credentials: LoginRequest) => {
    setState(prev => ({ ...prev, isLoading: true, error: null }));
    
    try {
      const response = await authApi.login(credentials);
      if (response.success && response.data) {
        const { accessToken, refreshToken } = response.data;
        
        // Store tokens in cookies
        setCookie('accessToken', accessToken);
        setCookie('refreshToken', refreshToken);
        
        setState(prev => ({ 
          ...prev, 
          accessToken, 
          refreshToken, 
          user: null, // Will be fetched by user store
          isLoading: false
        }));
        
        return { success: true };
      } else {
        const errorMessage = response.errorMessage || 'Login failed';
        setState(prev => ({ ...prev, error: errorMessage, isLoading: false }));
        return { success: false, errorMessage };
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Login failed';
      setState(prev => ({ ...prev, error: errorMessage, isLoading: false }));
      return { success: false, errorMessage };
    }
  }, []);

  const register = useCallback(async (userData: RegisterRequest) => {
    setState(prev => ({ ...prev, isLoading: true, error: null }));
    
    try {
      const response = await userApi.register(userData);
      setState(prev => ({ ...prev, isLoading: false }));
      
      if (response.success) {
        return { success: true };
      } else {
        const errorMessage = response.errorMessage || 'Registration failed';
        setState(prev => ({ ...prev, error: errorMessage }));
        return { success: false, errorMessage };
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Registration failed';
      setState(prev => ({ ...prev, error: errorMessage, isLoading: false }));
      return { success: false, errorMessage };
    }
  }, []);

  const logout = useCallback(() => {
    // Call logout API
    authApi.logout().catch(console.warn);
    
    // Clear state and cookies
    clearAuthCookies();
    setState({
      user: null,
      accessToken: null,
      refreshToken: null,
      isLoading: false,
      error: null,
    });
    
    // Redirect to login
    if (typeof window !== 'undefined') {
      window.location.href = '/login';
    }
  }, []);

  const clearError = useCallback(() => {
    setState(prev => ({ ...prev, error: null }));
  }, []);

  // Computed values
  const isAuthenticated = !!state.accessToken && isValidToken(state.accessToken);
  const isAdmin = state.user?.role === 'ADMIN';
  
  const hasRole = useCallback((role: string) => {
    return state.user?.role === role;
  }, [state.user?.role]);
  
  const getFullName = useCallback(() => {
    if (!state.user) return '';
    return `${state.user.firstName} ${state.user.lastName}`.trim();
  }, [state.user]);

  return {
    ...state,
    login,
    register,
    logout,
    clearError,
    isAuthenticated,
    isAdmin,
    hasRole,
    getFullName,
  };
}

// Re-export cookie utilities for backward compatibility
export { getCookie, setCookie, clearAuthCookies, isValidToken } from '@/utils/cookies';