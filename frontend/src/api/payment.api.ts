import { BaseApi } from './base.api';
import { ApiResponse } from './apiClient';
import { PaymentMethodResponse, PaymentNextActionResponse } from '@/interfaces/payment.interface';

class PaymentApi extends BaseApi {
  constructor() {
    super('/api/payment-svc');
  }

  async getPaymentMethods(): Promise<ApiResponse<PaymentMethodResponse>> {
    return this.get<PaymentMethodResponse>('/payment-methods');
  }

  async getPaymentNextAction(orderId: string): Promise<ApiResponse<PaymentNextActionResponse>> {
    return this.get<PaymentNextActionResponse>(`/payments/${orderId}/next-action`);
  }
}

export const paymentApi = new PaymentApi();
