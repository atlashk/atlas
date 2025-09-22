'use client';

import { useState } from 'react';
import { useStripe, useElements, PaymentElement } from '@stripe/react-stripe-js';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Loader2, X } from 'lucide-react';
import { toast } from 'sonner';

interface StripePaymentFormProps {
  isOpen: boolean;
  onClose: () => void;
  clientSecret: string;
  orderId: string;
}

export default function StripePaymentForm({
  isOpen,
  onClose,
  clientSecret,
  orderId
}: StripePaymentFormProps) {
  const stripe = useStripe();
  const elements = useElements();
  const [isProcessing, setIsProcessing] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    if (!stripe || !elements) {
      toast.error('Stripe has not loaded yet. Please try again.');
      return;
    }

    setIsProcessing(true);

    try {
      const { error } = await stripe.confirmPayment({
        elements,
        confirmParams: {
          return_url: `${window.location.origin}/payment-success`,
        },
      });

      if (error) {
        console.error('Payment confirmation error:', error);
        
        if (error.type === 'card_error' || error.type === 'validation_error') {
          toast.error(error.message || 'Payment failed. Please check your card details.');
        } else {
          toast.error('An unexpected error occurred. Please try again.');
          // Redirect to failure page for unexpected errors
          window.location.href = `/payment-failed?payment_intent=${clientSecret.split('_secret_')[0]}`;
        }
      }
      // If no error, the user will be redirected to the return_url
    } catch (err) {
      console.error('Payment processing error:', err);
      toast.error('Payment processing failed. Please try again.');
      window.location.href = `/payment-failed?payment_intent=${clientSecret.split('_secret_')[0]}`;
    } finally {
      setIsProcessing(false);
    }
  };

  const handleClose = () => {
    if (!isProcessing) {
      onClose();
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center justify-between">
            Complete Payment
            {!isProcessing && (
              <Button
                variant="ghost"
                size="sm"
                onClick={handleClose}
                className="h-6 w-6 p-0"
              >
                <X className="h-4 w-4" />
              </Button>
            )}
          </DialogTitle>
        </DialogHeader>
        
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Order #{orderId}</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <PaymentElement 
                options={{
                  layout: 'tabs'
                }}
              />
              
              <div className="flex gap-3">
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleClose}
                  disabled={isProcessing}
                  className="flex-1"
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  disabled={!stripe || isProcessing}
                  className="flex-1"
                >
                  {isProcessing ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Processing...
                    </>
                  ) : (
                    'Pay Now'
                  )}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </DialogContent>
    </Dialog>
  );
}