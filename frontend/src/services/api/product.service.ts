import type { ApiResponse } from '@/interfaces/api.interface'
import type {
  Brand,
  Category,
  CreateProductRequest,
  ExportProductFilters,
  FileType,
  ListProductFilters,
  Product,
  SearchProductFilters,
  UpdateProductRequest
} from '@/interfaces/product.interface'
import apiClient from './apiClient'
import { BaseService } from './base.service'

export class ProductService extends BaseService {
  constructor() {
    super('/api')
  }

  // Common operations
  async listBrand(): Promise<ApiResponse<Brand[]>> {
    return this.get<Brand[]>('/common/products/brands')
  }

  async listCategory(): Promise<ApiResponse<Category[]>> {
    return this.get<Category[]>('/common/products/categories');
  }

  // Front operations
  async searchProduct(filters: Partial<SearchProductFilters> = {}): Promise<ApiResponse<Product[]>> {
    const queryParams = new URLSearchParams()
    if (filters.keyword) queryParams.append('keyword', filters.keyword)
    if (filters.minPrice) queryParams.append('min_price', filters.minPrice.toString())
    if (filters.maxPrice) queryParams.append('max_price', filters.maxPrice.toString())
    if (filters.brandId) queryParams.append('brand_id', filters.brandId.toString())
    if (filters.categoryIds?.length) queryParams.append('category_ids', filters.categoryIds.join(','))
    queryParams.append('page', (filters.page || 1).toString())
    queryParams.append('size', (filters.size || 20).toString())

    return this.get<Product[]>(`/front/products?${queryParams.toString()}`)
  }

  async getProduct(productId: number): Promise<ApiResponse<Product>> {
    return this.get<Product>(`/front/products/${productId}`)
  }

  // Admin operations
  async listProduct(filters: ListProductFilters): Promise<ApiResponse<Product[]>> {
    const queryParams = new URLSearchParams()
    if (filters.id) queryParams.append('id', filters.id.toString())
    if (filters.keyword) queryParams.append('keyword', filters.keyword)
    if (filters.minPrice) queryParams.append('min_price', filters.minPrice.toString())
    if (filters.maxPrice) queryParams.append('max_price', filters.maxPrice.toString())
    if (filters.status) queryParams.append('status', filters.status)
    if (filters.availableFrom) queryParams.append('available_from', filters.availableFrom)
    if (filters.isActive != null) queryParams.append('is_active', filters.isActive.toString())
    if (filters.brandId) queryParams.append('brand_id', filters.brandId.toString())
    if (filters.categoryIds?.length) queryParams.append('category_ids', filters.categoryIds.join(','))
    queryParams.append('page', filters.page.toString())
    queryParams.append('size', filters.size.toString())

    return this.get<Product[]>(`/admin/products?${queryParams.toString()}`)
  }

  async getProductAdmin(productId: number): Promise<ApiResponse<Product>> {
    return this.get<Product>(`/admin/products/${productId}`)
  }

  async createProduct(data: CreateProductRequest): Promise<ApiResponse<Product>> {
    return this.post<Product>('/admin/products', data)
  }

  async updateProduct(data: UpdateProductRequest): Promise<ApiResponse<Product>> {
    return this.put<Product>(`/admin/products/${data.id}`, data)
  }

  async deleteProduct(productId: number): Promise<ApiResponse<void>> {
    return this.delete<void>(`/admin/products/${productId}`)
  }

  async importProduct(file: File, fileType: FileType): Promise<ApiResponse<void>> {
    console.log('=== DETAILED FILE DEBUG ===');
    console.log('File object:', file);
    console.log('File name:', file.name);
    console.log('File size:', file.size);
    console.log('File type:', file.type);
    console.log('File lastModified:', file.lastModified);
    
    // Check if file is actually readable
    try {
      const text = await file.text();
      console.log('File content preview (first 200 chars):', text.substring(0, 200));
      console.log('File content length:', text.length);
    } catch (error) {
      console.error('Error reading file content:', error);
    }

    const formData = new FormData()
    formData.append('file', file)
    formData.append('fileType', fileType)

    console.log('FormData entries:');
    for (const pair of formData.entries()) {
      console.log(`- ${pair[0]}:`, pair[1]);
      if (pair[1] instanceof File) {
        console.log(`  File details - name: ${pair[1].name}, size: ${pair[1].size}`);
      }
    }
    console.log('=== END DEBUG ===');

    try {
      const response = await apiClient.post(`${this.baseUrl}/admin/products/import`, formData)
      return response.data
    } catch (error) {
      console.error('Import error:', error)
      throw error
    }
  }

  async exportProduct(filters: ExportProductFilters): Promise<void> {
    const queryParams = new URLSearchParams()
    if (filters.id) queryParams.append('id', filters.id.toString())
    if (filters.keyword) queryParams.append('keyword', filters.keyword)
    if (filters.minPrice) queryParams.append('min_price', filters.minPrice.toString())
    if (filters.maxPrice) queryParams.append('max_price', filters.maxPrice.toString())
    if (filters.status) queryParams.append('status', filters.status)
    if (filters.availableFrom) queryParams.append('available_from', filters.availableFrom)
    if (filters.isActive != null) queryParams.append('is_active', filters.isActive.toString())
    if (filters.brandId) queryParams.append('brand_id', filters.brandId.toString())
    if (filters.categoryIds?.length) queryParams.append('category_ids', filters.categoryIds.join(','))
    queryParams.append('file_type', filters.fileType)

    const response = await apiClient.get(`${this.baseUrl}/admin/products/export?${queryParams.toString()}`, {
      responseType: 'blob'
    })

    // Create blob and download
    const blob = new Blob([response.data], {
      type: response.headers['content-type'] || 'application/octet-stream'
    })

    // Extract filename from Content-Disposition header or create default
    const contentDisposition = response.headers['content-disposition']
    let filename = `export-product-${new Date().toISOString().slice(0, 19).replace(/[:-]/g, '')}.${filters.fileType}`

    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      if (filenameMatch && filenameMatch[1]) {
        filename = filenameMatch[1].replace(/['"]/g, '')
      }
    }

    // Create download link
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()

    // Cleanup
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }
}

export const productService = new ProductService()
