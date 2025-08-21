"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  FileType,
  ProductStatus,
  type Brand,
  type Category,
  type ExportProductFilters,
  type ListProductFilters,
  type Product,
} from "@/interfaces/product.interface";
import { productService } from "@/services";
import { useUserStore } from "@/stores/user.store";
import { formatCurrency, getProductStatusBadge } from "@/utils/formatter.util";
import { getProductImageUrl } from "@/utils/productImage.util";
import {
  Edit,
  Loader2,
  Plus,
  RotateCcw,
  Search,
  Trash2,
  Upload,
} from "lucide-react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import React, { useCallback, useEffect, useState } from "react";
import { toast } from "sonner";
import ExportDropdown from "./ExportDropdown";
import ImportProductModal from "./ImportProductModal";

interface ProductListProps {
  className?: string;
}

interface Metadata {
  currentPage: number;
  pageSize: number;
  totalPages: number;
  totalRecords: number;
}

interface ActiveStates {
  active: boolean;
  inactive: boolean;
}

const ProductList: React.FC<ProductListProps> = ({ className = "" }) => {
  const router = useRouter();
  const { profile } = useUserStore();

  // State
  const [activeStates, setActiveStates] = useState<ActiveStates>({
    active: true,
    inactive: true,
  });
  const [brands, setBrands] = useState<Brand[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoadingBrands, setIsLoadingBrands] = useState(false);
  const [isLoadingCategories, setIsLoadingCategories] = useState(false);
  const [isLoadingProducts, setIsLoadingProducts] = useState(false);
  const [isExporting, setIsExporting] = useState(false);
  const [isImporting] = useState(false);
  const [showImportModal, setShowImportModal] = useState(false);
  const [hasInitialLoad, setHasInitialLoad] = useState(false);
  const [metadata, setMetadata] = useState<Metadata>({
    currentPage: 1,
    pageSize: 20,
    totalPages: 1,
    totalRecords: 0,
  });
  const [filters, setFilters] = useState<ListProductFilters>({
    id: undefined,
    keyword: undefined,
    minPrice: undefined,
    maxPrice: undefined,
    status: "" as const,
    availableFrom: undefined,
    isActive: undefined,
    brandId: "",
    categoryIds: [],
    page: 1,
    size: 20,
  });

  // Redirect if not admin
  useEffect(() => {
    if (!profile || profile.role !== "ADMIN") {
      router.push("/login");
    }
  }, [profile, router]);

  const productStatuses = Object.values(ProductStatus);

  const loadBrands = useCallback(async () => {
    setIsLoadingBrands(true);
    try {
      const { data } = await productService.listBrand();
      setBrands(data);
    } catch {
      toast.error("Failed to load brands");
    } finally {
      setIsLoadingBrands(false);
    }
  }, []);

  const loadCategories = useCallback(async () => {
    setIsLoadingCategories(true);
    try {
      const { data } = await productService.listCategory();
      setCategories(data);
    } catch {
      toast.error("Failed to load categories");
    } finally {
      setIsLoadingCategories(false);
    }
  }, []);

  const handleActiveStateChange = useCallback(() => {
    let isActive: boolean | undefined;
    if (activeStates.active && activeStates.inactive) {
      isActive = undefined;
    } else if (activeStates.active) {
      isActive = true;
    } else if (activeStates.inactive) {
      isActive = false;
    } else {
      isActive = undefined;
    }
    setFilters((prev) => ({ ...prev, isActive }));
  }, [activeStates]);

  useEffect(() => {
    handleActiveStateChange();
  }, [handleActiveStateChange]);

  const applyFilters = useCallback(
    async (page: number, currentFilters?: ListProductFilters) => {
      if (isLoadingProducts) return;

      setIsLoadingProducts(true);
      try {
        const filtersToUse = currentFilters || filters;
        const updatedFilters = { ...filtersToUse, page };
        setFilters(updatedFilters);
        setMetadata((prev) => ({ ...prev, currentPage: page }));

        // Clean up empty or undefined filters
        const apiFilters: ListProductFilters = { ...updatedFilters };
        Object.keys(apiFilters).forEach((key) => {
          const typedKey = key as keyof ListProductFilters;
          if (
            apiFilters[typedKey] === "" ||
            apiFilters[typedKey] === undefined
          ) {
            delete apiFilters[typedKey];
          }
        });

        const response = await productService.listProduct(apiFilters);
        setProducts(response.data);
        setMetadata((prev) => ({ ...prev, ...response.metadata }));
      } catch {
        toast.error("Failed to load products");
      } finally {
        setIsLoadingProducts(false);
      }
    },
    [filters, isLoadingProducts]
  );

  const changePage = useCallback(
    (newPage: number) => {
      if (newPage >= 1 && newPage <= metadata.totalPages) {
        applyFilters(newPage);
      }
    },
    [metadata.totalPages, applyFilters]
  );

  const resetFilters = useCallback(() => {
    const resetFiltersData: ListProductFilters = {
      id: undefined,
      keyword: undefined,
      minPrice: undefined,
      maxPrice: undefined,
      status: "" as const,
      availableFrom: undefined,
      isActive: undefined,
      brandId: "",
      categoryIds: [],
      page: 1,
      size: 20,
    };
    setActiveStates({ active: true, inactive: true });
    setFilters(resetFiltersData);
    applyFilters(1, resetFiltersData);
  }, [applyFilters]);

  const handleFilterChange = (
    field: keyof ListProductFilters,
    value: string | number | boolean | undefined | number[]
  ) => {
    setFilters((prev) => ({ ...prev, [field]: value }));
  };

  const handleSearch = () => {
    applyFilters(1);
  };

  const handleDelete = useCallback(
    async (productId: number) => {
      if (!confirm("Are you sure you want to delete this product?")) return;

      try {
        await productService.deleteProduct(productId);
        toast.success("Product deleted successfully");
        applyFilters(metadata.currentPage);
      } catch {
        toast.error("Failed to delete product");
      }
    },
    [metadata.currentPage, applyFilters]
  );

  const handleExport = useCallback(
    async (fileType: "csv" | "excel" | "pdf") => {
      if (isExporting) return;

      setIsExporting(true);
      try {
        const exportFilters: ExportProductFilters = {
          id: filters.id,
          keyword: filters.keyword,
          minPrice: filters.minPrice,
          maxPrice: filters.maxPrice,
          status: filters.status,
          availableFrom: filters.availableFrom,
          isActive: filters.isActive,
          brandId: filters.brandId,
          categoryIds: filters.categoryIds,
          fileType: FileType[fileType.toUpperCase() as keyof typeof FileType],
        };
        await productService.exportProduct(exportFilters);
        toast.success(
          `Products exported successfully as ${fileType.toUpperCase()}`
        );
      } catch (error) {
        console.error("Export failed:", error);
        toast.error("Failed to export products");
      } finally {
        setIsExporting(false);
      }
    },
    [isExporting, filters]
  );

  const handleImportClick = useCallback(() => {
    setShowImportModal(true);
  }, []);

  const handleImportSuccess = useCallback(() => {
    setShowImportModal(false);
    applyFilters(1);
  }, [applyFilters]);

  // Load initial data
  useEffect(() => {
    const loadInitialData = async () => {
      try {
        await Promise.all([loadBrands(), loadCategories(), applyFilters(1)]);
      } finally {
        setIsLoadingProducts(false);
      }
    };

    if (profile && profile.role === "ADMIN" && !hasInitialLoad) {
      loadInitialData();
      setHasInitialLoad(true);
    }
  }, [profile, hasInitialLoad]);

  if (!profile || profile.role !== "ADMIN") {
    return null;
  }

  return (
    <div className={`space-y-6 ${className}`}>
      {/* Header */}
      <div className="flex items-center justify-start">
        <div className="flex gap-2">
          <Button
            variant="outline"
            onClick={handleImportClick}
            disabled={isImporting}
          >
            <Upload className="h-4 w-4 mr-2" />
            {isImporting ? (
              <>
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                Importing...
              </>
            ) : (
              "Import"
            )}
          </Button>
          <ExportDropdown isExporting={isExporting} onExport={handleExport} />
          <Button onClick={() => router.push("/admin/product/add")}>
            <Plus className="h-4 w-4 mr-2" />
            Add New Product
          </Button>
        </div>
      </div>

      {/* Filters Card */}
      <Card>
        <CardHeader>
          <CardTitle>Product Filters</CardTitle>
        </CardHeader>
        <CardContent>
          {/* Row 1: Product ID, Product Keyword, Min Price, Max Price */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="space-y-2">
              <Label htmlFor="productId">Product ID</Label>
              <Input
                type="number"
                id="productId"
                placeholder="Enter product ID"
                value={filters.id || ""}
                onChange={(e) =>
                  handleFilterChange(
                    "id",
                    e.target.value ? Number(e.target.value) : undefined
                  )
                }
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="keyword">Product Keyword</Label>
              <Input
                type="text"
                id="keyword"
                placeholder="Search by product name, description, or an attribute"
                value={filters.keyword || ""}
                onChange={(e) =>
                  handleFilterChange("keyword", e.target.value || undefined)
                }
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="minPrice">Min Price</Label>
              <Input
                type="number"
                step="0.01"
                id="minPrice"
                placeholder="Min price"
                value={filters.minPrice || ""}
                onChange={(e) =>
                  handleFilterChange(
                    "minPrice",
                    e.target.value ? Number(e.target.value) : undefined
                  )
                }
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="maxPrice">Max Price</Label>
              <Input
                type="number"
                step="0.01"
                id="maxPrice"
                placeholder="Max price"
                value={filters.maxPrice || ""}
                onChange={(e) =>
                  handleFilterChange(
                    "maxPrice",
                    e.target.value ? Number(e.target.value) : undefined
                  )
                }
              />
            </div>
          </div>

          {/* Row 2: Product Status, Available From, Activity */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mt-5">
            <div className="space-y-2">
              <Label htmlFor="status">Product Status</Label>
              <Select
                value={filters.status}
                onValueChange={(value) => handleFilterChange("status", value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="All Statuses" />
                </SelectTrigger>
                <SelectContent>
                  {productStatuses.map((status) => (
                    <SelectItem key={status} value={status}>
                      {status}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="availableFrom">Available From</Label>
              <Input
                type="date"
                id="availableFrom"
                value={filters.availableFrom || ""}
                onChange={(e) =>
                  handleFilterChange(
                    "availableFrom",
                    e.target.value || undefined
                  )
                }
              />
            </div>

            <div className="space-y-2">
              <Label>Activity</Label>
              <div className="border rounded p-3 flex items-center space-x-6">
                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="activeCheck"
                    checked={activeStates.active}
                    onCheckedChange={(checked) =>
                      setActiveStates((prev) => ({
                        ...prev,
                        active: checked as boolean,
                      }))
                    }
                  />
                  <Label htmlFor="activeCheck">Active</Label>
                </div>
                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="inactiveCheck"
                    checked={activeStates.inactive}
                    onCheckedChange={(checked) =>
                      setActiveStates((prev) => ({
                        ...prev,
                        inactive: checked as boolean,
                      }))
                    }
                  />
                  <Label htmlFor="inactiveCheck">Inactive</Label>
                </div>
              </div>
            </div>
          </div>

          {/* Row 3: Brand, Categories */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mt-4">
            <div className="space-y-2">
              <Label htmlFor="brandId">Brand</Label>
              {isLoadingBrands ? (
                <div className="flex items-center justify-center py-2">
                  <Loader2 className="h-4 w-4 animate-spin" />
                </div>
              ) : (
                <Select
                  value={filters.brandId || ""}
                  onValueChange={(value) =>
                    handleFilterChange("brandId", value)
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="All Brands" />
                  </SelectTrigger>
                  <SelectContent>
                    {brands.map((brand) => (
                      <SelectItem key={brand.id} value={brand.id.toString()}>
                        {brand.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            </div>

            <div className="space-y-2">
              <Label>Categories</Label>
              {isLoadingCategories ? (
                <div className="flex items-center justify-center py-2">
                  <Loader2 className="h-4 w-4 animate-spin" />
                </div>
              ) : (
                <Select value="placeholder" onValueChange={() => {}}>
                  <SelectTrigger>
                    <SelectValue>
                      {filters.categoryIds && filters.categoryIds.length > 0
                        ? `${filters.categoryIds.length} categories selected`
                        : "All Categories"}
                    </SelectValue>
                  </SelectTrigger>
                  <SelectContent>
                    {categories.map((category) => (
                      <label
                        key={category.id}
                        className="flex items-center space-x-2 cursor-pointer hover:bg-gray-50 p-1 rounded"
                      >
                        <input
                          type="checkbox"
                          className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                          checked={
                            filters.categoryIds?.includes(category.id) || false
                          }
                          onChange={(e) => {
                            const currentCategories = filters.categoryIds || [];
                            if (e.target.checked) {
                              handleFilterChange("categoryIds", [
                                ...currentCategories,
                                category.id,
                              ]);
                            } else {
                              handleFilterChange(
                                "categoryIds",
                                currentCategories.filter(
                                  (id) => id !== category.id
                                )
                              );
                            }
                          }}
                        />
                        <span className="text-sm">{category.name}</span>
                      </label>
                    ))}
                  </SelectContent>
                </Select>
              )}
            </div>
          </div>

          <div className="flex justify-start space-x-2 mt-4">
            <Button onClick={handleSearch} disabled={isLoadingProducts}>
              <Search className="h-4 w-4 mr-2" />
              Search
            </Button>
            <Button
              variant="outline"
              onClick={resetFilters}
              disabled={isLoadingProducts}
            >
              <RotateCcw className="h-4 w-4 mr-2" />
              Reset
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Products Table */}
      <Card>
        <CardHeader>
          <CardTitle>Product Results</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoadingProducts ? (
            <div className="flex flex-col items-center justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
              <p className="mt-2 text-muted-foreground">Loading products...</p>
            </div>
          ) : (
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>ID</TableHead>
                    <TableHead>Name</TableHead>
                    <TableHead>Image</TableHead>
                    <TableHead>Price</TableHead>
                    <TableHead>Quantity</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {products.length === 0 ? (
                    <TableRow>
                      <TableCell
                        colSpan={7}
                        className="text-center py-8 text-muted-foreground"
                      >
                        No products found
                      </TableCell>
                    </TableRow>
                  ) : (
                    products.map((product) => (
                      <TableRow key={product.id}>
                        <TableCell>{product.id}</TableCell>
                        <TableCell>{product.name}</TableCell>
                        <TableCell>
                          <Image
                            src={getProductImageUrl(product.image)}
                            alt={product.name}
                            width={48}
                            height={48}
                            className="rounded object-cover"
                          />
                        </TableCell>
                        <TableCell>${formatCurrency(product.price)}</TableCell>
                        <TableCell>{product.quantity}</TableCell>
                        <TableCell>
                          {getProductStatusBadge(product.status)}
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() =>
                                router.push(`/admin/product/edit/${product.id}`)
                              }
                            >
                              <Edit className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleDelete(product.id)}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Pagination */}
      {metadata.totalPages > 1 && (
        <div className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">
            Page {metadata.currentPage} of {metadata.totalPages} (
            {metadata.totalRecords} records)
          </div>
          <Pagination>
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    if (metadata.currentPage > 1) {
                      changePage(metadata.currentPage - 1);
                    }
                  }}
                  className={
                    metadata.currentPage <= 1
                      ? "pointer-events-none opacity-50"
                      : ""
                  }
                />
              </PaginationItem>

              {/* Page numbers */}
              {Array.from(
                { length: Math.min(5, metadata.totalPages) },
                (_, i) => {
                  const pageNumber =
                    Math.max(
                      1,
                      Math.min(
                        metadata.totalPages - 4,
                        metadata.currentPage - 2
                      )
                    ) + i;

                  if (pageNumber <= metadata.totalPages) {
                    return (
                      <PaginationItem key={pageNumber}>
                        <PaginationLink
                          href="#"
                          onClick={(e) => {
                            e.preventDefault();
                            changePage(pageNumber);
                          }}
                          isActive={pageNumber === metadata.currentPage}
                        >
                          {pageNumber}
                        </PaginationLink>
                      </PaginationItem>
                    );
                  }
                  return null;
                }
              )}

              <PaginationItem>
                <PaginationNext
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    if (metadata.currentPage < metadata.totalPages) {
                      changePage(metadata.currentPage + 1);
                    }
                  }}
                  className={
                    metadata.currentPage >= metadata.totalPages
                      ? "pointer-events-none opacity-50"
                      : ""
                  }
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </div>
      )}

      {/* Import Modal */}
      {showImportModal && (
        <ImportProductModal
          isVisible={showImportModal}
          onClose={() => setShowImportModal(false)}
          onImportSuccess={handleImportSuccess}
        />
      )}
    </div>
  );
};

export default ProductList;
