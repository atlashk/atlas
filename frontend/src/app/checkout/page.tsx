"use client";

import { orderApi, paymentApi } from "@/api/index.api";
import { NextActionHandler } from "@/components/payment/NextActionHandler";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { PaymentMethod } from "@/constants";
import { PaymentNextAction } from "@/interfaces/payment.interface";
import { useOrderStatusPolling } from "@/hooks/useOrderStatusPolling";
import { useCartStore } from "@/stores";
import { formatCurrency } from "@/utils/formatter.util";
import { CheckCircle, Clock, CreditCard, XCircle } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { toast } from "sonner";

export default function CheckoutPage() {
  const router = useRouter();
  const { cart, getCartTotal } = useCartStore();
  
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<PaymentMethod>("CARD");
  const [orderId, setOrderId] = useState<string | null>(null);
  const [isCheckingOut, setIsCheckingOut] = useState(false);
  const [availablePaymentMethods, setAvailablePaymentMethods] = useState<string[]>([]);
  const [isLoadingPaymentMethods, setIsLoadingPaymentMethods] = useState(true);
  const [paymentNextAction, setPaymentNextAction] = useState<PaymentNextAction | null>(null);
  const [isProcessingPayment, setIsProcessingPayment] = useState(false);
  
  const { orderStatus, isLoading, error, needsPaymentProcessing } = useOrderStatusPolling(orderId);

  // Fetch available payment methods on component mount
  useEffect(() => {
    const fetchPaymentMethods = async () => {
      try {
        setIsLoadingPaymentMethods(true);
        const response = await paymentApi.getPaymentMethods();
        if (response.success && response.data) {
          setAvailablePaymentMethods(response.data);
          // Set first available method as default if current selection is not available
          if (response.data.length > 0 && !response.data.includes(selectedPaymentMethod)) {
            setSelectedPaymentMethod(response.data[0] as PaymentMethod);
          }
        }
      } catch (err) {
        console.error("Failed to fetch payment methods:", err);
        // Fallback to default methods if API fails
        setAvailablePaymentMethods(["CARD", "PAYPAL"]);
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

      try {
        setIsProcessingPayment(true);
        const response = await paymentApi.getPaymentNextAction(orderId);
        
        if (response.success && response.data?.nextAction) {
          setPaymentNextAction(response.data.nextAction);
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
  }, [needsPaymentProcessing, orderId, isProcessingPayment]);

  const handleCheckout = async () => {
    try {
      setIsCheckingOut(true);
      
      // 1. Call checkout API
      const response = await orderApi.checkout({
        paymentMethod: selectedPaymentMethod
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
    if (!orderStatus) return null;

    switch (orderStatus.status) {
      case 'AWAITING_PRODUCT_RESERVATION':
      case 'AWAITING_PAYMENT_INITIALIZED':
        // Skip these states - just continue polling without showing anything
        return null;

      case 'AWAITING_PAYMENT_PROCESSED':
        return (
          <div className="text-center space-y-4">
            <Clock className="w-12 h-12 text-blue-500 mx-auto animate-spin" />
            <h3 className="text-lg font-semibold">Processing Payment</h3>
            <p className="text-gray-600">Please wait while we process your payment...</p>
          </div>
        );

      case 'FULFILLED':
        return (
          <div className="text-center space-y-4">
            <CheckCircle className="w-12 h-12 text-green-500 mx-auto" />
            <h3 className="text-lg font-semibold text-green-600">Order Completed!</h3>
            <p className="text-gray-600">Your order has been processed successfully.</p>
            <Button onClick={() => router.push('/orders')} className="mt-4">
              View Orders
            </Button>
          </div>
        );

      case 'CANCELED':
        return (
          <div className="text-center space-y-4">
            <XCircle className="w-12 h-12 text-red-500 mx-auto" />
            <h3 className="text-lg font-semibold text-red-600">Order Canceled</h3>
            <p className="text-gray-600">
              {orderStatus.cancellationReason || 'Your order has been canceled.'}
            </p>
            <div className="space-x-2">
              <Button variant="outline" onClick={() => setOrderId(null)}>
                Try Again
              </Button>
              <Button variant="outline" onClick={() => router.push('/cart')}>
                Back to Cart
              </Button>
            </div>
          </div>
        );

      default:
        return (
          <div className="text-center space-y-4">
            <Clock className="w-12 h-12 text-blue-500 mx-auto animate-spin" />
            <h3 className="text-lg font-semibold">Processing...</h3>
            <p className="text-gray-600">Status: {orderStatus.status}</p>
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
              Order Status
            </CardTitle>
          </CardHeader>
          <CardContent>
            {error ? (
              <div className="text-center space-y-4">
                <XCircle className="w-12 h-12 text-red-500 mx-auto" />
                <h3 className="text-lg font-semibold text-red-600">An Error Occurred</h3>
                <p className="text-gray-600">{error}</p>
                <Button variant="outline" onClick={() => setOrderId(null)}>
                  Try Again
                </Button>
              </div>
            ) : (
              <>
                {paymentNextAction ? (
                  <div className="space-y-4">
                    <div className="text-center">
                      <h3 className="text-lg font-semibold">Complete Payment</h3>
                      <p className="text-gray-600">Please complete your payment to proceed</p>
                    </div>
                    <NextActionHandler 
                      nextAction={paymentNextAction}
                      onPaymentComplete={() => {
                        setPaymentNextAction(null);
                        toast.success("Payment completed successfully!");
                      }}
                      onPaymentError={(error: string) => {
                        setPaymentNextAction(null);
                        toast.error(`Payment failed: ${error}`);
                      }}
                    />
                  </div>
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
        <div className="flex items-center gap-4">
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
                  <span>{item.product.name} x {item.quantity}</span>
                  <span>{formatCurrency(item.product.price * item.quantity)}</span>
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
                <Clock className="w-4 h-4 animate-spin mr-2" />
                <span>Loading payment methods...</span>
              </div>
            ) : (
              <Select value={selectedPaymentMethod} onValueChange={(value) => setSelectedPaymentMethod(value as PaymentMethod)}>
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
                      {!["CARD", "PAYPAL", "BANK_TRANSFER", "E_WALLET"].includes(method) && method}
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
          disabled={isCheckingOut || !cart || cart.cartItems.length === 0 || isLoadingPaymentMethods}
          className="w-full bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white shadow-lg hover:shadow-xl transition-all duration-200"
          size="lg"
        >
          {isCheckingOut ? "Processing..." : `Pay ${formatCurrency(getCartTotal())}`}
        </Button>

        {/* White Back Button - navigates to cart */}
        <Button 
          variant="outline" 
          onClick={() => router.push('/cart')}
          className="w-full bg-white border-gray-300 text-gray-700 hover:bg-gray-50"
          size="lg"
        >
          Back to Shopping Cart
        </Button>
      </div>
    </div>
  );
}
