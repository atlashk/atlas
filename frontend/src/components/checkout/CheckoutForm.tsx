"use client";

import { InlineLoader, LoadingState } from "@/components/common/LoadingState";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { ErrorState } from "@/hooks/useErrorHandler";
import { PaymentGatewayResponse } from "@/interfaces/payment.interface";
import { formatCurrency } from "@/utils/formatter.util";
import { AlertCircle, ArrowLeft, CreditCard } from "lucide-react";
import { useRouter } from "next/navigation";
import React from "react";
import { Address, AddressForm } from "./AddressForm";

interface CheckoutFormProps {
  cart: {
    items: Array<{
      product: { id: string; name: string; price: number };
      quantity: number;
    }>;
  };
  cartTotal: number;
  paymentGateways: PaymentGatewayResponse[];
  selectedPaymentGateway: PaymentGatewayResponse | null;
  address: Address;
  addressErrors?: Partial<Record<keyof Address, string>>;
  isCheckingOut: boolean;
  isLoadingPaymentGateways: boolean;
  paymentGatewaysError: ErrorState | null;
  onPaymentGatewayChange: (gateway: PaymentGatewayResponse) => void;
  onAddressChange: (address: Address) => void;
  onCheckout: () => void;
}

export const CheckoutForm = React.memo<CheckoutFormProps>(
  function CheckoutForm({
    cart,
    cartTotal,
    paymentGateways,
    selectedPaymentGateway,
    address,
    addressErrors,
    isCheckingOut,
    isLoadingPaymentGateways,
    paymentGatewaysError,
    onPaymentGatewayChange,
    onAddressChange,
    onCheckout,
  }) {
    const router = useRouter();

    const handleBackToCart = React.useCallback(() => {
      router.push("/cart");
    }, [router]);

    const handlePaymentGatewayChange = React.useCallback(
      (value: string) => {
        const gateway = paymentGateways.find((g) => g.id.toString() === value);
        if (gateway) {
          onPaymentGatewayChange(gateway);
        }
      },
      [paymentGateways, onPaymentGatewayChange],
    );

    return (
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
              {cart?.items?.map(
                (item: {
                  product: { id: string; name: string; price: number };
                  quantity: number;
                }) => (
                  <div key={item.product.id} className="flex justify-between">
                    <span>
                      {item.product.name} x {item.quantity}
                    </span>
                    <span>
                      {formatCurrency(item.product.price * item.quantity)}
                    </span>
                  </div>
                ),
              )}
              <div className="border-t pt-2 font-semibold flex justify-between">
                <span>Total:</span>
                <span>{formatCurrency(cartTotal)}</span>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Address Form */}
        <AddressForm
          address={address}
          onAddressChange={onAddressChange}
          errors={addressErrors}
        />

        {/* Payment Gateway Selection */}
        <Card>
          <CardHeader>
            <CardTitle>Payment Gateway</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoadingPaymentGateways ? (
              <LoadingState
                message="Loading payment gateways..."
                size="sm"
                className="py-4"
              />
            ) : paymentGatewaysError ? (
              <Alert variant="destructive">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>
                  {paymentGatewaysError.message}
                </AlertDescription>
              </Alert>
            ) : paymentGateways.length === 0 ? (
              <Alert>
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>
                  No payment gateways available. Please contact support.
                </AlertDescription>
              </Alert>
            ) : (
              <RadioGroup
                value={selectedPaymentGateway?.id.toString() || ""}
                onValueChange={handlePaymentGatewayChange}
                className="space-y-3"
              >
                {paymentGateways.map((gateway) => (
                  <div key={gateway.id} className="flex items-center space-x-2">
                    <RadioGroupItem
                      value={gateway.id.toString()}
                      id={gateway.id.toString()}
                    />
                    <Label
                      htmlFor={gateway.id.toString()}
                      className="flex items-center space-x-2 cursor-pointer flex-1"
                    >
                      <span>{gateway.name}</span>
                    </Label>
                  </div>
                ))}
              </RadioGroup>
            )}
          </CardContent>
        </Card>

        {/* Action Buttons */}
        <div className="flex gap-3">
          <Button
            variant="outline"
            onClick={handleBackToCart}
            className="flex-1"
            disabled={isCheckingOut}
          >
            <ArrowLeft className="h-5 w-5 mr-2" />
            Back to Cart
          </Button>
          <Button
            onClick={onCheckout}
            disabled={
              !selectedPaymentGateway ||
              isCheckingOut ||
              isLoadingPaymentGateways
            }
            className="flex-1"
          >
            {isCheckingOut && <InlineLoader className="mr-2" />}
            {isCheckingOut
              ? "Processing..."
              : `Pay ${formatCurrency(cartTotal)}`}
          </Button>
        </div>
      </div>
    );
  },
);

CheckoutForm.displayName = "CheckoutForm";
