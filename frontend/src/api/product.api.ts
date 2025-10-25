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
    const queryParams = new URLSearchParams();
    if (filters.keyword) queryParams.append("keyword", filters.keyword);
    if (filters.minPrice)
      queryParams.append("minPrice", filters.minPrice.toString());
    if (filters.maxPrice)
      queryParams.append("maxPrice", filters.maxPrice.toString());
    if (filters.brandId)
      queryParams.append("brandId", filters.brandId.toString());
    if (filters.categoryIds?.length)
      queryParams.append("categoryIds", filters.categoryIds.join(","));
    queryParams.append("page", (filters.page || 1).toString());
    queryParams.append("size", (filters.size || 20).toString());
    return this.get<Product[]>(`/products?${queryParams.toString()}`);
  }

  async getProduct(productId: number): Promise<ApiResponse<Product>> {
    return this.get<Product>(`/products/${productId}`);
  }
}

export const productApi = new ProductApi();
