"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useUserStore } from "@/stores/user.store";
import AdminLayout from "@/components/admin/AdminLayout";

export default function AdminOrderListPage() {
  const router = useRouter();
  const { isAuthenticated, isAdmin, logout } = useUserStore();

  useEffect(() => {
    // Redirect if not authenticated or not admin
    if (!isAuthenticated() || !isAdmin()) {
      router.push("/login");
      return;
    }
  }, [isAuthenticated, isAdmin, router]);

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  return (
    <AdminLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Order Management</h1>
          <p className="text-muted-foreground">
            Manage customer orders and fulfillment.
          </p>
        </div>
        
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Orders</h2>
          <p className="text-gray-600">
            Order management functionality will be implemented here.
          </p>
          <div className="mt-4">
            <div className="text-sm text-gray-500">
              Features to be implemented:
              <ul className="list-disc list-inside mt-2 space-y-1">
                <li>View all orders</li>
                <li>Search and filter orders</li>
                <li>Update order status</li>
                <li>Process refunds</li>
                <li>Generate order reports</li>
                <li>Manage shipping information</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </AdminLayout>
  );
}
