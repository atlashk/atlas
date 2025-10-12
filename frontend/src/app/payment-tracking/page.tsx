"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useUserStore } from "@/stores/user.store";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { CheckCircle, Clock, XCircle, CreditCard, ArrowLeft } from "lucide-react";
import { formatCurrency } from "@/utils/formatter.util";

interface PaymentTrackingResponse {
  sagaId: string;
  status: string;
  nextAction?: {
    type: string;
    url?: string;
    data?: Record<string, unknown>;
  };
  paymentMethod?: string;
  amount?: number;
  createdAt?: string;
  updatedAt?: string;
}

function PaymentTrackingContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { isAuthenticated, isAdmin } = useUserStore();
  
  const [sagaId, setSagaId] = useState<string>("");
  const [paymentData, setPaymentData] = useState<PaymentTrackingResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>("");
  const [pollingInterval, setPollingInterval] = useState<NodeJS.Timeout | null>(null);

  // Get sagaId from URL params
  useEffect(() => {
    const sagaIdParam = searchParams.get("sagaId");
    if (sagaIdParam) {
      setSagaId(sagaIdParam);
    } else {
      setError("Không tìm thấy thông tin thanh toán");
      setLoading(false);
    }
  }, [searchParams]);

  // Redirect if not authenticated or is admin
  useEffect(() => {
    if (!isAuthenticated() || isAdmin()) {
      router.push("/");
      return;
    }
  }, [isAuthenticated, isAdmin, router]);

  // Fetch payment tracking data
  const fetchPaymentTracking = async (currentSagaId: string) => {
    try {
      const response = await fetch(`/api/payment-svc/payments/${currentSagaId}/tracking`);
      
      if (response.ok) {
        const data: PaymentTrackingResponse = await response.json();
        setPaymentData(data);
        setError("");
        
        // If status is CREATED, stop polling and handle next action
        if (data.status === "CREATED") {
          if (pollingInterval) {
            clearInterval(pollingInterval);
            setPollingInterval(null);
          }
          
          // Handle next action
          if (data.nextAction) {
            handleNextAction(data.nextAction);
          }
        }
      } else {
        throw new Error("Failed to fetch payment tracking");
      }
    } catch (error) {
      console.error("Error fetching payment tracking:", error);
      setError("Không thể lấy thông tin thanh toán");
    } finally {
      setLoading(false);
    }
  };

  // Handle next action from payment tracking
  const handleNextAction = (nextAction: { type: string; url?: string; data?: Record<string, unknown> }) => {
    switch (nextAction.type) {
      case "REDIRECT":
        if (nextAction.url) {
          window.location.href = nextAction.url;
        }
        break;
      case "COMPLETE":
        // Payment completed successfully
        router.push("/payment-success");
        break;
      case "FAILED":
        // Payment failed
        router.push("/payment-failed");
        break;
      default:
        console.log("Unknown next action type:", nextAction.type);
        break;
    }
  };

  // Start polling when sagaId is available
  useEffect(() => {
    if (sagaId && isAuthenticated() && !isAdmin()) {
      // Initial fetch
      fetchPaymentTracking(sagaId);
      
      // Start polling every second
      const interval = setInterval(() => {
        fetchPaymentTracking(sagaId);
      }, 1000);
      
      setPollingInterval(interval);
      
      // Cleanup on unmount
      return () => {
        if (interval) {
          clearInterval(interval);
        }
      };
    }
  }, [sagaId, isAuthenticated, isAdmin]);

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
        return "Thanh toán thành công";
      case "PROCESSING":
        return "Đang xử lý thanh toán";
      case "PENDING":
        return "Chờ xử lý";
      case "FAILED":
        return "Thanh toán thất bại";
      case "CANCELLED":
        return "Thanh toán đã hủy";
      default:
        return "Trạng thái không xác định";
    }
  };

  const getStatusVariant = (status: string) => {
    switch (status) {
      case "CREATED":
        return "default";
      case "PROCESSING":
      case "PENDING":
        return "secondary";
      case "FAILED":
      case "CANCELLED":
        return "destructive";
      default:
        return "outline";
    }
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8 pt-20">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
            <p className="text-lg">Đang kiểm tra trạng thái thanh toán...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-8 pt-20">
        <Card className="max-w-md mx-auto">
          <CardContent className="flex flex-col items-center justify-center py-12">
            <XCircle className="h-16 w-16 text-red-500 mb-4" />
            <h2 className="text-xl font-semibold text-red-600 mb-2">Lỗi</h2>
            <p className="text-gray-600 mb-4 text-center">{error}</p>
            <Button onClick={() => router.push("/cart")}>
              Quay lại giỏ hàng
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
          Theo dõi thanh toán
        </h1>
      </div>

      <Card className="max-w-2xl mx-auto">
        <CardHeader>
          <CardTitle className="text-center">Trạng thái thanh toán</CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          {paymentData && (
            <>
              {/* Status Display */}
              <div className="flex flex-col items-center text-center space-y-4">
                {getStatusIcon(paymentData.status)}
                <div>
                  <h2 className="text-2xl font-semibold mb-2">
                    {getStatusText(paymentData.status)}
                  </h2>
                  <Badge variant={getStatusVariant(paymentData.status)}>
                    {paymentData.status}
                  </Badge>
                </div>
              </div>

              {/* Payment Details */}
              <div className="space-y-4 border-t pt-6">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-600">Mã giao dịch:</label>
                    <p className="font-mono text-sm">{paymentData.sagaId}</p>
                  </div>
                  {paymentData.paymentMethod && (
                    <div>
                      <label className="text-sm font-medium text-gray-600">Phương thức:</label>
                      <p>{paymentData.paymentMethod}</p>
                    </div>
                  )}
                  {paymentData.amount && (
                    <div>
                      <label className="text-sm font-medium text-gray-600">Số tiền:</label>
                      <p className="font-semibold">{formatCurrency(paymentData.amount)}</p>
                    </div>
                  )}
                  {paymentData.createdAt && (
                    <div>
                      <label className="text-sm font-medium text-gray-600">Thời gian tạo:</label>
                      <p>{new Date(paymentData.createdAt).toLocaleString('vi-VN')}</p>
                    </div>
                  )}
                </div>
              </div>

              {/* Processing Message */}
              {(paymentData.status === "PROCESSING" || paymentData.status === "PENDING") && (
                <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 text-center">
                  <Clock className="h-6 w-6 text-yellow-600 mx-auto mb-2" />
                  <p className="text-yellow-800">
                    Đang xử lý thanh toán của bạn. Vui lòng không đóng trang này.
                  </p>
                  <div className="flex items-center justify-center mt-2">
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-yellow-600"></div>
                    <span className="ml-2 text-sm text-yellow-700">Đang kiểm tra...</span>
                  </div>
                </div>
              )}

              {/* Next Action Info */}
              {paymentData.nextAction && (
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <h3 className="font-semibold text-blue-800 mb-2">Hành động tiếp theo:</h3>
                  <p className="text-blue-700">
                    Loại: {paymentData.nextAction.type}
                  </p>
                  {paymentData.nextAction.url && (
                    <p className="text-blue-700 text-sm">
                      URL: {paymentData.nextAction.url}
                    </p>
                  )}
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

export default function PaymentTrackingPage() {
  return (
    <Suspense fallback={<div className="flex justify-center items-center min-h-screen">Loading...</div>}>
      <PaymentTrackingContent />
    </Suspense>
  );
}