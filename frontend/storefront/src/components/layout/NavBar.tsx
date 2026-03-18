"use client";

import ChangePasswordDialog from "@/components/layout/ChangePasswordDialog";
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
import { loginWithKeycloak } from "@/lib/keycloak";
import { useCartStore } from "@/stores/cart.store";
import { useUserStore } from "@/stores/user.store";
import { KeyRound, LogOut, Package, ShoppingCart, User } from "lucide-react";
import Link from "next/link";
import { useEffect, useState } from "react";

export default function NavBar() {
  const { profile, logout } = useUserStore();
  const { getCartItemCount, loadCart } = useCartStore();
  const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false);

  const cartItemCount = getCartItemCount();

  // Load cart when user is authenticated
  useEffect(() => {
    if (profile) {
      loadCart();
    }
  }, [profile, loadCart]);

  const userDisplayName =
    profile?.firstName && profile?.lastName
      ? `${profile.firstName} ${profile.lastName}`
      : profile?.email || "User";

  const handleLogout = async () => {
    await logout();
  };

  const handleLogin = async () => {
    if (IDP.toLowerCase() === "keycloak") {
      const redirectUrl = `${window.location.pathname}${window.location.search}`;
      await loginWithKeycloak(redirectUrl);
      return;
    }
    window.location.href = "/login";
  };

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
