"use client";

import { productApi } from "@/api/index.api";
import { productAdminApi } from "@/api/product.admin.api";
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
import { PRODUCT_STATUSES } from "@/constants";
import {
  FileType,
  type Brand,
  type Category,
  type ExportProductFilters,
  type ListProductFilters,
  type Product,
} from "@/interfaces/product.interface";
import { formatCurrency, getProductStatusBadge } from "@/utils/formatter.util";
import { getProductImageUrl } from "@/utils/productImage.util";

import { Metadata } from "@/api/apiClient";
import {
  Edit,
  Eye,
  Plus,
  RotateCcw,
  Search,
  Trash2,
  Upload,
} from "lucide-react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import React, { useCallback, useEffect, useRef, useState } from "react";
import { toast } from "sonner";
import ExportDropdown from "./ExportDropdown";
import ImportProductModal from "./ImportProductModal";

interface ProductListProps {
  className?: string;
}

interface ActiveStates {
  active: boolean;
  inactive: boolean;
}

const ProductList: React.FC<ProductListProps> = ({ className = "" }) => {
  const router = useRouter();
  const isInitialized = useRef(false);

  // UI State
  const [activeStates, setActiveStates] = useState<ActiveStates>({
    active: true,
    inactive: true,
  });
  const [isExporting, setIsExporting] = useState(false);
  const [isImporting] = useState(false);
  const [showImportModal, setShowImportModal] = useState(false);

  // Brands state
  const [brands, setBrands] = useState<Brand[]>([]);
  const [isLoadingBrands, setIsLoadingBrands] = useState(true);
  const [brandsError, setBrandsError] = useState<string | null>(null);

  // Categories state
  const [categories, setCategories] = useState<Category[]>([]);
  const [isLoadingCategories, setIsLoadingCategories] = useState(true);
  const [categoriesError, setCategoriesError] = useState<string | null>(null);

  // Load brands data
  const loadBrands = useCallback(async () => {
    try {
      setIsLoadingBrands(true);
      setBrandsError(null);

      const brandsResponse = await productApi.listBrand();

      if (!brandsResponse.success) {
        throw new Error(brandsResponse.errorMessage || "Failed to load brands");
      }

      setBrands(brandsResponse.data || []);
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "Failed to load brands";
      setBrandsError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setIsLoadingBrands(false);
    }
  }, []);

  // Load categories data
  const loadCategories = useCallback(async () => {
    try {
      setIsLoadingCategories(true);
      setCategoriesError(null);

      const categoriesResponse = await productApi.listCategory();

      if (!categoriesResponse.success) {
        throw new Error(
          categoriesResponse.errorMessage || "Failed to load categories"
        );
      }

      setCategories(categoriesResponse.data || []);
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "Failed to load categories";
      setCategoriesError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setIsLoadingCategories(false);
    }
  }, []);

  // Products state
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoadingProducts, setIsLoadingProducts] = useState(true);
  const [productsError, setProductsError] = useState<string | null>(null);
  const [pagination, setPagination] = useState<Metadata | null>(null);

  // Filter state - current form values being edited by user
  const [filters, setFilters] = useState<ListProductFilters>({
    id: undefined,
    keyword: undefined,
    minPrice: undefined,
    maxPrice: undefined,
    status: undefined,
    availableFrom: undefined,
    isActive: undefined,
    brandId: undefined,
    categoryIds: undefined,
    page: 1,
    size: 20,
  });

  // Load products based on filters
  const loadProducts = useCallback(async () => {
    try {
      setIsLoadingProducts(true);
      setProductsError(null);

      // Clean up empty or undefined filters for API call
      const apiFilters: ListProductFilters = { ...filters };
      Object.keys(apiFilters).forEach((key) => {
        const typedKey = key as keyof ListProductFilters;
        if (apiFilters[typedKey] === "" || apiFilters[typedKey] === undefined) {
          delete apiFilters[typedKey];
        }
      });

      const response = await productAdminApi.listProduct(apiFilters);

      if (!response.success) {
        throw new Error(response.errorMessage || "Failed to load products");
      }

      setProducts(response.data || []);
      setPagination(response.metadata || null);
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "Failed to load products";
      setProductsError(errorMessage);
      toast.error(errorMessage);
      setProducts([]);
      setPagination(null);
    } finally {
      setIsLoadingProducts(false);
    }
  }, [filters]);

  // Reload products function for external use
  const reloadProducts = useCallback(() => {
    loadProducts();
  }, [loadProducts]);

  const goToPage = useCallback((page: number) => {
    setFilters((prev) => ({ ...prev, page }));
  }, []);

  const resetProductFilters = useCallback(() => {
    setFilters({
      id: undefined,
      keyword: undefined,
      minPrice: undefined,
      maxPrice: undefined,
      status: undefined,
      availableFrom: undefined,
      isActive: undefined,
      brandId: undefined,
      categoryIds: undefined,
      page: 1,
      size: 20,
    });
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
  }, [activeStates.active, activeStates.inactive, setFilters]);

  useEffect(() => {
    handleActiveStateChange();
  }, [handleActiveStateChange]);

  const changePage = useCallback(
    (newPage: number) => {
      if (pagination && newPage >= 1 && newPage <= pagination.totalPages) {
        goToPage(newPage);
      }
    },
    [pagination, goToPage]
  );

  const resetFilters = useCallback(() => {
    setActiveStates({ active: true, inactive: true });
    resetProductFilters();
  }, [resetProductFilters]);

  // Initial data loading
  useEffect(() => {
    if (isInitialized.current) {
      return;
    }

    isInitialized.current = true;

    const initializeData = async () => {
      await Promise.all([loadBrands(), loadCategories(), loadProducts()]);
    };
    initializeData();
  }, [loadBrands, loadCategories, loadProducts]);

  // Filter update functions
  const updateFilter = useCallback(
    <K extends keyof ListProductFilters>(
      key: K,
      value: ListProductFilters[K]
    ) => {
      setFilters((prev) => ({ ...prev, [key]: value }));
    },
    []
  );

  const handleFilterChange = useCallback(
    (
      field: keyof ListProductFilters,
      value: string | number | boolean | undefined | number[]
    ) => {
      updateFilter(field, value);
    },
    [updateFilter]
  );

  const handleSearch = useCallback(() => {
    // Trigger products reload by updating filters
    setFilters((prev) => ({ ...prev }));
  }, []);

  const handleDelete = useCallback(
    async (productId: number) => {
      if (!confirm("Are you sure you want to delete this product?")) return;

      try {
        await productAdminApi.deleteProduct(productId);
        toast.success("Product deleted successfully");
        reloadProducts();
      } catch {
        toast.error("Failed to delete product");
      }
    },
    [reloadProducts]
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
        await productAdminApi.exportProduct(exportFilters);
        toast.success(
          `Products exported successfully as ${fileType.toUpperCase()}`
        );
      } catch {
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
    reloadProducts();
  }, [reloadProducts]);

  // Loading states
  const isLoading = isLoadingBrands || isLoadingCategories || isLoadingProducts;
  const hasError = brandsError || categoriesError || productsError;

  // Error handling
  if (hasError) {
    return (
      <div className={`space-y-6 ${className}`}>
        <div className="text-center text-red-600">
          Error loading data. Please try again later.
        </div>
      </div>
    );
  }

  return (
    <div className={`space-y-6 ${className}`}>
      {/* Header */}
      <div className="flex items-center justify-start">
        <div className="flex gap-2">
          <Button
            variant="outline"
            onClick={() => router.push("/admin/product/add")}
          >
            <Plus className="h-4 w-4" />
            New Product
          </Button>
          <Button
            variant="outline"
            onClick={handleImportClick}
            disabled={isImporting}
          >
            <Upload className="h-4 w-4" />
            {isImporting ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                Importing...
              </>
            ) : (
              "Import"
            )}
          </Button>
          <ExportDropdown isExporting={isExporting} onExport={handleExport} />
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
                value={filters.status || ""}
                onValueChange={(value) => handleFilterChange("status", value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="All Statuses" />
                </SelectTrigger>
                <SelectContent>
                  {PRODUCT_STATUSES.map((status) => (
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
                <div className="flex justify-center py-3">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                </div>
              ) : (
                <>
                  <Select
                    disabled={!brands.length}
                    value={filters.brandId || ""}
                    onValueChange={(value) =>
                      handleFilterChange("brandId", value)
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="All Brands" />
                    </SelectTrigger>
                    <SelectContent>
                      {brands
                        ?.filter((brand): brand is Brand => brand != null)
                        .map((brand) => (
                          <SelectItem
                            key={brand.id}
                            value={brand.id.toString()}
                          >
                            {brand.name}
                          </SelectItem>
                        ))}
                    </SelectContent>
                  </Select>
                  {!brands.length && (
                    <div className="text-gray-500 text-sm mt-1">
                      No brands available
                    </div>
                  )}
                </>
              )}
            </div>

            <div className="space-y-2">
              <Label>Categories</Label>
              {isLoadingCategories ? (
                <div className="flex justify-center py-3">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                </div>
              ) : (
                <>
                  <Select value="placeholder" onValueChange={() => {}}>
                    <SelectTrigger>
                      <SelectValue>
                        {filters.categoryIds && filters.categoryIds.length > 0
                          ? `${filters.categoryIds.length} categories selected`
                          : "All Categories"}
                      </SelectValue>
                    </SelectTrigger>
                    <SelectContent>
                      {categories
                        ?.filter(
                          (category): category is Category => category != null
                        )
                        .map((category) => (
                          <label
                            key={category.id}
                            className="flex items-center space-x-2 cursor-pointer hover:bg-gray-50 p-1 rounded"
                          >
                            <input
                              type="checkbox"
                              className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                              checked={
                                filters.categoryIds?.includes(category.id) ||
                                false
                              }
                              onChange={(e) => {
                                const currentCategories =
                                  filters.categoryIds || [];
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
                  {!categories.length && (
                    <div className="text-gray-500 text-sm mt-1">
                      No categories available
                    </div>
                  )}
                </>
              )}
            </div>
          </div>

          <div className="flex flex-col sm:flex-row gap-2 mt-4">
            <Button
              onClick={handleSearch}
              variant="default"
              disabled={isLoading}
            >
              <Search className="w-4 h-4" />
              Search
            </Button>
            <Button
              variant="outline"
              onClick={resetFilters}
              disabled={isLoading}
            >
              <RotateCcw className="w-4 h-4" />
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
            <div className="flex justify-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
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
                        <TableCell>{formatCurrency(product.price)}</TableCell>
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
                                router.push(`/admin/product/${product.id}`)
                              }
                              title="View Product"
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() =>
                                router.push(`/admin/product/${product.id}/edit`)
                              }
                              title="Edit Product"
                            >
                              <Edit className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleDelete(product.id)}
                              title="Delete Product"
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
      {pagination && pagination.totalPages > 1 && (
        <div className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">
            Page {pagination.currentPage} of {pagination.totalPages} (
            {pagination.totalRecords} records)
          </div>
          <Pagination>
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    if (pagination.currentPage > 1) {
                      changePage(pagination.currentPage - 1);
                    }
                  }}
                  className={
                    pagination.currentPage <= 1
                      ? "pointer-events-none opacity-50"
                      : ""
                  }
                />
              </PaginationItem>

              {/* Page numbers */}
              {Array.from(
                { length: Math.min(5, pagination.totalPages) },
                (_, i) => {
                  const pageNumber =
                    Math.max(
                      1,
                      Math.min(
                        pagination.totalPages - 4,
                        pagination.currentPage - 2
                      )
                    ) + i;

                  if (pageNumber <= pagination.totalPages) {
                    return (
                      <PaginationItem key={pageNumber}>
                        <PaginationLink
                          href="#"
                          onClick={(e) => {
                            e.preventDefault();
                            changePage(pageNumber);
                          }}
                          isActive={pageNumber === pagination.currentPage}
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
                    if (pagination.currentPage < pagination.totalPages) {
                      changePage(pagination.currentPage + 1);
                    }
                  }}
                  className={
                    pagination.currentPage >= pagination.totalPages
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
