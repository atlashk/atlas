'use client';

import React, { useEffect, useState } from 'react';
import { ProductSearch } from '../components/front';
import { Spinner } from '../components/ui/spinner';
import { useAuthRedirect } from '@/hooks/useAuthRedirect';

const StoreFront: React.FC = () => {
  const [isHydrated, setIsHydrated] = useState(false);
  const { isLoading } = useAuthRedirect(); // This handles all auth-based redirects automatically

  useEffect(() => {
    setIsHydrated(true);
  }, []);

  // Show loading while checking authentication or hydrating
  if (isLoading || !isHydrated) {
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
