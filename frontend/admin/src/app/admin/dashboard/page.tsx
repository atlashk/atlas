"use client";

import AdminLayout from "@/components/layout/AdminLayout";
import Dashboard from "@/components/admin/Dashboard";
import { withRequireAdmin } from "@/hoc/withAuth";

function AdminDashboardPage() {
  return (
    <AdminLayout>
      <Dashboard />
    </AdminLayout>
  );
}

export default withRequireAdmin(AdminDashboardPage);
