'use client';

import React, { useState, useEffect } from 'react';
import { Cart, OrderHistory, OrderTracking, ProductSearch } from '../components/front';
import { useCartStore } from '../stores/cart.store';
import { useUserStore } from '../stores/user.store';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ShoppingBag, Clock } from 'lucide-react';

const StoreFront: React.FC = () => {
  const { loadCart, currentOrderId, setCurrentOrderId } = useCartStore();
  const { isAuthenticated } = useUserStore();
  const [activeTab, setActiveTab] = useState('products');
  const [isHydrated, setIsHydrated] = useState(false);

  useEffect(() => {
    loadCart();
    // Clear order tracking on page reload
    setCurrentOrderId(null);
  }, [loadCart, setCurrentOrderId]);

  useEffect(() => {
    setIsHydrated(true);
  }, []);

  return (
    <div className="container mx-auto px-4 py-4">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left side: Product Search and Order History */}
        <div className="lg:col-span-8">
          <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
            <TabsList className={`grid w-full ${isHydrated && isAuthenticated() ? 'grid-cols-2' : 'grid-cols-1'}`}>
              <TabsTrigger value="products" className="flex items-center gap-2">
                <ShoppingBag className="h-4 w-4" />
                Products
              </TabsTrigger>
              {isHydrated && isAuthenticated() && (
                <TabsTrigger value="order-history" className="flex items-center gap-2">
                  <Clock className="h-4 w-4" />
                  Order History
                </TabsTrigger>
              )}
            </TabsList>
            <TabsContent value="products" className="mt-6">
              <ProductSearch />
            </TabsContent>
            {isHydrated && isAuthenticated() && (
              <TabsContent value="order-history" className="mt-6">
                <OrderHistory />
              </TabsContent>
            )}
          </Tabs>
        </div>

        {/* Right side: Cart and Order Tracking */}
        <div className="lg:col-span-4">
          <div className="sticky top-5 space-y-6">
            <Cart />
            {currentOrderId && <OrderTracking />}
          </div>
        </div>
      </div>
    </div>
  );
};

export default StoreFront;
