"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { PaymentGatewayResponse, PaymentNextAction } from "@/interfaces/payment.interface";
import { StripePaymentForm } from "../StripePaymentForm";
import { PaymentSimulatorForm } from "../PaymentSimulatorForm";

// Supported payment gateway codes
const SUPPORTED_PAYMENT_GATEWAYS = ["STRIPE", "SIMULATOR"] as const;
type PaymentGatewayCode = typeof SUPPORTED_PAYMENT_GATEWAYS[number];

interface PaymentElementActionProps {
  nextAction: PaymentNextAction;
  orderId: string;
  selectedPaymentGateway?: PaymentGatewayResponse | null;
  amount?: number | null;
  currency?: string | null;
  onPaymentComplete?: () => void;
  onPaymentError?: (error: string) => void;
}

export function PaymentElementAction({
  nextAction,
  orderId,
  selectedPaymentGateway,
  amount,
  currency,
  onPaymentComplete,
  onPaymentError,
}: PaymentElementActionProps) {
  // Check if gateway is supported
  const gatewayCode = selectedPaymentGateway?.code?.toUpperCase() as PaymentGatewayCode;
  const isSupported = gatewayCode && SUPPORTED_PAYMENT_GATEWAYS.includes(gatewayCode);
  
  // Different gateways have different requirements
  const requiresPublishableKey = gatewayCode === "STRIPE";
  const hasRequiredConfig = requiresPublishableKey ? !!nextAction.publishableKey : true;
  
  if (!isSupported || !hasRequiredConfig) {
    const errorMessage = !isSupported 
      ? `The payment gateway "${selectedPaymentGateway?.name || 'Unknown'}" is not supported yet.`
      : "Missing required payment configuration.";

    return (
      <Card>
        <CardContent className="p-2">
          <div className="text-center space-y-4">
            <h3 className="text-lg font-semibold text-red-600">
              Payment Configuration Error
            </h3>
            <p className="text-gray-600">
              {errorMessage}
            </p>
            <p className="text-sm text-gray-500">Order ID: {orderId}</p>
            <Button
              onClick={() =>
                onPaymentError?.(errorMessage)
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

  // Render the appropriate payment form based on gateway
  const renderPaymentForm = () => {
    switch (gatewayCode) {
      case "STRIPE":
        return (
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
        );
      
      case "SIMULATOR":
        return (
          <PaymentSimulatorForm
            amount={amount || 0}
            currency={currency || "USD"}
            onSubmit={(data) => {
              console.log("Simulator payment data:", data);
              onPaymentComplete?.();
            }}
            onCancel={() => {
              onPaymentError?.("Payment cancelled by user");
            }}
          />
        );
      
      default:
        return (
          <Card>
            <CardContent className="p-4">
              <p className="text-center text-gray-600">
                Payment gateway not implemented yet.
              </p>
            </CardContent>
          </Card>
        );
    }
  };

  return (
    <div className="space-y-4">
      <div className="text-center">
        <h3 className="text-lg font-semibold">Complete Your Payment</h3>
        <p className="text-sm text-gray-500 mt-2">
          Order ID: {orderId} | Gateway: {selectedPaymentGateway?.name || gatewayCode}
        </p>
      </div>

      {renderPaymentForm()}
    </div>
  );
}
