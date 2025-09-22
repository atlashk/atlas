import React, { useEffect, useState } from 'react';
import { Smartphone, Copy, CheckCircle, AlertCircle, ArrowRight } from 'lucide-react';

interface DeepLinkPaymentProps {
  url: string;
  onPaymentComplete?: () => void;
  onPaymentError?: (error: string) => void;
  autoLaunch?: boolean;
}

export const DeepLinkPayment: React.FC<DeepLinkPaymentProps> = ({
  url,
  onPaymentComplete,
  onPaymentError,
  autoLaunch = false
}) => {
  const [copied, setCopied] = useState(false);
  const [isLaunching, setIsLaunching] = useState(false);
  const [countdown, setCountdown] = useState(3);
  const [appDetected, setAppDetected] = useState<boolean | null>(null);

  useEffect(() => {
    if (autoLaunch && url) {
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            handleLaunchApp();
            return 0;
          }
          return prev - 1;
        });
      }, 1000);

      return () => clearInterval(timer);
    }
  }, [autoLaunch, url]);

  const detectMobileApp = (deepLinkUrl: string) => {
    return new Promise<boolean>((resolve) => {
      const startTime = Date.now();
      const timeout = 2000; // 2 seconds timeout
      
      // Create a hidden iframe to attempt the deep link
      const iframe = document.createElement('iframe');
      iframe.style.display = 'none';
      iframe.src = deepLinkUrl;
      document.body.appendChild(iframe);

      // Set up timeout to detect if app opened
      const timer = setTimeout(() => {
        const timeElapsed = Date.now() - startTime;
        // If less than timeout elapsed, likely app opened
        resolve(timeElapsed < timeout - 100);
        document.body.removeChild(iframe);
      }, timeout);

      // Listen for page visibility change (app switch)
      const handleVisibilityChange = () => {
        if (document.hidden) {
          clearTimeout(timer);
          resolve(true);
          document.body.removeChild(iframe);
          document.removeEventListener('visibilitychange', handleVisibilityChange);
        }
      };

      document.addEventListener('visibilitychange', handleVisibilityChange);
    });
  };

  const handleLaunchApp = async () => {
    if (!url) {
      onPaymentError?.('Invalid deep link URL');
      return;
    }

    try {
      setIsLaunching(true);
      
      // Attempt to launch the app
      const appOpened = await detectMobileApp(url);
      setAppDetected(appOpened);
      
      if (appOpened) {
        // App likely opened, start monitoring for return
        setTimeout(() => {
          // Assume payment completed when user returns after some time
          onPaymentComplete?.();
        }, 5000);
      } else {
        // App not detected, show manual options
        setIsLaunching(false);
      }
    } catch (error) {
      console.error('Failed to launch app:', error);
      onPaymentError?.('Failed to launch payment app');
      setIsLaunching(false);
    }
  };

  const handleCopyLink = async () => {
    try {
      await navigator.clipboard.writeText(url);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (error) {
      console.error('Failed to copy link:', error);
      onPaymentError?.('Failed to copy deep link');
    }
  };

  const handleManualLaunch = () => {
    window.location.href = url;
  };

  if (!url) {
    return (
      <div className="flex flex-col items-center space-y-4 p-6 bg-red-50 rounded-lg border border-red-200">
        <AlertCircle className="w-12 h-12 text-red-500" />
        <div className="text-center">
          <h3 className="text-lg font-semibold text-red-800">Invalid Deep Link</h3>
          <p className="text-sm text-red-600 mt-1">
            No valid deep link URL was provided for payment processing.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center space-y-6 p-6 bg-white rounded-lg shadow-sm border">
      <div className="flex items-center space-x-2 text-lg font-semibold text-gray-800">
        <Smartphone className="w-6 h-6" />
        <span>Open Payment App</span>
      </div>

      <div className="text-center space-y-2">
        <p className="text-gray-600">
          Complete your payment using your mobile banking or payment app.
        </p>
        
        {autoLaunch && countdown > 0 && (
          <p className="text-sm text-blue-600">
            Launching app automatically in {countdown} seconds...
          </p>
        )}

        {appDetected === false && (
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3 mt-4">
            <p className="text-sm text-yellow-800">
              App not detected. Please ensure you have the payment app installed.
            </p>
          </div>
        )}
      </div>

      <div className="flex flex-col items-center space-y-4">
        <button
          onClick={handleLaunchApp}
          disabled={isLaunching}
          className="inline-flex items-center space-x-2 px-6 py-3 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-medium rounded-lg transition-colors"
        >
          {isLaunching ? (
            <>
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
              <span>Launching App...</span>
            </>
          ) : (
            <>
              <Smartphone className="w-4 h-4" />
              <span>Open Payment App</span>
            </>
          )}
        </button>

        <div className="flex space-x-2">
          <button
            onClick={handleCopyLink}
            className="inline-flex items-center space-x-2 px-4 py-2 text-sm bg-gray-100 hover:bg-gray-200 rounded-md transition-colors"
          >
            {copied ? (
              <>
                <CheckCircle className="w-4 h-4 text-green-600" />
                <span className="text-green-600">Copied!</span>
              </>
            ) : (
              <>
                <Copy className="w-4 h-4" />
                <span>Copy Link</span>
              </>
            )}
          </button>

          <button
            onClick={handleManualLaunch}
            className="inline-flex items-center space-x-2 px-4 py-2 text-sm bg-gray-100 hover:bg-gray-200 rounded-md transition-colors"
          >
            <ArrowRight className="w-4 h-4" />
            <span>Manual Launch</span>
          </button>
        </div>
      </div>

      <div className="text-center space-y-2">
        <div className="text-xs text-gray-500 max-w-md">
          <p>If the app doesn't open automatically:</p>
          <ul className="list-disc list-inside mt-1 space-y-1">
            <li>Make sure you have the payment app installed</li>
            <li>Try copying the link and opening it manually</li>
            <li>Check if your browser allows app launches</li>
          </ul>
        </div>
      </div>

      <div className="w-full max-w-md">
        <div className="bg-gray-50 p-3 rounded border text-xs text-gray-600 break-all">
          <strong>Deep Link:</strong> {url}
        </div>
      </div>
    </div>
  );
};

export default DeepLinkPayment;