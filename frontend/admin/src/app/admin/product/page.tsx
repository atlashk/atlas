"use client";

import AdminLayout from "@/components/layout/AdminLayout";
import ProductList from "@/components/admin/ProductList";
import { withRequireAdmin } from "@/hoc/withAuth";

function AdminProductListPage() {
  return (
    <AdminLayout>
      <ProductList />
    </AdminLayout>
  );
}

export default withRequireAdmin(AdminProductListPage);
