"use client";

import React, { ComponentType } from 'react';
import { useAuthRedirect, useGuestRedirect } from '@/hooks/useAuthRedirect';
import { Spinner } from '@/components/ui/spinner';

interface WithAuthOptions {
  requireAuth?: boolean;
  requireAdmin?: boolean;
  redirectTo?: string;
  allowedRoles?: string[];
  fallbackComponent?: ComponentType;
}

// Default loading component
const DefaultLoadingComponent: React.FC<{ message?: string }> = ({ 
  message = "Loading..." 
}) => (
  <div className="flex items-center justify-center min-h-screen">
    <div className="text-center">
      <Spinner className="text-blue-600 mx-auto mb-4" />
      <p className="text-gray-600">{message}</p>
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
    const isHydrated = true;

    const { isLoading, canAccess } = useAuthRedirect({
      requireAuth,
      requireAdmin,
      redirectUnauthenticated: redirectTo || '/login',
      redirectUnauthorized: redirectTo || '/',
      allowedRoles
    });

    // During SSR and the very first client render, show a stable fallback
    // This prevents hydration mismatches when auth state differs between server and client
    if (!isHydrated) {
      return FallbackComponent ? 
        <FallbackComponent /> : 
        <DefaultLoadingComponent message="Checking authentication..." />;
    }

    // Show loading state
    if (isLoading) {
      return FallbackComponent ? 
        <FallbackComponent /> : 
        <DefaultLoadingComponent message="Checking authentication..." />;
    }

    // Check if user can access this component
    if (!canAccess()) {
      return null; // Will redirect in useAuthRedirect
    }

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
  Component: ComponentType<P>
) => {
  const WithGuestOnlyComponent: React.FC<P> = (props) => {
    const { isLoading, shouldRedirect } = useGuestRedirect();

    // Show loading while checking auth status
    if (isLoading) {
      return <DefaultLoadingComponent />;
    }

    // Redirect if authenticated (handled in useGuestRedirect)
    if (shouldRedirect) {
      return null;
    }

    return <Component {...props} />;
  };

  WithGuestOnlyComponent.displayName = `withGuestOnly(${Component.displayName || Component.name})`;

  return WithGuestOnlyComponent;
};
