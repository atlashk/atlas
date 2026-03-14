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
import { API_BASE_URL } from "@/config/env.config";
import { LoginRequest } from "@/interfaces";
import { zodResolver } from "@hookform/resolvers/zod";
import { Eye, EyeOff, Loader2, Lock, Mail } from "lucide-react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import React, { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";
import { withGuestOnly } from "../../hoc/withAuth";
import { useUserStore } from "../../stores/user.store";

const formSchema = z.object({
  email: z.email({
    message: "Email is required.",
  }),
  password: z
    .string()
    .min(1, { message: "Password is required." }),
});

const Login: React.FC = () => {
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoggingIn, setIsLoggingIn] = useState(false);
  const [isProcessingSso, setIsProcessingSso] = useState(false);
  const [ssoHandled, setSsoHandled] = useState(false);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const { login, loginWithTokens } = useUserStore();
  const router = useRouter();
  const searchParams = useSearchParams();

  // Get redirect parameter from URL
  const getRedirectUrl = () => {
    return searchParams.get("redirect");
  };

  useEffect(() => {
    const accessToken = searchParams.get("accessToken");
    const refreshToken = searchParams.get("refreshToken");
    const ssoError = searchParams.get("ssoError");

    if (ssoError) {
      setErrorMessage("Google login failed. Please try again.");
      return;
    }

    if (!accessToken || !refreshToken || ssoHandled) {
      return;
    }

    const completeSsoLogin = async () => {
      setIsProcessingSso(true);
      setErrorMessage("");

      const response = await loginWithTokens(accessToken, refreshToken);

      if (response.success) {
        toast.success("Google login successful!");
        setSsoHandled(true);
        const redirectUrl = sessionStorage.getItem("auth_redirect");
        sessionStorage.removeItem("auth_redirect");
        router.push(redirectUrl || "/");
        return;
      }

      setSsoHandled(true);
      setErrorMessage(response.errorMessage || "Google login failed.");
      setIsProcessingSso(false);
    };

    void completeSsoLogin();
  }, [loginWithTokens, router, searchParams, ssoHandled]);

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

      const response = await login(credentials);

      if (response.success) {
        toast.success("Login successful!");

        // Add a small delay to ensure tokens are properly set before redirect
        await new Promise((resolve) => setTimeout(resolve, 100));

        // Check if there's a redirect URL from the middleware
        const redirectUrl = getRedirectUrl();

        if (redirectUrl) {
          // Use the redirect URL from middleware
          console.log("Redirecting to original destination:", redirectUrl);
          router.push(redirectUrl);
        } else {
          console.log("No redirect URL, letting useGuestRedirect handle redirect");
        }
      } else {
        const message = response.errorMessage || "Login failed. Please check your credentials.";
        setErrorMessage(message);
      }
    } catch {
      const message = "An unexpected error occurred. Please try again.";
      setErrorMessage(message);
    } finally {
      setIsLoggingIn(false);
    }
  };

  const onGoogleLogin = () => {
    const redirectUrl = getRedirectUrl();
    if (redirectUrl) {
      sessionStorage.setItem("auth_redirect", redirectUrl);
    }
    window.location.href = `${API_BASE_URL}/services/identity/oauth2/authorization/google`;
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
                          className={`pl-10 ${errorMessage ? "border-red-500 focus-visible:ring-red-500" : ""}`}
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
                          className={`pl-10 pr-10 ${errorMessage ? "border-red-500 focus-visible:ring-red-500" : ""}`}
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
                disabled={isLoggingIn || isProcessingSso}
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

              <Button
                type="button"
                variant="outline"
                className="w-full"
                size="lg"
                disabled={isLoggingIn || isProcessingSso}
                onClick={onGoogleLogin}
              >
                {isProcessingSso ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Processing Google Login...
                  </>
                ) : (
                  "Continue with Google"
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
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  );
};

export default withGuestOnly(Login);
