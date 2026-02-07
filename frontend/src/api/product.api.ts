import {
  type Brand,
  type Category,
  type Product,
  type SearchProductFilters,
} from "@/interfaces/product.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class ProductApi extends BaseApi {
  constructor() {
    super("/services/product/api");
  }

  async listBrand(): Promise<ApiResponse<Brand[]>> {
    return this.get<Brand[]>("/brands");
  }

  async listCategory(): Promise<ApiResponse<Category[]>> {
    return this.get<Category[]>("/categories");
  }

  async searchProduct(
    filters: Partial<SearchProductFilters> = {}
  ): Promise<ApiResponse<Product[]>> {
    const payload: SearchProductFilters = {
      page: filters.page || 1,
      size: filters.size || 20,
      keyword: filters.keyword || undefined,
      minPrice: filters.minPrice,
      maxPrice: filters.maxPrice,
      brandId: filters.brandId || undefined,
      categoryIds: filters.categoryIds?.length ? filters.categoryIds : undefined,
    };
    return this.post<Product[], SearchProductFilters>("/products", payload);
  }

  async getProduct(productId: number): Promise<ApiResponse<Product>> {
    return this.get<Product>(`/products/${productId}`);
  }
}

export const productApi = new ProductApi();
