"use client";

import { catalogApi } from "@/api/index.api";
import AdminLayout from "@/components/layout/AdminLayout";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Spinner } from "@/components/ui/spinner";
import { withRequireAdmin } from "@/hoc/withAuth";
import { Product } from "@/interfaces/catalog.interface";
import { formatCurrency } from "@/utils/formatter.util";
import { ArrowLeft, Edit, Trash2 } from "lucide-react";
import Image from "next/image";
import { useParams, useRouter } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";
import { toast } from "sonner";

function AdminProductDetailsPage() {
  const router = useRouter();
  const params = useParams();
  const isInitialized = useRef(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [product, setProduct] = useState<Product | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const productId = params.id as string;

  // Load product data
  const loadProduct = useCallback(async () => {
    try {
      setIsLoading(true);
      const response = await catalogApi.retrieveAdminProduct(productId);
      if (response.success) {
        setProduct(response.data);
      } else {
        toast.error(response.errorMessage || "Product not found");
        router.push("/admin/product");
      }
    } catch {
      toast.error("Failed to load product");
      router.push("/admin/product");
    } finally {
      setIsLoading(false);
    }
  }, [productId, router]);

  useEffect(() => {
    if (isInitialized.current) {
      return;
    }

    isInitialized.current = true;
    loadProduct();
  }, [loadProduct]);

  const handleEdit = () => {
    router.push(`/admin/product/${productId}/edit`);
  };

  const handleDelete = async () => {
    if (!product) return;

    if (confirm("Are you sure you want to delete this product?")) {
      setIsDeleting(true);
      try {
        const response = await catalogApi.deleteProduct(
          (product as Product).id,
        );
        if (response.success) {
          toast.success("Product deleted successfully!");
          router.push("/admin/product");
        } else {
          toast.error(response.errorMessage || "Failed to delete product");
        }
      } catch {
        toast.error("Failed to delete product");
      } finally {
        setIsDeleting(false);
      }
    }
  };

  const handleBack = () => {
    router.back();
  };

  if (isLoading) {
    return (
      <AdminLayout>
        <div className="flex items-center justify-center min-h-[400px]">
          <Spinner className="text-blue-600" />
        </div>
      </AdminLayout>
    );
  }

  if (!product) {
    return (
      <AdminLayout>
        <div className="flex flex-col items-center justify-center min-h-[400px] space-y-4">
          <h2 className="text-2xl font-semibold">Product not found</h2>
          <Button onClick={handleBack} variant="outline">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Go Back
          </Button>
        </div>
      </AdminLayout>
    );
  }

  return (
    <AdminLayout>
      <div className="container mx-auto px-2">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center space-x-2">
            <Button onClick={handleEdit} variant="outline">
              <Edit className="mr-2 h-4 w-4" />
              Edit
            </Button>
            <Button
              onClick={handleDelete}
              variant="outline"
              className="text-destructive hover:text-destructive"
              disabled={isDeleting}
            >
              <Trash2 className="mr-2 h-4 w-4" />
              {isDeleting ? "Deleting..." : "Delete"}
            </Button>
            <Button onClick={handleBack} variant="outline">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back
            </Button>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Product Image and Basic Info */}
          <div className="lg:col-span-1">
            <Card>
              <CardContent className="p-6 pt-0">
                <div className="h-70 w-70 mx-auto mb-4 bg-muted rounded-lg overflow-hidden">
                  {(product as Product).image ? (
                    <Image
                      src={(product as Product).image}
                      alt={(product as Product).name}
                      width={280}
                      height={280}
                      className="w-full h-full object-cover"
                      unoptimized
                    />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-muted-foreground">
                      No Image
                    </div>
                  )}
                </div>

                <h2 className="text-2xl font-bold mb-3">
                  {(product as Product).name}
                </h2>

                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">Type:</span>
                    {(product as Product).type}
                  </div>

                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">Price:</span>
                    <span className="font-bold text-primary">
                      {formatCurrency((product as Product).price)}
                    </span>
                  </div>

                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">Published Date:</span>
                    <span className="text-sm">
                      {(product as Product).publishedAt
                        ? new Date(
                            (product as Product).publishedAt!,
                          ).toLocaleDateString()
                        : "Not set"}
                    </span>
                  </div>

                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">Brand:</span>
                    <div className="flex flex-wrap gap-1">
                      {(product as Product).brand?.name ? (
                        <Badge variant="secondary">
                          {(product as Product).brand?.name}
                        </Badge>
                      ) : (
                        <span className="text-sm text-gray-500">No brand</span>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">Categories:</span>
                    <div className="flex flex-wrap gap-1">
                      {(product as Product).categories?.map((category) => (
                        <Badge key={category.id} variant="secondary">
                          {category.name}
                        </Badge>
                      )) || (
                        <span className="text-sm text-gray-500">
                          No categories
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Product Details and Attributes */}
          <div className="lg:col-span-2 space-y-6">
            {/* Description */}
            <Card>
              <CardHeader>
                <CardTitle>Description</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm leading-relaxed">
                  {(product as Product).details?.description ||
                    "No description available."}
                </p>
              </CardContent>
            </Card>

            {/* Specifications */}
            <Card>
              <CardHeader>
                <CardTitle>Specifications</CardTitle>
              </CardHeader>
              <CardContent>
                {(product as Product).attributes?.length ? (
                  <div className="space-y-3">
                    {(product as Product).attributes?.map((attr) => (
                      <div
                        key={attr.id}
                        className="flex items-center justify-between py-2 border-b border-border last:border-b-0"
                      >
                        <span className="font-medium text-sm">{attr.name}</span>
                        <span className="text-sm text-muted-foreground">
                          {attr.value}
                        </span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-muted-foreground">
                    No specifications available.
                  </p>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </AdminLayout>
  );
}

export default withRequireAdmin(AdminProductDetailsPage);
