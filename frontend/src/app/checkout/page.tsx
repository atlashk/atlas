"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useUserStore, useCartStore } from "@/stores";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { CheckCircle, Clock, XCircle, CreditCard, ArrowLeft, ShoppingCart } from "lucide-react";
import { formatCurrency } from "@/utils/formatter.util";
import { orderApi } from "@/api";
import { PaymentMethod } from "@/constants/payment.constants";
import { PaymentFormProps, PaymentResult, paymentService } from "@/services/paymentService";
import { PaymentNextAction } from "@/interfaces";
import { toast } from "sonner";

interface PaymentTrackingResponse {
  sagaId: string;
  status: string;
  nextAction?: {
    type: string;
    url?: string;
    data?: Record<string, unknown>;
    client_secret?: string;
  };
  paymentMethod?: string;
  amount?: number;
  createdAt?: string;
  updatedAt?: string;
}

enum CheckoutStep {
  CART_REVIEW = "CART_REVIEW",
  PAYMENT_METHOD = "PAYMENT_METHOD", 
  PROCESSING = "PROCESSING",
  PAYMENT_FORM = "PAYMENT_FORM",
  COMPLETED = "COMPLETED",
  FAILED = "FAILED"
}

function CheckoutContent() {
  const router = useRouter();
  const { isAuthenticated, isAdmin } = useUserStore();
  const { cart, loadCart, clearCart, getCartTotal, isLoading: cartLoading } = useCartStore();
  
  const [currentStep, setCurrentStep] = useState<CheckoutStep>(CheckoutStep.CART_REVIEW);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<PaymentMethod>("CARD");
  const [paymentMethods, setPaymentMethods] = useState<string[]>([]);
  const [isProcessing, setIsProcessing] = useState(false);
  const [currentSagaId, setCurrentSagaId] = useState<string | null>(null);
  const [paymentData, setPaymentData] = useState<PaymentTrackingResponse | null>(null);
  const [pollingInterval, setPollingInterval] = useState<NodeJS.Timeout | null>(null);
  const [PaymentFormComponent, setPaymentFormComponent] = useState<React.ComponentType<PaymentFormProps> | null>(null);
  const [paymentNextAction, setPaymentNextAction] = useState<PaymentNextAction | null>(null);

  // Redirect if not authenticated or is admin
  useEffect(() => {
    if (!isAuthenticated() || isAdmin()) {
      router.push("/");
      return;
    }
  }, [isAuthenticated, isAdmin, router]);

  // Load cart and payment methods
  useEffect(() => {
    if (isAuthenticated() && !isAdmin()) {
      loadCart();
      loadPaymentMethods();
    }
  }, [isAuthenticated, isAdmin, loadCart]);

  // Load available payment methods
  const loadPaymentMethods = async () => {
    try {
      const response = await fetch("/api/payment-methods");
      if (response.ok) {
        const data = await response.json();
        setPaymentMethods(data.data || []);
      }
    } catch (error) {
      console.error("Error loading payment methods:", error);
    }
  };

  // Fetch payment tracking data
  const fetchPaymentTracking = async (sagaId: string) => {
    try {
      const response = await fetch(`/api/payment-svc/payments/${sagaId}/tracking`);
      
      if (response.ok) {
        const data: PaymentTrackingResponse = await response.json();
        setPaymentData(data);
        
        // Handle different payment statuses
        switch (data.status) {
          case "CREATED":
            // Payment intent created, stop polling and show payment form
            if (pollingInterval) {
              clearInterval(pollingInterval);
              setPollingInterval(null);
            }
            
            if (data.nextAction) {
              await handleNextAction(data.nextAction);
            }
            break;
            
          case "SUCCEEDED":
            // Payment completed successfully
            if (pollingInterval) {
              clearInterval(pollingInterval);
              setPollingInterval(null);
            }
            setCurrentStep(CheckoutStep.COMPLETED);
            toast.success("Payment successful!");
            break;
            
          case "FAILED":
          case "CANCELLED":
            // Payment failed or cancelled
            if (pollingInterval) {
              clearInterval(pollingInterval);
              setPollingInterval(null);
            }
            setCurrentStep(CheckoutStep.FAILED);
            toast.error("Payment failed!");
            break;
            
          case "PROCESSING":
          case "PENDING":
            // Continue polling
            break;
        }
      } else {
        throw new Error("Failed to fetch payment tracking");
      }
    } catch (error) {
      console.error("Error fetching payment tracking:", error);
      toast.error("Unable to get payment information");
    }
  };

  // Handle next action from payment tracking
  const handleNextAction = async (nextAction: PaymentTrackingResponse['nextAction']) => {
    if (!nextAction) return;

    switch (nextAction.type) {
      case "REDIRECT":
        if (nextAction.url) {
          window.location.href = nextAction.url;
        }
        break;
        
      case "PAYMENT_FORM":
        // Show payment form
        setCurrentStep(CheckoutStep.PAYMENT_FORM);
        
        // Create payment next action object
        const paymentNextActionObj: PaymentNextAction = {
          type: 'use_payment_element',
          client_secret: nextAction.client_secret || '',
          metadata: nextAction.data || {}
        };
        
        setPaymentNextAction(paymentNextActionObj);
        
        // Load payment form component
        try {
          const formComponent = await paymentService.createPaymentForm(paymentNextActionObj);
          setPaymentFormComponent(() => formComponent);
        } catch (error) {
          console.error("Error creating payment form:", error);
          toast.error("Unable to load payment form");
        }
        break;
        
      case "COMPLETE":
        setCurrentStep(CheckoutStep.COMPLETED);
        break;
        
      case "FAILED":
        setCurrentStep(CheckoutStep.FAILED);
        break;
        
      default:
        console.log("Unknown next action type:", nextAction.type);
        break;
    }
  };

  // Handle checkout process
  const handleCheckout = async () => {
    if (!cart?.cartItems.length || isProcessing) {
      return;
    }

    try {
      setIsProcessing(true);
      setCurrentStep(CheckoutStep.PROCESSING);

      const response = await orderApi.checkout({ 
        paymentMethod: selectedPaymentMethod as PaymentMethod
      });

      if (response.success && response.data) {
        const sagaId = response.data.sagaId.toString();
        setCurrentSagaId(sagaId);
        
        // Clear cart
        await clearCart();
        
        toast.success("Order created! Preparing payment...");
        
        // Start polling for payment status
        const interval = setInterval(() => {
          fetchPaymentTracking(sagaId);
        }, 1000);
        
        setPollingInterval(interval);
        
        // Initial fetch
        fetchPaymentTracking(sagaId);
        
      } else {
        throw new Error(response.errorMessage || "Failed to create order");
      }
    } catch (error) {
      console.error("Error during checkout:", error);
      toast.error("Unable to create order");
      setCurrentStep(CheckoutStep.PAYMENT_METHOD);
    } finally {
      setIsProcessing(false);
    }
  };

  // Handle payment result from payment form
  const handlePaymentResult = async (result: PaymentResult) => {
    if (result.success) {
      toast.success("Payment successful!");
      setCurrentStep(CheckoutStep.COMPLETED);
    } else {
      toast.error(result.error?.message || "Payment failed");
      setCurrentStep(CheckoutStep.FAILED);
    }
    
    // Stop polling
    if (pollingInterval) {
      clearInterval(pollingInterval);
      setPollingInterval(null);
    }
  };

  // Handle cancel payment
  const handleCancelPayment = () => {
    setCurrentStep(CheckoutStep.PAYMENT_METHOD);
    setPaymentFormComponent(null);
    setPaymentNextAction(null);
    
    // Stop polling
    if (pollingInterval) {
      clearInterval(pollingInterval);
      setPollingInterval(null);
    }
  };

  // Cleanup polling on unmount
  useEffect(() => {
    return () => {
      if (pollingInterval) {
        clearInterval(pollingInterval);
      }
    };
  }, [pollingInterval]);

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "CREATED":
      case "SUCCEEDED":
        return <CheckCircle className="h-8 w-8 text-green-500" />;
      case "PROCESSING":
      case "PENDING":
        return <Clock className="h-8 w-8 text-yellow-500" />;
      case "FAILED":
      case "CANCELLED":
        return <XCircle className="h-8 w-8 text-red-500" />;
      default:
        return <Clock className="h-8 w-8 text-gray-500" />;
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case "CREATED":
        return "Preparing payment";
      case "SUCCEEDED":
        return "Payment successful";
      case "PROCESSING":
        return "Processing payment";
      case "PENDING":
        return "Pending";
      case "FAILED":
        return "Payment failed";
      case "CANCELLED":
        return "Payment cancelled";
      default:
        return "Unknown status";
    }
  };

  if (cartLoading) {
    return (
      <div className="container mx-auto px-4 py-8 pt-20">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
            <p className="text-lg">Loading cart...</p>
          </div>
        </div>
      </div>
    );
  }

  const cartItems = cart?.cartItems || [];
  const totalAmount = getCartTotal();
  const isEmpty = cartItems.length === 0;

  if (isEmpty) {
    return (
      <div className="container mx-auto px-4 py-8 pt-20">
        <Card className="max-w-md mx-auto">
          <CardContent className="flex flex-col items-center justify-center py-12">
            <ShoppingCart className="h-16 w-16 text-gray-400 mb-4" />
            <h2 className="text-xl font-semibold text-gray-600 mb-2">Giỏ hàng trống</h2>
            <p className="text-gray-500 mb-4 text-center">Vui lòng thêm sản phẩm vào giỏ hàng trước khi thanh toán</p>
            <Button onClick={() => router.push("/")}>
              Tiếp tục mua sắm
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 pt-20">
      {/* Header */}
      <div className="flex items-center gap-4 mb-6">
        <Button
          variant="ghost"
          onClick={() => router.push("/cart")}
          className="flex items-center gap-2"
        >
          <ArrowLeft className="h-4 w-4" />
          Quay lại giỏ hàng
        </Button>
        <h1 className="text-3xl font-bold flex items-center gap-2">
          <CreditCard className="h-8 w-8" />
          Thanh toán
        </h1>
      </div>

      <div className="max-w-4xl mx-auto">
        {/* Cart Review Step */}
        {currentStep === CheckoutStep.CART_REVIEW && (
          <div className="space-y-6">
            {/* Order Summary */}
            <Card>
              <CardHeader>
                <CardTitle>Review Order</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                {cartItems.map((item) => (
                  <div key={item.product.id} className="flex items-center justify-between py-2 border-b">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 bg-gray-200 rounded-lg flex items-center justify-center">
                        <span className="text-xs font-medium">{item.product.name.charAt(0)}</span>
                      </div>
                      <div>
                        <h4 className="font-medium">{item.product.name}</h4>
                        <p className="text-sm text-gray-600">Số lượng: {item.quantity}</p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="font-semibold">{formatCurrency(item.product.price * item.quantity)}</p>
                    </div>
                  </div>
                ))}
                <div className="flex items-center justify-between pt-4 border-t">
                  <span className="text-lg font-semibold">Tổng cộng:</span>
                  <span className="text-lg font-bold">{formatCurrency(totalAmount)}</span>
                </div>
              </CardContent>
            </Card>

            <Button 
              onClick={() => setCurrentStep(CheckoutStep.PAYMENT_METHOD)}
              className="w-full"
              size="lg"
            >
              Tiếp tục thanh toán
            </Button>
          </div>
        )}

        {/* Payment Method Selection Step */}
        {currentStep === CheckoutStep.PAYMENT_METHOD && (
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Select Payment Method</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <Select value={selectedPaymentMethod} onValueChange={(value) => setSelectedPaymentMethod(value as PaymentMethod)}>
                  <SelectTrigger>
                    <SelectValue placeholder="Chọn phương thức thanh toán" />
                  </SelectTrigger>
                  <SelectContent>
                    {paymentMethods.map((method) => (
                      <SelectItem key={method} value={method}>
                        {method === "CARD" ? "Thẻ tín dụng/ghi nợ" : 
                         method === "BANK_TRANSFER" ? "Chuyển khoản ngân hàng" :
                         method === "E_WALLET" ? "Ví điện tử" : method}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>

                <div className="bg-gray-50 p-4 rounded-lg">
                  <div className="flex items-center justify-between">
                    <span className="font-medium">Total Payment:</span>
                    <span className="text-xl font-bold">{formatCurrency(totalAmount)}</span>
                  </div>
                </div>

                <div className="flex gap-3">
                  <Button 
                    variant="outline" 
                    onClick={() => setCurrentStep(CheckoutStep.CART_REVIEW)}
                    className="flex-1"
                  >
                    Back
                  </Button>
                  <Button 
                    onClick={handleCheckout}
                    disabled={isProcessing}
                    className="flex-1"
                  >
                    {isProcessing ? "Processing..." : "Pay Now"}
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        )}

        {/* Processing Step */}
        {currentStep === CheckoutStep.PROCESSING && (
          <Card className="max-w-2xl mx-auto">
            <CardContent className="flex flex-col items-center justify-center py-12">
              <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-primary mb-4"></div>
              <h2 className="text-xl font-semibold mb-2">Processing Payment</h2>
              <p className="text-gray-600 text-center mb-4">
                Please do not close this page. We are preparing your payment.
              </p>
              
              {paymentData && (
                <div className="w-full max-w-md space-y-4">
                  <div className="flex items-center justify-center space-y-2">
                    {getStatusIcon(paymentData.status)}
                    <div className="ml-3">
                      <p className="font-medium">{getStatusText(paymentData.status)}</p>
                      <Badge variant="secondary">{paymentData.status}</Badge>
                    </div>
                  </div>
                  
                  {currentSagaId && (
                    <div className="bg-gray-50 p-3 rounded-lg">
                      <p className="text-sm text-gray-600">Mã giao dịch:</p>
                      <p className="font-mono text-sm">{currentSagaId}</p>
                    </div>
                  )}
                </div>
              )}
            </CardContent>
          </Card>
        )}

        {/* Payment Form Step */}
        {currentStep === CheckoutStep.PAYMENT_FORM && PaymentFormComponent && paymentNextAction && (
          <div className="max-w-2xl mx-auto">
            <Card>
              <CardHeader>
                <CardTitle>Complete Payment</CardTitle>
              </CardHeader>
              <CardContent>
                <PaymentFormComponent
                  clientSecret={paymentNextAction.client_secret || ''}
                  orderId={currentSagaId || ''}
                  onPaymentResult={handlePaymentResult}
                  onCancel={handleCancelPayment}
                />
                <Button 
                  onClick={handleCancelPayment}
                  variant="outline"
                  className="mt-4"
                >
                  Cancel Payment
                </Button>
              </CardContent>
            </Card>
          </div>
        )}

        {/* Completed Step */}
        {currentStep === CheckoutStep.COMPLETED && (
          <Card className="max-w-2xl mx-auto">
            <CardContent className="flex flex-col items-center justify-center py-12">
              <CheckCircle className="h-16 w-16 text-green-500 mb-4" />
              <h2 className="text-2xl font-semibold text-green-600 mb-2">Payment Successful!</h2>
              <p className="text-gray-600 text-center mb-6">
                Thank you for your purchase. Your order has been processed successfully.
              </p>
              
              {paymentData && (
                <div className="w-full max-w-md space-y-4 mb-6">
                  <div className="bg-gray-50 p-4 rounded-lg space-y-2">
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Mã giao dịch:</span>
                      <span className="font-mono text-sm">{currentSagaId}</span>
                    </div>
                    {paymentData.amount && (
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-600">Số tiền:</span>
                        <span className="font-semibold">{formatCurrency(paymentData.amount)}</span>
                      </div>
                    )}
                  </div>
                </div>
              )}
              
              <div className="space-y-3 w-full max-w-md">
                <Button onClick={() => router.push("/?tab=order-history")} className="w-full">
                  Xem đơn hàng của tôi
                </Button>
                <Button onClick={() => router.push("/")} variant="outline" className="w-full">
                  <ArrowLeft className="w-4 h-4 mr-2" />
                  Tiếp tục mua sắm
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Failed Step */}
        {currentStep === CheckoutStep.FAILED && (
          <Card className="max-w-2xl mx-auto">
            <CardContent className="flex flex-col items-center justify-center py-12">
              <XCircle className="h-16 w-16 text-red-500 mb-4" />
              <h2 className="text-2xl font-semibold text-red-600 mb-2">Payment Failed</h2>
              <p className="text-gray-600 text-center mb-6">
                Sorry, your payment could not be processed. Please try again.
              </p>
              
              <div className="space-y-3 w-full max-w-md">
                <Button onClick={() => setCurrentStep(CheckoutStep.PAYMENT_METHOD)} className="w-full">
                  Try Payment Again
                </Button>
                <Button onClick={() => router.push("/cart")} variant="outline" className="w-full">
                  <ArrowLeft className="w-4 h-4 mr-2" />
                  Back to Cart
                </Button>
              </div>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}

export default function CheckoutPage() {
  return (
    <Suspense fallback={
      <div className="container mx-auto px-4 py-8 pt-20">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
            <p className="text-lg">Đang tải...</p>
          </div>
        </div>
      </div>
    }>
      <CheckoutContent />
    </Suspense>
  );
}