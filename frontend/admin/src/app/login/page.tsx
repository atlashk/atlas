"use client";

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { AUTHORIZATION_API_BASE_URL } from "@/config/env.config";
import { LoginRequest } from "@/interfaces/authorization.interface";
import { zodResolver } from "@hookform/resolvers/zod";
import { Eye, EyeOff, Loader2, Lock, Mail } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";
import { withGuestOnly } from "../../hoc/withAuth";
import { useUserStore } from "../../stores/user.store";
import {
  createOAuth2AuthorizationUrl,
  executeJwtLoginFlow,
  resolveAuthorizationBaseUrl,
} from "./login.flows";

const formSchema = z.object({
  email: z.email({
    message: "Email is required.",
  }),
  password: z
    .string()
    .min(1, { message: "Password is required." }),
});

const OAUTH2_CLIENT_ID = "web-client";
const OAUTH2_PKCE_VERIFIER_STORAGE_KEY = "oauth2_pkce_verifier_web";
const OAUTH2_STATE_STORAGE_KEY = "oauth2_state_web";

const Login: React.FC = () => {
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoggingIn, setIsLoggingIn] = useState(false);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const { login } = useUserStore();
  const router = useRouter();

  // Get redirect parameter from URL
  const getRedirectUrl = () => {
    if (typeof window !== "undefined") {
      const urlParams = new URLSearchParams(window.location.search);
      return urlParams.get("redirect") || null;
    }
    return null;
  };

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = async (values: z.infer<typeof formSchema>) => {
    try {
      setIsLoggingIn(true);
      setErrorMessage("");

      const credentials: LoginRequest = {
        email: values.email,
        password: values.password,
      };

      const result = await executeJwtLoginFlow(login, credentials, getRedirectUrl);
      if (result.success) {
        toast.success("Login successful!");
        await new Promise((resolve) => setTimeout(resolve, 100));
        if (result.redirectUrl) {
          router.push(result.redirectUrl);
        }
      } else {
        setErrorMessage(result.errorMessage);
      }
    } catch {
      const message = "An unexpected error occurred. Please try again.";
      setErrorMessage(message);
    } finally {
      setIsLoggingIn(false);
    }
  };

  const persistRedirectUrl = () => {
    const redirectUrl = getRedirectUrl();
    if (redirectUrl) {
      sessionStorage.setItem("auth_redirect", redirectUrl);
    }
  };

  const onOAuth2Login = async () => {
    persistRedirectUrl();
    const oauthAuthorizationBaseUrl = resolveAuthorizationBaseUrl(
      AUTHORIZATION_API_BASE_URL
    );
    const redirectUri = `${window.location.origin}/login/callback`;
    const authorizeUrl = await createOAuth2AuthorizationUrl({
      authorizationBaseUrl: oauthAuthorizationBaseUrl,
      clientId: OAUTH2_CLIENT_ID,
      scope: "openid profile email phone offline_access",
      redirectUri,
      pkceVerifierStorageKey: OAUTH2_PKCE_VERIFIER_STORAGE_KEY,
      stateStorageKey: OAUTH2_STATE_STORAGE_KEY,
    });
    window.location.href = authorizeUrl;
  };

  return (
    <div className="h-screen flex items-start justify-center pt-40">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center space-y-2 pb-4">
          <div>
            <CardTitle className="text-2xl font-bold text-primary">
              Welcome Back
            </CardTitle>
            <p className="text-muted-foreground mt-2">
              Sign in to your account
            </p>
          </div>
        </CardHeader>

        <CardContent className="space-y-6">
          {errorMessage && (
            <Alert variant="destructive">
              <AlertDescription>{errorMessage}</AlertDescription>
            </Alert>
          )}

          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="email"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Email</FormLabel>
                    <FormControl>
                      <div className="relative">
                        <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                        <Input
                          {...field}
                          type="email"
                          className="pl-10"
                          placeholder="Enter your email"
                        />
                      </div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <div className="flex justify-between items-center">
                      <FormLabel>Password</FormLabel>
                    </div>
                    <FormControl>
                      <div className="relative">
                        <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                        <Input
                          {...field}
                          type={isPasswordVisible ? "text" : "password"}
                          className="pl-10 pr-10"
                          placeholder="Enter your password"
                        />
                        <Button
                          type="button"
                          variant="ghost"
                          size="icon-xs"
                          className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                          aria-label={
                            isPasswordVisible ? "Hide password" : "Show password"
                          }
                          onClick={() =>
                            setIsPasswordVisible((current) => !current)
                          }
                        >
                          {isPasswordVisible ? (
                            <EyeOff className="size-4" />
                          ) : (
                            <Eye className="size-4" />
                          )}
                        </Button>
                      </div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <Button
                type="submit"
                className="w-full"
                size="lg"
                disabled={isLoggingIn}
              >
                {isLoggingIn ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Signing In...
                  </>
                ) : (
                  "Sign In"
                )}
              </Button>

              <div className="text-center">
                <p className="text-sm text-muted-foreground">
                  Don&apos;t have an account?{" "}
                  <Link
                    href="/register"
                    className="text-primary hover:underline font-medium"
                  >
                    Sign up
                  </Link>
                </p>
              </div>

              <div className="relative py-1">
                <div className="absolute inset-0 flex items-center">
                  <span className="w-full border-t" />
                </div>
              </div>

              <Button
                type="button"
                variant="outline"
                className="w-full"
                size="lg"
                disabled={isLoggingIn}
                onClick={onOAuth2Login}
              >
                OAuth2 Login
              </Button>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  );
};

export default withGuestOnly(Login);
