"use client";

interface LoadingScreenProps {
  message?: string;
  fullScreen?: boolean;
}

export function LoadingScreen({ 
  message = "Loading...", 
  fullScreen = true 
}: LoadingScreenProps) {
  const containerClass = fullScreen 
    ? "flex items-center justify-center min-h-screen" 
    : "flex items-center justify-center py-12";

  return (
    <div className={containerClass}>
      <div className="text-center">
        <div className="relative">
          {/* Outer spinning ring */}
          <div className="animate-spin rounded-full h-16 w-16 border-4 border-gray-200 border-t-blue-600 mx-auto"></div>
          
          {/* Inner pulsing dot */}
          <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2">
            <div className="h-4 w-4 bg-blue-600 rounded-full animate-pulse"></div>
          </div>
        </div>
        
        <p className="mt-4 text-gray-600 font-medium">{message}</p>
        
        {/* Optional: Add subtle animation to the text */}
        <div className="mt-2 flex justify-center space-x-1">
          <div className="h-1 w-1 bg-blue-600 rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></div>
          <div className="h-1 w-1 bg-blue-600 rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></div>
          <div className="h-1 w-1 bg-blue-600 rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></div>
        </div>
      </div>
    </div>
  );
}

export default LoadingScreen;
