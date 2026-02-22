"use client";

import AdminLayout from "@/components/layout/AdminLayout";
import Dashboard from "@/components/dashboard/Dashboard";
import { withRequireAdmin } from "@/hoc/withAuth";

function AdminDashboardPage() {
  return (
    <AdminLayout>
      <Dashboard />
    </AdminLayout>
  );
}

export default withRequireAdmin(AdminDashboardPage);
