import type { ApiResponse } from '@/interfaces/api.interface'
import type {
  Brand,
  Category,
  CreateProductRequest,
  ListProductFilters,
  Product,
  SearchProductFilters,
  UpdateProductRequest
} from '@/interfaces/product.interface'
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
}

export const productService = new ProductService() 