"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { UsePaymentElementProvider } from "@/constants";
import { PaymentNextAction } from "@/interfaces/payment.interface";
import { StripePaymentForm } from "../StripePaymentForm";

interface PaymentElementActionProps {
  nextAction: PaymentNextAction;
  orderId: string;
  amount?: number | null;
  currency?: string | null;
  onPaymentComplete?: () => void;
  onPaymentError?: (error: string) => void;
}

export function PaymentElementAction({
  nextAction,
  orderId,
  amount,
  currency,
  onPaymentComplete,
  onPaymentError,
}: PaymentElementActionProps) {
  // Check if provider is supported
  if (
    nextAction.provider !== UsePaymentElementProvider.STRIPE ||
    !nextAction.publishableKey
  ) {
    return (
      <Card>
        <CardContent className="p-2">
          <div className="text-center space-y-4">
            <h3 className="text-lg font-semibold text-red-600">
              Payment Configuration Error
            </h3>
            <p className="text-gray-600">
              {nextAction.provider !== UsePaymentElementProvider.STRIPE
                ? `The payment provider "${
                    nextAction.provider || "unknown"
                  }" is not supported yet.`
                : "Missing required payment configuration (publishable key)."}
            </p>
            <p className="text-sm text-gray-500">
              Currently supported providers: Stripe
            </p>
            <p className="text-sm text-gray-500">Order ID: {orderId}</p>
            <Button
              onClick={() =>
                onPaymentError?.(
                  `Unsupported payment provider: ${
                    nextAction.provider || "unknown"
                  }`
                )
              }
              variant="outline"
              className="w-full"
            >
              Go Back
            </Button>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      <div className="text-center">
        <h3 className="text-lg font-semibold">Complete Your Payment</h3>
        <p className="text-gray-600">
          Please enter your payment details below
        </p>
        <p className="text-sm text-gray-500 mt-2">Order ID: {orderId}</p>
      </div>

      <StripePaymentForm
        clientSecret={nextAction.clientSecret || ""}
        publishableKey={nextAction.publishableKey || ""}
        amount={amount || 0}
        currency={currency || "USD"}
        onSuccess={(paymentIntent) => {
          console.log("Payment successful:", paymentIntent);
          onPaymentComplete?.();
        }}
        onError={(error) => {
          console.error("Payment error:", error);
          onPaymentError?.(error);
        }}
      />
    </div>
  );
}
