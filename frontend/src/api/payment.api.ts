import { BaseApi } from './base.api';
import { ApiResponse } from './apiClient';
import { PaymentGatewayResponse, PaymentNextActionResponse } from '@/interfaces/payment.interface';

class PaymentApi extends BaseApi {
  constructor() {
    super('/services/payment/api');
  }

  async retrievePaymentGatewayList(): Promise<ApiResponse<PaymentGatewayResponse[]>> {
    return this.get<PaymentGatewayResponse[]>('/payment-gateways');
  }

  async retrievePaymentNextAction(orderId: string): Promise<ApiResponse<PaymentNextActionResponse>> {
    return this.get<PaymentNextActionResponse>(`/payments/${orderId}/next-action`);
  }
}

export const paymentApi = new PaymentApi();
