"use client";

import AdminLayout from "@/components/layout/AdminLayout";
import OrderList from "@/components/order/OrderList";
import { withRequireAdmin } from "@/hoc/withAuth";

function AdminOrderListPage() {
  return (
    <AdminLayout>
      <OrderList />
    </AdminLayout>
  );
}

export default withRequireAdmin(AdminOrderListPage);
