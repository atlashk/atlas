import { BaseApi } from './baseApi';
import { ApiResponse } from './apiClient';

export interface PaymentMethodResponse {
  id: string;
  name: string;
  type: string;
  enabled: boolean;
  description?: string;
  icon?: string;
}

class PaymentApi extends BaseApi {
  constructor() {
    super('/api/payment-service');
  }

  async getPaymentMethods(): Promise<ApiResponse<PaymentMethodResponse[]>> {
    return this.get<PaymentMethodResponse[]>('/payment-methods');
  }
}

export const paymentApi = new PaymentApi();