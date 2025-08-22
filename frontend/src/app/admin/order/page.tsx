"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useUserStore } from "@/stores/user.store";
import AdminLayout from "@/components/admin/AdminLayout";
import OrderList from "@/components/admin/OrderList";

export default function AdminOrderListPage() {
  const router = useRouter();
  const { isAuthenticated, isAdmin } = useUserStore();

  useEffect(() => {
    // Redirect if not authenticated or not admin
    if (!isAuthenticated() || !isAdmin()) {
      router.push("/login");
      return;
    }
  }, [isAuthenticated, isAdmin, router]);

  return (
    <AdminLayout>
      <OrderList />
    </AdminLayout>
  );
}
