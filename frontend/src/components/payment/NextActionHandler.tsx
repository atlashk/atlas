"use client";

import { Card, CardContent } from "@/components/ui/card";

import { useNextActionHandler } from "@/hooks/useNextActionHandler";
import {
  PaymentGatewayResponse,
  PaymentNextAction,
} from "@/interfaces/payment.interface";
import { AlertCircle, CreditCard } from "lucide-react";
import {
  DeeplinkAction,
  PaymentElementAction,
  QrCodeAction,
  RedirectAction,
} from "./actions";

interface NextActionHandlerProps {
  nextAction: PaymentNextAction | null;
  orderId: string;
  selectedPaymentGateway?: PaymentGatewayResponse | null;
  amount?: number | null;
  currency?: string | null;
  onPaymentComplete?: () => void;
  onPaymentError?: (error: string) => void;
}

export function NextActionHandler({
  nextAction,
  orderId,
  selectedPaymentGateway,
  amount,
  currency,
  onPaymentComplete,
  onPaymentError,
}: NextActionHandlerProps) {
  const {
    error,
    isValidNextAction,
    handlePaymentComplete,
    handlePaymentError,
  } = useNextActionHandler({
    nextAction,
    orderId,
    onPaymentComplete,
    onPaymentError,
  });

  // Handle null or invalid nextAction
  if (!nextAction) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="text-center space-y-4">
            <AlertCircle className="w-12 h-12 mx-auto text-yellow-500" />
            <h3 className="text-lg font-semibold">No Payment Action</h3>
            <p className="text-gray-600">
              No payment action was provided for this order.
            </p>
            <p className="text-sm text-gray-500">Order ID: {orderId}</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (!isValidNextAction) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="text-center space-y-4">
            <AlertCircle className="w-12 h-12 mx-auto text-red-500" />
            <h3 className="text-lg font-semibold">Invalid Payment Action</h3>
            <p className="text-gray-600">
              The payment action is missing required information.
            </p>
            <p className="text-sm text-gray-500">
              Action Type: {nextAction.type}
            </p>
            <p className="text-sm text-gray-500">Order ID: {orderId}</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  // Handle error state
  if (error) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="text-center space-y-4">
            <AlertCircle className="w-12 h-12 mx-auto text-red-500" />
            <h3 className="text-lg font-semibold">Payment Error</h3>
            <p className="text-gray-600">{error}</p>
            <p className="text-sm text-gray-500">Order ID: {orderId}</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  // Render appropriate action component based on type
  switch (nextAction.type) {
    case "USE_PAYMENT_ELEMENT":
      return (
        <PaymentElementAction
          nextAction={nextAction}
          orderId={orderId}
          selectedPaymentGateway={selectedPaymentGateway}
          amount={amount}
          currency={currency}
          onPaymentComplete={handlePaymentComplete}
          onPaymentError={handlePaymentError}
        />
      );

    case "REDIRECT_URL":
      return (
        <RedirectAction
          redirectUrl={nextAction.url || ""}
          orderId={orderId}
          onError={handlePaymentError}
        />
      );

    case "QR_CODE":
      return <QrCodeAction qrCodeData={nextAction.content} orderId={orderId} />;

    case "DEEPLINK":
      return (
        <DeeplinkAction
          deeplinkUrl={nextAction.url || ""}
          orderId={orderId}
          onError={handlePaymentError}
        />
      );

    default:
      return (
        <Card>
          <CardContent className="p-6">
            <div className="text-center space-y-4">
              <CreditCard className="w-12 h-12 mx-auto text-gray-400" />
              <h3 className="text-lg font-semibold">Unknown Payment Method</h3>
              <p className="text-gray-600">
                This payment method is not supported yet
              </p>
              <p className="text-sm text-gray-500">
                Action Type: {nextAction.type}
              </p>
              <p className="text-sm text-gray-500">Order ID: {orderId}</p>
            </div>
          </CardContent>
        </Card>
      );
  }
}
