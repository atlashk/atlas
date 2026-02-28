"use client";

import { Metadata } from "@/api/apiClient";
import { catalogApi } from "@/api/index.api";
import ExportDropdown from "@/components/common/ExportDropdown";
import AdminLayout from "@/components/layout/AdminLayout";
import ImportProductModal from "@/components/product/ImportProductModal";
import StockDialog from "@/components/product/StockDialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
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
import { Spinner } from "@/components/ui/spinner";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { withRequireAdmin } from "@/hoc/withAuth";
import {
  FileType,
  type Brand,
  type Category,
  type ExportProductFilters,
  type Product,
  type RetrieveProductListFilters,
} from "@/interfaces/catalog.interface";
import { formatCurrency } from "@/utils/formatter.util";
import { getProductImageUrl } from "@/utils/productImage.util";
import {
  Boxes,
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

interface ProductListProps {
  className?: string;
}

interface ActiveStates {
  inStock: boolean;
  outOfStock: boolean;
}

const ProductList: React.FC<ProductListProps> = ({ className = "" }) => {
  const router = useRouter();
  const isInitialized = useRef(false);
  const hasInitialized = useRef(false); // Flag to prevent double initialization

  const [activeStates, setActiveStates] = useState<ActiveStates>({
    inStock: true,
    outOfStock: true,
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

  // Product types state
  const [productTypes, setProductTypes] = useState<Record<string, string>>({});
  const [isLoadingProductTypes, setIsLoadingProductTypes] = useState(false);

  const loadProductTypes = useCallback(async () => {
    if (isLoadingProductTypes || Object.keys(productTypes).length > 0) return;
    setIsLoadingProductTypes(true);
    try {
      const response = await catalogApi.retrieveProductTypes();
      if (response.success && response.data) {
        setProductTypes(response.data);
      } else {
        toast.error(response.errorMessage || "Failed to load product types");
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "Failed to load product types";
      toast.error(errorMessage);
    } finally {
      setIsLoadingProductTypes(false);
    }
  }, [isLoadingProductTypes, productTypes]);

  // Load brands data
  const loadBrands = useCallback(async () => {
    try {
      setIsLoadingBrands(true);
      setBrandsError(null);

      const brandsResponse = await catalogApi.retrieveAllBrand();

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

      const categoriesResponse = await catalogApi.retrieveAllCategory();

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

  const [isStockDialogOpen, setIsStockDialogOpen] = useState(false);
  const [stockProductId, setStockProductId] = useState<string | null>(null);

  // Filter state - current form values being edited by user (UI only)
  const [formFilters, setFormFilters] = useState<RetrieveProductListFilters>({
    id: undefined,
    keyword: undefined,
    type: undefined,
    minPrice: undefined,
    maxPrice: undefined,
    startPublishedAt: undefined,
    endPublishedAt: undefined,
    inStock: undefined,
    brandId: undefined,
    categoryIds: undefined,
    page: 1,
    size: 20,
  });

  // Applied filters - used for API calls (only updated when Search is clicked)
  const [appliedFilters, setAppliedFilters] = useState<RetrieveProductListFilters>({
    id: undefined,
    keyword: undefined,
    type: undefined,
    minPrice: undefined,
    maxPrice: undefined,
    startPublishedAt: undefined,
    endPublishedAt: undefined,
    inStock: undefined,
    brandId: undefined,
    categoryIds: undefined,
    page: 1,
    size: 20,
  });

  // Reload products function for external use
  const reloadProducts = useCallback(() => {
    // Force reload by updating applied filters with current values
    setAppliedFilters((prev) => ({ ...prev }));
  }, []);

  const goToPage = useCallback((page: number) => {
    setAppliedFilters((prev) => ({ ...prev, page }));
  }, []);

  const resetProductFilters = useCallback(() => {
    const resetFilters = {
      id: undefined,
      keyword: undefined,
      minPrice: undefined,
      maxPrice: undefined,
      type: undefined,
      startPublishedAt: undefined,
      endPublishedAt: undefined,
      inStock: undefined,
      brandId: undefined,
      categoryIds: undefined,
      page: 1,
      size: 20,
    };
    setFormFilters(resetFilters);
    setAppliedFilters(resetFilters);
  }, []);

  const handleActiveStateChange = useCallback(() => {
    let inStock: boolean | undefined;
    if (activeStates.inStock && activeStates.outOfStock) {
      inStock = undefined;
    } else if (activeStates.inStock) {
      inStock = true;
    } else if (activeStates.outOfStock) {
      inStock = false;
    } else {
      inStock = undefined;
    }
    setFormFilters((prev) => ({ ...prev, inStock }));
  }, [activeStates.inStock, activeStates.outOfStock]);

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
    setActiveStates({ inStock: true, outOfStock: true });
    resetProductFilters();
  }, [resetProductFilters]);

  // Initial data loading
  useEffect(() => {
    if (hasInitialized.current) {
      return;
    }
    hasInitialized.current = true;

    const initializeData = async () => {
      // Inline API calls to avoid dependency issues
      try {
        // Load brands
        setIsLoadingBrands(true);
        setBrandsError(null);
        const brandsResponse = await catalogApi.retrieveAllBrand();
        if (!brandsResponse.success) {
          throw new Error(
            brandsResponse.errorMessage || "Failed to load brands"
          );
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
      try {
        // Load categories
        setIsLoadingCategories(true);
        setCategoriesError(null);
        const categoriesResponse = await catalogApi.retrieveAllCategory();
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

      await loadProductTypes();

      // Mark as initialized and trigger initial product load
      isInitialized.current = true;
      // Force a filter update to trigger product loading
      setAppliedFilters((prev) => ({ ...prev }));
    };
    initializeData();
  }, [loadProductTypes]); // Empty dependency array to run only once

  // Reload products when applied filters change (including initial load)
  useEffect(() => {
    // Don't fetch on initial mount until initialization is complete
    if (!isInitialized.current) {
      return;
    }

    const fetchProducts = async () => {
      try {
        setIsLoadingProducts(true);
        setProductsError(null);

        // Clean up empty or undefined filters for API call
        const apiFilters: RetrieveProductListFilters = { ...appliedFilters };
        Object.keys(apiFilters).forEach((key) => {
          const typedKey = key as keyof RetrieveProductListFilters;
          if (
            apiFilters[typedKey] === "" ||
            apiFilters[typedKey] === undefined
          ) {
            delete apiFilters[typedKey];
          }
        });

        const response = await catalogApi.retrieveProductList(apiFilters);

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
    };

    fetchProducts();
  }, [appliedFilters]);

  // Filter update functions
  const updateFormFilter = useCallback(
    <K extends keyof RetrieveProductListFilters>(
      key: K,
      value: RetrieveProductListFilters[K]
    ) => {
      setFormFilters((prev) => ({ ...prev, [key]: value }));
    },
    []
  );

  const handleFilterChange = useCallback(
    (
      field: keyof RetrieveProductListFilters,
      value: string | number | boolean | undefined | number[]
    ) => {
      updateFormFilter(field, value);
    },
    [updateFormFilter]
  );

  const handleSearch = useCallback(() => {
    // Copy form filters to applied filters and reset to first page
    setAppliedFilters((prev) => ({ ...formFilters, page: 1 }));
  }, [formFilters]);

  const handleDelete = useCallback(
    async (productId: string) => {
      if (!confirm("Are you sure you want to delete this product?")) return;

      try {
        await catalogApi.deleteProduct(productId);
        toast.success("Product deleted successfully");
        reloadProducts();
      } catch {
        toast.error("Failed to delete product");
      }
    },
    [reloadProducts]
  );

  const closeStockDialog = useCallback(() => {
    setIsStockDialogOpen(false);
    setStockProductId(null);
  }, []);

  const openStockDialog = useCallback(
    (productId: string) => {
      setIsStockDialogOpen(true);
      setStockProductId(productId);
    },
    []
  );

  const handleExport = useCallback(
    async (fileType: "csv" | "excel" | "pdf") => {
      if (isExporting) return;

      setIsExporting(true);
      try {
        const exportFilters: ExportProductFilters = {
          id: appliedFilters.id,
          keyword: appliedFilters.keyword,
          minPrice: appliedFilters.minPrice,
          maxPrice: appliedFilters.maxPrice,
          type: appliedFilters.type,
          startPublishedAt: appliedFilters.startPublishedAt,
          endPublishedAt: appliedFilters.endPublishedAt,
          inStock: appliedFilters.inStock,
          brandId: appliedFilters.brandId,
          categoryIds: appliedFilters.categoryIds?.map(String),
          fileType: FileType[fileType.toUpperCase() as keyof typeof FileType],
        };
        await catalogApi.exportProduct(exportFilters);
        toast.success(
          `Products exported successfully as ${fileType.toUpperCase()}`
        );
      } catch {
        toast.error("Failed to export products");
      } finally {
        setIsExporting(false);
      }
    },
    [isExporting, appliedFilters]
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
      <div className="flex items-center justify-start">
        <div className="flex gap-2">
          <Button
            variant="outline"
            onClick={() => router.push("/product/add")}
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
                <Spinner className="text-blue-600" />
                Importing...
              </>
            ) : (
              "Import"
            )}
          </Button>
          <ExportDropdown isExporting={isExporting} onExport={handleExport} />
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Product Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="space-y-2">
              <Label htmlFor="productId">Product ID</Label>
              <Input
                type="text"
                id="productId"
                placeholder="Enter product ID"
                value={formFilters.id ?? ""}
                onChange={(e) =>
                  handleFilterChange("id", e.target.value || undefined)
                }
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="keyword">Product Keyword</Label>
              <Input
                type="text"
                id="keyword"
                placeholder="Search by product name, description, or an attribute"
                value={formFilters.keyword || ""}
                onChange={(e) =>
                  handleFilterChange("keyword", e.target.value || undefined)
                }
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="productType">Product Type</Label>
              <Select
                value={formFilters.type || ""}
                onValueChange={(value) => handleFilterChange("type", value)}
                disabled={isLoadingProductTypes}
              >
                <SelectTrigger>
                  <SelectValue placeholder="All Types" />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(productTypes).map(([typeKey, typeLabel]) => (
                    <SelectItem key={typeKey} value={typeKey}>
                      {typeLabel}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mt-5">
            <div className="space-y-2">
              <Label htmlFor="minPrice">Min Price</Label>
              <Input
                type="number"
                step="0.01"
                id="minPrice"
                placeholder="Min price"
                value={formFilters.minPrice || ""}
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
                value={formFilters.maxPrice || ""}
                onChange={(e) =>
                  handleFilterChange(
                    "maxPrice",
                    e.target.value ? Number(e.target.value) : undefined
                  )
                }
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="startPublishedAt">Start Publish date</Label>
              <Input
                type="date"
                id="startPublishedAt"
                placeholder="Start Publish date"
                value={formFilters.startPublishedAt || ""}
                onChange={(e) =>
                  handleFilterChange(
                    "startPublishedAt",
                    e.target.value || undefined
                  )
                }
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="endPublishedAt">End Publish date</Label>
              <Input
                type="date"
                id="endPublishedAt"
                placeholder="End Publish date"
                value={formFilters.endPublishedAt || ""}
                onChange={(e) =>
                  handleFilterChange(
                    "endPublishedAt",
                    e.target.value || undefined
                  )
                }
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mt-4">
            <div className="space-y-2">
              <Label>Stock Status</Label>
              <div className="border rounded p-3 flex items-center space-x-6">
                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="inStockCheck"
                    checked={activeStates.inStock}
                    onCheckedChange={(checked) =>
                      setActiveStates((prev) => ({
                        ...prev,
                        inStock: checked as boolean,
                      }))
                    }
                  />
                  <Label htmlFor="inStockCheck">In Stock</Label>
                </div>
                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="outOfStockCheck"
                    checked={activeStates.outOfStock}
                    onCheckedChange={(checked) =>
                      setActiveStates((prev) => ({
                        ...prev,
                        outOfStock: checked as boolean,
                      }))
                    }
                  />
                  <Label htmlFor="outOfStockCheck">Out of Stock</Label>
                </div>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="brandId">Brand</Label>
              {isLoadingBrands ? (
                <div className="flex justify-center py-3">
                  <Spinner className="text-blue-600" />
                </div>
              ) : (
                <>
                  <Select
                    disabled={!brands.length}
                    value={formFilters.brandId || ""}
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
                  <Spinner className="text-blue-600" />
                </div>
              ) : (
                <>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button
                        variant="outline"
                        className="w-full justify-between font-normal"
                        disabled={!categories.length}
                      >
                        {formFilters.categoryIds &&
                          formFilters.categoryIds.length > 0
                          ? `${formFilters.categoryIds.length} categories selected`
                          : "All Categories"}
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent className="w-64">
                      {categories
                        ?.filter(
                          (category): category is Category => category != null
                        )
                        .map((category) => {
                          const isChecked =
                            formFilters.categoryIds?.includes(category.id) ||
                            false;
                          return (
                            <DropdownMenuCheckboxItem
                              key={category.id}
                              checked={isChecked}
                              onCheckedChange={(checked) => {
                                const currentCategories =
                                  formFilters.categoryIds || [];
                                if (checked) {
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
                            >
                              {category.name}
                            </DropdownMenuCheckboxItem>
                          );
                        })}
                    </DropdownMenuContent>
                  </DropdownMenu>
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

      <Card>
        <CardHeader>
          <CardTitle>Product Results</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoadingProducts ? (
            <div className="flex justify-center py-12">
              <Spinner className="text-blue-600" />
            </div>
          ) : (
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>ID</TableHead>
                    <TableHead>Name</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Image</TableHead>
                    <TableHead>Price</TableHead>
                    <TableHead>Stock Status</TableHead>
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
                        <TableCell>{productTypes[product.type] || "Unknown"}</TableCell>
                        <TableCell>
                          <Image
                            src={getProductImageUrl(product.image)}
                            alt={product.name}
                            width={48}
                            height={48}
                            className="rounded object-cover"
                            unoptimized
                          />
                        </TableCell>
                        <TableCell>{formatCurrency(product.price)}</TableCell>
                        <TableCell>{product.inStock ? "In Stock" : "Out of Stock"}</TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() =>
                                router.push(`/product/${product.id}`)
                              }
                              title="View Product"
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => openStockDialog(product.id)}
                              title="Stock"
                            >
                              <Boxes className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() =>
                                router.push(`/product/${product.id}/edit`)
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

      <StockDialog
        isVisible={isStockDialogOpen}
        productId={stockProductId}
        onClose={closeStockDialog}
      />

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
function AdminProductListPage() {
  return (
    <AdminLayout>
      <ProductList />
    </AdminLayout>
  );
}

export default withRequireAdmin(AdminProductListPage);
