'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import { useUserStore } from '@/stores/user.store';
import { toast } from 'sonner';
import { productService } from '@/services';
import {
  ProductStatus,
  FileType,
  type Brand,
  type Category,
  type ListProductFilters,
  type Product,
  type ExportProductFilters
} from '@/interfaces/product.interface';
import ExportDropdown from './ExportDropdown';
import { formatCurrency, formatProductStatusLabel, getProductStatusBadgeClasses } from '@/utils/formatter.util';
import { getProductImageUrl } from '@/utils/productImage.util';
import ImportProductModal from './ImportProductModal';

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

const ProductList: React.FC<ProductListProps> = ({ className = '' }) => {
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
    status: '' as const,
    availableFrom: undefined,
    isActive: undefined,
    brandId: '',
    categoryIds: [],
    page: 1,
    size: 20,
  });

  // Redirect if not admin
  useEffect(() => {
    if (!profile || profile.role !== 'ADMIN') {
      router.push('/login');
    }
  }, [profile, router]);

  const productStatuses = Object.values(ProductStatus);

  const loadBrands = useCallback(async () => {
    setIsLoadingBrands(true);
    try {
      const { data } = await productService.listBrand();
      setBrands(data);
    } catch {
      toast.error('Failed to load brands');
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
      toast.error('Failed to load categories');
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
    setFilters(prev => ({ ...prev, isActive }));
  }, [activeStates]);

  useEffect(() => {
    handleActiveStateChange();
  }, [handleActiveStateChange]);

  const applyFilters = useCallback(async (page: number) => {
    if (isLoadingProducts) return;

    setIsLoadingProducts(true);
    try {
      const updatedFilters = { ...filters, page };
      setFilters(updatedFilters);
      setMetadata(prev => ({ ...prev, currentPage: page }));
      const response = await productService.listProduct(updatedFilters);
      setProducts(response.data);
      setMetadata(prev => ({ ...prev, ...response.metadata }));
    } catch {
      toast.error('Failed to load products');
    } finally {
      setIsLoadingProducts(false);
    }
  }, [filters, isLoadingProducts]);

  const changePage = useCallback((newPage: number) => {
    if (newPage >= 1 && newPage <= metadata.totalPages) {
      applyFilters(newPage);
    }
  }, [metadata.totalPages, applyFilters]);

  const resetFilters = useCallback(() => {
    setActiveStates({ active: true, inactive: true });
    setFilters({
      id: undefined,
      keyword: undefined,
      minPrice: undefined,
      maxPrice: undefined,
      status: '' as const,
      availableFrom: undefined,
      isActive: undefined,
      brandId: '',
      categoryIds: [],
      page: 1,
      size: 20,
    });
    applyFilters(1);
  }, [applyFilters]);

  const handleDelete = useCallback(async (productId: number) => {
    if (!confirm('Are you sure you want to delete this product?')) return;

    try {
      await productService.deleteProduct(productId);
      toast.success('Product deleted successfully');
      applyFilters(metadata.currentPage);
    } catch {
          toast.error('Failed to delete product');
        }
  }, [metadata.currentPage, applyFilters]);

  const handleExport = useCallback(async (fileType: 'csv' | 'excel' | 'pdf') => {
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
        fileType: FileType[fileType.toUpperCase() as keyof typeof FileType]
      };
      await productService.exportProduct(exportFilters);
      toast.success(`Products exported successfully as ${fileType.toUpperCase()}`);
    } catch (error) {
      console.error('Export failed:', error);
      toast.error('Failed to export products');
    } finally {
      setIsExporting(false);
    }
  }, [isExporting, filters]);

  const handleImportClick = useCallback(() => {
    setShowImportModal(true);
  }, []);

  const handleImportSuccess = useCallback(() => {
    setShowImportModal(false);
    applyFilters(1);
  }, [applyFilters]);

  const handleSubmit = useCallback((e: React.FormEvent) => {
    e.preventDefault();
    applyFilters(1);
  }, [applyFilters]);

  // Load initial data
  useEffect(() => {
    const loadInitialData = async () => {
      try {
        await Promise.all([
          loadBrands(),
          loadCategories(),
          applyFilters(1),
        ]);
      } finally {
        setIsLoadingProducts(false);
      }
    };

    if (profile && profile.role === 'ADMIN') {
      loadInitialData();
    }
  }, [profile, loadBrands, loadCategories, applyFilters]);

  if (!profile || profile.role !== 'ADMIN') {
    return null;
  }

  return (
    <div className={`container-fluid px-5 py-4 ${className}`}>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h3 className="mb-1">Product Management</h3>
          <p className="text-muted mb-0">Manage your product catalog</p>
        </div>
        <div className="d-flex gap-2">
          <button 
            className="btn btn-outline-primary" 
            onClick={handleImportClick} 
            disabled={isImporting}
          >
            <i className="bi bi-upload me-1"></i>
            {isImporting ? (
              <>
                <span className="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                Importing...
              </>
            ) : (
              'Import'
            )}
          </button>
          <ExportDropdown isExporting={isExporting} onExport={handleExport} />
          <button 
            className="btn btn-success" 
            onClick={() => router.push('/admin/product/add')}
          >
            <i className="bi bi-plus-lg"></i> Add New Product
          </button>
        </div>
      </div>

      {/* Filters Card */}
      <div className="card mb-4 shadow-sm">
        <div className="card-header bg-light py-3">
          <h5 className="mb-0">Search Filters</h5>
        </div>
        <div className="card-body p-4">
          <form onSubmit={handleSubmit}>
            <div className="row g-4">
              <div className="col-md-6">
                <label htmlFor="productId" className="form-label">Product ID</label>
                <input 
                  id="productId" 
                  value={filters.id || ''} 
                  onChange={(e) => setFilters(prev => ({ ...prev, id: e.target.value ? Number(e.target.value) : undefined }))}
                  type="text" 
                  placeholder="Enter product ID..." 
                  className="form-control" 
                />
              </div>

              <div className="col-md-6">
                <label htmlFor="keyword" className="form-label">Search</label>
                <input 
                  id="keyword" 
                  value={filters.keyword || ''} 
                  onChange={(e) => setFilters(prev => ({ ...prev, keyword: e.target.value || undefined }))}
                  type="text" 
                  placeholder="Search by product name or description..." 
                  className="form-control" 
                />
              </div>

              <div className="col-md-6">
                <label className="form-label">Price Range</label>
                <div className="input-group">
                  <input 
                    value={filters.minPrice || ''} 
                    onChange={(e) => setFilters(prev => ({ ...prev, minPrice: e.target.value ? Number(e.target.value) : undefined }))}
                    type="number" 
                    step="0.01" 
                    placeholder="Min price" 
                    className="form-control" 
                  />
                  <span className="input-group-text bg-light">to</span>
                  <input 
                    value={filters.maxPrice || ''} 
                    onChange={(e) => setFilters(prev => ({ ...prev, maxPrice: e.target.value ? Number(e.target.value) : undefined }))}
                    type="number" 
                    step="0.01" 
                    placeholder="Max price" 
                    className="form-control" 
                  />
                </div>
              </div>

              <div className="col-md-6">
                <label htmlFor="status" className="form-label">Status</label>
                <select 
                  id="status" 
                  value={filters.status} 
                  onChange={(e) => setFilters(prev => ({ ...prev, status: e.target.value as ProductStatus | '' }))}
                  className="form-select"
                >
                  <option value="">All statuses</option>
                  {productStatuses.map(status => (
                    <option key={status} value={status}>
                      {formatProductStatusLabel(status)}
                    </option>
                  ))}
                </select>
              </div>

              <div className="col-md-6">
                <label htmlFor="availableFrom" className="form-label">Available From</label>
                <input 
                  id="availableFrom" 
                  value={filters.availableFrom || ''} 
                  onChange={(e) => setFilters(prev => ({ ...prev, availableFrom: e.target.value || undefined }))}
                  type="date" 
                  className="form-control" 
                />
              </div>

              <div className="col-md-6">
                <label className="form-label">Activity</label>
                <div className="border rounded p-3">
                  <div className="d-flex gap-4">
                    <div className="form-check">
                      <input 
                        type="checkbox" 
                        className="form-check-input" 
                        id="activeCheck" 
                        checked={activeStates.active}
                        onChange={(e) => setActiveStates(prev => ({ ...prev, active: e.target.checked }))}
                      />
                      <label className="form-check-label" htmlFor="activeCheck">Active</label>
                    </div>
                    <div className="form-check">
                      <input 
                        type="checkbox" 
                        className="form-check-input" 
                        id="inactiveCheck" 
                        checked={activeStates.inactive}
                        onChange={(e) => setActiveStates(prev => ({ ...prev, inactive: e.target.checked }))}
                      />
                      <label className="form-check-label" htmlFor="inactiveCheck">Inactive</label>
                    </div>
                  </div>
                </div>
              </div>

              <div className="col-md-6">
                <label htmlFor="brandId" className="form-label">Brand</label>
                {isLoadingBrands ? (
                  <div className="text-center py-2" aria-live="polite">
                    <div className="spinner-border spinner-border-sm" role="status" aria-label="Loading brands">
                      <span className="visually-hidden">Loading...</span>
                    </div>
                  </div>
                ) : (
                  <select 
                    id="brandId" 
                    value={filters.brandId || ''} 
                    onChange={(e) => setFilters(prev => ({ ...prev, brandId: e.target.value || null }))}
                    className="form-select" 
                    disabled={!brands.length}
                  >
                    <option value="">All brands</option>
                    {brands.map(brand => (
                      <option key={brand.id} value={brand.id}>
                        {brand.name}
                      </option>
                    ))}
                  </select>
                )}
              </div>

              <div className="col-md-6">
                <label htmlFor="categoryIds" className="form-label">Categories</label>
                {isLoadingCategories ? (
                  <div className="text-center py-2" aria-live="polite">
                    <div className="spinner-border spinner-border-sm" role="status" aria-label="Loading categories">
                      <span className="visually-hidden">Loading...</span>
                    </div>
                  </div>
                ) : (
                  <>
                    <select 
                      id="categoryIds" 
                      value={filters.categoryIds?.map(String) || []} 
                      onChange={(e) => {
                        const selectedOptions = Array.from(e.target.selectedOptions, option => Number(option.value));
                        setFilters(prev => ({ ...prev, categoryIds: selectedOptions }));
                      }}
                      multiple 
                      className="form-select" 
                      disabled={!categories.length} 
                      size={3}
                    >
                      {categories.map(category => (
                        <option key={category.id} value={category.id}>
                          {category.name}
                        </option>
                      ))}
                    </select>
                    <small className="text-muted">Hold Ctrl/Cmd to select multiple</small>
                  </>
                )}
              </div>
            </div>

            <div className="d-flex gap-2 mt-4 pt-3 border-top">
              <button type="submit" className="btn btn-primary">
                <i className="bi bi-search"></i> Search
              </button>
              <button type="button" onClick={resetFilters} className="btn btn-outline-secondary">
                <i className="bi bi-arrow-counterclockwise"></i> Reset
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* Products Table */}
      <div className="card shadow-sm">
        <div className="card-header bg-light py-3">
          <h5 className="mb-0">Product List</h5>
        </div>
        {isLoadingProducts ? (
          <div className="text-center py-5" aria-live="polite">
            <div className="spinner-border text-primary" role="status" aria-label="Loading products">
              <span className="visually-hidden">Loading...</span>
            </div>
          </div>
        ) : (
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-hover table-bordered mb-0">
                <thead className="table-light">
                  <tr>
                    <th scope="col" className="px-4">ID</th>
                    <th scope="col" className="px-4">Name</th>
                    <th scope="col" className="px-4">Image</th>
                    <th scope="col" className="px-4">Price</th>
                    <th scope="col" className="px-4">Quantity</th>
                    <th scope="col" className="px-4">Status</th>
                    <th scope="col" className="px-4">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {products.map(product => (
                    <tr key={product.id}>
                      <td className="px-4">{product.id}</td>
                      <td className="px-4">{product.name}</td>
                      <td className="px-4">
                        <Image 
                          src={getProductImageUrl(product.image)} 
                          alt={product.name} 
                          width={48}
                          height={48}
                          className="rounded object-cover" 
                        />
                      </td>
                      <td className="px-4">${formatCurrency(product.price)}</td>
                      <td className="px-4">{product.quantity}</td>
                      <td className="px-4">
                        <span className={getProductStatusBadgeClasses(product.status)}>
                          {formatProductStatusLabel(product.status)}
                        </span>
                      </td>
                      <td className="px-4">
                        <div className="d-flex gap-2">
                          <button 
                            className="btn btn-sm btn-outline-secondary"
                            onClick={() => router.push(`/admin/product/${product.id}`)}
                          >
                            <i className="bi bi-eye me-1"></i> View
                          </button>
                          <button 
                            className="btn btn-sm btn-outline-primary"
                            onClick={() => router.push(`/admin/product/${product.id}/edit`)}
                          >
                            <i className="bi bi-pencil me-1"></i> Edit
                          </button>
                          <button 
                            className="btn btn-sm btn-outline-danger" 
                            onClick={() => handleDelete(product.id)}
                          >
                            <i className="bi bi-trash me-1"></i> Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            <div className="card-footer bg-light py-3">
              <div className="d-flex justify-content-between align-items-center">
                <span className="text-muted">
                  Page {metadata.currentPage} of {metadata.totalPages}
                  <span className="ms-2">({metadata.totalRecords} records)</span>
                </span>
                <div className="btn-group">
                  <button 
                    onClick={() => changePage(metadata.currentPage - 1)} 
                    disabled={metadata.currentPage <= 1}
                    className="btn btn-outline-secondary px-3"
                  >
                    <i className="bi bi-chevron-left me-1"></i> Previous
                  </button>
                  <button 
                    onClick={() => changePage(metadata.currentPage + 1)}
                    disabled={metadata.currentPage >= metadata.totalPages} 
                    className="btn btn-outline-secondary px-3"
                  >
                    Next <i className="bi bi-chevron-right ms-1"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

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
