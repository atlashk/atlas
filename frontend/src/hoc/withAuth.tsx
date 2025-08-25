"use client";

import React, { ComponentType, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';

interface WithAuthOptions {
  requireAuth?: boolean;
  requireAdmin?: boolean;
  redirectTo?: string;
  allowedRoles?: string[];
  fallbackComponent?: ComponentType;
}

interface LoadingComponentProps {
  message?: string;
}

const DefaultLoadingComponent: React.FC<LoadingComponentProps> = ({ message = 'Loading...' }) => (
  <div className="flex items-center justify-center min-h-screen">
    <div className="text-center">
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
      <p className="text-gray-600">{message}</p>
    </div>
  </div>
);

const DefaultUnauthorizedComponent: React.FC = () => (
  <div className="flex items-center justify-center min-h-screen">
    <div className="text-center">
      <h1 className="text-4xl font-bold text-red-600 mb-4">403</h1>
      <h2 className="text-2xl font-semibold text-gray-800 mb-2">Access Denied</h2>
      <p className="text-gray-600 mb-4">You don&apos;t have permission to access this page.</p>
      <button 
        onClick={() => window.history.back()}
        className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors"
      >
        Go Back
      </button>
    </div>
  </div>
);

export function withAuth<P extends object>(
  WrappedComponent: ComponentType<P>,
  options: WithAuthOptions = {}
) {
  const {
    requireAuth = true,
    requireAdmin = false,
    redirectTo,
    allowedRoles = [],
    fallbackComponent: FallbackComponent
  } = options;

  const WithAuthComponent: React.FC<P> = (props) => {
    const router = useRouter();
    const auth = useAuth();

    useEffect(() => {
      // Wait for auth to finish loading
      if (auth.isLoading) return;

      // If authentication is not required, allow access
      if (!requireAuth) return;

      // Handle unauthenticated users
      if (!auth.isAuthenticated) {
        const redirectPath = redirectTo || '/login';
        const currentPath = window.location.pathname;
        const redirectUrl = `${redirectPath}?redirect=${encodeURIComponent(currentPath)}`;
        router.push(redirectUrl);
        return;
      }

      // Handle unauthorized access (admin or role-based)
      const hasRequiredRole = allowedRoles.length === 0 || allowedRoles.some(role => auth.hasRole(role));
      const hasAdminAccess = !requireAdmin || auth.isAdmin;
      
      if (!hasRequiredRole || !hasAdminAccess) {
        if (redirectTo) {
          router.push(redirectTo);
        }
        return;
      }
    }, [auth.isLoading, auth.isAuthenticated, auth.isAdmin, router]);

    // Show loading state
    if (auth.isLoading) {
      return <DefaultLoadingComponent message="Checking authentication..." />;
    }

    // If authentication is not required, render component
    if (!requireAuth) {
      return <WrappedComponent {...props} />;
    }

    // Check authentication
    if (!auth.isAuthenticated) {
      return null; // Will redirect in useEffect
    }

    // Check authorization
    const hasRequiredRole = allowedRoles.length === 0 || allowedRoles.some(role => auth.hasRole(role));
    const hasAdminAccess = !requireAdmin || auth.isAdmin;
    
    if (!hasRequiredRole || !hasAdminAccess) {
      return FallbackComponent ? <FallbackComponent /> : <DefaultUnauthorizedComponent />;
    }

    // Render the wrapped component
    return <WrappedComponent {...props} />;
  };

  WithAuthComponent.displayName = `withAuth(${WrappedComponent.displayName || WrappedComponent.name})`;

  return WithAuthComponent;
}

// Convenience HOCs for common use cases
export const withRequireAuth = <P extends object>(Component: ComponentType<P>) =>
  withAuth(Component, { requireAuth: true });

export const withRequireAdmin = <P extends object>(Component: ComponentType<P>) =>
  withAuth(Component, { requireAuth: true, requireAdmin: true });

// HOC for pages that should redirect authenticated users (like login/register)
export const withGuestOnly = <P extends object>(
  Component: ComponentType<P>,
  redirectTo: string = '/dashboard'
) => {
  const WithGuestOnlyComponent: React.FC<P> = (props) => {
    const router = useRouter();
    const auth = useAuth();

    useEffect(() => {
      if (!auth.isLoading && auth.isAuthenticated) {
        const destination = auth.isAdmin ? '/admin/dashboard' : redirectTo;
        router.push(destination);
      }
    }, [auth.isLoading, auth.isAuthenticated, auth.isAdmin, router]);

    // Show loading while checking auth status
    if (auth.isLoading) {
      return <DefaultLoadingComponent />;
    }

    // Redirect if authenticated (handled in useEffect)
    if (auth.isAuthenticated) {
      return null;
    }

    return <Component {...props} />;
  };

  WithGuestOnlyComponent.displayName = `withGuestOnly(${Component.displayName || Component.name})`;

  return WithGuestOnlyComponent;
};