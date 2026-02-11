"use client";

import { Badge } from "@/components/ui/badge";
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { zodResolver } from "@hookform/resolvers/zod";
import { AlertCircle, CreditCard, Lock, Shield } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";

// Validation schema
const paymentSchema = z.object({
  cardNumber: z
    .string()
    .min(1, "Card number is required")
    .regex(/^\d{4}\s?\d{4}\s?\d{4}\s?\d{4}$/, "Please enter a valid card number"),
  expiryMonth: z.string().min(1, "Expiry month is required"),
  expiryYear: z.string().min(1, "Expiry year is required"),
  cvv: z
    .string()
    .min(3, "CVV must be at least 3 digits")
    .max(4, "CVV must be at most 4 digits")
    .regex(/^\d+$/, "CVV must contain only numbers"),
  cardholderName: z
    .string()
    .min(1, "Cardholder name is required")
    .min(2, "Name must be at least 2 characters"),
});

type PaymentFormData = z.infer<typeof paymentSchema>;

interface PaymentSimulatorFormProps {
  amount?: number;
  currency?: string;
  onSubmit?: (data: PaymentFormData) => void;
  onError?: (error: string) => void;
  onCancel?: () => void;
  isLoading?: boolean;
}

// Card type detection
const getCardType = (cardNumber: string): string => {
  const number = cardNumber.replace(/\s/g, "");
  if (/^4/.test(number)) return "visa";
  if (/^5[1-5]/.test(number)) return "mastercard";
  if (/^3[47]/.test(number)) return "amex";
  if (/^6/.test(number)) return "discover";
  return "unknown";
};

// Format card number with spaces
const formatCardNumber = (value: string): string => {
  const v = value.replace(/\s+/g, "").replace(/[^0-9]/gi, "");
  const matches = v.match(/\d{4,16}/g);
  const match = (matches && matches[0]) || "";
  const parts = [];

  for (let i = 0, len = match.length; i < len; i += 4) {
    parts.push(match.substring(i, i + 4));
  }

  if (parts.length) {
    return parts.join(" ");
  } else {
    return v;
  }
};

// Generate month and year options
const months = Array.from({ length: 12 }, (_, i) => {
  const month = (i + 1).toString().padStart(2, "0");
  return { value: month, label: month };
});

const currentYear = new Date().getFullYear();
const years = Array.from({ length: 20 }, (_, i) => {
  const year = (currentYear + i).toString();
  return { value: year, label: year };
});

export function PaymentSimulatorForm({
  amount = 99.99,
  currency = "USD",
  onSubmit,
  onError,
  onCancel,
  isLoading = false,
}: PaymentSimulatorFormProps) {
  const [paymentStatus, setPaymentStatus] = useState<"idle" | "processing" | "error">("idle");
  const [errorMessage, setErrorMessage] = useState<string>("");

  const form = useForm<PaymentFormData>({
    resolver: zodResolver(paymentSchema),
    defaultValues: {
      cardNumber: "4532 1234 5678 9012",
      expiryMonth: "12",
      expiryYear: "2025",
      cvv: "123",
      cardholderName: "John Doe",
    },
  });

  const cardNumber = form.watch("cardNumber");
  const cardType = getCardType(cardNumber);

  const handleSubmit = async (data: PaymentFormData) => {
    setPaymentStatus("processing");
    setErrorMessage("");

    try {
      // Call onSubmit callback immediately for any immediate processing needs
      onSubmit?.(data);
      
      // Simulate payment processing delay
      await new Promise((resolve) => setTimeout(resolve, 2000));

      // Do NOT call onSuccess here - success should be determined by order status polling
      // The payment status remains "processing" until the order status polling determines success
      // onSuccess will be called by the parent component when order status becomes FULFILLED
    } catch (err) {
      setPaymentStatus("error");
      const errorMessage = "An unexpected error occurred. Please try again.";
      setErrorMessage(errorMessage);
      onError?.(errorMessage);
    }
  };

  const handleCardNumberChange = (value: string) => {
    const formatted = formatCardNumber(value);
    form.setValue("cardNumber", formatted);
  };

  // Success state is now handled by the parent component based on order status polling
  // No local success state needed here

  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader className="space-y-1">
        <CardTitle className="flex items-center gap-2">
          <CreditCard className="w-5 h-5" />
          Payment Simulator
        </CardTitle>
        <div className="flex items-center justify-between">
          <span className="text-2xl font-bold">
            {currency} {amount.toFixed(2)}
          </span>
          <Badge variant="outline" className="flex items-center gap-1">
            <Shield className="w-3 h-3" />
            Secure
          </Badge>
        </div>
      </CardHeader>

      <CardContent className="space-y-6">
        {paymentStatus === "error" && (
          <div className="flex items-center gap-2 p-3 bg-destructive/10 border border-destructive/20 rounded-md">
            <AlertCircle className="w-4 h-4 text-destructive" />
            <span className="text-sm text-destructive">{errorMessage}</span>
          </div>
        )}

        <Form {...form}>
          <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
            {/* Card Information */}
            <div className="space-y-4">
              <h4 className="font-medium flex items-center gap-2">
                <CreditCard className="w-4 h-4" />
                Card Information
              </h4>

              {/* Card Number */}
              <FormField
                control={form.control}
                name="cardNumber"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Card Number</FormLabel>
                    <FormControl>
                      <div className="relative">
                        <Input
                          placeholder="1234 1234 1234 1234"
                          {...field}
                          onChange={(e) => {
                            handleCardNumberChange(e.target.value);
                          }}
                          maxLength={19}
                          disabled={isLoading || paymentStatus === "processing"}
                        />
                        {cardType !== "unknown" && (
                          <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
                            <Badge variant="outline" className="text-xs">
                              {cardType.toUpperCase()}
                            </Badge>
                          </div>
                        )}
                      </div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {/* Expiry and CVV */}
              <div className="grid grid-cols-3 gap-3">
                <FormField
                  control={form.control}
                  name="expiryMonth"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Month</FormLabel>
                      <Select
                        onValueChange={field.onChange}
                        defaultValue={field.value}
                        disabled={isLoading || paymentStatus === "processing"}
                      >
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="MM" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {months.map((month) => (
                            <SelectItem key={month.value} value={month.value}>
                              {month.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="expiryYear"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Year</FormLabel>
                      <Select
                        onValueChange={field.onChange}
                        defaultValue={field.value}
                        disabled={isLoading || paymentStatus === "processing"}
                      >
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="YYYY" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {years.map((year) => (
                            <SelectItem key={year.value} value={year.value}>
                              {year.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="cvv"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>CVV</FormLabel>
                      <FormControl>
                        <Input
                          placeholder="123"
                          {...field}
                          maxLength={4}
                          disabled={isLoading || paymentStatus === "processing"}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              {/* Cardholder Name */}
              <FormField
                control={form.control}
                name="cardholderName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Cardholder Name</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="John Doe"
                        {...field}
                        disabled={isLoading || paymentStatus === "processing"}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            {/* Security Notice */}
            <div className="flex items-center gap-2 p-3 bg-muted/50 rounded-md">
              <Lock className="w-4 h-4 text-muted-foreground" />
              <span className="text-xs text-muted-foreground">
                Your payment information is encrypted and secure
              </span>
            </div>

            {/* Action Buttons */}
            <div className="space-y-3">
              <Button
                type="submit"
                className="w-full"
                disabled={isLoading || paymentStatus === "processing"}
              >
                {paymentStatus === "processing" ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2" />
                    Processing...
                  </>
                ) : (
                  `Pay ${currency} ${amount.toFixed(2)}`
                )}
              </Button>

              {onCancel && (
                <Button
                  type="button"
                  variant="outline"
                  className="w-full"
                  onClick={onCancel}
                  disabled={isLoading || paymentStatus === "processing"}
                >
                  Cancel
                </Button>
              )}
            </div>
          </form>
        </Form>
      </CardContent>
    </Card>
  );
}

export default PaymentSimulatorForm;
