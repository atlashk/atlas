"use client";

import NotificationBell from "./NotificationBell";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useCartStore } from "@/stores/cart.store";
import { useUserStore } from "@/stores/user.store";
import { Clock, ShoppingCart } from "lucide-react";

import { useRouter } from "next/navigation";
import { useEffect, useRef } from "react";

export default function NavBar() {
  const router = useRouter();
  const { isAuthenticated, isAdmin, fullName, logout, loading } =
    useUserStore();
  const {
    loadCart,
    getCartItemCount,
    cart,
    isLoading,
    error,
    isIntentionallyCleared,
    resetIntentionallyCleared,
  } = useCartStore();
  const cartItemCount = getCartItemCount();
  const isHydrated = true;
  const cartLoadAttemptedRef = useRef(false);

  const isAuth = isAuthenticated();
  const admin = isAdmin();

  // Load cart data when user is authenticated
  useEffect(() => {
    const loadCartData = async () => {
      // Only attempt to load cart if:
      // 1. Component is hydrated
      // 2. User is authenticated
      // 3. User is not admin
      // 4. Cart hasn't been loaded yet (cart is null)
      // 5. Not currently loading
      // 6. Haven't attempted to load cart yet OR there's no error (to allow retry after successful auth)
      if (
        isHydrated &&
        isAuth &&
        !admin &&
        !cart &&
        !isLoading &&
        (!cartLoadAttemptedRef.current || !error)
      ) {
        try {
          cartLoadAttemptedRef.current = true;
          await loadCart();
        } catch (error) {
          console.error("Failed to load cart:", error);
          // Don't retry immediately - let user manually refresh or navigate
        }
      }
    };

    loadCartData();
  }, [isHydrated, isAuth, admin, cart, isLoading, error, loadCart]);

  // Reset cart load attempt when user authentication changes
  useEffect(() => {
    if (!isAuth) {
      cartLoadAttemptedRef.current = false;
    }
  }, [isAuth]);

  // Reset intentionally cleared flag when user navigates to shopping areas
  useEffect(() => {
    if (isIntentionallyCleared && isAuth && !admin) {
      const currentPath = window.location.pathname;
      // Reset flag when user navigates to shopping areas (home, products, categories)
      if (
        currentPath === "/" ||
        currentPath.startsWith("/products") ||
        currentPath.startsWith("/categories")
      ) {
        resetIntentionallyCleared();
      }
    }
  }, [isIntentionallyCleared, isAuth, admin, resetIntentionallyCleared]);

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

  const handleBrandClick = async () => {
    // Reset intentionally cleared flag when user goes back to shopping
    if (isIntentionallyCleared) {
      resetIntentionallyCleared();
    }

    const href = getBrandHref();

    // Add refresh parameter if navigating to home page to trigger ProductSearch refresh
    const finalHref = href === "/" ? `/?refresh=${Date.now()}` : href;

    // Navigate to the appropriate page
    router.push(finalHref);

    // Load cart data if user is authenticated and not admin
    if (isAuthenticated() && !isAdmin()) {
      try {
        await loadCart();
      } catch (error) {
        console.error("Failed to reload cart:", error);
      }
    }
  };

  const handleOrderHistoryClick = () => {
    // Add timestamp parameter to force page refresh and data reload
    const timestamp = Date.now();
    router.push(`/order-history?refresh=${timestamp}`);
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
            <button
              onClick={handleBrandClick}
              className="text-xl text-white hover:text-gray-300 transition-colors duration-200 bg-transparent border-none cursor-pointer"
            >
              {getBrandName()}
            </button>
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
                        <ShoppingCart
                          style={{ width: "1.25rem", height: "1.25rem" }}
                        />
                        <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center min-w-[20px]">
                          {cartItemCount > 99 ? "99+" : cartItemCount}
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
                        onClick={handleOrderHistoryClick}
                        className="text-white hover:text-gray-300 hover:bg-white/10 p-3"
                      >
                        <Clock
                          style={{ width: "1.25rem", height: "1.25rem" }}
                        />
                      </Button>
                      {/* Tooltip for Order History */}
                      <div className="absolute top-full left-1/2 transform -translate-x-1/2 mt-2 px-2 py-1 bg-gray-800 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none whitespace-nowrap">
                        Order History
                      </div>
                    </div>

                    <NotificationBell />
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
