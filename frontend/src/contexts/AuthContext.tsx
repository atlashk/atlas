"use client";

import React, { createContext, useContext, useEffect, useState } from 'react';
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

export function AuthProvider({ children }: AuthProviderProps) {
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

  useEffect(() => {
    const initializeAuth = async () => {
      // If we have an access token but no profile, fetch it
      if (accessToken && !profile) {
        try {
          await fetchProfile();
        } catch (error) {
          console.error('Failed to fetch profile on initialization:', error);
        }
      }
      setIsInitialized(true);
    };

    initializeAuth();
  }, [accessToken, profile, fetchProfile]);

  const contextValue: AuthContextType = {
    // Auth state
    user: profile,
    isAuthenticated: storeIsAuthenticated(),
    isAdmin: storeIsAdmin(),
    isLoading: loading || !isInitialized,
    error,
    
    // Auth actions
    login: storeLogin,
    logout: storeLogout,
    clearError: storeClearError,
    
    // Utility functions
    hasRole: storeHasRole,
    getFullName: fullName
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
