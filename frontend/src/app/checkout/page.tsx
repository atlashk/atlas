"use client";

import { orderApi, paymentApi } from "@/api/index.api";
import { NextActionHandler } from "@/components/payment/NextActionHandler";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { PaymentMethod } from "@/constants";
import { usePaymentPolling } from "@/hooks/usePaymentPolling";
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
  const [sagaId, setSagaId] = useState<number | null>(null);
  const [isCheckingOut, setIsCheckingOut] = useState(false);
  const [availablePaymentMethods, setAvailablePaymentMethods] = useState<string[]>([]);
  const [isLoadingPaymentMethods, setIsLoadingPaymentMethods] = useState(true);
  
  const { paymentStatus, isLoading, error } = usePaymentPolling(sagaId);

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

  const handleCheckout = async () => {
    try {
      setIsCheckingOut(true);
      
      // 1. Call checkout API
      const response = await orderApi.checkout({
        paymentMethod: selectedPaymentMethod
      });

      if (response.success && response.data) {
        // 2. Get sagaId and start polling
        setSagaId(response.data.sagaId);
        toast.success("Order created successfully, processing payment...");
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

  const renderPaymentStatus = () => {
    if (!paymentStatus) return null;

    switch (paymentStatus.status) {
      case 'CREATED':
        return (
          <div className="text-center space-y-4">
            <Clock className="w-12 h-12 text-blue-500 mx-auto animate-spin" />
            <h3 className="text-lg font-semibold">Processing Payment</h3>
            <p className="text-gray-600">Please wait while we process your payment...</p>
            
            {/* Render next action using dedicated component */}
             {paymentStatus.nextAction && (
               <div className="mt-4">
                 <NextActionHandler
                   nextAction={paymentStatus.nextAction}
                   transactionId={paymentStatus.transactionId}
                   onPaymentComplete={() => {
                     toast.success("Payment completed successfully!");
                     // The polling will automatically detect the status change
                   }}
                   onPaymentError={(error) => {
                     toast.error(error);
                   }}
                 />
               </div>
             )}
          </div>
        );

      case 'SUCCEEDED':
        return (
          <div className="text-center space-y-4">
            <CheckCircle className="w-12 h-12 text-green-500 mx-auto" />
            <h3 className="text-lg font-semibold text-green-600">Payment Successful!</h3>
            <p className="text-gray-600">Your order has been processed successfully.</p>
            {paymentStatus.transactionId && (
              <p className="text-sm text-gray-500">Transaction ID: {paymentStatus.transactionId}</p>
            )}
            <Button onClick={() => router.push('/orders')} className="mt-4">
              View Orders
            </Button>
          </div>
        );

      case 'FAILED':
      case 'CANCELED':
        return (
          <div className="text-center space-y-4">
            <XCircle className="w-12 h-12 text-red-500 mx-auto" />
            <h3 className="text-lg font-semibold text-red-600">
              {paymentStatus.status === 'FAILED' ? 'Payment Failed' : 'Payment Canceled'}
            </h3>
            <p className="text-gray-600">
              {paymentStatus.errorMessage || paymentStatus.cancellationReason || 'An error occurred during payment processing.'}
            </p>
            {paymentStatus.errorCode && (
              <p className="text-sm text-gray-500">Error Code: {paymentStatus.errorCode}</p>
            )}
            <div className="space-x-2">
              <Button variant="outline" onClick={() => setSagaId(null)}>
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
            <p className="text-gray-600">Status: {paymentStatus.status}</p>
          </div>
        );
    }
  };

  // If polling payment status, show status
  if (sagaId && (isLoading || paymentStatus)) {
    return (
      <div className="container mx-auto px-4 py-8 max-w-2xl">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <CreditCard className="w-5 h-5" />
              Payment Status
            </CardTitle>
          </CardHeader>
          <CardContent>
            {error ? (
              <div className="text-center space-y-4">
                <XCircle className="w-12 h-12 text-red-500 mx-auto" />
                <h3 className="text-lg font-semibold text-red-600">An Error Occurred</h3>
                <p className="text-gray-600">{error}</p>
                <Button variant="outline" onClick={() => setSagaId(null)}>
                  Try Again
                </Button>
              </div>
            ) : (
              renderPaymentStatus()
            )}
          </CardContent>
        </Card>
      </div>
    );
  }

  // Show initial checkout form
  return (
    <div className="container mx-auto px-4 py-8 max-w-2xl">
      <div className="space-y-6">
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
          className="w-full"
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
