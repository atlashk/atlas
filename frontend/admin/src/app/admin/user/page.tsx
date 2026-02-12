"use client";

import AdminLayout from "@/components/layout/AdminLayout";
import UserList from "@/components/admin/UserList";
import { withRequireAdmin } from "@/hoc/withAuth";

function AdminUserListPage() {
  return (
    <AdminLayout>
      <UserList />
    </AdminLayout>
  );
}

export default withRequireAdmin(AdminUserListPage);
