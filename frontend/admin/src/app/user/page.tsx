"use client";

import { Metadata } from "@/api/apiClient";
import { userApi } from "@/api/user.api";
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
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { withRequireAdmin } from "@/hoc/withAuth";
import type { RetrieveUserListFilter, User } from "@/interfaces/user.interface";
import { Edit, Loader2, Plus, RotateCcw, Search, Trash2 } from "lucide-react";
import { useRouter } from "next/navigation";
import React, { useCallback, useEffect, useRef, useState } from "react";
import { toast } from "sonner";

const AdminUserListPage: React.FC = () => {
  const router = useRouter();
  const isInitialized = useRef(false);
  const [users, setUsers] = useState<User[]>([]);
  const [isLoadingUsers, setIsLoadingUsers] = useState(true);
  const [userRoles, setUserRoles] = useState<Record<string, string>>({});
  const [isLoadingUserRoles, setIsLoadingUserRoles] = useState(false);
  const [filter, setFilter] = useState<RetrieveUserListFilter>({
    id: undefined,
    firstName: undefined,
    lastName: undefined,
    email: undefined,
    phoneNumber: undefined,
    role: undefined,
    page: 1,
    size: 20,
  });
  const [metadata, setMetadata] = useState<Metadata>({
    currentPage: 1,
    pageSize: 20,
    totalPages: 1,
    totalRecords: 0,
  });
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<User | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

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

  const applyFilter = useCallback(
    async (page: number, currentFilter?: RetrieveUserListFilter) => {
      setIsLoadingUsers(true);
      try {
        const filterToUse = currentFilter || filter;
        const updatedFilter = { ...filterToUse, page };
        setFilter(updatedFilter);
        setMetadata((prev) => ({ ...prev, currentPage: page }));

        const apiFilter: RetrieveUserListFilter = { ...updatedFilter };
        Object.keys(apiFilter).forEach((key) => {
          const typedKey = key as keyof RetrieveUserListFilter;
          if (
            apiFilter[typedKey] === "" ||
            apiFilter[typedKey] === undefined
          ) {
            delete apiFilter[typedKey];
          }
        });

        const response = await userApi.retrieveUserList(apiFilter);

        if (response.success) {
          setUsers(response.data || []);
          if (response.metadata) {
            setMetadata(response.metadata);
          }
        } else {
          toast.error(response.errorMessage || "Failed to load users");
        }
      } catch (error: unknown) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to load users";
        toast.error(errorMessage);
        setUsers([]);
      } finally {
        setIsLoadingUsers(false);
      }
    },
    [filter]
  );

  const changePage = useCallback(
    (newPage: number) => {
      if (newPage >= 1 && newPage <= metadata.totalPages) {
        applyFilter(newPage);
      }
    },
    [metadata.totalPages, applyFilter]
  );

  const resetFilter = useCallback(() => {
    const resetFilterData: RetrieveUserListFilter = {
      id: undefined,
      firstName: undefined,
      lastName: undefined,
      email: undefined,
      phoneNumber: undefined,
      role: undefined,
      page: 1,
      size: 20,
    };
    setFilter(resetFilterData);
    applyFilter(1, resetFilterData);
  }, [applyFilter]);

  const handleFilterChange = useCallback(
    (
      field: keyof RetrieveUserListFilter,
      value: string | number | boolean | undefined
    ) => {
      setFilter((prev) => ({ ...prev, [field]: value }));
    },
    []
  );

  const handleSearch = useCallback(() => {
    applyFilter(1);
  }, [applyFilter]);

  const openCreateUser = useCallback(() => {
    router.push("/user/add");
  }, [router]);

  const openEditUser = useCallback((user: User) => {
    router.push(`/user/${user.id}/edit`);
  }, [router]);

  const openDeleteDialog = useCallback((user: User) => {
    setDeleteTarget(user);
    setIsDeleteOpen(true);
  }, []);

  const closeDeleteDialog = useCallback(() => {
    setIsDeleteOpen(false);
    setDeleteTarget(null);
  }, []);

  const confirmDelete = useCallback(async () => {
    if (!deleteTarget || isDeleting) return;
    setIsDeleting(true);
    try {
      const response = await userApi.deleteUser(deleteTarget.id);
      if (response.success) {
        toast.success("User deleted successfully");
        const targetPage =
          users.length === 1 && metadata.currentPage > 1
            ? metadata.currentPage - 1
            : metadata.currentPage;
        closeDeleteDialog();
        applyFilter(targetPage);
      } else {
        toast.error(response.errorMessage || "Failed to delete user");
      }
    } catch (error: unknown) {
      const errorMessage =
        error instanceof Error ? error.message : "Failed to delete user";
      toast.error(errorMessage);
    } finally {
      setIsDeleting(false);
    }
  }, [
    deleteTarget,
    isDeleting,
    users.length,
    metadata.currentPage,
    applyFilter,
    closeDeleteDialog,
  ]);

  useEffect(() => {
    if (isInitialized.current) {
      return;
    }

    isInitialized.current = true;

    const initializeData = async () => {
      await applyFilter(1);
    };
    initializeData();
  }, [applyFilter]);

  useEffect(() => {
    loadUserRoles();
  }, [loadUserRoles]);

  return (
    <AdminLayout>
      <div className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>User Filter</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="space-y-2">
                <Label htmlFor="userId">User ID</Label>
                <Input
                  type="text"
                  id="userId"
                  placeholder="Enter user ID"
                  value={filter.id || ""}
                  onChange={(e) =>
                    handleFilterChange(
                      "id",
                      e.target.value ? parseInt(e.target.value) : undefined
                    )
                  }
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="firstName">First Name</Label>
                <Input
                  type="text"
                  id="firstName"
                  placeholder="Enter first name"
                  value={filter.firstName || ""}
                  onChange={(e) =>
                    handleFilterChange("firstName", e.target.value || undefined)
                  }
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="lastName">Last Name</Label>
                <Input
                  type="text"
                  id="lastName"
                  placeholder="Enter last name"
                  value={filter.lastName || ""}
                  onChange={(e) =>
                    handleFilterChange("lastName", e.target.value || undefined)
                  }
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input
                  type="text"
                  id="email"
                  placeholder="Enter email"
                  value={filter.email || ""}
                  onChange={(e) =>
                    handleFilterChange("email", e.target.value || undefined)
                  }
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="phoneNumber">Phone Number</Label>
                <Input
                  type="text"
                  id="phoneNumber"
                  placeholder="Enter phone"
                  value={filter.phoneNumber || ""}
                  onChange={(e) =>
                    handleFilterChange("phoneNumber", e.target.value || undefined)
                  }
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="role">Role</Label>
                <Select
                  value={filter.role}
                  onValueChange={(value) => handleFilterChange("role", value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="All Roles" />
                  </SelectTrigger>
                  <SelectContent>
                    {Object.entries(userRoles).map(([roleKey, roleLabel]) => (
                      <SelectItem key={roleKey} value={roleKey}>
                        {roleLabel}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div className="flex justify-start space-x-2 mt-4">
              <Button onClick={handleSearch} disabled={isLoadingUsers}>
                <Search className="h-4 w-4 mr-2" />
                Search
              </Button>
              <Button
                variant="outline"
                onClick={resetFilter}
                disabled={isLoadingUsers}
              >
                <RotateCcw className="h-4 w-4 mr-2" />
                Reset
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <CardTitle>User Results</CardTitle>
              <Button onClick={openCreateUser}>
                <Plus className="h-4 w-4 mr-2" />
                Add User
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            {isLoadingUsers ? (
              <div className="flex flex-col items-center justify-center py-12">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
                <p className="mt-2 text-muted-foreground">Loading users...</p>
              </div>
            ) : (
              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>ID</TableHead>
                      <TableHead>Full Name</TableHead>
                      <TableHead>Email Address</TableHead>
                      <TableHead>Phone Number</TableHead>
                      <TableHead>Role</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {users.length === 0 ? (
                      <TableRow>
                        <TableCell
                          colSpan={6}
                          className="text-center py-8 text-muted-foreground"
                        >
                          No users found
                        </TableCell>
                      </TableRow>
                    ) : (
                      users.map((user) => (
                        <TableRow key={user.id}>
                          <TableCell>{user.id}</TableCell>
                          <TableCell>
                            {user.firstName && user.lastName
                              ? `${user.firstName} ${user.lastName}`
                              : "N/A"}
                          </TableCell>
                          <TableCell>{user.email || "N/A"}</TableCell>
                          <TableCell>{user.phoneNumber || "N/A"}</TableCell>
                          <TableCell>
                            <Badge
                              variant={
                                user.role === "ADMIN" ? "default" : "secondary"
                              }
                            >
                              {userRoles[user.role] || "Unknown"}
                            </Badge>
                          </TableCell>
                          <TableCell className="text-right">
                            <div className="flex items-center justify-end gap-2">
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => openEditUser(user)}
                              >
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => openDeleteDialog(user)}
                                className="text-destructive hover:text-destructive"
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </div>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>

        <AlertDialog
          open={isDeleteOpen}
          onOpenChange={(open) =>
            open ? setIsDeleteOpen(true) : closeDeleteDialog()
          }
        >
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Delete User</AlertDialogTitle>
              <AlertDialogDescription>
                {deleteTarget
                  ? `Are you sure you want to delete ${deleteTarget.email}? This action cannot be undone.`
                  : "Are you sure you want to delete this user?"}
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel onClick={closeDeleteDialog}>
                Cancel
              </AlertDialogCancel>
              <AlertDialogAction onClick={confirmDelete} disabled={isDeleting}>
                {isDeleting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Delete
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>

        {metadata.totalPages > 1 && (
          <div className="flex items-center justify-between">
            <div className="text-sm text-muted-foreground">
              Page {metadata.currentPage} of {metadata.totalPages} (
              {metadata.totalRecords} records)
            </div>
            <Pagination>
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious
                    href="#"
                    onClick={(e) => {
                      e.preventDefault();
                      if (metadata.currentPage > 1) {
                        changePage(metadata.currentPage - 1);
                      }
                    }}
                    className={
                      metadata.currentPage <= 1
                        ? "pointer-events-none opacity-50"
                        : ""
                    }
                  />
                </PaginationItem>

                {Array.from(
                  { length: Math.min(5, metadata.totalPages) },
                  (_, i) => {
                    const pageNumber =
                      Math.max(
                        1,
                        Math.min(
                          metadata.totalPages - 4,
                          metadata.currentPage - 2
                        )
                      ) + i;

                    if (pageNumber <= metadata.totalPages) {
                      return (
                        <PaginationItem key={pageNumber}>
                          <PaginationLink
                            href="#"
                            onClick={(e) => {
                              e.preventDefault();
                              changePage(pageNumber);
                            }}
                            isActive={pageNumber === metadata.currentPage}
                          >
                            {pageNumber}
                          </PaginationLink>
                        </PaginationItem>
                      );
                    }
                    return null;
                  }
                )}

                <PaginationItem>
                  <PaginationNext
                    href="#"
                    onClick={(e) => {
                      e.preventDefault();
                      if (metadata.currentPage < metadata.totalPages) {
                        changePage(metadata.currentPage + 1);
                      }
                    }}
                    className={
                      metadata.currentPage >= metadata.totalPages
                        ? "pointer-events-none opacity-50"
                        : ""
                    }
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default withRequireAdmin(AdminUserListPage);
