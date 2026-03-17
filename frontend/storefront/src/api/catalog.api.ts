import {
  type Brand,
  type Category,
  type Product,
  type RetrieveProductListFilter
} from "@/interfaces/catalog.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class CatalogApi extends BaseApi {
  constructor() {
    super("/services/catalog/api/public");
  }

  async retrieveAllBrand(): Promise<ApiResponse<Brand[]>> {
    return this.get<Brand[]>("/brands");
  }

  async retrieveAllCategory(): Promise<ApiResponse<Category[]>> {
    return this.get<Category[]>("/categories");
  }

  async retrieveProductList(
    filters: Partial<RetrieveProductListFilter> = {}
  ): Promise<ApiResponse<Product[]>> {
    const payload: RetrieveProductListFilter = {
      page: filters.page || 1,
      size: filters.size || 20,
      keyword: filters.keyword || undefined,
      minPrice: filters.minPrice,
      maxPrice: filters.maxPrice,
      brandId: filters.brandId || undefined,
      categoryIds: filters.categoryIds?.length ? filters.categoryIds : undefined,
      mode: filters.mode || "DATABASE",
    };
    return this.post<Product[], RetrieveProductListFilter>("/products/list", payload);
  }

  async retrieveProduct(productId: string): Promise<ApiResponse<Product>> {
    return this.get<Product>(`/products/${productId}`);
  }
}

export const catalogApi = new CatalogApi();
