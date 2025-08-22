"use client";

import { Button } from "@/components/ui/button";
import { useUserStore } from "@/stores/user.store";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function NavBar() {
  const router = useRouter();
  const { isAuthenticated, isAdmin, fullName, logout, loading } =
    useUserStore();
  const [isHydrated, setIsHydrated] = useState(false);

  useEffect(() => {
    setIsHydrated(true);
  }, []);

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
      return "Admin Store";
    }

    if (isAuthenticated() && isAdmin()) {
      return "Atlas Admin";
    }
    return "Admin Store";
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
                <Button
                  variant="secondary"
                  onClick={handleLogout}
                  disabled={loading}
                  size="sm"
                >
                  {loading ? (
                    <div className="flex items-center">
                      <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
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
