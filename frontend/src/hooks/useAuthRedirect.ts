"use client";

import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import { useUserStore } from '@/stores/user.store';

interface UseAuthRedirectOptions {
  requireAuth?: boolean;
  requireAdmin?: boolean;
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
    requireAdmin = false,
    redirectUnauthenticated = '/login',
    redirectUnauthorized = '/',
    allowedRoles = []
  } = options;

  const router = useRouter();
  const { isAuthenticated, profile, loading, profileLoading } = useUserStore();

  useEffect(() => {
    // Wait for both auth state and profile to load
    if (loading || profileLoading) {
      console.log('[useAuthRedirect] Waiting for auth state to load...');
      return;
    }

    const isUserAuthenticated = isAuthenticated();
    const userRole = profile?.role;

    console.log('[useAuthRedirect] Checking access:', { 
      isUserAuthenticated, 
      userRole, 
      requireAuth, 
      requireAdmin 
    });

    // Handle unauthenticated users
    if (requireAuth && !isUserAuthenticated) {
      console.log('[useAuthRedirect] User not authenticated, redirecting to login');
      const currentPath = window.location.pathname;
      const redirectUrl = `${redirectUnauthenticated}?redirect=${encodeURIComponent(currentPath)}`;
      router.push(redirectUrl);
      return;
    }

    // Handle authenticated users - role-based redirects
    if (isUserAuthenticated && userRole) {
      // Admin users should go to admin dashboard from home
      if (userRole === 'ADMIN' && window.location.pathname === '/') {
        console.log('[useAuthRedirect] Admin user on home page, redirecting to dashboard');
        router.push('/admin/dashboard');
        return;
      }

      // Regular users should be blocked from admin routes
      if (userRole === 'USER' && window.location.pathname.startsWith('/admin')) {
        console.log('[useAuthRedirect] Regular user attempting to access admin route, redirecting home');
        router.push('/');
        return;
      }

      // Check admin requirement
      if (requireAdmin && userRole !== 'ADMIN') {
        console.log('[useAuthRedirect] User lacks admin privileges, redirecting');
        router.push(redirectUnauthorized);
        return;
      }

      // Check specific role requirements
      if (allowedRoles.length > 0 && !allowedRoles.includes(userRole)) {
        console.log('[useAuthRedirect] User role not in allowed roles, redirecting');
        router.push(redirectUnauthorized);
        return;
      }
    }
  }, [loading, profileLoading, isAuthenticated, profile?.role, router, requireAuth, requireAdmin, redirectUnauthenticated, redirectUnauthorized, allowedRoles]);

  return {
    isLoading: loading || profileLoading,
    isAuthenticated: isAuthenticated(),
    userRole: profile?.role,
    canAccess: () => {
      if (loading || profileLoading) return false;
      
      const isUserAuthenticated = isAuthenticated();
      const userRole = profile?.role;

      if (requireAuth && !isUserAuthenticated) return false;
      if (requireAdmin && userRole !== 'ADMIN') return false;
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

    if (isUserAuthenticated && userRole) {
      console.log('[useGuestRedirect] User authenticated, redirecting based on role');
      // Redirect based on user role
      const destination = userRole === 'ADMIN' ? '/admin/dashboard' : '/';
      router.push(destination);
    }
  }, [loading, profileLoading, isAuthenticated, profile?.role, router]);

  return {
    isLoading: loading || profileLoading,
    shouldRedirect: isAuthenticated() && !!profile?.role
  };
}