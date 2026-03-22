import React from "react";
import { Loader2 } from "lucide-react";
import { cn } from "@/lib/utils";

interface LoadingStateProps {
  message?: string;
  size?: "sm" | "md" | "lg";
  variant?: "spinner" | "skeleton" | "pulse";
  className?: string;
  fullScreen?: boolean;
}

export const LoadingState = React.memo<LoadingStateProps>(
  ({
    message = "Loading...",
    size = "md",
    variant = "spinner",
    className,
    fullScreen = false,
  }) => {
    const sizeClasses = {
      sm: "h-4 w-4",
      md: "h-6 w-6",
      lg: "h-8 w-8",
    };

    const textSizeClasses = {
      sm: "text-sm",
      md: "text-base",
      lg: "text-lg",
    };

    const containerClasses = cn(
      "flex flex-col items-center justify-center gap-3",
      fullScreen && "min-h-screen",
      !fullScreen && "py-8",
      className,
    );

    if (variant === "skeleton") {
      return (
        <div className={containerClasses}>
          <div className="space-y-3 w-full max-w-sm">
            <div className="h-4 bg-muted rounded animate-pulse" />
            <div className="h-4 bg-muted rounded animate-pulse w-3/4" />
            <div className="h-4 bg-muted rounded animate-pulse w-1/2" />
          </div>
        </div>
      );
    }

    if (variant === "pulse") {
      return (
        <div className={containerClasses}>
          <div
            className={cn(
              "rounded-full bg-primary/20 animate-pulse",
              sizeClasses[size],
            )}
          />
          <p
            className={cn(
              "text-muted-foreground animate-pulse",
              textSizeClasses[size],
            )}
          >
            {message}
          </p>
        </div>
      );
    }

    return (
      <div className={containerClasses}>
        <Loader2
          className={cn("animate-spin text-primary", sizeClasses[size])}
        />
        <p className={cn("text-muted-foreground", textSizeClasses[size])}>
          {message}
        </p>
      </div>
    );
  },
);

LoadingState.displayName = "LoadingState";

// Inline loading spinner for buttons and small spaces
export const InlineLoader = React.memo<{
  size?: "sm" | "md";
  className?: string;
}>(({ size = "sm", className }) => {
  const sizeClasses = {
    sm: "h-3 w-3",
    md: "h-4 w-4",
  };

  return (
    <Loader2 className={cn("animate-spin", sizeClasses[size], className)} />
  );
});

InlineLoader.displayName = "InlineLoader";
