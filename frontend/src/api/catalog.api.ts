import {
  FileType,
  type Brand,
  type Category,
  type CreateProductRequest,
  type ExportProductFilter,
  type Product,
  type RetrieveProductListFilter,
  type UpdateProductRequest,
} from "@/interfaces/catalog.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class CatalogApi extends BaseApi {
  constructor() {
    super("/services/catalog/api");
  }

  async retrieveAllBrand(): Promise<ApiResponse<Brand[]>> {
    return this.get<Brand[]>("/public/brands");
  }

  async retrieveAllCategory(): Promise<ApiResponse<Category[]>> {
    return this.get<Category[]>("/public/categories");
  }

  async retrievePublicProductList(
    filter: Partial<RetrieveProductListFilter> = {},
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
    return this.post<Product[], RetrieveProductListFilter>(
      "/public/products/list",
      payload,
    );
  }

  async retrievePublicProduct(
    productId: string,
  ): Promise<ApiResponse<Product>> {
    return this.get<Product>(`/public/products/${productId}`);
  }

  async retrieveReferenceData(
    type: string,
  ): Promise<ApiResponse<Record<string, string>>> {
    return this.get<Record<string, string>>(
      `/public/reference-data?type=${type}`,
    );
  }

  async retrieveProductTypes(): Promise<ApiResponse<Record<string, string>>> {
    return this.retrieveReferenceData("PRODUCT_TYPE");
  }

  async retrieveAdminProductList(
    filter: RetrieveProductListFilter,
  ): Promise<ApiResponse<Product[]>> {
    return this.post<Product[], RetrieveProductListFilter>(
      "/products/admin/list",
      filter,
    );
  }

  async retrieveAdminProduct(productId: string): Promise<ApiResponse<Product>> {
    return this.get<Product>(`/products/admin/${productId}`);
  }

  async createProduct(
    data: CreateProductRequest,
    imageFile?: File,
  ): Promise<ApiResponse<string>> {
    const formData = new FormData();
    formData.append(
      "request",
      new Blob([JSON.stringify(data)], { type: "application/json" }),
    );
    if (imageFile) {
      formData.append("image", imageFile);
    }
    return this.post<string>("/products/admin", formData);
  }

  async updateProduct(
    data: UpdateProductRequest,
    imageFile?: File,
  ): Promise<ApiResponse<void>> {
    const formData = new FormData();
    formData.append(
      "request",
      new Blob([JSON.stringify(data)], { type: "application/json" }),
    );
    if (imageFile) {
      formData.append("image", imageFile);
    }
    return this.put<void>(`/products/admin/${data.id}`, formData);
  }

  async deleteProduct(id: string): Promise<ApiResponse<void>> {
    return this.delete<void>(`/products/admin/${id}`);
  }

  async importProduct(
    file: File,
    fileType: FileType,
  ): Promise<ApiResponse<void>> {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("file_type", fileType);

    return this.post<void>("/products/admin/import", formData);
  }

  async exportProduct(filter: ExportProductFilter): Promise<void> {
    const response = await this.postBlob("/products/admin/export", filter);

    const blob = new Blob([response.data], {
      type: response.headers["content-type"] || "application/octet-stream",
    });

    const contentDisposition = response.headers["content-disposition"];

    const extensionMap: Record<FileType, string> = {
      [FileType.CSV]: "csv",
      [FileType.EXCEL]: "xlsx",
      [FileType.PDF]: "pdf",
    };

    let filename: string;

    if (contentDisposition) {
      let filenameMatch = contentDisposition.match(/filename\*=([^;\n]+)/i);
      if (filenameMatch && filenameMatch[1]) {
        try {
          const encodedFilename = filenameMatch[1].replace(/^UTF-8''/, "");
          filename = decodeURIComponent(encodedFilename);
        } catch {
          filename = filenameMatch[1];
        }
      } else {
        filenameMatch = contentDisposition.match(/filename=([^;\n]+)/i);
        if (filenameMatch && filenameMatch[1]) {
          filename = filenameMatch[1].replace(/^["']|["']$/g, "");
        } else {
          filename = `export-product-${new Date()
            .toISOString()
            .slice(0, 19)
            .replace(/[:-]/g, "")}.${extensionMap[filter.fileType]}`;
        }
      }
    } else {
      filename = `export-product-${new Date()
        .toISOString()
        .slice(0, 19)
        .replace(/[:-]/g, "")}.${extensionMap[filter.fileType]}`;
    }

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();

    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  async retrieveTotalProductCount(): Promise<ApiResponse<number>> {
    return this.get<number>("/products/admin/statistics/count");
  }

  async retrieveProductList(
    filter: Partial<RetrieveProductListFilter> = {},
  ): Promise<ApiResponse<Product[]>> {
    return this.retrievePublicProductList(filter);
  }

  async retrieveProduct(productId: string): Promise<ApiResponse<Product>> {
    return this.retrievePublicProduct(productId);
  }
}

export const catalogApi = new CatalogApi();
