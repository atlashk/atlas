"use client";

import AdminLayout from "@/components/layout/AdminLayout";
import ProductList from "@/components/product/ProductList";
import { withRequireAdmin } from "@/hoc/withAuth";

function AdminProductListPage() {
  return (
    <AdminLayout>
      <ProductList />
    </AdminLayout>
  );
}

export default withRequireAdmin(AdminProductListPage);
