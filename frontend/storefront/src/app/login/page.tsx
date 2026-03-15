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
import React, { useState } from "react";
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
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const { login } = useUserStore();
  const router = useRouter();
  const searchParams = useSearchParams();

  // Get redirect parameter from URL
  const getRedirectUrl = () => {
    return searchParams.get("redirect");
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
    const oauthGatewayBaseUrl = (() => {
      try {
        const url = new URL(API_BASE_URL);
        if (
          url.protocol === "http:" &&
          (url.hostname === "127.0.0.1" || url.hostname === "0.0.0.0")
        ) {
          url.hostname = "localhost";
        }
        return url.toString().replace(/\/$/, "");
      } catch {
        return API_BASE_URL.replace(/\/$/, "");
      }
    })();
    window.location.href = `${oauthGatewayBaseUrl}/services/identity/oauth2/authorization/google`;
  };

  const GoogleLogo = () => (
    <svg
      aria-hidden="true"
      className="mr-2 h-4 w-4"
      viewBox="0 0 24 24"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        d="M21.35 11.1H12v2.98h5.35a4.58 4.58 0 0 1-1.98 3.01v2.5h3.2c1.87-1.72 2.93-4.25 2.93-7.24 0-.41-.05-.83-.15-1.25Z"
        fill="#4285F4"
      />
      <path
        d="M12 22a9.76 9.76 0 0 0 6.57-2.41l-3.2-2.5a5.97 5.97 0 0 1-8.88-3.14H3.2v2.58A10 10 0 0 0 12 22Z"
        fill="#34A853"
      />
      <path
        d="M6.49 13.95a5.98 5.98 0 0 1 0-3.9V7.47H3.2a10 10 0 0 0 0 9.06l3.29-2.58Z"
        fill="#FBBC05"
      />
      <path
        d="M12 6.03c1.68 0 3.2.58 4.39 1.72l2.74-2.74A9.98 9.98 0 0 0 3.2 7.47l3.29 2.58A5.97 5.97 0 0 1 12 6.03Z"
        fill="#EA4335"
      />
    </svg>
  );

  return (
    <div className="h-screen flex items-start justify-center pt-30">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center space-y-2">
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
                onClick={onGoogleLogin}
              >
                <>
                  <GoogleLogo />
                  Continue with Google
                </>
              </Button>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  );
};

export default withGuestOnly(Login);
