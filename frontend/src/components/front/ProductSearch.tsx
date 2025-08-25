import { productApi } from "@/api";
import { Metadata } from "@/api/apiClient";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
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
import { Brand, Category, Product, SearchProductFilters } from "@/interfaces";
import { useCartStore } from "@/stores";
import { formatCurrency } from "@/utils/formatter.util";
import { getProductImageUrl } from "@/utils/productImage.util";
import { RotateCcw, Search } from "lucide-react";
import Image from "next/image";
import React, { useCallback, useEffect, useRef, useState } from "react";
import { toast } from "sonner";
import ProductDetailsModal from "./ProductDetailsModal";

const ProductSearch: React.FC = () => {
  const { addToCart } = useCartStore();

  const [showModal, setShowModal] = useState(false);
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [brands, setBrands] = useState<Brand[]>([]);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [isLoadingProducts, setIsLoadingProducts] = useState(false);
  const [isLoadingProduct, setIsLoadingProduct] = useState(false);
  const [isLoadingBrands, setIsLoadingBrands] = useState(false);
  const [isLoadingCategories, setIsLoadingCategories] = useState(false);
  const brandsLoaded = useRef(false);
  const categoriesLoaded = useRef(false);
  const hasInitialLoad = useRef(false);
  const isInitializing = useRef(false);

  const [metadata, setMetadata] = useState<Metadata>({
    currentPage: 1,
    pageSize: 9,
    totalPages: 1,
    totalRecords: 0,
  });

  const [filters, setFilters] = useState<SearchProductFilters>({
    keyword: undefined,
    minPrice: undefined,
    maxPrice: undefined,
    brandId: undefined,
    categoryIds: [],
    page: 1,
    size: 9,
  });

  const loadBrands = async () => {
    if (brandsLoaded.current || isLoadingBrands) return;
    
    setIsLoadingBrands(true);
    try {
      const response = await productApi.listBrand();
      if (response.success) {
        setBrands(response.data || []);
        brandsLoaded.current = true;
      }
    } catch {
      toast.error("Failed to load brands");
    } finally {
      setIsLoadingBrands(false);
    }
  };

  const loadCategories = async () => {
    if (categoriesLoaded.current || isLoadingCategories) return;
    
    setIsLoadingCategories(true);
    try {
      const response = await productApi.listCategory();
      if (response.success) {
        setCategories(response.data || []);
        categoriesLoaded.current = true;
      }
    } catch {
      toast.error("Failed to load categories");
    } finally {
      setIsLoadingCategories(false);
    }
  };

  const applyFilters = useCallback(
    async (page: number) => {
      if (isLoadingProducts) return;

      setIsLoadingProducts(true);
      try {
        const updatedFilters = { ...filters, page };
        setMetadata((prev) => ({ ...prev, currentPage: page }));

        const response = await productApi.searchProduct(updatedFilters);
        if (response.success) {
          setProducts(response.data || []);
          setMetadata({
            currentPage: response.metadata?.currentPage ?? 1,
            pageSize: response.metadata?.pageSize ?? 9,
            totalPages: response.metadata?.totalPages ?? 1,
            totalRecords: response.metadata?.totalRecords ?? 0,
          });
        } else {
          toast.error(response.errorMessage || "Failed to load products");
        }
      } catch {
        toast.error("Failed to load products");
      } finally {
        setIsLoadingProducts(false);
      }
    },
    [filters, isLoadingProducts]
  );

  const changePage = (newPage: number) => {
    if (newPage >= 1 && newPage <= metadata.totalPages) {
      applyFilters(newPage);
    }
  };

  const resetFilters = () => {
    const resetFilters: SearchProductFilters = {
      keyword: undefined,
      minPrice: undefined,
      maxPrice: undefined,
      brandId: undefined,
      categoryIds: [],
      page: 1,
      size: 9,
    };
    setFilters(resetFilters);
    applyFilters(1);
  };

  const showProductDetails = async (product: Product) => {
    setIsLoadingProduct(true);
    try {
      const response = await productApi.getProduct(product.id);
      if (response.success && response.data) {
        setSelectedProduct(response.data);
        setShowModal(true);
      } else {
        toast.error(response.errorMessage || "Failed to load product details");
      }
    } catch {
      toast.error("Failed to load product details");
    } finally {
      setIsLoadingProduct(false);
    }
  };

  const handleAddToCart = (product: Product) => {
    addToCart({
      productId: product.id,
      name: product.name,
      price: product.price,
      imageUrl: getProductImageUrl(product.image),
    });
    toast.success(`${product.name} added to cart`);
  };

  const handleFilterChange = (
    field: keyof SearchProductFilters,
    value: string | number | number[] | undefined
  ) => {
    // Convert "all" to undefined for brandId to avoid sending it to backend
    if (field === "brandId" && value === "all") {
      value = undefined;
    }
    setFilters((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    applyFilters(1);
  };

  useEffect(() => {
    const initializeData = async () => {
      if (hasInitialLoad.current || isInitializing.current) {
        return;
      }

      isInitializing.current = true;

      try {
        await Promise.all([loadBrands(), loadCategories(), applyFilters(1)]);
        hasInitialLoad.current = true;
      } finally {
        isInitializing.current = false;
      }
    };
    initializeData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Empty dependency array - only run once on mount

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
                  value={filters.keyword || ""}
                  onChange={(e) =>
                    handleFilterChange(
                      "keyword",
                      e.target.value.trim() || undefined
                    )
                  }
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
                      value={filters.minPrice || ""}
                      onChange={(e) =>
                        handleFilterChange(
                          "minPrice",
                          e.target.value
                            ? parseFloat(e.target.value)
                            : undefined
                        )
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
                      value={filters.maxPrice || ""}
                      onChange={(e) =>
                        handleFilterChange(
                          "maxPrice",
                          e.target.value
                            ? parseFloat(e.target.value)
                            : undefined
                        )
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
                      htmlFor="brandId"
                      className="block text-sm font-medium text-gray-700 mb-2"
                    >
                      Brand
                    </label>
                    {isLoadingBrands ? (
                      <div
                        className="flex justify-center py-3"
                        aria-live="polite"
                      >
                        <div
                          className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600"
                          role="status"
                          aria-label="Loading brands"
                        >
                          <span className="sr-only">Loading...</span>
                        </div>
                      </div>
                    ) : (
                      <>
                        <Select
                          disabled={!brands.length}
                          value={
                            filters.brandId ? filters.brandId.toString() : "all"
                          }
                          onValueChange={(value) =>
                            handleFilterChange("brandId", value || undefined)
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
                      htmlFor="categoryIds"
                      className="block text-sm font-medium text-gray-700 mb-2"
                    >
                      Categories
                    </label>
                    {isLoadingCategories ? (
                      <div
                        className="flex justify-center py-3"
                        aria-live="polite"
                      >
                        <div
                          className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600"
                          role="status"
                          aria-label="Loading categories"
                        >
                          <span className="sr-only">Loading...</span>
                        </div>
                      </div>
                    ) : (
                      <>
                        <Select
                          disabled={!categories.length}
                          value="placeholder"
                          onValueChange={() => {}}
                        >
                          <SelectTrigger>
                            <SelectValue>
                              {filters.categoryIds &&
                              filters.categoryIds.length > 0
                                ? `${filters.categoryIds.length} categories selected`
                                : "All Categories"}
                            </SelectValue>
                          </SelectTrigger>
                          <SelectContent>
                            <div className="p-2 space-y-2">
                              {categories.map((category) => (
                                <label
                                  key={category.id}
                                  className="flex items-center space-x-2 cursor-pointer hover:bg-gray-50 p-1 rounded"
                                >
                                  <input
                                    type="checkbox"
                                    className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                                    checked={
                                      filters.categoryIds?.includes(
                                        category.id
                                      ) || false
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
                                  <span className="text-sm">
                                    {category.name}
                                  </span>
                                </label>
                              ))}
                            </div>
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

              {/* Submit Button */}
              <div className="flex gap-3">
                <Button type="submit" className="px-4 flex items-center gap-2">
                  <Search className="h-4 w-4" />
                  Search
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={resetFilters}
                  className="px-4 flex items-center gap-2"
                >
                  <RotateCcw className="h-4 w-4" />
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
            <div className="flex justify-center py-12" aria-live="polite">
              <div
                className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"
                role="status"
                aria-label="Loading products"
              >
                <span className="sr-only">Loading...</span>
              </div>
            </div>
          ) : products && products.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {products.map((product) => (
                <Card
                  key={product.id}
                  className="hover:shadow-md transition-shadow duration-200 overflow-hidden p-0"
                >
                  <div className="aspect-[4/3] relative overflow-hidden">
                    <Image
                      src={getProductImageUrl(product.image)}
                      alt={product.name}
                      fill
                      className="object-cover hover:scale-105 transition-transform duration-200"
                      sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
                    />
                  </div>
                  <CardContent className="p-3 py-0">
                    <CardTitle
                      className="text-base font-semibold text-gray-900 mb-1 line-clamp-2 cursor-pointer hover:text-blue-600 hover:underline transition-colors"
                      onClick={() => showProductDetails(product)}
                    >
                      {product.name}
                    </CardTitle>
                    <p className="text-lg font-bold text-blue-600">
                      {formatCurrency(product.price)}
                    </p>
                  </CardContent>
                  <CardFooter className="p-3 pt-0">
                    <Button
                      onClick={() => handleAddToCart(product)}
                      className="w-full"
                    >
                      Add to Cart
                    </Button>
                  </CardFooter>
                </Card>
              ))}
            </div>
          ) : (
            <p className="text-center text-gray-500 py-8">No products found.</p>
          )}
        </CardContent>
      </Card>

      {/* Product Details Modal */}
      <ProductDetailsModal
        isOpen={showModal}
        product={selectedProduct}
        isLoading={isLoadingProduct}
        onClose={() => setShowModal(false)}
      />

      {/* Pagination */}
      {metadata.totalPages > 1 && (
        <div className="flex justify-center mt-8">
          <Pagination>
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  onClick={() => changePage(metadata.currentPage - 1)}
                  className={
                    metadata.currentPage === 1
                      ? "pointer-events-none opacity-50"
                      : "cursor-pointer"
                  }
                />
              </PaginationItem>

              {Array.from({ length: metadata.totalPages }, (_, i) => i + 1).map(
                (page) => (
                  <PaginationItem key={page}>
                    <PaginationLink
                      onClick={() => changePage(page)}
                      isActive={metadata.currentPage === page}
                      className="cursor-pointer"
                    >
                      {page}
                    </PaginationLink>
                  </PaginationItem>
                )
              )}

              <PaginationItem>
                <PaginationNext
                  onClick={() => changePage(metadata.currentPage + 1)}
                  className={
                    metadata.currentPage === metadata.totalPages
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
