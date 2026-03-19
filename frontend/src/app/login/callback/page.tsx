"use client";

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { AUTHORIZATION_API_BASE_URL, KEYCLOAK_CLIENT_ID } from "@/config/env.config";
import { useUserStore } from "@/stores/user.store";
import { useRouter, useSearchParams } from "next/navigation";
import React, { useEffect, useRef, useState } from "react";
import { toast } from "sonner";
import {
  clearPkceState,
  exchangeAuthorizationCodeForTokens,
  executeOAuth2LoginFlow,
  resolveAuthorizationBaseUrl,
  resolveInitialSsoErrorMessage,
  resolvePkceVerifier,
  resolveProviderLabel,
} from "../login.flows";

const OAUTH2_CLIENT_ID = KEYCLOAK_CLIENT_ID;
const OAUTH2_PKCE_VERIFIER_STORAGE_KEY = "oauth2_pkce_verifier_storefront";
const OAUTH2_STATE_STORAGE_KEY = "oauth2_state_storefront";

const LoginCallback: React.FC = () => {
  const searchParams = useSearchParams();
  const provider = searchParams.get("provider");
  const code = searchParams.get("code");
  const state = searchParams.get("state");
  const ssoError = searchParams.get("ssoError");
  const oauth2Error = searchParams.get("error");
  const accessToken = searchParams.get("accessToken");
  const refreshToken = searchParams.get("refreshToken");
  
  const providerLabel = resolveProviderLabel(provider);
  const initialErrorMessage = resolveInitialSsoErrorMessage({
    providerLabel,
    ssoError,
    oauth2Error,
    accessToken,
    refreshToken,
    code,
  });

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
    const completeSsoLogin = async () => {
      const oauthAuthorizationBaseUrl = resolveAuthorizationBaseUrl(
        AUTHORIZATION_API_BASE_URL
      );

      let resolvedAccessToken = accessToken;
      let resolvedRefreshToken = refreshToken;

      if (!resolvedAccessToken) {
        if (!code || !state) {
          setErrorMessage("Missing OAuth2 authorization response. Please try login again.");
          setIsProcessing(false);
          return;
        }
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
        try {
          const exchangedTokens = await exchangeAuthorizationCodeForTokens({
            authorizationBaseUrl: oauthAuthorizationBaseUrl,
            clientId: OAUTH2_CLIENT_ID,
            code,
            redirectUri,
            codeVerifier,
          });
          resolvedAccessToken = exchangedTokens.accessToken || null;
          resolvedRefreshToken = exchangedTokens.refreshToken || null;
        } catch (error) {
          setErrorMessage(
            error instanceof Error
              ? error.message
              : "OAuth2 token exchange failed."
          );
          setIsProcessing(false);
          return;
        }
      }

      if (!resolvedAccessToken) {
        setErrorMessage("Missing OAuth2 access token. Please try login again.");
        setIsProcessing(false);
        return;
      }

      const result = await executeOAuth2LoginFlow(
        loginWithTokens,
        resolvedAccessToken,
        resolvedRefreshToken
      );
      if (result.success) {
        toast.success(`${providerLabel} login successful!`);
        clearPkceState(
          OAUTH2_STATE_STORAGE_KEY,
          OAUTH2_PKCE_VERIFIER_STORAGE_KEY
        );
        const redirectUrl = sessionStorage.getItem("auth_redirect");
        sessionStorage.removeItem("auth_redirect");
        router.push(redirectUrl || "/");
        return;
      }

      setErrorMessage(result.errorMessage);
      setIsProcessing(false);
    };

    void completeSsoLogin();
  }, [accessToken, code, initialErrorMessage, loginWithTokens, oauth2Error, providerLabel, refreshToken, router, state]);

  return (
    <div className="flex items-center justify-center min-h-screen px-4">
      <div className="w-full max-w-md space-y-4">
        {isProcessing ? (
          <div className="text-center space-y-3">
            <Spinner className="mx-auto text-blue-600" />
            <p className="text-sm text-muted-foreground">
              {`Processing ${providerLabel} login...`}
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
