"use client";

import { useUserStore } from "@/stores/user.store";
import { useEffect, useState } from "react";

interface AuthProviderProps {
  children: React.ReactNode;
}

export default function AuthProvider({ children }: AuthProviderProps) {
  const { accessToken, fetchProfile } = useUserStore();
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    const initializeAuth = async () => {
      // If we have an access token but no profile, fetch it
      if (accessToken) {
        try {
          await fetchProfile();
        } catch (error) {
          console.error('Failed to fetch profile on initialization:', error);
        }
      }
      setIsInitialized(true);
    };

    initializeAuth();
  }, [accessToken, fetchProfile]);

  // Show loading state during initialization
  if (!isInitialized) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Initializing...</p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}