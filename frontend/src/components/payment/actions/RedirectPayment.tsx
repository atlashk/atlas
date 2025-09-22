import React, { useEffect, useState } from 'react';
import { ExternalLink, ArrowRight, AlertCircle } from 'lucide-react';

interface RedirectPaymentProps {
  url: string;
  onPaymentComplete?: () => void;
  onPaymentError?: (error: string) => void;
  autoRedirect?: boolean;
}

export const RedirectPayment: React.FC<RedirectPaymentProps> = ({
  url,
  onPaymentComplete,
  onPaymentError,
  autoRedirect = false
}) => {
  const [isRedirecting, setIsRedirecting] = useState(false);
  const [countdown, setCountdown] = useState(5);

  useEffect(() => {
    if (autoRedirect && url) {
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            handleRedirect();
            return 0;
          }
          return prev - 1;
        });
      }, 1000);

      return () => clearInterval(timer);
    }
  }, [autoRedirect, url]);

  const handleRedirect = () => {
    if (!url) {
      onPaymentError?.('Invalid redirect URL');
      return;
    }

    try {
      setIsRedirecting(true);
      
      // Check if URL is valid
      new URL(url);
      
      // Open in new window/tab for better UX
      const newWindow = window.open(url, '_blank', 'noopener,noreferrer');
      
      if (!newWindow) {
        // Fallback to same window if popup blocked
        window.location.href = url;
      } else {
        // Monitor the popup window
        const checkClosed = setInterval(() => {
          if (newWindow.closed) {
            clearInterval(checkClosed);
            setIsRedirecting(false);
            // Assume payment completed when user returns
            onPaymentComplete?.();
          }
        }, 1000);
      }
    } catch (error) {
      console.error('Invalid redirect URL:', error);
      onPaymentError?.('Invalid redirect URL provided');
      setIsRedirecting(false);
    }
  };

  const handleManualRedirect = () => {
    handleRedirect();
  };

  if (!url) {
    return (
      <div className="flex flex-col items-center space-y-4 p-6 bg-red-50 rounded-lg border border-red-200">
        <AlertCircle className="w-12 h-12 text-red-500" />
        <div className="text-center">
          <h3 className="text-lg font-semibold text-red-800">Invalid Payment URL</h3>
          <p className="text-sm text-red-600 mt-1">
            No valid redirect URL was provided for payment processing.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center space-y-6 p-6 bg-white rounded-lg shadow-sm border">
      <div className="flex items-center space-x-2 text-lg font-semibold text-gray-800">
        <ExternalLink className="w-6 h-6" />
        <span>Complete Payment</span>
      </div>

      <div className="text-center space-y-2">
        <p className="text-gray-600">
          You will be redirected to complete your payment securely.
        </p>
        
        {autoRedirect && countdown > 0 && (
          <p className="text-sm text-blue-600">
            Redirecting automatically in {countdown} seconds...
          </p>
        )}
      </div>

      <div className="flex flex-col items-center space-y-4">
        <button
          onClick={handleManualRedirect}
          disabled={isRedirecting}
          className="inline-flex items-center space-x-2 px-6 py-3 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-medium rounded-lg transition-colors"
        >
          {isRedirecting ? (
            <>
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
              <span>Redirecting...</span>
            </>
          ) : (
            <>
              <span>Continue to Payment</span>
              <ArrowRight className="w-4 h-4" />
            </>
          )}
        </button>

        <div className="text-xs text-gray-500 text-center max-w-md">
          <p>You will be taken to a secure payment page.</p>
          <p className="mt-1">
            If the popup is blocked, you may need to allow popups for this site or the page will redirect automatically.
          </p>
        </div>
      </div>

      <div className="w-full max-w-md">
        <div className="bg-gray-50 p-3 rounded border text-xs text-gray-600 break-all">
          <strong>Redirect URL:</strong> {url}
        </div>
      </div>
    </div>
  );
};

export default RedirectPayment;