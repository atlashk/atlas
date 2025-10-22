"use client";

import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useCartStore } from "@/stores/cart.store";
import { useUserStore } from "@/stores/user.store";
import { Clock, ShoppingCart } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function NavBar() {
  const router = useRouter();
  const { isAuthenticated, isAdmin, fullName, logout, loading, profile, accessToken } =
    useUserStore();
  const { loadCart, getCartItemCount, cart, isLoading } = useCartStore();
  const cartItemCount = getCartItemCount();
  const [isHydrated, setIsHydrated] = useState(false);

  useEffect(() => {
    setIsHydrated(true);
  }, []);

  // Load cart data when user is authenticated
  useEffect(() => {
    const loadCartData = async () => {
      if (isHydrated && isAuthenticated() && !isAdmin() && !cart && !isLoading) {
        try {
          await loadCart();
        } catch (error) {
          console.error('Failed to load cart:', error);
        }
      }
    };

    loadCartData();
  }, [isHydrated, accessToken, profile, cart, isLoading, loadCart, isAuthenticated, isAdmin]);

  const getBrandHref = () => {
    // Always return default during hydration to prevent mismatch
    if (!isHydrated) {
      return "/";
    }

    if (!isAuthenticated()) {
      return "/";
    } else if (isAdmin()) {
      return "/admin/dashboard";
    } else {
      return "/";
    }
  };

  const getBrandName = () => {
    // Always return default during hydration to prevent mismatch
    if (!isHydrated) {
      return "Atlas Store";
    }

    if (isAuthenticated() && isAdmin()) {
      return "Atlas Admin";
    }
    return "Atlas Store";
  };

  const handleLogout = async () => {
    logout();
    router.push("/");
  };

  return (
    <nav className="bg-primary border-b fixed top-0 left-0 right-0 z-50 mb-4">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Brand */}
          <div className="flex items-center">
            <Link
              href={getBrandHref()}
              className="text-xl text-white hover:text-gray-300 transition-colors duration-200"
            >
              {getBrandName()}
            </Link>
          </div>

          {/* Right side - User info & auth buttons */}
          <div className="flex items-center space-x-4">
            {!isHydrated ? (
              // Show loading state during hydration to prevent mismatch
              <div className="flex items-center space-x-4">
                <Button
                  onClick={() => router.push("/login")}
                  size="sm"
                  className="bg-white text-gray-900 hover:bg-gray-100"
                >
                  Login
                </Button>
                <Button
                  variant="secondary"
                  onClick={() => router.push("/register")}
                  size="sm"
                  className="bg-blue-600 text-white hover:bg-blue-700"
                >
                  Register
                </Button>
              </div>
            ) : isAuthenticated() ? (
              <>
                <span className="text-gray-300 text-sm">
                  Welcome, {fullName()}
                </span>
                
                {/* Navigation items - Only show for non-admin users */}
                {!isAdmin() && (
                  <div className="flex items-center space-x-2">
                    {/* Cart Icon */}
                    <div className="relative group">
                      <Button
                        variant="ghost"
                        onClick={() => router.push("/cart")}
                        className="text-white hover:text-gray-300 hover:bg-white/10 p-3"
                      >
                        <ShoppingCart style={{ width: '1.25rem', height: '1.25rem' }} />
                        <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center min-w-[20px]">
                            {cartItemCount > 99 ? '99+' : cartItemCount}
                          </span>
                      </Button>
                      {/* Tooltip for Cart */}
                      <div className="absolute top-full left-1/2 transform -translate-x-1/2 mt-2 px-2 py-1 bg-gray-800 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none whitespace-nowrap">
                        Cart
                      </div>
                    </div>
        
                    {/* Order History */}
                    <div className="relative group">
                      <Button
                        variant="ghost"
                        onClick={() => router.push("/order-history")}
                        className="text-white hover:text-gray-300 hover:bg-white/10 p-3"
                      >
                        <Clock style={{ width: '1.25rem', height: '1.25rem' }} />
                      </Button>
                      {/* Tooltip for Order History */}
                      <div className="absolute top-full left-1/2 transform -translate-x-1/2 mt-2 px-2 py-1 bg-gray-800 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none whitespace-nowrap">
                        Order History
                      </div>
                    </div>
                  </div>
                )}
                
                <Button
                  variant="secondary"
                  onClick={handleLogout}
                  disabled={loading}
                  size="sm"
                >
                  {loading ? (
                    <div className="flex items-center">
                      <Spinner className="text-blue-600 mr-2" />
                      Logging out...
                    </div>
                  ) : (
                    "Logout"
                  )}
                </Button>
              </>
            ) : (
              <>
                <Button
                  onClick={() => router.push("/login")}
                  size="sm"
                  className="bg-white text-gray-900 hover:bg-gray-100"
                >
                  Login
                </Button>
                <Button
                  variant="secondary"
                  onClick={() => router.push("/register")}
                  size="sm"
                  className="bg-blue-600 text-white hover:bg-blue-700"
                >
                  Register
                </Button>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
