import { productApi } from "@/api";
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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useDataLoader } from "@/hooks";
import { Brand, Category, Product } from "@/interfaces";
import { useCartStore } from "@/stores";
import { getProductImageUrl } from "@/utils/productImage.util";
import { RotateCcw, Search } from "lucide-react";
import React, { useCallback, useState } from "react";
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

interface StaticData {
  brands: Brand[];
  categories: Category[];
}

const ProductSearch: React.FC = () => {
  const { addToCart } = useCartStore();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);

  // Combined static data loader
  const {
    data: staticData,
    loading: staticDataLoading,
    error: staticDataError,
  } = useDataLoader<StaticData>({
    loadFunction: async () => {
      const [brandsResponse, categoriesResponse] = await Promise.all([
        productApi.listBrand(),
        productApi.listCategory(),
      ]);

      if (!brandsResponse.success) throw new Error(brandsResponse.errorMessage);
      if (!categoriesResponse.success)
        throw new Error(categoriesResponse.errorMessage);

      return {
        brands: brandsResponse.data || [],
        categories: categoriesResponse.data || [],
      };
    },
    autoLoad: true,
    onError: () => toast.error("Failed to load brands and categories"),
  });

  const brands = (staticData as StaticData)?.brands ?? [];
  const categories = (staticData as StaticData)?.categories ?? [];

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

  // Products loader with committed search filters
  const [committedSearchFilters, setCommittedSearchFilters] =
    useState<SearchFilters>({});

  // API-ready filters with undefined values cleaned up
  const apiSearchParams = {
    keyword: committedSearchFilters.keyword || undefined,
    minPrice: committedSearchFilters.minPrice,
    maxPrice: committedSearchFilters.maxPrice,
    brandId: committedSearchFilters.brandId || undefined,
    categoryIds: committedSearchFilters.categoryIds?.length
      ? committedSearchFilters.categoryIds
      : undefined,
  };

  const {
    data: products = [],
    loading: isLoadingProducts,
    error: productsError,
    pagination,
    execute: searchProducts,
    goToPage,
  } = useDataLoader<Product>({
    loadFunction: async (page, size, filters) => {
      const response = await productApi.searchProduct({
        page,
        size,
        ...apiSearchParams,
        ...filters,
      });
      if (!response.success) throw new Error(response.errorMessage);
      return {
        data: response.data || [],
        totalRecords: response.metadata?.totalRecords || 0,
      };
    },
    pagination: true,
    autoLoad: true,
    onError: (error) => {
      toast.error("Failed to load products: " + error);
    },
  });

  const changePage = (newPage: number) => {
    if (newPage >= 1 && newPage <= (pagination?.totalPages ?? 0)) {
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

  // Loading states
  const isLoading = staticDataLoading || isLoadingProducts;
  const hasError = staticDataError || productsError;

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

  const handleSubmit = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      // Commit form filters to search and trigger search
      setCommittedSearchFilters(formFilters);
      searchProducts();
    },
    [formFilters, searchProducts]
  );

  return (
    <>
      {/* Filters Card */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Search Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit}>
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
                    {staticDataLoading ? (
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
                              <SelectItem key={brand.id} value={brand.id.toString()}>
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
                    {staticDataLoading ? (
                      <div className="flex justify-center py-3">
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                      </div>
                    ) : (
                      <>
                        <Select value="placeholder" onValueChange={() => {}}>
                          <SelectTrigger>
                            <SelectValue>
                              {formFilters.categoryIds && formFilters.categoryIds.length > 0
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
                <Button type="submit" variant="default" disabled={isLoading}>
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
          </form>
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
          ) : products && (products as Product[]).length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {(products as Product[]).map((product) => (
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
                  onClick={() => changePage(pagination.page - 1)}
                  className={
                    pagination.page === 1
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
                    isActive={pagination.page === page}
                    className="cursor-pointer"
                  >
                    {page}
                  </PaginationLink>
                </PaginationItem>
              ))}

              <PaginationItem>
                <PaginationNext
                  onClick={() => changePage(pagination.page + 1)}
                  className={
                    pagination.page === pagination.totalPages
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
