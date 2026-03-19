 "use client";
 
 import { Alert, AlertDescription } from "@/components/ui/alert";
 import { Button } from "@/components/ui/button";
 import { Spinner } from "@/components/ui/spinner";
 import { initKeycloakOnCallback } from "@/lib/keycloak";
 import { useUserStore } from "@/stores/user.store";
 import { useRouter, useSearchParams } from "next/navigation";
 import React, { useEffect, useRef, useState } from "react";
 import { toast } from "sonner";
 import {
   clearPkceState,
   executeOauth2LoginFlow,
 } from "../../login.flows";
 
 const OAUTH2_PKCE_VERIFIER_STORAGE_KEY = "oauth2_pkce_verifier_admin";
 const OAUTH2_STATE_STORAGE_KEY = "oauth2_state_admin";
 
 const resolveInitialSsoErrorMessage = (args: {
   providerLabel: string;
   ssoError: string | null;
   oauth2Error: string | null;
   accessToken: string | null;
   refreshToken: string | null;
   code: string | null;
 }) => {
   if (args.ssoError || args.oauth2Error) {
     return `${args.providerLabel} login failed. Please try again.`;
   }
   if ((!args.accessToken || !args.refreshToken) && !args.code) {
     return "Missing SSO tokens. Please try login again.";
   }
   return "";
 };
 
 const KeycloakCallback: React.FC = () => {
   const searchParams = useSearchParams();
   const ssoError = searchParams.get("ssoError");
   const oauth2Error = searchParams.get("error");
   const accessToken = searchParams.get("accessToken");
   const refreshToken = searchParams.get("refreshToken");
 
   const providerLabel = "Keycloak";
   const initialErrorMessage = resolveInitialSsoErrorMessage({
     providerLabel,
     ssoError,
     oauth2Error,
     accessToken,
     refreshToken,
     code: searchParams.get("code"),
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
 
     const completeKeycloakLogin = async () => {
       let resolvedAccessToken = accessToken;
       let resolvedRefreshToken = refreshToken;
 
       if (!resolvedAccessToken) {
         try {
           const keycloakTokens = await initKeycloakOnCallback();
           if (keycloakTokens) {
             resolvedAccessToken = keycloakTokens.accessToken;
             resolvedRefreshToken = keycloakTokens.refreshToken;
           }
         } catch (error) {
           console.error("Keycloak callback error:", error);
           setErrorMessage("Keycloak token initialization failed.");
           setIsProcessing(false);
           return;
         }
       }
 
       if (!resolvedAccessToken) {
         setErrorMessage("Missing Keycloak access token. Please try login again.");
         setIsProcessing(false);
         return;
       }
 
       const result = await executeOauth2LoginFlow(
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
 
     void completeKeycloakLogin();
   }, [accessToken, initialErrorMessage, loginWithTokens, refreshToken, router]);
 
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
 
 export default KeycloakCallback;
