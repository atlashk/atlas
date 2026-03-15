"use client";

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useUserStore } from "@/stores/user.store";
import { useRouter, useSearchParams } from "next/navigation";
import React, { useEffect, useRef, useState } from "react";
import { toast } from "sonner";

const LoginCallback: React.FC = () => {
  const searchParams = useSearchParams();
  const ssoError = searchParams.get("ssoError");
  const accessToken = searchParams.get("accessToken");
  const refreshToken = searchParams.get("refreshToken");
  const initialErrorMessage = ssoError
    ? "Google login failed. Please try again."
    : (!accessToken || !refreshToken)
      ? "Missing SSO tokens. Please try login again."
      : "";

  const [errorMessage, setErrorMessage] = useState(initialErrorMessage);
  const [isProcessing, setIsProcessing] = useState(!initialErrorMessage);
  const hasProcessedRef = useRef(false);
  const { loginWithTokens } = useUserStore();
  const router = useRouter();

  useEffect(() => {
    if (hasProcessedRef.current) {
      return;
    }
    hasProcessedRef.current = true;

    if (initialErrorMessage) {
      return;
    }
    if (!accessToken || !refreshToken) {
      return;
    }

    const completeSsoLogin = async () => {
      const response = await loginWithTokens(accessToken, refreshToken);

      if (response.success) {
        toast.success("Google login successful!");
        const redirectUrl = sessionStorage.getItem("auth_redirect");
        sessionStorage.removeItem("auth_redirect");
        router.push(redirectUrl || "/");
        return;
      }

      setErrorMessage(response.errorMessage || "Google login failed.");
      setIsProcessing(false);
    };

    void completeSsoLogin();
  }, [accessToken, initialErrorMessage, loginWithTokens, refreshToken, router]);

  return (
    <div className="flex items-center justify-center min-h-screen px-4">
      <div className="w-full max-w-md space-y-4">
        {isProcessing ? (
          <div className="text-center space-y-3">
            <Spinner className="mx-auto text-blue-600" />
            <p className="text-sm text-muted-foreground">
              Processing Google login...
            </p>
          </div>
        ) : (
          <>
            <Alert variant="destructive">
              <AlertDescription>{errorMessage}</AlertDescription>
            </Alert>
            <Button className="w-full" onClick={() => router.push("/login")}>
              Back to Login
            </Button>
          </>
        )}
      </div>
    </div>
  );
};

export default LoginCallback;
