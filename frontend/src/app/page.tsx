'use client';

import { useRouter } from 'next/navigation';
import React, { useEffect, useState } from 'react';
import { ProductSearch } from '../components/front';
import { Spinner } from '../components/ui/spinner';
import { useUserStore } from '../stores/user.store';

const StoreFront: React.FC = () => {
  const { isAuthenticated, profile, loading } = useUserStore();
  const [isHydrated, setIsHydrated] = useState(false);
  const router = useRouter();

  useEffect(() => {
    setIsHydrated(true);
  }, []);

  // Redirect admin users to admin dashboard
  useEffect(() => {
    if (!loading && isAuthenticated() && profile?.role === 'ADMIN') {
      router.push('/admin/dashboard');
    }
  }, [loading, isAuthenticated, profile?.role, router]);

  // Show loading while checking authentication
  if (loading || !isHydrated) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <Spinner className="text-blue-600 mx-auto mb-4" />
          <p className="text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-4">
      <div className="w-full">
        {/* Product Search */}
        <ProductSearch />
      </div>
    </div>
  );
};

export default StoreFront;
