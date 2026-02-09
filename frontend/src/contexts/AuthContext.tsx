"use client";

import type { LoginRequest } from '@/interfaces/auth.interface';
import type { User } from '@/interfaces/user.interface';
import { useUserStore } from '@/stores/user.store';
import React, { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';

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
    loading,
    error,
    isAuthenticated: storeIsAuthenticated,
    isAdmin: storeIsAdmin,
    fullName,
    hasRole: storeHasRole,
    login: storeLogin,
    logout: storeLogout,
    clearError: storeClearError,
    initializeFromCookies
  } = useUserStore();
  
  const [isInitialized, setIsInitialized] = useState(false);
  const [initError, setInitError] = useState<string | null>(null);
  const initStartedRef = useRef(false);

  const initializeAuth = useCallback(async () => {
    try {
      setInitError(null);
      const { accessToken, profile } = useUserStore.getState();
      console.log('AuthContext initializing:', { accessToken: !!accessToken, profile: !!profile });

      await useUserStore.persist.rehydrate();
      
      // Initialize store from cookies to sync state
      initializeFromCookies();

      const { fetchProfile } = useUserStore.getState();
      await fetchProfile({ force: true });
    } catch (error) {
      console.warn('AuthContext initialization error:', error);
      setInitError(error instanceof Error ? error.message : 'Initialization failed');
    } finally {
      console.log('AuthContext initialization complete');
      setIsInitialized(true);
    }
  }, [initializeFromCookies]);

  useEffect(() => {
    if (initStartedRef.current) return;
    initStartedRef.current = true;
    void initializeAuth();
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
      {isInitialized ? children : null}
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
