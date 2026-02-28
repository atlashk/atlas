"use client";

import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbList,
  BreadcrumbPage,
} from "@/components/ui/breadcrumb";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Separator } from "@/components/ui/separator";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarInset,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
  SidebarTrigger
} from "@/components/ui/sidebar";
import ChangePasswordDialog from "@/components/layout/ChangePasswordDialog";
import { useUserStore } from "@/stores/user.store";
import { KeyRound, LogOut, Package, ShoppingCart, User as UserIcon, Users } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import React, { useMemo, useState } from "react";

interface AdminLayoutProps {
  children: React.ReactNode;
}

const menuItems = [
  {
    title: "Users",
    url: "/user",
    icon: Users,
  },
  {
    title: "Products",
    url: "/product",
    icon: Package,
  },
  {
    title: "Orders",
    url: "/order",
    icon: ShoppingCart,
  },
];

function AppSidebar() {
  const pathname = usePathname();
  
  return (
    <Sidebar>
      {/* Logo Section */}
      <div className="flex h-16 items-center border-b px-6">
        <Link href="/" className="flex items-center space-x-2">
          <div className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
            Atlas Admin
          </div>
        </Link>
      </div>
      
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupContent>
            <SidebarMenu>
              {menuItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild isActive={pathname === item.url}>
                    <Link href={item.url} className="flex items-center gap-2">
                      <item.icon className="h-4 w-4" />
                      <span>{item.title}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
    </Sidebar>
  );
}

export default function AdminLayout({ children }: AdminLayoutProps) {
  const pathname = usePathname();
  const { profile, logout } = useUserStore();
  const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false);

  const displayName = useMemo(() => {
    if (!profile) return "Account";
    if (profile.firstName && profile.lastName) return `${profile.firstName} ${profile.lastName}`;
    return profile.username || profile.email || "Account";
  }, [profile]);

  const handleLogout = async () => {
    await logout();
  };

  const handleOpenChangePassword = () => {
    setIsChangePasswordOpen(true);
  };
  
  // Function to get breadcrumb text based on current path
  const getBreadcrumbText = () => {
    switch (pathname) {
      case '/':
      case '/dashboard':
        return 'Dashboard';
      case '/user':
        return 'User Management';
      case '/product':
        return 'Product Management';
      case '/order':
        return 'Order Management';
      default:
        // Handle dynamic product routes
        if (pathname.startsWith('/product/')) {
          if (pathname.includes('/edit')) {
            return 'Edit Product';
          } else if (pathname.endsWith('/add')) {
            return 'Add Product';
          } else {
            // Product details page (e.g., /product/123)
            return 'Product Details';
          }
        }
        return 'Dashboard';
    }
  };

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset>
        <header className="flex h-16 shrink-0 items-center justify-between gap-2 border-b px-4">
          <div className="flex items-center gap-2">
            <SidebarTrigger />
            <Separator orientation="vertical" className="mr-2 h-4" />
            <Breadcrumb>
              <BreadcrumbList>
                <BreadcrumbItem>
                  <BreadcrumbPage className="font-semibold">{getBreadcrumbText()}</BreadcrumbPage>
                </BreadcrumbItem>
              </BreadcrumbList>
            </Breadcrumb>
          </div>
          <div className="flex items-center gap-2">
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="gap-2">
                  <UserIcon className="h-4 w-4" />
                  <span className="max-w-[220px] truncate">{displayName}</span>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-64">
                <DropdownMenuItem onSelect={handleOpenChangePassword}>
                  <KeyRound className="h-4 w-4" />
                  Change password
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem variant="destructive" onClick={handleLogout}>
                  <LogOut className="h-4 w-4" />
                  Logout
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </header>
        <div className="flex flex-col h-full">
          <div className="flex-1 flex flex-col">
            <div className="flex-1 p-4">
              {children}
            </div>
          </div>
        </div>
      </SidebarInset>
      <ChangePasswordDialog open={isChangePasswordOpen} onOpenChange={setIsChangePasswordOpen} />
    </SidebarProvider>
  );
}
