import { Brand, Category, Product, SearchProductFilters } from '@/interfaces';
import { productService } from '@/services';
import { useCartStore } from '@/stores';
import { formatCurrency } from '@/utils/formatter.util';
import { getProductImageUrl } from '@/utils/productImage.util';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Pagination, PaginationContent, PaginationItem, PaginationLink, PaginationNext, PaginationPrevious } from '@/components/ui/pagination';
import { Search, RotateCcw } from 'lucide-react';
import Image from 'next/image';
import React, { useCallback, useEffect, useState } from 'react';
import { toast } from 'sonner';
import ProductDetailsModal from './ProductDetailsModal';

interface Metadata {
  currentPage: number;
  pageSize: number;
  totalPages: number;
  totalRecords: number;
}

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
    brandId: '',
    categoryIds: [],
    page: 1,
    size: 9,
  });

  const loadBrands = async () => {
    setIsLoadingBrands(true);
    try {
      const response = await productService.listBrand();
      if (response.success) {
        setBrands(response.data || []);
      }
    } catch (error) {
      console.error('Error loading brands:', error);
      toast.error('Failed to load brands');
    } finally {
      setIsLoadingBrands(false);
    }
  };

  const loadCategories = async () => {
    setIsLoadingCategories(true);
    try {
      const response = await productService.listCategory();
      if (response.success) {
        setCategories(response.data || []);
      }
    } catch (error) {
      console.error('Error loading categories:', error);
      toast.error('Failed to load categories');
    } finally {
      setIsLoadingCategories(false);
    }
  };

  const applyFilters = useCallback(async (page: number) => {
    if (isLoadingProducts) return;

    setIsLoadingProducts(true);
    try {
      const updatedFilters = { ...filters, page };
      setMetadata(prev => ({ ...prev, currentPage: page }));
      
      const response = await productService.searchProduct(updatedFilters);
      if (response.success) {
        setProducts(response.data || []);
        setMetadata({
          currentPage: response.metadata?.currentPage ?? 1,
          pageSize: response.metadata?.pageSize ?? 9,
          totalPages: response.metadata?.totalPages ?? 1,
          totalRecords: response.metadata?.totalRecords ?? 0,
        });
      } else {
        toast.error(response.errorMessage || 'Failed to load products');
      }
    } catch (error) {
      console.error('Error loading products:', error);
      toast.error('Failed to load products');
    } finally {
      setIsLoadingProducts(false);
    }
  }, [filters, isLoadingProducts]);

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
      brandId: '',
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
      const response = await productService.getProduct(product.id);
      if (response.success && response.data) {
        setSelectedProduct(response.data);
        setShowModal(true);
      } else {
        toast.error(response.errorMessage || 'Failed to load product details');
      }
    } catch (error) {
      console.error('Error loading product details:', error);
      toast.error('Failed to load product details');
    } finally {
      setIsLoadingProduct(false);
    }
  };

  const handleAddToCart = (product: Product) => {
    addToCart({
      productId: String(product.id),
      name: product.name,
      price: product.price,
      imageUrl: getProductImageUrl(product.image)
    });
    toast.success(`${product.name} added to cart`);
  };

  const handleFilterChange = (field: keyof SearchProductFilters, value: string | number | number[] | undefined) => {
    setFilters(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    applyFilters(1);
  };

  const handleCategoryChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = Array.from(e.target.selectedOptions, option => parseInt(option.value));
    handleFilterChange('categoryIds', selectedOptions);
  };

  useEffect(() => {
    const initializeData = async () => {
      await Promise.all([loadBrands(), loadCategories(), applyFilters(1)]);
    };
    initializeData();
  }, []); // Empty dependency array - only run once on mount

  return (
    <div className="bg-white p-6 border border-gray-200 shadow-sm rounded-lg mb-6">
      {/* Filters Form */}
      <form onSubmit={handleSubmit} className="bg-gray-50 p-6 rounded-lg mb-6 border border-gray-100">
        <div className="space-y-6">
          {/* Search Input */}
          <div>
            <label htmlFor="keyword" className="block text-sm font-medium text-gray-700 mb-2">Search Products</label>
            <input
              id="keyword"
              type="text"
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="Search products..."
              value={filters.keyword || ''}
              onChange={(e) => handleFilterChange('keyword', e.target.value.trim() || undefined)}
            />
          </div>

          {/* Price Range */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Price Range</label>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <input
                  type="number"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Min Price"
                  min="0"
                  step="0.01"
                  value={filters.minPrice || ''}
                  onChange={(e) => handleFilterChange('minPrice', e.target.value ? parseFloat(e.target.value) : undefined)}
                />
              </div>
              <div>
                <input
                  type="number"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Max Price"
                  min="0"
                  step="0.01"
                  value={filters.maxPrice || ''}
                  onChange={(e) => handleFilterChange('maxPrice', e.target.value ? parseFloat(e.target.value) : undefined)}
                />
              </div>
            </div>
          </div>

          {/* Brand and Category Filters */}
          <div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label htmlFor="brandId" className="block text-sm font-medium text-gray-700 mb-2">Brand</label>
                {isLoadingBrands ? (
                  <div className="flex justify-center py-3" aria-live="polite">
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600" role="status" aria-label="Loading brands">
                      <span className="sr-only">Loading...</span>
                    </div>
                  </div>
                ) : (
                  <>
                    <select
                      id="brandId"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                      disabled={!brands.length}
                      value={filters.brandId || ''}
                      onChange={(e) => handleFilterChange('brandId', e.target.value || undefined)}
                    >
                      <option value="">All Brands</option>
                      {brands.map((brand) => (
                        <option key={brand.id} value={brand.id}>
                          {brand.name}
                        </option>
                      ))}
                    </select>
                    {!brands.length && (
                      <div className="text-gray-500 text-sm mt-1">
                        No brands available
                      </div>
                    )}
                  </>
                )}
              </div>
              <div>
                <label htmlFor="categoryIds" className="block text-sm font-medium text-gray-700 mb-2">Categories</label>
                {isLoadingCategories ? (
                  <div className="flex justify-center py-3" aria-live="polite">
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600" role="status" aria-label="Loading categories">
                      <span className="sr-only">Loading...</span>
                    </div>
                  </div>
                ) : (
                  <>
                    <select
                      id="categoryIds"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                      multiple
                      disabled={!categories.length}
                      size={3}
                      value={filters.categoryIds?.map(String) || []}
                      onChange={handleCategoryChange}
                    >
                      {categories.map((category) => (
                        <option key={category.id} value={category.id}>
                          {category.name}
                        </option>
                      ))}
                    </select>
                    {!categories.length ? (
                      <div className="text-gray-500 text-sm mt-1">
                        No categories available
                      </div>
                    ) : (
                      <small className="text-gray-500">Hold Ctrl/Cmd to select multiple</small>
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

      {/* Products Grid */}
      {isLoadingProducts ? (
        <div className="flex justify-center py-12" aria-live="polite">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" role="status" aria-label="Loading products">
            <span className="sr-only">Loading...</span>
          </div>
        </div>
      ) : products && products.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {products.map((product) => (
            <Card key={product.id} className="hover:shadow-md transition-shadow duration-200 overflow-hidden p-0">
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
                <p className="text-lg font-bold text-blue-600">{formatCurrency(product.price)}</p>
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
                  className={metadata.currentPage === 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                />
              </PaginationItem>
              
              {Array.from({ length: metadata.totalPages }, (_, i) => i + 1).map((page) => (
                <PaginationItem key={page}>
                  <PaginationLink
                    onClick={() => changePage(page)}
                    isActive={metadata.currentPage === page}
                    className="cursor-pointer"
                  >
                    {page}
                  </PaginationLink>
                </PaginationItem>
              ))}
              
              <PaginationItem>
                <PaginationNext 
                  onClick={() => changePage(metadata.currentPage + 1)}
                  className={metadata.currentPage === metadata.totalPages ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </div>
      )}
    </div>
  );
};

export default ProductSearch;
