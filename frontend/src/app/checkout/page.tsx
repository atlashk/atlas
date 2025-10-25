"use client";

import { orderApi, paymentApi } from "@/api/index.api";
import { NextActionHandler } from "@/components/payment/NextActionHandler";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Spinner } from "@/components/ui/spinner";
import { PaymentMethod } from "@/constants";
import { useOrderStatusPolling } from "@/hooks/useOrderStatusPolling";
import { PaymentNextAction } from "@/interfaces/payment.interface";
import { useCartStore } from "@/stores";
import { formatCurrency } from "@/utils/formatter.util";
import { CheckCircle, CreditCard, XCircle } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { toast } from "sonner";

export default function CheckoutPage() {
  const router = useRouter();
  const { cart, getCartTotal } = useCartStore();

  const [selectedPaymentMethod, setSelectedPaymentMethod] =
    useState<PaymentMethod>("CARD");
  const [orderId, setOrderId] = useState<string | null>(null);
  const [isCheckingOut, setIsCheckingOut] = useState(false);
  const [availablePaymentMethods, setAvailablePaymentMethods] = useState<
    string[]
  >([]);
  const [isLoadingPaymentMethods, setIsLoadingPaymentMethods] = useState(true);
  const [paymentNextAction, setPaymentNextAction] =
    useState<PaymentNextAction | null>(null);
  const [isProcessingPayment, setIsProcessingPayment] = useState(false);

  // Ref to track if payment methods have been fetched to prevent duplicate calls
  const paymentMethodsFetched = useRef(false);
  // Ref to track if payment processing has been initiated for current order
  const paymentProcessingInitiated = useRef<string | null>(null);

  const {
    orderStatus,
    isLoading,
    error,
    needsPaymentProcessing,
    stopPolling,
    startPolling,
  } = useOrderStatusPolling(orderId);

  // Fetch available payment methods on component mount
  useEffect(() => {
    const fetchPaymentMethods = async () => {
      // Prevent duplicate API calls during development mode double renders
      if (paymentMethodsFetched.current) {
        return;
      }

      try {
        setIsLoadingPaymentMethods(true);
        paymentMethodsFetched.current = true;
        const response = await paymentApi.getPaymentMethods();
        if (response.success && response.data) {
          setAvailablePaymentMethods(response.data);
          // Set first available method as default if current selection is not available
          if (
            response.data.length > 0 &&
            !response.data.includes(selectedPaymentMethod)
          ) {
            setSelectedPaymentMethod(response.data[0] as PaymentMethod);
          }
        }
      } catch (err) {
        console.error("Failed to fetch payment methods:", err);
        // Fallback to default methods if API fails
        setAvailablePaymentMethods(["CARD", "PAYPAL"]);
        // Reset the ref on error so it can be retried
        paymentMethodsFetched.current = false;
      } finally {
        setIsLoadingPaymentMethods(false);
      }
    };

    fetchPaymentMethods();
  }, []); // Empty dependency array - only run once on mount

  // Handle payment processing when order status is AWAITING_PAYMENT_PROCESSED
  useEffect(() => {
    const processPayment = async () => {
      if (!needsPaymentProcessing || !orderId || isProcessingPayment) return;

      // Prevent duplicate processing for the same order
      if (paymentProcessingInitiated.current === orderId) return;

      try {
        setIsProcessingPayment(true);
        paymentProcessingInitiated.current = orderId;

        const response = await paymentApi.getPaymentNextAction(orderId);

        if (response.success && response.data?.nextAction) {
          // Include amount and currency from API response in nextAction
          const nextActionWithPaymentInfo = {
            ...response.data.nextAction,
            amount: response.data.amount,
            currency: response.data.currency,
          };
          setPaymentNextAction(nextActionWithPaymentInfo);
          // Stop order status polling since we now have the payment next action
          stopPolling();
        } else {
          toast.error("Failed to get payment next action");
        }
      } catch (err) {
        console.error("Failed to process payment:", err);
        toast.error("An error occurred while processing payment");
      } finally {
        setIsProcessingPayment(false);
      }
    };

    processPayment();
  }, [needsPaymentProcessing, orderId]);

  const handleCheckout = async () => {
    try {
      setIsCheckingOut(true);

      // Reset payment processing state for new order
      paymentProcessingInitiated.current = null;
      setPaymentNextAction(null);

      // 1. Call checkout API
      const response = await orderApi.checkout({
        paymentMethod: selectedPaymentMethod,
      });

      if (response.success && response.data) {
        // 2. Get orderId and start polling
        setOrderId(response.data.orderId.toString());
        toast.success("Order created successfully, processing...");
      } else {
        throw new Error("Checkout failed");
      }
    } catch (err) {
      toast.error("An error occurred while creating the order");
      console.error(err);
    } finally {
      setIsCheckingOut(false);
    }
  };

  const renderOrderStatus = () => {
    // Show spinner when polling and no order status yet, or when loading
    if (!orderStatus || isLoading) {
      return (
        <div className="text-center space-y-4">
          <Spinner className="text-blue-600 mx-auto" />
          <h3 className="text-lg font-semibold">Processing Order</h3>
          <p className="text-gray-600">
            {!orderStatus
              ? "Creating your order..."
              : "Updating order status..."}
          </p>
        </div>
      );
    }

    switch (orderStatus.status) {
      case "AWAITING_PRODUCT_RESERVATION":
      case "AWAITING_PAYMENT_INITIALIZED":
      case "AWAITING_PAYMENT_PROCESSED":
        // Show spinner for these intermediate states
        return (
          <div className="text-center space-y-4">
            <Spinner className="text-blue-600 mx-auto" />
            <h3 className="text-lg font-semibold">Processing Order</h3>
            <p className="text-gray-600">
              {orderStatus.status === "AWAITING_PRODUCT_RESERVATION" &&
                "Reserving products..."}
              {orderStatus.status === "AWAITING_PAYMENT_INITIALIZED" &&
                "Initializing payment..."}
              {orderStatus.status === "AWAITING_PAYMENT_PROCESSED" &&
                "Processing payment..."}
            </p>
          </div>
        );

      case "FULFILLED":
        return (
          <div className="text-center space-y-4">
            <CheckCircle className="w-12 h-12 text-green-500 mx-auto" />
            <h3 className="text-lg font-semibold text-green-600">
              Order Completed!
            </h3>
            <p className="text-gray-600">
              Your order has been processed successfully.
            </p>
            <Button onClick={() => router.push("/order-history")} className="mt-4">
              View Orders
            </Button>
          </div>
        );

      case "CANCELED":
        return (
          <div className="text-center space-y-4">
            <XCircle className="w-12 h-12 text-red-500 mx-auto" />
            <h3 className="text-lg font-semibold text-red-600">
              Order Canceled
            </h3>
            <p className="text-gray-600">
              {orderStatus.cancellationReason ||
                "Your order has been canceled."}
            </p>
            <div className="space-x-2">
              <Button
                variant="outline"
                onClick={() => {
                  setOrderId(null);
                  paymentProcessingInitiated.current = null;
                  setPaymentNextAction(null);
                }}
              >
                Try Again
              </Button>
              <Button variant="outline" onClick={() => router.push("/cart")}>
                Back to Cart
              </Button>
            </div>
          </div>
        );
    }
  };

  // If polling order status, show status
  if (orderId && (isLoading || orderStatus)) {
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
                <Button
                  variant="outline"
                  onClick={() => {
                    setOrderId(null);
                    paymentProcessingInitiated.current = null;
                    setPaymentNextAction(null);
                  }}
                >
                  Try Again
                </Button>
              </div>
            ) : (
              <>
                {paymentNextAction ? (
                  <NextActionHandler
                    nextAction={paymentNextAction}
                    orderId={orderId}
                    onPaymentComplete={() => {
                      setPaymentNextAction(null);
                      toast.success("Payment completed successfully!");
                      // Restart polling to check for order completion
                      startPolling();
                    }}
                    onPaymentError={(error: string) => {
                      setPaymentNextAction(null);
                      toast.error(`Payment failed: ${error}`);
                      // Restart polling to check order status after payment error
                      startPolling();
                    }}
                  />
                ) : (
                  renderOrderStatus()
                )}
              </>
            )}
          </CardContent>
        </Card>
      </div>
    );
  }

  // Show initial checkout form
  return (
    <div className="container mx-auto px-4 py-8 max-w-2xl">
      <div className="space-y-4">
        {/* Header - removed back button */}
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
              {cart?.cartItems?.map((item) => (
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
                <span>{formatCurrency(getCartTotal())}</span>
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
              <div className="flex items-center justify-center py-4">
                <Spinner className="text-blue-600 mr-2" />
                <span>Loading payment methods...</span>
              </div>
            ) : (
              <Select
                value={selectedPaymentMethod}
                onValueChange={(value) =>
                  setSelectedPaymentMethod(value as PaymentMethod)
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select payment method" />
                </SelectTrigger>
                <SelectContent>
                  {availablePaymentMethods.map((method) => (
                    <SelectItem key={method} value={method}>
                      {method === "CARD" && "Credit/Debit Card"}
                      {method === "PAYPAL" && "PayPal"}
                      {method === "BANK_TRANSFER" && "Bank Transfer"}
                      {method === "E_WALLET" && "E-Wallet"}
                      {![
                        "CARD",
                        "PAYPAL",
                        "BANK_TRANSFER",
                        "E_WALLET",
                      ].includes(method) && method}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}
          </CardContent>
        </Card>

        {/* Checkout Button */}
        <Button
          onClick={handleCheckout}
          disabled={
            isCheckingOut ||
            !cart ||
            cart.cartItems.length === 0 ||
            isLoadingPaymentMethods
          }
          className="w-full bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white shadow-lg hover:shadow-xl transition-all duration-200"
          size="lg"
        >
          {isCheckingOut
            ? "Processing..."
            : `Pay ${formatCurrency(getCartTotal())}`}
        </Button>

        {/* White Back Button - navigates to cart */}
        <Button
          variant="outline"
          onClick={() => router.push("/cart")}
          className="w-full bg-white border-gray-300 text-gray-700 hover:bg-gray-50"
          size="lg"
        >
          Back to Shopping Cart
        </Button>
      </div>
    </div>
  );
}
