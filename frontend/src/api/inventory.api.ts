import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export interface RetrieveStockResponse {
  productId: string;
  availableQuantity: number;
  reservedQuantity: number;
}

export interface UpdateAvailableQuantityRequest {
  availableQuantity: number;
}

export class InventoryApi extends BaseApi {
  constructor() {
    super("/services/inventory/api");
  }

  async retrieveStock(
    productId: string,
  ): Promise<ApiResponse<RetrieveStockResponse>> {
    return this.get<RetrieveStockResponse>(`/stocks/admin/${productId}`);
  }

  async updateAvailableQuantity(
    productId: string,
    request: UpdateAvailableQuantityRequest,
  ): Promise<ApiResponse<void>> {
    return this.put<void, UpdateAvailableQuantityRequest>(
      `/stocks/admin/${productId}/available-quantity`,
      request,
    );
  }
}

export const inventoryApi = new InventoryApi();
