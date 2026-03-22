"use client";

import ChangePasswordDialog from "@/components/user/ChangePasswordDialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { IDP } from "@/config/env.config";
import { useCartStore } from "@/stores/cart.store";
import { useUserStore } from "@/stores/user.store";
import {
  House,
  KeyRound,
  LogOut,
  Package,
  Shield,
  ShoppingCart,
  User,
} from "lucide-react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { logoutWithKeycloak } from "@/lib/keycloak";

export default function NavBar() {
  const { profile, logout, clearAuthState } = useUserStore();
  const { getCartItemCount, loadCart, clearCartState } = useCartStore();
  const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false);
  const router = useRouter();
  const pathname = usePathname();
  const isAdminRoute = pathname.startsWith("/admin");

  const cartItemCount = getCartItemCount();

  // Load cart when user is authenticated
  useEffect(() => {
    if (profile && !isAdminRoute) {
      loadCart();
    }
  }, [profile, loadCart, isAdminRoute]);

  const userDisplayName =
    profile?.firstName && profile?.lastName
      ? `${profile.firstName} ${profile.lastName}`
      : profile?.email || "User";

  const handleLogout = async () => {
    if (IDP.toLowerCase() === "keycloak") {
      clearAuthState();
      clearCartState();
      await logoutWithKeycloak();
      return;
    }
    await logout();
  };

  const handleLogin = async () => {
    const target =
      IDP.toLowerCase() === "keycloak" ? "/login/keycloak" : "/login";
    router.push(target);
  };

  if (isAdminRoute) {
    return (
      <nav className="fixed top-0 left-0 right-0 z-50 bg-white/80 backdrop-blur-md border-b border-gray-200 shadow-sm">
        <div className="container mx-auto px-4">
          <div className="flex justify-between items-center h-16">
            <Link
              href="/admin/dashboard"
              className="flex items-center space-x-2"
            >
              <div className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                Atlas Admin
              </div>
            </Link>
            <div className="flex items-center space-x-4">
              <Button
                variant="outline"
                size="sm"
                className="flex items-center gap-2 border-violet-300 bg-gradient-to-r from-violet-100 to-indigo-100 text-indigo-800 hover:from-violet-200 hover:to-indigo-200 hover:text-indigo-900 shadow-md ring-1 ring-violet-200/60"
                onClick={() => router.push("/")}
              >
                <House className="h-4 w-4" />
                <span>Back to Home</span>
              </Button>
              {profile ? (
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="flex items-center gap-2"
                    >
                      <User className="h-5 w-5" />
                      <span className="hidden sm:inline text-sm font-medium max-w-[10rem] truncate">
                        {userDisplayName}
                      </span>
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end" className="w-56">
                    <DropdownMenuItem
                      onSelect={() => setIsChangePasswordOpen(true)}
                      className="cursor-pointer"
                    >
                      <KeyRound className="h-4 w-4" />
                      Change password
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem
                      onClick={handleLogout}
                      className="cursor-pointer text-red-600"
                    >
                      <LogOut className="h-4 w-4" />
                      Logout
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              ) : (
                <div className="flex items-center space-x-2">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => void handleLogin()}
                  >
                    Login
                  </Button>
                  <Link href="/register">
                    <Button size="sm">Sign Up</Button>
                  </Link>
                </div>
              )}
            </div>
          </div>
        </div>
        <ChangePasswordDialog
          open={isChangePasswordOpen}
          onOpenChange={setIsChangePasswordOpen}
        />
      </nav>
    );
  }

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-white/80 backdrop-blur-md border-b border-gray-200 shadow-sm">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link href="/" className="flex items-center space-x-2">
            <div className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
              Atlas Storefront
            </div>
          </Link>

          {/* Right Side Actions */}
          <div className="flex items-center space-x-4">
            {profile?.role === "ADMIN" && (
              <Button
                variant="outline"
                size="sm"
                className="flex items-center gap-2 border-indigo-300 bg-indigo-600 text-white hover:bg-indigo-700 hover:text-white shadow-sm"
                onClick={() => router.push("/admin/dashboard")}
              >
                <Shield className="h-4 w-4" />
                <span>Admin Console</span>
              </Button>
            )}

            {/* Cart */}
            <Link href="/cart">
              <Button variant="ghost" size="icon" className="relative">
                <ShoppingCart className="h-5 w-5" />
                {cartItemCount > 0 && (
                  <Badge
                    variant="destructive"
                    className="absolute -top-1 -right-1 h-4 w-4 flex items-center justify-center p-0 text-xs"
                  >
                    {cartItemCount}
                  </Badge>
                )}
              </Button>
            </Link>

            {/* User Menu */}
            {profile ? (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="flex items-center gap-2"
                  >
                    <User className="h-5 w-5" />
                    <span className="hidden sm:inline text-sm font-medium max-w-[10rem] truncate">
                      {userDisplayName}
                    </span>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-56">
                  <DropdownMenuItem asChild>
                    <Link
                      href="/order-history"
                      className="flex items-center gap-2 cursor-pointer whitespace-nowrap"
                    >
                      <Package className="h-4 w-4" />
                      <span>Order History</span>
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    onSelect={() => setIsChangePasswordOpen(true)}
                    className="cursor-pointer"
                  >
                    <KeyRound className="h-4 w-4" />
                    Change password
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    onClick={handleLogout}
                    className="cursor-pointer text-red-600"
                  >
                    <LogOut className="h-4 w-4" />
                    Logout
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <div className="flex items-center space-x-2">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => void handleLogin()}
                >
                  Login
                </Button>
                <Link href="/register">
                  <Button size="sm">Sign Up</Button>
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
      <ChangePasswordDialog
        open={isChangePasswordOpen}
        onOpenChange={setIsChangePasswordOpen}
      />
    </nav>
  );
}
