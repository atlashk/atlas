"use client";

import { useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import { useUserStore } from "@/stores/user.store";
import AdminLayout from "@/components/admin/AdminLayout";
import ProductList from "@/components/admin/ProductList";

export default function AdminProductListPage() {
  const router = useRouter();
  const { profile } = useUserStore();
  const hasCheckedAuth = useRef(false);

  useEffect(() => {
    if (hasCheckedAuth.current) return;
    
    // Redirect if not authenticated or not admin
    if (!profile || profile.role !== "ADMIN") {
      router.push("/login");
      return;
    }
    
    hasCheckedAuth.current = true;
  }, [profile, router]);

  // Don't render until auth check is complete
  if (!profile || profile.role !== "ADMIN") {
    return null;
  }

  return (
    <AdminLayout>
      <ProductList />
    </AdminLayout>
  );
}
