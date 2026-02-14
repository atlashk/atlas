"use client";

import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbList,
  BreadcrumbPage,
} from "@/components/ui/breadcrumb";
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
import { Package, ShoppingCart, Users } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import React from "react";

interface AdminLayoutProps {
  children: React.ReactNode;
}

const menuItems = [
  {
    title: "Users",
    url: "/admin/user",
    icon: Users,
  },
  {
    title: "Products",
    url: "/admin/product",
    icon: Package,
  },
  {
    title: "Orders",
    url: "/admin/order",
    icon: ShoppingCart,
  },
];

function AppSidebar() {
  const pathname = usePathname();
  
  return (
    <Sidebar>
      {/* Logo Section */}
      <div className="flex h-16 items-center border-b px-6">
        <Link href="/admin/dashboard" className="flex items-center space-x-2">
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
  
  // Function to get breadcrumb text based on current path
  const getBreadcrumbText = () => {
    switch (pathname) {
      case '/admin/dashboard':
        return 'Dashboard';
      case '/admin/user':
        return 'User Management';
      case '/admin/product':
        return 'Product Management';
      case '/admin/order':
        return 'Order Management';
      default:
        // Handle dynamic product routes
        if (pathname.startsWith('/admin/product/')) {
          if (pathname.includes('/edit')) {
            return 'Edit Product';
          } else if (pathname.endsWith('/add')) {
            return 'Add Product';
          } else {
            // Product details page (e.g., /admin/product/123)
            return 'Product Details';
          }
        }
        return 'Admin Dashboard';
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
            {/* Optional: Add user menu or other actions here */}
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
    </SidebarProvider>
  );
}
