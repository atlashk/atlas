"use client";

import { orderAdminApi, productAdminApi, userAdminApi } from "@/api";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { formatCurrency } from "@/utils/formatter.util";
import { useDataLoader } from "@/hooks";
import { toast } from "sonner";
import {
  Activity,
  DollarSign,
  ShoppingCart,
  Users,
} from "lucide-react";
import React from "react";

interface DashboardStats {
  totalUsers: number;
  totalProducts: number;
  totalOrders: number;
  totalRevenue: number;
}

const Dashboard: React.FC = () => {
  // Load dashboard statistics
  const { data: stats, loading } = useDataLoader({
    loadFunction: async () => {
      const [usersResponse, productsResponse, ordersResponse, revenueResponse] = await Promise.all([
        userAdminApi.countUser(),
        productAdminApi.countProduct(),
        orderAdminApi.countOrder(),
        orderAdminApi.getTotalRevenue()
      ]);

      return {
        totalUsers: usersResponse.success ? usersResponse.data || 0 : 0,
        totalProducts: productsResponse.success ? productsResponse.data || 0 : 0,
        totalOrders: ordersResponse.success ? ordersResponse.data || 0 : 0,
        totalRevenue: revenueResponse.success ? revenueResponse.data || 0 : 0,
      };
    },
    autoLoad: true,
    onError: () => toast.error('Failed to load dashboard statistics')
  });

  const displayStats = (stats as DashboardStats) || {
    totalUsers: 0,
    totalProducts: 0,
    totalOrders: 0,
    totalRevenue: 0,
  };

  return (
    <div className="space-y-6">
      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Users</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {loading ? "Loading..." : displayStats.totalUsers.toLocaleString()}
            </div>
            <p className="text-xs text-muted-foreground">
              +20.1% from last month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Total Products
            </CardTitle>
            <ShoppingCart className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {loading ? "Loading..." : displayStats.totalProducts.toLocaleString()}
            </div>
            <p className="text-xs text-muted-foreground">
              +180.1% from last month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Orders</CardTitle>
            <Activity className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {loading ? "Loading..." : displayStats.totalOrders.toLocaleString()}
            </div>
            <p className="text-xs text-muted-foreground">
              +19% from last month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Revenue</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {loading
                ? "Loading..."
                : formatCurrency(displayStats.totalRevenue)}
            </div>
            <p className="text-xs text-muted-foreground">
              +201 since last hour
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Dashboard;
