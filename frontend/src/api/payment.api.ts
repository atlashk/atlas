import { BaseApi } from './base.api';
import { ApiResponse } from './apiClient';
import { PaymentMethodResponse, PaymentTrackingResponse } from '@/interfaces/payment.interface';

class PaymentApi extends BaseApi {
  constructor() {
    super('/api/payment-svc');
  }

  async getPaymentMethods(): Promise<ApiResponse<PaymentMethodResponse>> {
    return this.get<PaymentMethodResponse>('/payment-methods');
  }

  async getPaymentTracking(sagaId: number): Promise<ApiResponse<PaymentTrackingResponse>> {
    return this.get<PaymentTrackingResponse>(`/payments/${sagaId}/tracking`);
  }
}

export const paymentApi = new PaymentApi();
