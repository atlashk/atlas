"use client";

import React from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { ArrowLeft, CreditCard, Smartphone, QrCode, AlertCircle } from "lucide-react";
import { PaymentMethod } from "@/interfaces/payment.interface";
import { PAYMENT_METHOD_LABELS } from "@/constants/checkout.constants";
import { LoadingState, InlineLoader } from "@/components/common/LoadingState";
import { ErrorState } from "@/hooks/useErrorHandler";
import { formatCurrency } from "@/utils/formatter.util";
import { useRouter } from "next/navigation";

interface CheckoutFormProps {
  cart: { items: Array<{ product: { id: string; name: string; price: number }; quantity: number }> };
  cartTotal: number;
  paymentMethods: string[];
  selectedPaymentMethod: PaymentMethod | null;
  isCheckingOut: boolean;
  isLoadingPaymentMethods: boolean;
  paymentMethodsError: ErrorState | null;
  onPaymentMethodChange: (method: PaymentMethod) => void;
  onCheckout: () => void;
  onRetryPaymentMethods: () => void;
}

const getPaymentIcon = (method: string) => {
  switch (method) {
    case "CARD":
      return <CreditCard className="h-4 w-4" />;
    case "PAYPAL":
      return <CreditCard className="h-4 w-4" />;
    case "E_WALLET":
      return <Smartphone className="h-4 w-4" />;
    case "QR_CODE":
      return <QrCode className="h-4 w-4" />;
    default:
      return <CreditCard className="h-4 w-4" />;
  }
};

export const CheckoutForm = React.memo<CheckoutFormProps>(function CheckoutForm({
  cart,
  cartTotal,
  paymentMethods,
  selectedPaymentMethod,
  isCheckingOut,
  isLoadingPaymentMethods,
  paymentMethodsError,
  onPaymentMethodChange,
  onCheckout,
  onRetryPaymentMethods,
}) {
  const router = useRouter();

  const handleBackToCart = React.useCallback(() => {
    router.push("/cart");
  }, [router]);

  const handlePaymentMethodChange = React.useCallback((value: string) => {
    if (paymentMethods.includes(value)) {
      onPaymentMethodChange(value as PaymentMethod);
    }
  }, [paymentMethods, onPaymentMethodChange]);

  return (
    <div className="container mx-auto px-4 py-8 max-w-2xl">
      <div className="space-y-4">
        {/* Header */}
        <div className="flex items-center gap-3 mb-8">
          <div className="p-2 bg-primary/10 rounded-lg">
            <CreditCard className="h-6 w-6 text-primary" />
          </div>
          <h1 className="text-2xl font-bold">Checkout</h1>
        </div>

        {/* Cart Summary */}
        <Card>
          <CardHeader>
            <CardTitle>Order Summary</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {cart?.items?.map((item: { product: { id: string; name: string; price: number }; quantity: number }) => (
                <div key={item.product.id} className="flex justify-between">
                  <span>
                    {item.product.name} x {item.quantity}
                  </span>
                  <span>
                    {formatCurrency(item.product.price * item.quantity)}
                  </span>
                </div>
              ))}
              <div className="border-t pt-2 font-semibold flex justify-between">
                <span>Total:</span>
                <span>{formatCurrency(cartTotal)}</span>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Payment Method Selection */}
        <Card>
          <CardHeader>
            <CardTitle>Payment Method</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoadingPaymentMethods ? (
              <LoadingState 
                message="Loading payment methods..." 
                size="sm" 
                className="py-4"
              />
            ) : paymentMethodsError ? (
              <div className="space-y-3">
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>
                    {paymentMethodsError.message}
                  </AlertDescription>
                </Alert>
                <Button 
                  variant="outline" 
                  onClick={onRetryPaymentMethods}
                  className="w-full"
                >
                  Try Again
                </Button>
              </div>
            ) : paymentMethods.length === 0 ? (
              <Alert>
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>
                  No payment methods available. Please contact support.
                </AlertDescription>
              </Alert>
            ) : (
              <RadioGroup
                value={selectedPaymentMethod || ""}
                onValueChange={handlePaymentMethodChange}
                className="space-y-3"
              >
                {paymentMethods.map((method) => (
                  <div key={method} className="flex items-center space-x-2">
                    <RadioGroupItem value={method} id={method} />
                    <Label
                      htmlFor={method}
                      className="flex items-center space-x-2 cursor-pointer flex-1"
                    >
                      {getPaymentIcon(method)}
                      <span>{PAYMENT_METHOD_LABELS[method as PaymentMethod] || method}</span>
                    </Label>
                  </div>
                ))}
              </RadioGroup>
            )}
          </CardContent>
        </Card>

        <Separator />

        {/* Action Buttons */}
        <div className="flex gap-3">
          <Button
            variant="outline"
            onClick={handleBackToCart}
            className="flex-1"
            disabled={isCheckingOut}
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Cart
          </Button>
          <Button
            onClick={onCheckout}
            disabled={!selectedPaymentMethod || isCheckingOut || isLoadingPaymentMethods}
            className="flex-1"
          >
            {isCheckingOut && <InlineLoader className="mr-2" />}
            {isCheckingOut ? "Processing..." : `Pay ${formatCurrency(cartTotal)}`}
          </Button>
        </div>
      </div>
    </div>
  );
});

CheckoutForm.displayName = "CheckoutForm";
