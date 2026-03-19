'use client';

import React, { Suspense } from 'react';
import { ProductSearch } from '../components/product';
import { Spinner } from '../components/ui/spinner';
import { useAuthRedirect } from '@/hooks/useAuthRedirect';

const StoreFront: React.FC = () => {
  const isHydrated = true;
  const { isLoading } = useAuthRedirect(); // This handles all auth-based redirects automatically

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
        <Suspense
          fallback={
            <div className="flex items-center justify-center min-h-screen">
              <div className="text-center">
                <Spinner className="text-blue-600 mx-auto mb-4" />
                <p className="text-gray-600">Loading...</p>
              </div>
            </div>
          }
        >
          <ProductSearch />
        </Suspense>
      </div>
    </div>
  );
};

export default StoreFront;
