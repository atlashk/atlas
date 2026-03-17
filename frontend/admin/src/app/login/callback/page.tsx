"use client";

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { AUTHORIZATION_API_BASE_URL } from "@/config/env.config";
import { useUserStore } from "@/stores/user.store";
import { useRouter, useSearchParams } from "next/navigation";
import React, { useEffect, useRef, useState } from "react";
import { toast } from "sonner";
import {
  clearPkceState,
  exchangeAuthorizationCodeForTokens,
  executeOauth2LoginFlow,
  resolveAuthorizationBaseUrl,
  resolveInitialOauth2ErrorMessage,
  resolvePkceVerifier,
} from "../login.flows";

const OAUTH2_CLIENT_ID = "admin-oidc-client";
const OAUTH2_PKCE_VERIFIER_STORAGE_KEY = "oauth2_pkce_verifier_admin";
const OAUTH2_STATE_STORAGE_KEY = "oauth2_state_admin";

const LoginCallback: React.FC = () => {
  const searchParams = useSearchParams();
  const code = searchParams.get("code");
  const state = searchParams.get("state");
  const oauth2Error = searchParams.get("error");
  const initialErrorMessage = resolveInitialOauth2ErrorMessage(oauth2Error, code);

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
    const completeOauth2Login = async () => {
      if (!code || !state) {
        setErrorMessage("Missing OAuth2 authorization response. Please try login again.");
        setIsProcessing(false);
        return;
      }
      const oauthAuthorizationBaseUrl = resolveAuthorizationBaseUrl(
        AUTHORIZATION_API_BASE_URL
      );
      const codeVerifier = resolvePkceVerifier(
        state,
        OAUTH2_STATE_STORAGE_KEY,
        OAUTH2_PKCE_VERIFIER_STORAGE_KEY
      );
      if (!codeVerifier) {
        setErrorMessage("Invalid OAuth2 state. Please try login again.");
        setIsProcessing(false);
        return;
      }
      const redirectUri = `${window.location.origin}/login/callback`;
      let accessToken: string | null = null;
      let refreshToken: string | null = null;
      try {
        const exchangedTokens = await exchangeAuthorizationCodeForTokens({
          authorizationBaseUrl: oauthAuthorizationBaseUrl,
          clientId: OAUTH2_CLIENT_ID,
          code,
          redirectUri,
          codeVerifier,
        });
        accessToken = exchangedTokens.accessToken || null;
        refreshToken = exchangedTokens.refreshToken || null;
      } catch (error) {
        setErrorMessage(
          error instanceof Error
            ? error.message
            : "OAuth2 token exchange failed."
        );
        setIsProcessing(false);
        return;
      }
      if (!accessToken) {
        setErrorMessage("Missing OAuth2 access token. Please try login again.");
        setIsProcessing(false);
        return;
      }
      const result = await executeOauth2LoginFlow(
        loginWithTokens,
        accessToken,
        refreshToken
      );
      if (!result.success) {
        setErrorMessage(result.errorMessage);
        setIsProcessing(false);
        return;
      }
      toast.success("OAuth2 login successful!");
      clearPkceState(
        OAUTH2_STATE_STORAGE_KEY,
        OAUTH2_PKCE_VERIFIER_STORAGE_KEY
      );
      const redirectUrl = sessionStorage.getItem("auth_redirect");
      sessionStorage.removeItem("auth_redirect");
      router.push(redirectUrl || "/");
    };

    void completeOauth2Login();
  }, [code, initialErrorMessage, loginWithTokens, oauth2Error, router, state]);

  return (
    <div className="flex items-center justify-center min-h-screen px-4">
      <div className="w-full max-w-md space-y-4">
        {isProcessing ? (
          <div className="text-center space-y-3">
            <Spinner className="mx-auto text-blue-600" />
            <p className="text-sm text-muted-foreground">
              Processing OAuth2 login...
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
