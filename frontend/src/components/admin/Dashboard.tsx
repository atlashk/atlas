"use client";

import React, { useEffect, useState, useRef } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { DollarSign, Users, ShoppingCart, Activity } from "lucide-react";
import { userAdminApi, productAdminApi, orderAdminApi } from "@/api";

interface StatisticsData {
  totalUsers: number;
  totalProducts: number;
  totalOrders: number;
  totalRevenue: number;
}

const Dashboard: React.FC = () => {
  const [statistics, setStatistics] = useState<StatisticsData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const isLoadingRef = useRef(false);

  useEffect(() => {
    const loadStatistics = async () => {
      // Prevent duplicate calls
      if (isLoadingRef.current) {
        return;
      }
      
      try {
        isLoadingRef.current = true;
        setLoading(true);
        
        const [
          usersResponse,
          productsResponse,
          ordersResponse,
          revenueResponse,
        ] = await Promise.all([
          userAdminApi.countUser(),
          productAdminApi.countProduct(),
          orderAdminApi.countOrder(),
          orderAdminApi.getTotalRevenue(),
        ]);

        setStatistics({
          totalUsers: usersResponse.data || 0,
          totalProducts: productsResponse.data || 0,
          totalOrders: ordersResponse.data || 0,
          totalRevenue: revenueResponse.data || 0,
        });
        setError(null);
      } catch {
        setError("Failed to load statistics");
      } finally {
        setLoading(false);
        isLoadingRef.current = false;
      }
    };

    loadStatistics();
  }, []);

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {[1, 2, 3, 4].map((i) => (
            <Card key={i}>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">
                  Loading...
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">--</div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-6">
        <div className="text-red-500">Error: {error}</div>
      </div>
    );
  }

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
              {statistics?.totalUsers?.toLocaleString() || "0"}
            </div>
            <p className="text-xs text-muted-foreground">Registered users</p>
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
              {statistics?.totalProducts?.toLocaleString() || "0"}
            </div>
            <p className="text-xs text-muted-foreground">Available products</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Orders</CardTitle>
            <Activity className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {statistics?.totalOrders?.toLocaleString() || "0"}
            </div>
            <p className="text-xs text-muted-foreground">All orders placed</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Revenue</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${statistics?.totalRevenue?.toLocaleString() || "0"}
            </div>
            <p className="text-xs text-muted-foreground">
              From confirmed orders
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Dashboard;
