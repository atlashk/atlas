"use client";

import { orderAdminApi, productAdminApi, userAdminApi } from "@/api";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { formatCurrency } from "@/utils/formatter.util";
import { toast } from "sonner";
import {
  Activity,
  DollarSign,
  ShoppingCart,
  Users,
} from "lucide-react";
import React, { useState, useEffect, useCallback, useRef } from "react";

interface DashboardStats {
  totalUsers: number;
  totalProducts: number;
  totalOrders: number;
  totalRevenue: number;
}

const Dashboard: React.FC = () => {
  const isInitialized = useRef(false);
  const [stats, setStats] = useState<DashboardStats>({
    totalUsers: 0,
    totalProducts: 0,
    totalOrders: 0,
    totalRevenue: 0,
  });
  const [loading, setLoading] = useState(true);

  // Load dashboard statistics
  const loadStats = useCallback(async () => {
    try {
      setLoading(true);
      const [usersResponse, productsResponse, ordersResponse, revenueResponse] = await Promise.all([
        userAdminApi.countUser(),
        productAdminApi.countProduct(),
        orderAdminApi.countOrder(),
        orderAdminApi.getTotalRevenue()
      ]);

      setStats({
        totalUsers: usersResponse.success ? usersResponse.data || 0 : 0,
        totalProducts: productsResponse.success ? productsResponse.data || 0 : 0,
        totalOrders: ordersResponse.success ? ordersResponse.data || 0 : 0,
        totalRevenue: revenueResponse.success ? revenueResponse.data || 0 : 0,
      });
    } catch {
      toast.error('Failed to load dashboard statistics');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isInitialized.current) {
      return;
    }
    
    isInitialized.current = true;
    loadStats();
  }, [loadStats]);

  const displayStats = stats;

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
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Dashboard;
