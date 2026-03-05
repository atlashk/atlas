import { Metadata } from "@/api/apiClient";
import { catalogApi } from "@/api/index.api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Spinner } from "@/components/ui/spinner";
import { Brand, Category, Product } from "@/interfaces";
import { useCartStore, useUserStore } from "@/stores";

import { RotateCcw, Search } from "lucide-react";
import { useRouter, useSearchParams } from "next/navigation";
import React, { useCallback, useEffect, useRef, useState } from "react";
import { toast } from "sonner";
import ProductCard from "./ProductCard";
import ProductDetailsModal from "./ProductDetailsModal";

// Types for better type safety
interface SearchFilters {
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  brandId?: string;
  categoryIds?: string[];
  mode?: "DATABASE" | "FULL_TEXT_SEARCH";
}

const ProductSearch: React.FC = () => {
  const { addToCart } = useCartStore();
  const { isAuthenticated } = useUserStore();
  const router = useRouter();
  const searchParams = useSearchParams();
  const isInitialized = useRef(false);
  const lastRefreshParam = useRef<string | null>(null);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [isLoadingProductDetails, setIsLoadingProductDetails] = useState(false);

  // Brands state
  const [brands, setBrands] = useState<Brand[]>([]);
  const [isLoadingBrands, setIsLoadingBrands] = useState(true);

  // Categories state
  const [categories, setCategories] = useState<Category[]>([]);
  const [isLoadingCategories, setIsLoadingCategories] = useState(true);

  // Products state
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoadingProducts, setIsLoadingProducts] = useState(false);
  const [pagination, setPagination] = useState<Metadata>({
    currentPage: 1,
    totalPages: 1,
    totalRecords: 0,
    pageSize: 20,
  });

  // Filter state - current form values being edited by user
  const [formFilters, setFormFilters] = useState<SearchFilters>({
    keyword: "",
    minPrice: undefined,
    maxPrice: undefined,
    brandId: "",
    categoryIds: [],
    mode: "DATABASE",
  });

  // Filter update functions
  const updateFilter = useCallback(
    <K extends keyof SearchFilters>(key: K, value: SearchFilters[K]) => {
      setFormFilters((prev) => ({ ...prev, [key]: value }));
    },
    []
  );

  const resetFilters = useCallback(() => {
    setFormFilters({
      keyword: "",
      minPrice: undefined,
      maxPrice: undefined,
      brandId: "",
      categoryIds: [],
      mode: "DATABASE",
    });
  }, []);

  const updateNumericFilter = useCallback(
    (key: "minPrice" | "maxPrice", value: string) => {
      const numValue = value ? parseFloat(value) : undefined;
      updateFilter(key, numValue);
    },
    [updateFilter]
  );

  const changeCategory = useCallback((categoryId: string) => {
    setFormFilters((prev) => ({
      ...prev,
      categoryIds: prev.categoryIds?.includes(categoryId)
        ? prev.categoryIds.filter((id) => id !== categoryId)
        : [...(prev.categoryIds ?? []), categoryId],
    }));
  }, []);

  // Committed search filters
  const [committedSearchFilters, setCommittedSearchFilters] =
    useState<SearchFilters>({});

  // Load brands data
  const loadBrands = useCallback(async () => {
    try {
      setIsLoadingBrands(true);

      const brandsResponse = await catalogApi.retrieveAllBrand();

      if (!brandsResponse.success) {
        throw new Error(brandsResponse.errorMessage || "Failed to load brands");
      }

      setBrands(brandsResponse.data || []);
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "Failed to load brands";
      toast.error(errorMessage);
    } finally {
      setIsLoadingBrands(false);
    }
  }, []);

  // Load categories data
  const loadCategories = useCallback(async () => {
    try {
      setIsLoadingCategories(true);

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
      toast.error(errorMessage);
    } finally {
      setIsLoadingCategories(false);
    }
  }, []);

  // Load products data
  const loadProducts = useCallback(
    async (
      filters: SearchFilters = {},
      page: number = 1,
      pageSize: number = 20
    ) => {
      try {
        setIsLoadingProducts(true);

        // Clean up filters for API call
        const apiSearchParams = {
          page,
          size: pageSize,
          keyword: filters.keyword || undefined,
          minPrice: filters.minPrice,
          maxPrice: filters.maxPrice,
          brandId: filters.brandId || undefined,
          categoryIds: filters.categoryIds?.length
            ? filters.categoryIds
            : undefined,
          mode: filters.mode || "DATABASE",
        };

        const response = await catalogApi.retrieveProductList(apiSearchParams);

        if (!response.success) {
          throw new Error(response.errorMessage || "Failed to load products");
        }

        setProducts(response.data || []);
        setPagination({
          currentPage: page,
          totalPages: response.metadata?.totalPages || 1,
          totalRecords: response.metadata?.totalRecords || 0,
          pageSize: response.metadata?.pageSize || 20,
        });
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to load products";
        toast.error(errorMessage);
        setProducts([]);
      } finally {
        setIsLoadingProducts(false);
      }
    },
    []
  );

  // Go to specific page
  const goToPage = useCallback(
    (page: number) => {
      loadProducts(committedSearchFilters, page, pagination.pageSize);
    },
    [loadProducts, committedSearchFilters, pagination.pageSize]
  );

  const changePage = (newPage: number) => {
    if (newPage >= 1 && newPage <= pagination.totalPages) {
      goToPage(newPage);
    }
  };

  // Load product details
  const loadProductDetails = useCallback(async (productId: string) => {
    try {
      setIsLoadingProductDetails(true);
      const response = await catalogApi.retrieveProduct(productId);
      
      if (!response.success) {
        throw new Error(response.errorMessage || "Failed to load product details");
      }
      
      setSelectedProduct(response.data);
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "Failed to load product details";
      toast.error(errorMessage);
      setSelectedProduct(null);
    } finally {
      setIsLoadingProductDetails(false);
    }
  }, []);

  // Simplified event handlers
  const handleAddToCart = useCallback(
    async (product: Product) => {
      // Check if user is authenticated
      if (!isAuthenticated()) {
        toast.info("Please login to add items to cart");
        router.push("/login");
        return;
      }

      try {
        const success = await addToCart(product.id, 1);
        
        if (success) {
          toast.success(`${product.name} added to cart`);
        } else {
          toast.error("Failed to add product to cart");
        }
      } catch (error) {
        toast.error("Failed to add product to cart");
        console.error(error);
      }
    },
    [addToCart, isAuthenticated, router]
  );

  const handleProductClick = useCallback((product: Product) => {
    setIsModalOpen(true);
    setSelectedProduct(null); // Clear previous product
    loadProductDetails(product.id); // Load full details
  }, [loadProductDetails]);

  const handleCloseModal = useCallback(() => {
    setIsModalOpen(false);
    setSelectedProduct(null);
  }, []);

  // Initial data loading - load static data once and initial products
  useEffect(() => {
    const refreshParam = searchParams.get("refresh");

    // This effect should run only once on initialization or when a new refresh is triggered
    if (isInitialized.current && !refreshParam) {
      return;
    }

    // If it's a refresh, ensure it's a new one
    if (refreshParam && refreshParam === lastRefreshParam.current) {
      return;
    }

    if (refreshParam) {
      lastRefreshParam.current = refreshParam;
    }

    const initializeData = async () => {
      // Reset filters if it's a refresh
      if (refreshParam) {
        setFormFilters({
          keyword: "",
          minPrice: undefined,
          maxPrice: undefined,
          brandId: "",
          categoryIds: [],
          mode: "DATABASE",
        });
        setCommittedSearchFilters({});
      }

      // Fetch all data
      await Promise.all([
        loadBrands(),
        loadCategories(),
        loadProducts(refreshParam ? {} : committedSearchFilters, 1, 9),
      ]);
    };

    initializeData();

    // Mark as initialized after the first load
    if (!isInitialized.current) {
      isInitialized.current = true;
    }
  }, [
    searchParams,
    loadBrands,
    loadCategories,
    loadProducts,
    committedSearchFilters,
  ]);

  // Loading states
  const isLoading = isLoadingBrands || isLoadingCategories || isLoadingProducts;

  const handleSearch = useCallback(() => {
    // Commit form filters to search and trigger search
    setCommittedSearchFilters(formFilters);
    // Only load products, not static data
    loadProducts(formFilters, 1, pagination.pageSize);
  }, [formFilters, loadProducts, pagination.pageSize]
  );

  return (
    <div className="grid grid-cols-12 gap-6">
      {/* Filters Column - Left Side (col-4) */}
      <div className="col-span-12 lg:col-span-4">
        <Card className="sticky top-4">
          <CardHeader>
            <CardTitle>Search Filters</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
                {/* Search Input */}
                <div>
                  <Label htmlFor="keyword" className="mb-2">
                    Product Keyword
                  </Label>
                  <Input
                    id="keyword"
                    type="text"
                    placeholder="Product name, description, attributes, etc."
                    value={formFilters.keyword || ""}
                    onChange={(e) => updateFilter("keyword", e.target.value)}
                  />
                </div>

                {/* Price Range */}
                 <div>
                   <Label className="mb-2">
                     Price Range
                   </Label>
                   <div className="grid grid-cols-2 gap-3">
                     <div>
                       <Input
                         type="number"
                         placeholder="Min Price"
                         min="0"
                         step="0.01"
                         value={formFilters.minPrice || ""}
                         onChange={(e) =>
                           updateNumericFilter("minPrice", e.target.value)
                         }
                       />
                     </div>
                     <div>
                       <Input
                         type="number"
                         placeholder="Max Price"
                         min="0"
                         step="0.01"
                         value={formFilters.maxPrice || ""}
                         onChange={(e) =>
                           updateNumericFilter("maxPrice", e.target.value)
                         }
                       />
                     </div>
                   </div>
                 </div>

                {/* Brand Filter */}
                <div>
                  <Label htmlFor="brand" className="mb-2">
                    Brand
                  </Label>
                  {isLoadingBrands ? (
                    <div className="flex justify-center py-3">
                      <Spinner className="text-blue-600" />
                    </div>
                  ) : (
                    <>
                      <Select
                        disabled={!brands.length}
                        value={formFilters.brandId || "all"}
                        onValueChange={(value) =>
                          updateFilter(
                            "brandId",
                            value === "all" ? "" : value
                          )
                        }
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="All Brands" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="all">All Brands</SelectItem>
                          {brands.map((brand) => (
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

                {/* Category Filter */}
                <div>
                  <Label htmlFor="category" className="mb-2">
                    Categories
                  </Label>
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
                          {categories.map((category) => {
                            const isChecked =
                              formFilters.categoryIds?.includes(category.id) ??
                              false;
                            return (
                              <DropdownMenuCheckboxItem
                                key={category.id}
                                checked={isChecked}
                                onCheckedChange={() => changeCategory(category.id)}
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

                {/* Search Mode */}
                <div>
                  <Label className="mb-2">
                    Search Mode
                  </Label>
                  <RadioGroup
                    value={formFilters.mode}
                    onValueChange={(val) =>
                      updateFilter("mode", val as "DATABASE" | "FULL_TEXT_SEARCH")
                    }
                    className="gap-2"
                  >
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem id="mode-db" value="DATABASE" />
                      <Label htmlFor="mode-db">Database Search</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem id="mode-fts" value="FULL_TEXT_SEARCH" />
                      <Label htmlFor="mode-fts">Full Text Search</Label>
                    </div>
                  </RadioGroup>
                </div>

                {/* Action Buttons */}
                 <div className="flex gap-3">
                   <Button
                     type="button"
                     variant="default"
                     onClick={handleSearch}
                     disabled={isLoading}
                   >
                     <Search className="w-4 h-4" />
                     Search
                   </Button>
                   <Button
                     type="button"
                     variant="outline"
                     onClick={resetFilters}
                     disabled={isLoading}
                   >
                     <RotateCcw className="w-4 h-4" />
                     Reset
                   </Button>
                 </div>
              </div>
          </CardContent>
        </Card>
      </div>

      {/* Products Column - Right Side (col-8) */}
      <div className="col-span-12 lg:col-span-8">
        {/* Search Results Card */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Search Results</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoadingProducts ? (
              <div className="flex justify-center py-12">
                <Spinner className="text-blue-600" />
              </div>
            ) : products.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                {products.map((product) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    onProductClick={handleProductClick}
                    onAddToCart={handleAddToCart}
                  />
                ))}
              </div>
            ) : (
              <p className="text-center text-gray-500 py-8">No products found.</p>
            )}
          </CardContent>
        </Card>

        {/* Pagination */}
        {pagination.totalPages > 1 && (
          <div className="flex justify-center">
            <Pagination>
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious
                    onClick={() => changePage(pagination.currentPage - 1)}
                    className={
                      pagination.currentPage === 1
                        ? "pointer-events-none opacity-50"
                        : "cursor-pointer"
                    }
                  />
                </PaginationItem>

                {Array.from(
                  { length: pagination.totalPages },
                  (_, i) => i + 1
                ).map((page) => (
                  <PaginationItem key={page}>
                    <PaginationLink
                      onClick={() => changePage(page)}
                      isActive={pagination.currentPage === page}
                      className="cursor-pointer"
                    >
                      {page}
                    </PaginationLink>
                  </PaginationItem>
                ))}

                <PaginationItem>
                  <PaginationNext
                    onClick={() => changePage(pagination.currentPage + 1)}
                    className={
                      pagination.currentPage === pagination.totalPages
                        ? "pointer-events-none opacity-50"
                        : "cursor-pointer"
                    }
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          </div>
        )}
      </div>

      {/* Product Details Modal */}
      <ProductDetailsModal
        isOpen={isModalOpen}
        product={selectedProduct}
        isLoading={isLoadingProductDetails}
        onClose={handleCloseModal}
      />
    </div>
  );
};

export default ProductSearch;
