import { productApi } from "@/api";
import { Metadata } from "@/api/apiClient";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
import { Brand, Category, Product } from "@/interfaces";
import { useCartStore } from "@/stores";
import { getProductImageUrl } from "@/utils/productImage.util";
import { RotateCcw, Search } from "lucide-react";
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
  categoryIds?: number[];
}

const ProductSearch: React.FC = () => {
  const { addToCart } = useCartStore();
  const isInitialized = useRef(false);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);

  // Brands state
  const [brands, setBrands] = useState<Brand[]>([]);
  const [isLoadingBrands, setIsLoadingBrands] = useState(true);
  const [brandsError, setBrandsError] = useState<string | null>(null);

  // Categories state
  const [categories, setCategories] = useState<Category[]>([]);
  const [isLoadingCategories, setIsLoadingCategories] = useState(true);
  const [categoriesError, setCategoriesError] = useState<string | null>(null);

  // Products state
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoadingProducts, setIsLoadingProducts] = useState(false);
  const [productsError, setProductsError] = useState<string | null>(null);
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
    });
  }, []);

  const updateNumericFilter = useCallback(
    (key: "minPrice" | "maxPrice", value: string) => {
      const numValue = value ? parseFloat(value) : undefined;
      updateFilter(key, numValue);
    },
    [updateFilter]
  );

  const changeCategory = useCallback((categoryId: number) => {
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

  // Load products data
  const loadProducts = useCallback(
    async (
      filters: SearchFilters = {},
      page: number = 1,
      pageSize: number = 20
    ) => {
      try {
        setIsLoadingProducts(true);
        setProductsError(null);

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
        };

        const response = await productApi.searchProduct(apiSearchParams);

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
        setProductsError(errorMessage);
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

  // Simplified event handlers
  const handleAddToCart = useCallback(
    (product: Product) => {
      addToCart({
        productId: product.id,
        name: product.name,
        price: product.price,
        imageUrl: getProductImageUrl(product.image),
      });
      toast.success(`${product.name} added to cart`);
    },
    [addToCart]
  );

  const handleProductClick = useCallback((product: Product) => {
    setSelectedProduct(product);
    setIsModalOpen(true);
  }, []);

  const handleCloseModal = useCallback(() => {
    setIsModalOpen(false);
    setSelectedProduct(null);
  }, []);

  // Initial data loading - load static data once and initial products
  useEffect(() => {
    if (isInitialized.current) {
      return;
    }
    
    isInitialized.current = true;
    
    const initializeData = async () => {
      await Promise.all([
        loadBrands(),
        loadCategories(),
        loadProducts({}, 1, 9),
      ]);
    };
    initializeData();
  }, []); // Empty dependency array to run only once on mount

  // Loading states
  const isLoading = isLoadingBrands || isLoadingCategories || isLoadingProducts;
  const hasError = brandsError || categoriesError || productsError;

  const handleSearch = useCallback(
    () => {
      // Commit form filters to search and trigger search
      setCommittedSearchFilters(formFilters);
      // Only load products, not static data
      loadProducts(formFilters, 1, pagination.pageSize);
    },
    [formFilters, loadProducts, pagination.pageSize]
  );

  // Error handling
  if (hasError) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center text-red-600">
          Error loading data. Please try again later.
        </div>
      </div>
    );
  }

  return (
    <>
      {/* Filters Card */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Search Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-6">
              {/* Search Input */}
              <div>
                <label
                  htmlFor="keyword"
                  className="block text-sm font-medium text-gray-700 mb-2"
                >
                  Product Keyword
                </label>
                <input
                  id="keyword"
                  type="text"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Product name, description, attributes, etc."
                  value={formFilters.keyword || ""}
                  onChange={(e) => updateFilter("keyword", e.target.value)}
                />
              </div>

              {/* Price Range */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Price Range
                </label>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <input
                      type="number"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
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
                    <input
                      type="number"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
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

              {/* Brand and Category Filters */}
              <div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label
                      htmlFor="brand"
                      className="block text-sm font-medium text-gray-700 mb-2"
                    >
                      Brand
                    </label>
                    {isLoadingBrands ? (
                      <div className="flex justify-center py-3">
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
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
                  <div>
                    <label
                      htmlFor="category"
                      className="block text-sm font-medium text-gray-700 mb-2"
                    >
                      Categories
                    </label>
                    {isLoadingCategories ? (
                      <div className="flex justify-center py-3">
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                      </div>
                    ) : (
                      <>
                        <Select value="placeholder" onValueChange={() => {}}>
                          <SelectTrigger>
                            <SelectValue>
                              {formFilters.categoryIds &&
                              formFilters.categoryIds.length > 0
                                ? `${formFilters.categoryIds.length} categories selected`
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
                                    formFilters.categoryIds?.includes(
                                      category.id
                                    ) ?? false
                                  }
                                  onChange={() => changeCategory(category.id)}
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
              </div>

              {/* Action Buttons */}
              <div className="flex flex-col sm:flex-row gap-2">
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

      {/* Search Results Card */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Search Results</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoadingProducts ? (
            <div className="flex justify-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </div>
          ) : products.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
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

      {/* Product Details Modal */}
      <ProductDetailsModal
        isOpen={isModalOpen}
        product={selectedProduct}
        isLoading={false}
        onClose={handleCloseModal}
      />

      {/* Pagination */}
      {pagination.totalPages > 1 && (
        <div className="flex justify-center mt-8">
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
    </>
  );
};

export default ProductSearch;
