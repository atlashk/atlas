'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { CheckCircle, XCircle } from 'lucide-react';

interface PaymentStatusModalProps {
  isOpen: boolean;
  isSuccess: boolean;
  message: string;
  onClose: () => void;
  autoRedirect?: boolean;
  redirectDelay?: number;
}

export default function PaymentStatusModal({
  isOpen,
  isSuccess,
  message,
  onClose,
  autoRedirect = true,
  redirectDelay = 3000,
}: PaymentStatusModalProps) {
  const router = useRouter();

  useEffect(() => {
    if (isOpen && isSuccess && autoRedirect) {
      const timer = setTimeout(() => {
        onClose();
        router.push('/');
      }, redirectDelay);

      return () => clearTimeout(timer);
    }
  }, [isOpen, isSuccess, autoRedirect, redirectDelay, onClose, router]);

  const handleClose = () => {
    onClose();
    if (isSuccess) {
      router.push('/');
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            {isSuccess ? (
              <>
                <CheckCircle className="h-6 w-6 text-green-600" />
                Payment Successful
              </>
            ) : (
              <>
                <XCircle className="h-6 w-6 text-red-600" />
                Payment Failed
              </>
            )}
          </DialogTitle>
          <DialogDescription className="text-center py-4">
            {message}
          </DialogDescription>
        </DialogHeader>
        <div className="flex justify-center">
          <Button onClick={handleClose} className="w-full">
            {isSuccess ? 'Continue' : 'Try Again'}
          </Button>
        </div>
        {isSuccess && autoRedirect && (
          <p className="text-sm text-gray-500 text-center">
            Redirecting to home page in {redirectDelay / 1000} seconds...
          </p>
        )}
      </DialogContent>
    </Dialog>
  );
}