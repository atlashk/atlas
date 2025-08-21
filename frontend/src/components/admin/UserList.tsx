"use client";

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
import type { ListUserFilters, User } from "@/interfaces/user.interface";
import { userService } from "@/services";
import { Loader2, RotateCcw, Search } from "lucide-react";
import React, { useCallback, useEffect, useState } from "react";
import { toast } from "sonner";

const UserList: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoadingUsers, setIsLoadingUsers] = useState(true);
  const [hasInitialLoad, setHasInitialLoad] = useState(false);
  const [filters, setFilters] = useState<ListUserFilters>({
    id: undefined,
    username: undefined,
    role: "",
    page: 1,
    size: 20,
  });
  const [metadata, setMetadata] = useState({
    currentPage: 1,
    pageSize: 20,
    totalPages: 1,
    totalRecords: 0,
  });

  // Available roles for dropdown
  const availableRoles: string[] = ["ADMIN", "USER"];

  const applyFilters = useCallback(
    async (page: number, currentFilters?: ListUserFilters) => {
      setIsLoadingUsers(true);
      try {
        const filtersToUse = currentFilters || filters;
        const updatedFilters = { ...filtersToUse, page };
        setFilters(updatedFilters);
        setMetadata((prev) => ({ ...prev, currentPage: page }));

        // Clean up empty or undefined filters
        const apiFilters: ListUserFilters = { ...updatedFilters };
        Object.keys(apiFilters).forEach((key) => {
          const typedKey = key as keyof ListUserFilters;
          if (
            apiFilters[typedKey] === "" ||
            apiFilters[typedKey] === undefined
          ) {
            delete apiFilters[typedKey];
          }
        });

        const response = await userService.listUser(apiFilters);

        if (response.success) {
          setUsers(response.data || []);
          if (response.metadata) {
            setMetadata(response.metadata);
          }
          setHasInitialLoad(true);
        } else {
          toast.error(response.errorMessage || "Failed to load users");
          console.error("Failed to load users:", response.errorMessage);
        }
      } catch (error: unknown) {
        console.error("Error loading users:", error);
        const errorMessage =
          error instanceof Error ? error.message : "Failed to load users";
        toast.error(errorMessage);
        setUsers([]);
      } finally {
        setIsLoadingUsers(false);
      }
    },
    [filters]
  );

  const changePage = (newPage: number) => {
    if (newPage >= 1 && newPage <= metadata.totalPages) {
      applyFilters(newPage);
    }
  };

  const resetFilters = () => {
    const resetFiltersData: ListUserFilters = {
      id: undefined,
      username: undefined,
      role: "",
      page: 1,
      size: 20,
    };
    setFilters(resetFiltersData);
    applyFilters(1, resetFiltersData);
  };

  const handleFilterChange = (
    field: keyof ListUserFilters,
    value: string | number | boolean | undefined
  ) => {
    setFilters((prev) => ({ ...prev, [field]: value }));
  };

  const handleSearch = () => {
    applyFilters(1);
  };

  // Initial load
  useEffect(() => {
    if (!hasInitialLoad) {
      applyFilters(1);
    }
  }, []);

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>User Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="space-y-2">
              <Label htmlFor="userId">User ID</Label>
              <Input
                type="number"
                id="userId"
                placeholder="Enter user ID"
                value={filters.id || ""}
                onChange={(e) =>
                  handleFilterChange(
                    "id",
                    e.target.value ? parseInt(e.target.value) : undefined
                  )
                }
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="username">Username</Label>
              <Input
                type="text"
                id="username"
                placeholder="Enter username"
                value={filters.username || ""}
                onChange={(e) =>
                  handleFilterChange("username", e.target.value || undefined)
                }
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="role">Role</Label>
              <Select
                value={filters.role}
                onValueChange={(value) => handleFilterChange("role", value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="All Roles" />
                </SelectTrigger>
                <SelectContent>
                  {availableRoles.map((role) => (
                    <SelectItem key={role} value={role}>
                      {role}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <div className="flex justify-start space-x-2 mt-4">
            <Button
              onClick={handleSearch}
              disabled={isLoadingUsers}
            >
              <Search className="h-4 w-4 mr-2" />
              Search
            </Button>
            <Button
              variant="outline"
              onClick={resetFilters}
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
          <CardTitle>User Results</CardTitle>
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
                    <TableHead>Username</TableHead>
                    <TableHead>Name</TableHead>
                    <TableHead>Email</TableHead>
                    <TableHead>Phone</TableHead>
                    <TableHead>Role</TableHead>
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
                        <TableCell>{user.username}</TableCell>
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
                            {user.role}
                          </Badge>
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

      {/* Pagination */}
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

              {/* Page numbers */}
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
  );
};

export default UserList;
