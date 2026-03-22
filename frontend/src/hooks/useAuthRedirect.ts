"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { useUserStore } from "@/stores/user.store";

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
    redirectUnauthenticated = "/login",
    allowedRoles = [],
  } = options;

  const router = useRouter();
  const { isAuthenticated, loading, profileLoading, profile } = useUserStore();

  useEffect(() => {
    // Wait for both auth state and profile to load
    if (loading || profileLoading) {
      console.log("[useAuthRedirect] Waiting for auth state to load...");
      return;
    }

    const isUserAuthenticated = isAuthenticated();

    const userRole = profile?.role;

    console.log("[useAuthRedirect] Checking access:", {
      isUserAuthenticated,
      requireAuth,
      userRole,
    });

    // Handle unauthenticated users
    if (requireAuth && !isUserAuthenticated) {
      console.log(
        "[useAuthRedirect] User not authenticated, redirecting to login",
      );
      const currentPath = window.location.pathname;
      const redirectUrl = `${redirectUnauthenticated}?redirect=${encodeURIComponent(currentPath)}`;
      router.push(redirectUrl);
      return;
    }

    if (
      isUserAuthenticated &&
      allowedRoles.length > 0 &&
      userRole &&
      !allowedRoles.includes(userRole)
    ) {
      router.push("/admin/dashboard");
    }
  }, [
    loading,
    profileLoading,
    isAuthenticated,
    profile?.role,
    router,
    requireAuth,
    redirectUnauthenticated,
    allowedRoles,
  ]);

  const isUserAuthenticated = isAuthenticated();
  const userRole = profile?.role;
  const isUnauthorized =
    !loading &&
    !profileLoading &&
    isUserAuthenticated &&
    !!userRole &&
    allowedRoles.length > 0 &&
    !allowedRoles.includes(userRole);

  return {
    isLoading: loading || profileLoading,
    isAuthenticated: isUserAuthenticated,
    isUnauthorized,
    canAccess: () => {
      if (loading || profileLoading) return false;

      if (requireAuth && !isUserAuthenticated) return false;
      if (
        allowedRoles.length > 0 &&
        userRole &&
        !allowedRoles.includes(userRole)
      )
        return false;

      return true;
    },
  };
}

/**
 * Hook for pages that should redirect authenticated users
 * Useful for login/register pages
 */
export function useGuestRedirect() {
  const router = useRouter();
  const { isAuthenticated, loading, profileLoading, profile } = useUserStore();

  useEffect(() => {
    // Wait for both loading states
    if (loading || profileLoading) {
      console.log("[useGuestRedirect] Waiting for auth state to load...");
      return;
    }

    const isUserAuthenticated = isAuthenticated();
    const userRole = profile?.role;

    console.log("[useGuestRedirect] Checking guest access:", {
      isUserAuthenticated,
      userRole,
    });

    if (isUserAuthenticated) {
      if (userRole === "ADMIN") {
        router.push("/admin/dashboard");
        return;
      }
      router.push("/");
    }
  }, [loading, profileLoading, isAuthenticated, profile?.role, router]);

  return {
    isLoading: loading || profileLoading,
    shouldRedirect: isAuthenticated(),
  };
}
