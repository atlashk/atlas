"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useUserStore } from "@/stores/user.store";
import AdminLayout from "@/components/admin/AdminLayout";
import ProductList from "@/components/admin/ProductList";

export default function AdminProductListPage() {
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
      <ProductList />
    </AdminLayout>
  );
}
