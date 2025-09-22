import React, { useEffect, useState } from 'react';
import { QrCode, Copy, CheckCircle } from 'lucide-react';

interface QRCodePaymentProps {
  content: string;
  onPaymentComplete?: () => void;
  onPaymentError?: (error: string) => void;
}

export const QRCodePayment: React.FC<QRCodePaymentProps> = ({
  content,
  onPaymentComplete,
  onPaymentError
}) => {
  const [copied, setCopied] = useState(false);
  const [qrCodeUrl, setQrCodeUrl] = useState<string>('');

  useEffect(() => {
    // If content is a Base64 string, create data URL
    if (content.startsWith('data:image/')) {
      setQrCodeUrl(content);
    } else if (content.startsWith('iVBORw0KGgo') || content.includes('base64')) {
      // Handle Base64 encoded image
      setQrCodeUrl(`data:image/png;base64,${content}`);
    } else {
      // If it's a URL or text, generate QR code using a service
      // For now, we'll use a simple QR code generator API
      setQrCodeUrl(`https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(content)}`);
    }
  }, [content]);

  const handleCopyContent = async () => {
    try {
      await navigator.clipboard.writeText(content);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (error) {
      console.error('Failed to copy content:', error);
      onPaymentError?.('Failed to copy QR code content');
    }
  };

  return (
    <div className="flex flex-col items-center space-y-4 p-6 bg-white rounded-lg shadow-sm border">
      <div className="flex items-center space-x-2 text-lg font-semibold text-gray-800">
        <QrCode className="w-6 h-6" />
        <span>Scan QR Code to Pay</span>
      </div>
      
      <div className="bg-gray-50 p-4 rounded-lg">
        {qrCodeUrl ? (
          <img 
            src={qrCodeUrl} 
            alt="Payment QR Code" 
            className="w-48 h-48 object-contain"
            onError={() => onPaymentError?.('Failed to load QR code')}
          />
        ) : (
          <div className="w-48 h-48 bg-gray-200 flex items-center justify-center rounded">
            <QrCode className="w-16 h-16 text-gray-400" />
          </div>
        )}
      </div>

      <div className="text-center space-y-2">
        <p className="text-sm text-gray-600">
          Use your mobile banking app or payment app to scan this QR code
        </p>
        
        <button
          onClick={handleCopyContent}
          className="inline-flex items-center space-x-2 px-3 py-2 text-sm bg-gray-100 hover:bg-gray-200 rounded-md transition-colors"
        >
          {copied ? (
            <>
              <CheckCircle className="w-4 h-4 text-green-600" />
              <span className="text-green-600">Copied!</span>
            </>
          ) : (
            <>
              <Copy className="w-4 h-4" />
              <span>Copy QR Content</span>
            </>
          )}
        </button>
      </div>

      <div className="text-xs text-gray-500 text-center max-w-md">
        After completing the payment on your mobile device, this page will automatically update.
      </div>
    </div>
  );
};

export default QRCodePayment;