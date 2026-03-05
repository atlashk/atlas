"use client";

import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import { useUserStore } from '@/stores/user.store';

interface UseAuthRedirectOptions {
  requireAuth?: boolean;
  redirectUnauthenticated?: string;
  redirectUnauthorized?: string;
  allowedRoles?: string[];
}

/**
 * Centralized hook for handling authentication-based redirects
 */
export function useAuthRedirect(options: UseAuthRedirectOptions = {}) {
  const {
    requireAuth = false,
    redirectUnauthenticated = '/login'
  } = options;

  const router = useRouter();
  const { isAuthenticated, loading, profileLoading } = useUserStore();

  useEffect(() => {
    // Wait for both auth state and profile to load
    if (loading || profileLoading) {
      console.log('[useAuthRedirect] Waiting for auth state to load...');
      return;
    }

    const isUserAuthenticated = isAuthenticated();

    console.log('[useAuthRedirect] Checking access:', { 
      isUserAuthenticated, 
      requireAuth
    });

    // Handle unauthenticated users
    if (requireAuth && !isUserAuthenticated) {
      console.log('[useAuthRedirect] User not authenticated, redirecting to login');
      const currentPath = window.location.pathname;
      const redirectUrl = `${redirectUnauthenticated}?redirect=${encodeURIComponent(currentPath)}`;
      router.push(redirectUrl);
      return;
    }
  }, [loading, profileLoading, isAuthenticated, router, requireAuth, redirectUnauthenticated]);

  return {
    isLoading: loading || profileLoading,
    isAuthenticated: isAuthenticated(),
    canAccess: () => {
      if (loading || profileLoading) return false;
      
      const isUserAuthenticated = isAuthenticated();

      if (requireAuth && !isUserAuthenticated) return false;

      return true;
    }
  };
}

/**
 * Hook for pages that should redirect authenticated users
 * Useful for login/register pages
 */
export function useGuestRedirect() {
  const router = useRouter();
  const { isAuthenticated, loading, profileLoading } = useUserStore();

  useEffect(() => {
    // Wait for both loading states
    if (loading || profileLoading) {
      console.log('[useGuestRedirect] Waiting for auth state to load...');
      return;
    }

    const isUserAuthenticated = isAuthenticated();

    console.log('[useGuestRedirect] Checking guest access:', { 
      isUserAuthenticated
    });

    if (isUserAuthenticated) {
      console.log('[useGuestRedirect] User authenticated, redirecting to home');
      router.push('/');
    }
  }, [loading, profileLoading, isAuthenticated, router]);

  return {
    isLoading: loading || profileLoading,
    shouldRedirect: isAuthenticated()
  };
}
