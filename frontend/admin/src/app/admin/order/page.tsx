"use client";

import AdminLayout from "@/components/admin/AdminLayout";
import OrderList from "@/components/admin/OrderList";
import { withRequireAdmin } from "@/hoc/withAuth";

function AdminOrderListPage() {
  return (
    <AdminLayout>
      <OrderList />
    </AdminLayout>
  );
}

export default withRequireAdmin(AdminOrderListPage);
