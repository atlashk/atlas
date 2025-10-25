"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  UsePaymentElementProvider,
  PAYMENT_NEXT_ACTION_TYPES,
} from "@/constants";
import { PaymentNextAction } from "@/interfaces/payment.interface";
import { CreditCard, ExternalLink, QrCode } from "lucide-react";
import { StripePaymentForm } from "./StripePaymentForm";

interface NextActionHandlerProps {
  nextAction: PaymentNextAction;
  orderId: string;
  onPaymentComplete?: () => void;
  onPaymentError?: (error: string) => void;
}

export function NextActionHandler({
  nextAction,
  orderId,
  onPaymentComplete,
  onPaymentError,
}: NextActionHandlerProps) {
  const handleRedirect = () => {
    if (nextAction.url) {
      window.open(nextAction.url, "_blank");
    }
  };

  switch (nextAction.type) {
    case PAYMENT_NEXT_ACTION_TYPES[3]: // "USE_PAYMENT_ELEMENT"
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
            amount={nextAction.amount || 0}
            currency={nextAction.currency || "USD"}
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

    case PAYMENT_NEXT_ACTION_TYPES[0]: // "REDIRECT_URL"
      return (
        <Card>
          <CardContent className="p-6">
            <div className="text-center space-y-4">
              <ExternalLink className="w-12 h-12 mx-auto text-blue-500" />
              <h3 className="text-lg font-semibold">
                Redirect to Payment Gateway
              </h3>
              <p className="text-gray-600">
                You will be redirected to complete your payment securely
              </p>

              <p className="text-sm text-gray-500">Order ID: {orderId}</p>

              <Button onClick={handleRedirect} className="w-full" size="lg">
                <ExternalLink className="w-4 h-4 mr-2" />
                Continue to Payment
              </Button>

              <p className="text-xs text-gray-500">
                You will be redirected to a secure payment page
              </p>
            </div>
          </CardContent>
        </Card>
      );

    case PAYMENT_NEXT_ACTION_TYPES[2]: // "QR_CODE"
      return (
        <Card>
          <CardContent className="p-6">
            <div className="text-center space-y-4">
              <QrCode className="w-12 h-12 mx-auto text-blue-500" />
              <h3 className="text-lg font-semibold">Scan QR Code to Pay</h3>
              <p className="text-gray-600">
                Use your mobile banking app to scan the QR code below
              </p>

              <p className="text-sm text-gray-500">Order ID: {orderId}</p>

              {/* QR Code Placeholder */}
              <div className="bg-gray-100 p-8 rounded-lg border-2 border-dashed border-gray-300 mx-auto max-w-xs">
                <div className="aspect-square bg-white rounded flex items-center justify-center">
                  <QrCode className="w-24 h-24 text-gray-400" />
                </div>
              </div>

              <div className="space-y-2">
                <p className="text-sm font-medium">Instructions:</p>
                <ol className="text-sm text-gray-600 text-left space-y-1">
                  <li>1. Open your mobile banking app</li>
                  <li>
                    2. Select &ldquo;Scan QR Code&rdquo; or &ldquo;Pay by
                    QR&rdquo;
                  </li>
                  <li>3. Point your camera at the QR code above</li>
                  <li>4. Confirm the payment amount</li>
                  <li>5. Complete the payment</li>
                </ol>
              </div>

              <p className="text-xs text-gray-500">
                The QR code will expire in 10 minutes
              </p>
            </div>
          </CardContent>
        </Card>
      );

    case PAYMENT_NEXT_ACTION_TYPES[1]: // "DEEPLINK"
      return (
        <Card>
          <CardContent className="p-6">
            <div className="text-center space-y-4">
              <CreditCard className="w-12 h-12 mx-auto text-blue-500" />
              <h3 className="text-lg font-semibold">Open Payment App</h3>
              <p className="text-gray-600">
                Complete your payment using your preferred payment app
              </p>

              <p className="text-sm text-gray-500">Order ID: {orderId}</p>

              <Button
                onClick={() =>
                  nextAction.url && window.open(nextAction.url, "_self")
                }
                className="w-full"
                size="lg"
              >
                Open Payment App
              </Button>

              <p className="text-xs text-gray-500">
                This will open your payment app to complete the transaction
              </p>
            </div>
          </CardContent>
        </Card>
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
                Next Action Type: {nextAction.type}
              </p>
            </div>
          </CardContent>
        </Card>
      );
  }
}
