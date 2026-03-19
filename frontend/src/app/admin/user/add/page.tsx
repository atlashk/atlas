"use client";

import { userApi } from "@/api/user.api";
import AdminLayout from "@/components/layout/AdminLayout";
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
import { withRequireAdmin } from "@/hoc/withAuth";
import type { CreateUserRequest } from "@/interfaces/user.interface";
import { PASSWORD_REGEX, PASSWORD_REQUIREMENTS_MESSAGE } from "@/lib/utils";
import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowLeft, Loader2 } from "lucide-react";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

const userSchema = z
  .object({
    firstName: z.string().min(1, "First name is required."),
    lastName: z.string().min(1, "Last name is required."),
    email: z.email("Please enter a valid email address."),
    phoneNumber: z.string().optional(),
    password: z
      .string()
      .min(1, "Password is required.")
      .refine((value) => PASSWORD_REGEX.test(value), {
        message: PASSWORD_REQUIREMENTS_MESSAGE,
      }),
    confirmPassword: z.string().min(1, "Confirm password is required."),
    role: z.string().min(1, "Role is required."),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords do not match.",
    path: ["confirmPassword"],
  });

type UserCreateFormData = z.infer<typeof userSchema>;

function AdminUserAddPage() {
  const router = useRouter();
  const [userRoles, setUserRoles] = useState<Record<string, string>>({});
  const [isLoadingUserRoles, setIsLoadingUserRoles] = useState(false);

  const form = useForm<UserCreateFormData>({
    resolver: zodResolver(userSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      phoneNumber: "",
      password: "",
      confirmPassword: "",
      role: "USER",
    },
  });

  const loadUserRoles = useCallback(async () => {
    if (isLoadingUserRoles || Object.keys(userRoles).length > 0) return;

    setIsLoadingUserRoles(true);
    try {
      const response = await userApi.retrieveUserRoles();
      if (response.success) {
        setUserRoles(response.data || {});
      } else {
        toast.error(response.errorMessage || "Failed to load roles");
        setUserRoles({});
      }
    } catch (error: unknown) {
      const errorMessage =
        error instanceof Error ? error.message : "Failed to load roles";
      toast.error(errorMessage);
      setUserRoles({});
    } finally {
      setIsLoadingUserRoles(false);
    }
  }, [isLoadingUserRoles, userRoles]);

  useEffect(() => {
    loadUserRoles();
  }, [loadUserRoles]);

  const onSubmit = async (values: UserCreateFormData) => {
    const request: CreateUserRequest = {
      firstName: values.firstName,
      lastName: values.lastName,
      email: values.email,
      phoneNumber: values.phoneNumber || undefined,
      password: values.password,
      role: values.role
    };

    try {
      const response = await userApi.createUser(request);
      if (response.success) {
        toast.success("User created successfully");
        router.push("/admin/user");
      } else {
        toast.error(response.errorMessage || "Failed to create user");
      }
    } catch {
      toast.error("Failed to create user");
    }
  };

  return (
    <AdminLayout>
      <div className="container mx-auto px-2">
        <div className="flex items-center justify-between mb-6">
          <Button onClick={() => router.back()} variant="outline">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back
          </Button>
        </div>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>User Information</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <FormField
                    control={form.control}
                    name="role"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Role *</FormLabel>
                        <Select
                          value={field.value}
                          onValueChange={field.onChange}
                          disabled={isLoadingUserRoles}
                        >
                          <FormControl>
                            <SelectTrigger>
                              <SelectValue placeholder="Select role" />
                            </SelectTrigger>
                          </FormControl>
                          <SelectContent>
                            {Object.entries(userRoles).map(
                              ([roleKey, roleLabel]) => (
                                <SelectItem key={roleKey} value={roleKey}>
                                  {roleLabel}
                                </SelectItem>
                              )
                            )}
                          </SelectContent>
                        </Select>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <FormField
                    control={form.control}
                    name="firstName"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>First Name *</FormLabel>
                        <FormControl>
                          <Input {...field} placeholder="Enter first name" />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <FormField
                    control={form.control}
                    name="lastName"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Last Name *</FormLabel>
                        <FormControl>
                          <Input {...field} placeholder="Enter last name" />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <FormField
                    control={form.control}
                    name="email"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Email Address *</FormLabel>
                        <FormControl>
                          <Input {...field} type="email" placeholder="Enter email address" />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <FormField
                    control={form.control}
                    name="phoneNumber"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Phone Number</FormLabel>
                        <FormControl>
                          <Input {...field} placeholder="Enter phone number" />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <FormField
                    control={form.control}
                    name="password"
                    render={({ field }) => (
                      <FormItem className="md:col-span-2">
                        <FormLabel>Password *</FormLabel>
                        <FormControl>
                          <Input
                            {...field}
                            type="password"
                            placeholder="Enter password"
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <FormField
                    control={form.control}
                    name="confirmPassword"
                    render={({ field }) => (
                      <FormItem className="md:col-span-2">
                        <FormLabel>Confirm Password *</FormLabel>
                        <FormControl>
                          <Input
                            {...field}
                            type="password"
                            placeholder="Confirm password"
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </div>
              </CardContent>
            </Card>

            <div className="flex justify-end space-x-2">
              <Button type="button" variant="outline" onClick={() => router.back()}>
                Cancel
              </Button>
              <Button type="submit" disabled={form.formState.isSubmitting}>
                {form.formState.isSubmitting && (
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                )}
                Create User
              </Button>
            </div>
          </form>
        </Form>
      </div>
    </AdminLayout>
  );
}

export default withRequireAdmin(AdminUserAddPage);
