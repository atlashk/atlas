"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useUserStore } from "@/stores/user.store";
import AdminLayout from "@/components/admin/AdminLayout";
import Dashboard from "@/components/admin/Dashboard";

export default function AdminDashboardPage() {
  const router = useRouter();
  const { isAuthenticated, isAdmin, profile, logout, accessToken } = useUserStore();

  const [isHydrated, setIsHydrated] = useState(false);
  const [hasCheckedAuth, setHasCheckedAuth] = useState(false);

  useEffect(() => {
    setIsHydrated(true);
  }, []);

  useEffect(() => {
    // Only check authentication after hydration and if we haven't checked yet
    if (!isHydrated || hasCheckedAuth) return;
    
    // Add a small delay to ensure Zustand persist has loaded
    const timeoutId = setTimeout(() => {
      console.log('Auth check:', { 
        isAuthenticated: isAuthenticated(), 
        isAdmin: isAdmin(), 
        hasToken: !!accessToken,
        profile: profile 
      });
      
      // Redirect if not authenticated or not admin
      if (!isAuthenticated() || !isAdmin()) {
        console.log('Redirecting to login - not authenticated or not admin');
        router.push("/login");
        return;
      }

      console.log('User is authenticated admin, loading dashboard');
      setHasCheckedAuth(true);
    }, 100);
    
    return () => clearTimeout(timeoutId);
  }, [isAuthenticated, isAdmin, router, isHydrated, hasCheckedAuth, accessToken, profile]);

  // Show loading until hydrated and auth check is complete
  if (!isHydrated || !hasCheckedAuth) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  return (
    <AdminLayout>
      <Dashboard />
    </AdminLayout>
  );
}
