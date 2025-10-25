"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { NextActionHandler } from "@/components/payment/NextActionHandler";
import React, { useCallback, useEffect } from "react";
import { PaymentNextAction } from "@/interfaces/payment.interface";
import { OrderStatusResponse } from "@/interfaces/order.interface";
import { CreditCard, XCircle, CheckCircle, Loader2 } from "lucide-react";
import { toast } from "sonner";
import {
  ORDER_STATUS_DESCRIPTIONS,
  ORDER_STATUS_MESSAGES,
} from "@/constants/checkout.constants";
import { useRouter } from "next/navigation";
import { useCartStore } from "@/stores/cart.store";

interface CheckoutProgressProps {
  orderId: string;
  orderStatus: OrderStatusResponse | null;
  paymentNextAction: PaymentNextAction | null;
  paymentAmount?: number | null;
  paymentCurrency?: string | null;
  isProcessingPayment: boolean;
  onPaymentComplete: () => void;
  onPaymentError: (error: string) => void;
  isLoading: boolean;
  error: string | null;
  onRetry: () => void;
  startPolling: () => void;
  stopPolling: () => void;
}

export const CheckoutProgress = React.memo(function CheckoutProgress({
  orderId,
  orderStatus,
  isLoading,
  error,
  paymentNextAction,
  paymentAmount,
  paymentCurrency,
  onRetry,
  onPaymentComplete,
  onPaymentError,
  startPolling,
  stopPolling,
}: CheckoutProgressProps) {

  const router = useRouter();
  const { clearCartState } = useCartStore();

  // Clear cart and stop polling when order is fulfilled or canceled
  useEffect(() => {
    if (orderStatus?.status === "FULFILLED") {
      // Stop polling immediately
      stopPolling();
      
      // Clear cart state immediately (no API call needed)
      clearCartState();
    } else if (orderStatus?.status === "CANCELED") {
      // Stop polling for canceled orders
      stopPolling();
    }
  }, [orderStatus?.status, clearCartState, stopPolling, orderId]);

  const handlePaymentComplete = () => {
    toast.success("Payment completed successfully!");
    onPaymentComplete();
    // Only restart polling if order is not in final state
    if (orderStatus?.status !== "FULFILLED" && orderStatus?.status !== "CANCELED") {
      startPolling();
    }
  };

  const handlePaymentError = (errorMessage: string) => {
    toast.error(`Payment failed: ${errorMessage}`);
    onPaymentError(errorMessage);
    // Only restart polling if order is not in final state
    if (orderStatus?.status !== "FULFILLED" && orderStatus?.status !== "CANCELED") {
      startPolling();
    }
  };

  const handleViewOrders = useCallback(() => {
    router.push("/order-history");
  }, [router]);

  const handleBackToCart = useCallback(() => {
    router.push("/cart");
  }, [router]);

  return (
    <div className="container mx-auto px-4 py-8 max-w-2xl">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <CreditCard className="w-5 h-5" />
            Checkout In Progress
          </CardTitle>
        </CardHeader>
        <CardContent>
          {error ? (
            <div className="text-center space-y-4">
              <XCircle className="w-12 h-12 text-red-500 mx-auto" />
              <h3 className="text-lg font-semibold text-red-600">
                An Error Occurred
              </h3>
              <p className="text-gray-600">{error}</p>
              <Button variant="outline" onClick={onRetry}>
                Try Again
              </Button>
            </div>
          ) : (
            <>
              {/* Prioritize final order states over payment actions */}
              {orderStatus?.status === "FULFILLED" || orderStatus?.status === "CANCELED" ? (
                // Order Status Logic (merged from OrderStatus component)
                <>
                  {isLoading || !orderStatus ? (
                    <div className="text-center space-y-4">
                      <Loader2 className="w-16 h-16 text-blue-500 mx-auto animate-spin" />
                      <h2 className="text-xl font-semibold">
                        Processing your order...
                      </h2>
                      <p className="text-gray-600">
                        Please wait while we process your payment.
                      </p>
                      <p className="text-sm text-gray-500">
                        Order ID: {orderId}
                      </p>
                    </div>
                  ) : orderStatus.status === "FULFILLED" ? (
                    <div className="text-center space-y-4">
                      <CheckCircle className="w-16 h-16 text-green-500 mx-auto" />
                      <h2 className="text-2xl font-bold text-green-600">
                        {ORDER_STATUS_MESSAGES.FULFILLED}
                      </h2>
                      <p className="text-gray-600">
                        {ORDER_STATUS_DESCRIPTIONS.FULFILLED}
                      </p>
                      <p className="text-sm text-gray-500">
                        Order ID: {orderId}
                      </p>
                      <Button
                        onClick={handleViewOrders}
                        className="w-full"
                        size="lg"
                      >
                        View Your Orders
                      </Button>
                    </div>
                  ) : orderStatus.status === "CANCELED" ? (
                    <div className="text-center space-y-4">
                      <XCircle className="w-16 h-16 text-red-500 mx-auto" />
                      <h3 className="text-xl font-bold text-red-600">
                        {ORDER_STATUS_MESSAGES.CANCELED}
                      </h3>
                      <div className="bg-red-50 border border-red-200 rounded-lg p-4 mx-4">
                        <p className="text-sm text-red-800">
                          <span className="font-semibold">
                            Cancellation Reason:
                          </span>{" "}
                          {orderStatus.cancellationReason}
                        </p>
                      </div>
                      <div className="space-y-2">
                        {onRetry && (
                          <Button
                            onClick={onRetry}
                            className="w-full"
                            size="lg"
                          >
                            Try Again
                          </Button>
                        )}
                        <Button
                          variant="outline"
                          onClick={handleBackToCart}
                          className="w-full"
                          size="lg"
                        >
                          Back to Cart
                        </Button>
                      </div>
                    </div>
                  ) : null}
                </>
              ) : (
                // Show payment actions or loading for non-final states
                <>
                  {paymentNextAction ? (
                    <NextActionHandler
                      nextAction={paymentNextAction}
                      orderId={orderId}
                      amount={paymentAmount}
                      currency={paymentCurrency}
                      onPaymentComplete={handlePaymentComplete}
                      onPaymentError={handlePaymentError}
                    />
                  ) : (
                    // Default loading state
                    <div className="text-center space-y-4">
                      <Loader2 className="w-16 h-16 text-blue-500 mx-auto animate-spin" />
                      <h2 className="text-xl font-semibold">
                        Processing your order...
                      </h2>
                      <p className="text-gray-600">
                        Please wait while we process your payment.
                      </p>
                      <p className="text-sm text-gray-500">
                        Order ID: {orderId}
                      </p>
                    </div>
                  )}
                </>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
});
