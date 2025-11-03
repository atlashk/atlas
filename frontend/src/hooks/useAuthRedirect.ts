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
  const { isAuthenticated, profile, loading } = useUserStore();

  useEffect(() => {
    // Wait for auth state to load
    if (loading) return;

    const isUserAuthenticated = isAuthenticated();
    const userRole = profile?.role;

    // Handle unauthenticated users
    if (requireAuth && !isUserAuthenticated) {
      const currentPath = window.location.pathname;
      const redirectUrl = `${redirectUnauthenticated}?redirect=${encodeURIComponent(currentPath)}`;
      router.push(redirectUrl);
      return;
    }

    // Handle authenticated users - role-based redirects
    if (isUserAuthenticated && userRole) {
      // Admin users should go to admin dashboard
      if (userRole === 'ADMIN' && window.location.pathname === '/') {
        router.push('/admin/dashboard');
        return;
      }

      // Regular users should go to main page if they're on admin routes
      if (userRole === 'USER' && window.location.pathname.startsWith('/admin')) {
        router.push('/');
        return;
      }

      // Check admin requirement
      if (requireAdmin && userRole !== 'ADMIN') {
        router.push(redirectUnauthorized);
        return;
      }

      // Check specific role requirements
      if (allowedRoles.length > 0 && !allowedRoles.includes(userRole)) {
        router.push(redirectUnauthorized);
        return;
      }
    }
  }, [loading, isAuthenticated, profile?.role, router, requireAuth, requireAdmin, redirectUnauthenticated, redirectUnauthorized, allowedRoles]);

  return {
    isLoading: loading,
    isAuthenticated: isAuthenticated(),
    userRole: profile?.role,
    canAccess: () => {
      if (loading) return false;
      
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
  const { isAuthenticated, profile, loading } = useUserStore();

  useEffect(() => {
    if (loading) return;

    const isUserAuthenticated = isAuthenticated();
    const userRole = profile?.role;

    if (isUserAuthenticated && userRole) {
      // Redirect based on user role
      const destination = userRole === 'ADMIN' ? '/admin/dashboard' : '/';
      router.push(destination);
    }
  }, [loading, isAuthenticated, profile?.role, router]);

  return {
    isLoading: loading,
    shouldRedirect: isAuthenticated() && !!profile?.role
  };
}