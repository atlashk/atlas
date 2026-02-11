"use client";

import { iamAdminApi, orderAdminApi, productAdminApi } from "@/api/index.api";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  ChartConfig,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { formatCurrency } from "@/utils/formatter.util";
import {
  Activity,
  DollarSign,
  ShoppingCart,
  TrendingUp,
  Users,
} from "lucide-react";
import React, { useCallback, useEffect, useRef, useState } from "react";
import { Area, AreaChart, CartesianGrid, XAxis } from "recharts";
import { toast } from "sonner";

interface DashboardStats {
  totalUsers: number;
  totalProducts: number;
  totalOrders: number;
  totalRevenue: number;
}

interface MonthlyAggregation {
  year: number;
  month: number;
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
  const [monthlyStats, setMonthlyStats] = useState<MonthlyAggregation[]>([]);
  const [selectedYear, setSelectedYear] = useState<number | null>(null);

  // Load dashboard statistics
  const loadStats = useCallback(async () => {
    try {
      setLoading(true);
      const [
        usersResponse,
        productsResponse,
        ordersResponse,
        revenueResponse,
        monthlyResponse,
      ] = await Promise.all([
        iamAdminApi.countUser(),
        productAdminApi.countProduct(),
        orderAdminApi.retrieveOrderCount(),
        orderAdminApi.retrieveTotalRevenue(),
        orderAdminApi.retrieveMonthlyOrderStatistics(),
      ]);

      setStats({
        totalUsers: usersResponse.success ? usersResponse.data || 0 : 0,
        totalProducts: productsResponse.success ? productsResponse.data || 0 : 0,
        totalOrders: ordersResponse.success ? ordersResponse.data || 0 : 0,
        totalRevenue: revenueResponse.success ? revenueResponse.data || 0 : 0,
      });

      if (monthlyResponse.success && Array.isArray(monthlyResponse.data)) {
        const data = monthlyResponse.data || [];
        setMonthlyStats(data);
        setSelectedYear((prev) => {
          if (prev !== null) return prev;
          const years = data.map((d) => d.year);
          return years.length ? Math.max(...years) : null;
        });
      } else {
        setMonthlyStats([]);
      }
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

    // Set up polling to update data every 5 seconds
    const interval = setInterval(() => {
      loadStats();
    }, 5000);

    // Cleanup interval on component unmount
    return () => clearInterval(interval);
  }, [loadStats]);

  const displayStats = stats;

  const MONTH_NAMES = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
  ];

  const years = Array.from(new Set(monthlyStats.map((m) => m.year))).sort(
    (a, b) => b - a
  );

  const filteredMonthly = monthlyStats
    .filter((m) => (selectedYear !== null ? m.year === selectedYear : true))
    .sort((a, b) => a.month - b.month);

  const chartData = filteredMonthly.map((m) => ({
    month: MONTH_NAMES[m.month - 1] || String(m.month),
    revenue: Number(m.totalRevenue) || 0,
  }));

  const chartConfig: ChartConfig = {
    revenue: {
      label: "Revenue",
      color: "var(--chart-1)",
    },
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

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0">
          <div>
            <CardTitle className="text-sm font-medium">Revenue by Month</CardTitle>
          </div>
          <div className="w-[140px]">
            <Select
              value={selectedYear !== null ? String(selectedYear) : undefined}
              onValueChange={(value) => setSelectedYear(Number(value))}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select year" />
              </SelectTrigger>
              <SelectContent>
                {years.map((y) => (
                  <SelectItem key={y} value={String(y)}>
                    {y}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          <ChartContainer config={chartConfig}>
            <AreaChart
              accessibilityLayer
              data={chartData}
              margin={{ left: 12, right: 12 }}
            >
              <CartesianGrid vertical={false} />
              <XAxis
                dataKey="month"
                tickLine={false}
                axisLine={false}
                tickMargin={8}
                tickFormatter={(value) => String(value).slice(0, 3)}
              />
              <ChartTooltip
                cursor={false}
                content={
                  <ChartTooltipContent
                    indicator="line"
                    formatter={(value) => formatCurrency(Number(value))}
                  />
                }
              />
              <Area
                dataKey="revenue"
                type="natural"
                fill="var(--color-revenue)"
                fillOpacity={0.4}
                stroke="var(--color-revenue)"
              />
            </AreaChart>
          </ChartContainer>
        </CardContent>
        <CardContent>
          <div className="flex w-full items-start gap-2 text-sm">
            <div className="grid gap-2">
              <div className="flex items-center gap-2 leading-none font-medium">
                {selectedYear !== null ? selectedYear : ""} <TrendingUp className="h-4 w-4" />
              </div>
              <div className="text-muted-foreground flex items-center gap-2 leading-none">
                {selectedYear !== null && chartData.length
                  ? `${chartData[0].month} - ${chartData[chartData.length - 1].month}`
                  : "No data"}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Dashboard;
