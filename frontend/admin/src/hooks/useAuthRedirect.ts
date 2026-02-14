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
 * Simplifies role-based routing logic across the application
 */
export function useAuthRedirect(options: UseAuthRedirectOptions = {}) {
  const {
    requireAuth = false,
    redirectUnauthenticated = '/login',
    redirectUnauthorized = '/',
    allowedRoles = []
  } = options;

  const router = useRouter();
  const { isAuthenticated, profile, loading, profileLoading } = useUserStore();

  useEffect(() => {
    if (loading || profileLoading) {
      console.log('[useAuthRedirect] Waiting for auth state to load...');
      return;
    }

    const isUserAuthenticated = isAuthenticated();
    const userRole = profile?.role;

    console.log('[useAuthRedirect] Checking access:', { 
      isUserAuthenticated, 
      userRole, 
      requireAuth
    });

    if (requireAuth && !isUserAuthenticated) {
      console.log('[useAuthRedirect] User not authenticated, redirecting to login');
      const currentPath = window.location.pathname;
      const redirectUrl = `${redirectUnauthenticated}?redirect=${encodeURIComponent(currentPath)}`;
      router.push(redirectUrl);
      return;
    }

    if (isUserAuthenticated && userRole) {
      if (allowedRoles.length > 0 && !allowedRoles.includes(userRole)) {
        console.log('[useAuthRedirect] User role not in allowed roles');
        return;
      }
    }
  }, [loading, profileLoading, isAuthenticated, profile?.role, router, requireAuth, redirectUnauthenticated, redirectUnauthorized, allowedRoles]);

  const isUserAuthenticated = isAuthenticated();
  const userRole = profile?.role;
  const isUnauthorized = !loading && !profileLoading && isUserAuthenticated && userRole && allowedRoles.length > 0 && !allowedRoles.includes(userRole);

  return {
    isLoading: loading || profileLoading,
    isAuthenticated: isUserAuthenticated,
    userRole,
    isUnauthorized,
    canAccess: () => {
      if (loading || profileLoading) return false;
      
      if (requireAuth && !isUserAuthenticated) return false;
      if (allowedRoles.length > 0 && userRole && !allowedRoles.includes(userRole)) return false;

      return true;
    }
  };
}

/**
 * Hook for pages that should redirect authenticated users based on their role
 * Useful for login/register pages
 */
export function useGuestRedirect() {
  const router = useRouter();
  const { isAuthenticated, profile, loading, profileLoading } = useUserStore();

  useEffect(() => {
    // Wait for both loading states
    if (loading || profileLoading) {
      console.log('[useGuestRedirect] Waiting for auth state to load...');
      return;
    }

    const isUserAuthenticated = isAuthenticated();
    const userRole = profile?.role;

    console.log('[useGuestRedirect] Checking guest access:', { 
      isUserAuthenticated, 
      userRole 
    });

    if (isUserAuthenticated && userRole === 'ADMIN') {
      console.log('[useGuestRedirect] Admin authenticated, redirecting to dashboard');
      router.push('/admin/dashboard');
    }
  }, [loading, profileLoading, isAuthenticated, profile?.role, router]);

  return {
    isLoading: loading || profileLoading,
    shouldRedirect: isAuthenticated() && profile?.role === 'ADMIN'
  };
}
