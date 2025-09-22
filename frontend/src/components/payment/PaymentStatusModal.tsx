'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { CheckCircle, XCircle, Clock, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

interface PaymentStatusModalProps {
  isOpen: boolean;
  onClose: () => void;
  status: 'AWAITING_PAYMENT' | 'PAYMENT_SUCCEEDED' | 'PAYMENT_FAILED' | null;
  orderId: string;
}

export default function PaymentStatusModal({
  isOpen,
  onClose,
  status,
  orderId
}: PaymentStatusModalProps) {
  const router = useRouter();
  const [countdown, setCountdown] = useState(5);

  const handleAutoRedirect = useCallback(() => {
    if (status === 'PAYMENT_SUCCEEDED') {
      router.push('/payment-success');
    } else if (status === 'PAYMENT_FAILED') {
      router.push('/payment-failed');
    }
    onClose();
  }, [status, router, onClose]);

  useEffect(() => {
    if (status === 'PAYMENT_SUCCEEDED' || status === 'PAYMENT_FAILED') {
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            handleAutoRedirect();
            return 0;
          }
          return prev - 1;
        });
      }, 1000);

      return () => clearInterval(timer);
    }
  }, [status, handleAutoRedirect]);

  const handleManualRedirect = () => {
    if (status === 'PAYMENT_SUCCEEDED') {
      router.push('/payment-success');
      toast.success('Payment completed successfully!');
    } else if (status === 'PAYMENT_FAILED') {
      router.push('/payment-failed');
      toast.error('Payment failed. Please try again.');
    }
    onClose();
  };

  const getStatusIcon = () => {
    switch (status) {
      case 'AWAITING_PAYMENT':
        return <Clock className="w-8 h-8 text-blue-600" />;
      case 'PAYMENT_SUCCEEDED':
        return <CheckCircle className="w-8 h-8 text-green-600" />;
      case 'PAYMENT_FAILED':
        return <XCircle className="w-8 h-8 text-red-600" />;
      default:
        return <Loader2 className="w-8 h-8 text-gray-600 animate-spin" />;
    }
  };

  const getStatusTitle = () => {
    switch (status) {
      case 'AWAITING_PAYMENT':
        return 'Payment Processing';
      case 'PAYMENT_SUCCEEDED':
        return 'Payment Successful!';
      case 'PAYMENT_FAILED':
        return 'Payment Failed';
      default:
        return 'Processing...';
    }
  };

  const getStatusMessage = () => {
    switch (status) {
      case 'AWAITING_PAYMENT':
        return 'Your payment is being processed. Please wait...';
      case 'PAYMENT_SUCCEEDED':
        return 'Your payment has been processed successfully. Thank you for your order!';
      case 'PAYMENT_FAILED':
        return 'Your payment could not be processed. Please try again or contact support.';
      default:
        return 'Please wait while we process your request...';
    }
  };

  const getStatusColor = () => {
    switch (status) {
      case 'AWAITING_PAYMENT':
        return 'text-blue-600';
      case 'PAYMENT_SUCCEEDED':
        return 'text-green-600';
      case 'PAYMENT_FAILED':
        return 'text-red-600';
      default:
        return 'text-gray-600';
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="text-center">Order #{orderId}</DialogTitle>
        </DialogHeader>
        
        <div className="text-center space-y-4 py-4">
          <div className="mx-auto w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center">
            {getStatusIcon()}
          </div>
          
          <div>
            <h3 className={`text-xl font-semibold ${getStatusColor()}`}>
              {getStatusTitle()}
            </h3>
            <p className="text-gray-600 mt-2">
              {getStatusMessage()}
            </p>
          </div>

          {(status === 'PAYMENT_SUCCEEDED' || status === 'PAYMENT_FAILED') && (
            <div className="space-y-3">
              <Button onClick={handleManualRedirect} className="w-full">
                {status === 'PAYMENT_SUCCEEDED' ? 'View Order Details' : 'Try Again'}
              </Button>
              <p className="text-sm text-gray-500">
                Redirecting in {countdown} seconds...
              </p>
            </div>
          )}

          {status === 'AWAITING_PAYMENT' && (
            <div className="flex items-center justify-center space-x-2">
              <Loader2 className="w-4 h-4 animate-spin" />
              <span className="text-sm text-gray-500">Processing payment...</span>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}