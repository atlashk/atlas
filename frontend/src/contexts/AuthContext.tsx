"use client";

import LoadingScreen from '@/components/common/LoadingScreen';
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
  const profile = useUserStore(state => state.profile);
  const loading = useUserStore(state => state.loading);
  const profileLoading = useUserStore(state => state.profileLoading);
  const error = useUserStore(state => state.error);
  const storeIsAuthenticated = useUserStore(state => state.isAuthenticated);
  const storeIsAdmin = useUserStore(state => state.isAdmin);
  const fullName = useUserStore(state => state.fullName);
  const storeHasRole = useUserStore(state => state.hasRole);
  const storeLogin = useUserStore(state => state.login);
  const storeLogout = useUserStore(state => state.logout);
  const storeClearError = useUserStore(state => state.clearError);
  const initializeFromCookies = useUserStore(state => state.initializeFromCookies);
  
  const [isInitialized, setIsInitialized] = useState(false);
  const [initError, setInitError] = useState<string | null>(null);
  const initStartedRef = useRef(false);

  const initializeAuth = useCallback(async () => {
    try {
      setInitError(null);
      console.log('[AuthProvider] Starting authentication initialization...');

      await useUserStore.persist.rehydrate();
      
      initializeFromCookies();

      const { accessToken, fetchProfile } = useUserStore.getState();
      
      if (accessToken) {
        console.log('[AuthProvider] Valid token found, fetching user profile...');
        await fetchProfile({ skipCache: false });
        const { profile } = useUserStore.getState();
        console.log('[AuthProvider] Profile loaded:', profile?.email);
      } else {
        console.log('[AuthProvider] No valid token found, skipping profile fetch');
      }
    } catch (error) {
      console.error('[AuthProvider] Initialization error:', error);
      setInitError(error instanceof Error ? error.message : 'Initialization failed');
      const { clearAuthState } = useUserStore.getState();
      clearAuthState();
    } finally {
      console.log('[AuthProvider] Initialization complete');
      setIsInitialized(true);
    }
  }, [initializeFromCookies]);

  useEffect(() => {
    if (initStartedRef.current) return;
    initStartedRef.current = true;
    void initializeAuth();
  }, [initializeAuth]);

  const isAuthenticated = useMemo(() => storeIsAuthenticated(), [storeIsAuthenticated]);
  const isAdmin = useMemo(() => storeIsAdmin(), [storeIsAdmin]);
  const isLoading = useMemo(() => loading || profileLoading || !isInitialized, [loading, profileLoading, isInitialized]);
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
      {isInitialized ? (
        children
      ) : (
        <LoadingScreen message="Initializing application..." />
      )}
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
