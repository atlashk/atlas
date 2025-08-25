"use client";

import React, { createContext, useContext, useEffect, useState, useMemo, useCallback } from 'react';
import { useUserStore } from '@/stores/user.store';
import type { User } from '@/interfaces/user.interface';
import type { LoginRequest } from '@/interfaces/auth.interface';

interface AuthContextType {
  // Auth state
  user: User | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isLoading: boolean;
  error: string | null;
  
  // Auth actions
  login: (credentials: LoginRequest) => Promise<{ success: boolean; errorMessage?: string; isAdmin?: boolean }>;
  logout: () => void;
  clearError: () => void;
  
  // Utility functions
  hasRole: (role: string) => boolean;
  getFullName: () => string;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: React.ReactNode;
}

export const AuthProvider = React.memo(function AuthProvider({ children }: AuthProviderProps) {
  const {
    profile,
    accessToken,
    loading,
    error,
    isAuthenticated: storeIsAuthenticated,
    isAdmin: storeIsAdmin,
    fullName,
    hasRole: storeHasRole,
    login: storeLogin,
    logout: storeLogout,
    clearError: storeClearError,
    fetchProfile
  } = useUserStore();
  
  const [isInitialized, setIsInitialized] = useState(false);
  const [initError, setInitError] = useState<string | null>(null);

  const initializeAuth = useCallback(async () => {
    try {
      setInitError(null);
      console.log('AuthContext initializing:', { accessToken: !!accessToken, profile: !!profile });
      
      // No need to fetch profile here - user store handles this during login
      // Just mark as initialized
    } catch (error) {
      console.warn('AuthContext initialization error:', error);
      setInitError(error instanceof Error ? error.message : 'Initialization failed');
    } finally {
      console.log('AuthContext initialization complete');
      setIsInitialized(true);
    }
  }, [accessToken, profile]);

  useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  // Memoize computed values to prevent unnecessary re-renders
  const isAuthenticated = useMemo(() => storeIsAuthenticated(), [storeIsAuthenticated]);
  const isAdmin = useMemo(() => storeIsAdmin(), [storeIsAdmin]);
  const isLoading = useMemo(() => loading || !isInitialized, [loading, isInitialized]);
  const currentError = useMemo(() => error || initError, [error, initError]);
  
  // Memoize callback functions to prevent unnecessary re-renders
  const handleClearError = useCallback(() => {
    setInitError(null);
    storeClearError();
  }, [storeClearError]);
  
  const contextValue: AuthContextType = useMemo(() => ({
    // Auth state
    user: profile,
    isAuthenticated,
    isAdmin,
    isLoading,
    error: currentError,
    
    // Auth actions
    login: storeLogin,
    logout: storeLogout,
    clearError: handleClearError,
    
    // Utility functions
    hasRole: storeHasRole,
    getFullName: fullName
  }), [profile, isAuthenticated, isAdmin, isLoading, currentError, storeLogin, storeLogout, handleClearError, storeHasRole, fullName]);

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
});

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
