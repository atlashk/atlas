"use client";

import LoadingScreen from "@/components/common/LoadingScreen";
import { useUserStore } from "@/stores/user.store";
import React, { useEffect, useRef, useState } from "react";

interface AuthProviderProps {
  children: React.ReactNode;
}

export const AuthProvider = React.memo(function AuthProvider({
  children,
}: AuthProviderProps) {
  const initializeFromCookies = useUserStore(
    (state) => state.initializeFromCookies,
  );
  const [isInitialized, setIsInitialized] = useState(false);
  const initStartedRef = useRef(false);

  useEffect(() => {
    if (initStartedRef.current) return;
    initStartedRef.current = true;
    const initializeAuth = async () => {
      try {
        await useUserStore.persist.rehydrate();
        initializeFromCookies();
        const { accessToken, fetchProfile } = useUserStore.getState();
        if (accessToken) {
          await fetchProfile();
        }
      } catch {
        const { clearAuthState } = useUserStore.getState();
        clearAuthState();
      } finally {
        setIsInitialized(true);
      }
    };
    void initializeAuth();
  }, [initializeFromCookies]);

  return isInitialized ? (
    children
  ) : (
    <LoadingScreen message="Initializing application..." />
  );
});
