"use client";

import { authorizationApi } from "@/api/authorization.api";
import AdminLayout from "@/components/layout/AdminLayout";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
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
import type { RegisterRequest, User } from "@/interfaces/identity.interface";
import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowLeft, Loader2 } from "lucide-react";
import { useParams, useRouter } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

const userSchema = z.object({
  firstName: z.string().min(1, "First name is required."),
  lastName: z.string().min(1, "Last name is required."),
  role: z.string().min(1, "Role is required."),
});

type UserEditFormData = z.infer<typeof userSchema>;

function AdminUserEditPage() {
  const router = useRouter();
  const params = useParams();
  const userIdParam = params.id;
  const userId = Array.isArray(userIdParam) ? userIdParam[0] : userIdParam;
  const isInitialized = useRef(false);
  const [isLoadingUser, setIsLoadingUser] = useState(true);
  const [user, setUser] = useState<User | null>(null);
  const [isResetDialogOpen, setIsResetDialogOpen] = useState(false);
  const [isResettingPassword, setIsResettingPassword] = useState(false);
  const [userRoles, setUserRoles] = useState<Record<string, string>>({});
  const [isLoadingUserRoles, setIsLoadingUserRoles] = useState(false);

  const form = useForm<UserEditFormData>({
    resolver: zodResolver(userSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      role: "USER",
    },
  });

  const loadUserRoles = useCallback(async () => {
    if (isLoadingUserRoles || Object.keys(userRoles).length > 0) return;

    setIsLoadingUserRoles(true);
    try {
      const response = await authorizationApi.retrieveUserRoles();
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
    if (isInitialized.current) {
      return;
    }

    isInitialized.current = true;

    const loadUser = async () => {
      if (!userId) {
        toast.error("User not found");
        router.push("/user");
        return;
      }

      setIsLoadingUser(true);
      try {
        const response = await authorizationApi.retrieveUser(userId);
        if (response.success) {
          const userData = response.data;
          setUser(userData);
          form.reset({
            firstName: userData.firstName || "",
            lastName: userData.lastName || "",
            role: userData.role,
          });
        } else {
          toast.error(response.errorMessage || "User not found");
          router.push("/user");
        }
      } catch {
        toast.error("Failed to load user");
        router.push("/user");
      } finally {
        setIsLoadingUser(false);
      }
    };

    loadUser();
  }, [form, router, userId]);

  useEffect(() => {
    loadUserRoles();
  }, [loadUserRoles]);

  const onSubmit = async (values: UserEditFormData) => {
    if (!userId) {
      toast.error("User not found");
      return;
    }

    const payload: Partial<RegisterRequest> & { role?: string } = {
      firstName: values.firstName,
      lastName: values.lastName,
      role: values.role,
    };

    try {
      const response = await authorizationApi.updateUser(userId, payload);
      if (response.success) {
        toast.success("User updated successfully");
      } else {
        toast.error(response.errorMessage || "Failed to update user");
      }
    } catch {
      toast.error("Failed to update user");
    }
  };

  const closeResetDialog = () => {
    if (!isResettingPassword) {
      setIsResetDialogOpen(false);
    }
  };

  const confirmResetPassword = async () => {
    if (!userId) {
      toast.error("User not found");
      return;
    }

    setIsResettingPassword(true);
    try {
      const response = await authorizationApi.resetUserPassword(userId);
      if (response.success) {
        toast.success("Password reset successfully");
        setIsResetDialogOpen(false);
      } else {
        toast.error(response.errorMessage || "Failed to reset password");
      }
    } catch {
      toast.error("Failed to reset password");
    } finally {
      setIsResettingPassword(false);
    }
  };

  if (isLoadingUser) {
    return (
      <AdminLayout>
        <div className="container mx-auto px-6 py-8">
          <div className="flex items-center justify-center h-64">
            <Loader2 className="h-8 w-8 animate-spin" />
            <span className="ml-2">Loading user...</span>
          </div>
        </div>
      </AdminLayout>
    );
  }

  if (!user) {
    return (
      <AdminLayout>
        <div className="container mx-auto px-6 py-8">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-gray-900 mb-4">
              User Not Found
            </h1>
            <p className="text-gray-600 mb-4">
              The user you&apos;re looking for doesn&apos;t exist.
            </p>
            <Button onClick={() => router.push("/user")}>
              Back to Users
            </Button>
          </div>
        </div>
      </AdminLayout>
    );
  }

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
                <div className="grid grid-cols-1 gap-4">
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
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle>Security</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <p className="text-sm text-muted-foreground">
                  Resetting the password will set it to the default value.
                </p>
                <Button
                  type="button"
                  variant="destructive"
                  onClick={() => setIsResetDialogOpen(true)}
                >
                  Reset Password
                </Button>
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
                Update User
              </Button>
            </div>
          </form>
        </Form>
        <AlertDialog
          open={isResetDialogOpen}
          onOpenChange={(open) =>
            open ? setIsResetDialogOpen(true) : closeResetDialog()
          }
        >
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Reset Password</AlertDialogTitle>
              <AlertDialogDescription>
                This will reset the user&apos;s password to the default value.
                The user must use the new password to sign in.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel onClick={closeResetDialog}>
                Cancel
              </AlertDialogCancel>
              <AlertDialogAction
                onClick={confirmResetPassword}
                disabled={isResettingPassword}
              >
                {isResettingPassword && (
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                )}
                Reset Password
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </div>
    </AdminLayout>
  );
}

export default withRequireAdmin(AdminUserEditPage);
