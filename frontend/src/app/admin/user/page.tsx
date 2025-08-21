"use client";

import AdminLayout from "@/components/admin/AdminLayout";
import UserList from "@/components/admin/UserList";
import { useUserStore } from "@/stores/user.store";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function AdminUserListPage() {
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
      <UserList />
    </AdminLayout>
  );
}
