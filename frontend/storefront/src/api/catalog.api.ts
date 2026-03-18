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
    filter: Partial<RetrieveProductListFilter> = {}
  ): Promise<ApiResponse<Product[]>> {
    const payload: RetrieveProductListFilter = {
      page: filter.page || 1,
      size: filter.size || 20,
      keyword: filter.keyword || undefined,
      minPrice: filter.minPrice,
      maxPrice: filter.maxPrice,
      brandId: filter.brandId || undefined,
      categoryIds: filter.categoryIds?.length ? filter.categoryIds : undefined,
      mode: filter.mode || "DATABASE",
    };
    return this.post<Product[], RetrieveProductListFilter>("/products/list", payload);
  }

  async retrieveProduct(productId: string): Promise<ApiResponse<Product>> {
    return this.get<Product>(`/products/${productId}`);
  }
}

export const catalogApi = new CatalogApi();
