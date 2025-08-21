'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { userService } from '@/services';
import { getRoleBadgeClasses } from '@/utils/formatter.util';
import { toast } from 'sonner';
import type { ListUserFilters, User } from '@/interfaces/user.interface';

const UserList: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoadingUsers, setIsLoadingUsers] = useState(true);
  const [filters, setFilters] = useState<ListUserFilters>({
    id: undefined,
    username: undefined,
    role: '',
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
  const availableRoles: string[] = ['ADMIN', 'USER'];

  const applyFilters = useCallback(async (page: number) => {
    setIsLoadingUsers(true);
    try {
      const updatedFilters = { ...filters, page };
      setFilters(updatedFilters);
      setMetadata(prev => ({ ...prev, currentPage: page }));

      // Clean up empty or undefined filters
      const apiFilters: ListUserFilters = { ...updatedFilters };
      Object.keys(apiFilters).forEach((key) => {
        const typedKey = key as keyof ListUserFilters;
        if (apiFilters[typedKey] === '' || apiFilters[typedKey] === undefined) {
          delete apiFilters[typedKey];
        }
      });

      console.log('Fetching users with filters:', apiFilters);
      const response = await userService.listUser(apiFilters);

      if (response.success) {
        setUsers(response.data || []);
        if (response.metadata) {
          setMetadata(response.metadata);
        }
        console.log('Users loaded:', response.data?.length || 0);
      } else {
        toast.error(response.errorMessage || 'Failed to load users');
        console.error('Failed to load users:', response.errorMessage);
      }
    } catch (error: unknown) {
      console.error('Error loading users:', error);
      const errorMessage = error instanceof Error ? error.message : 'Failed to load users';
      toast.error(errorMessage);
      setUsers([]);
    } finally {
      setIsLoadingUsers(false);
    }
  }, [filters]);

  const changePage = (newPage: number) => {
    if (newPage >= 1 && newPage <= metadata.totalPages) {
      applyFilters(newPage);
    }
  };

  const resetFilters = () => {
    const resetFilters: ListUserFilters = {
      id: undefined,
      username: undefined,
      role: '',
      page: 1,
      size: 20,
    };
    setFilters(resetFilters);
    applyFilters(1);
  };

  const handleFilterChange = (field: keyof ListUserFilters, value: string | number | boolean | undefined) => {
    setFilters(prev => ({ ...prev, [field]: value }));
  };

  const handleSearch = () => {
    applyFilters(1);
  };

  // Initial load
  useEffect(() => {
    console.log('UserList component mounted, fetching users...');
    applyFilters(1);
  }, [applyFilters]);

  return (
    <div className="container-fluid py-4">
      <div className="row">
        <div className="col-12">
          <div className="card">
            <div className="card-header">
              <h5 className="card-title mb-0">
                <i className="bi bi-people me-2"></i>
                User Management
              </h5>
            </div>

            {/* Filters */}
            <div className="card-body border-bottom">
              <div className="row g-3">
                <div className="col-md-3">
                  <label htmlFor="userId" className="form-label">User ID</label>
                  <input
                    type="number"
                    className="form-control"
                    id="userId"
                    placeholder="Enter user ID"
                    value={filters.id || ''}
                    onChange={(e) => handleFilterChange('id', e.target.value ? parseInt(e.target.value) : undefined)}
                  />
                </div>
                <div className="col-md-3">
                  <label htmlFor="username" className="form-label">Username</label>
                  <input
                    type="text"
                    className="form-control"
                    id="username"
                    placeholder="Enter username"
                    value={filters.username || ''}
                    onChange={(e) => handleFilterChange('username', e.target.value || undefined)}
                  />
                </div>
                <div className="col-md-3">
                  <label htmlFor="role" className="form-label">Role</label>
                  <select
                    className="form-select"
                    id="role"
                    value={filters.role}
                    onChange={(e) => handleFilterChange('role', e.target.value)}
                  >
                    <option value="">All Roles</option>
                    {availableRoles.map(role => (
                      <option key={role} value={role}>
                        {role}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="col-md-3 d-flex align-items-end">
                  <div className="btn-group w-100">
                    <button
                      type="button"
                      className="btn btn-primary"
                      onClick={handleSearch}
                      disabled={isLoadingUsers}
                    >
                      <i className="bi bi-search me-1"></i>
                      Search
                    </button>
                    <button
                      type="button"
                      className="btn btn-outline-secondary"
                      onClick={resetFilters}
                      disabled={isLoadingUsers}
                    >
                      <i className="bi bi-arrow-clockwise me-1"></i>
                      Reset
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Users Table */}
            <div className="table-responsive">
              {isLoadingUsers ? (
                <div className="text-center py-5">
                  <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                  </div>
                  <p className="mt-2 text-muted">Loading users...</p>
                </div>
              ) : (
                <table className="table table-hover mb-0">
                  <thead className="table-light">
                    <tr>
                      <th scope="col" className="px-4">ID</th>
                      <th scope="col" className="px-4">Username</th>
                      <th scope="col" className="px-4">Name</th>
                      <th scope="col" className="px-4">Email</th>
                      <th scope="col" className="px-4">Phone</th>
                      <th scope="col" className="px-4">Role</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.length === 0 ? (
                      <tr>
                        <td colSpan={6} className="text-center py-4 text-muted">
                          No users found
                        </td>
                      </tr>
                    ) : (
                      users.map(user => (
                        <tr key={user.id}>
                          <td className="px-4">{user.id}</td>
                          <td className="px-4">{user.username}</td>
                          <td className="px-4">
                            {user.firstName && user.lastName 
                              ? `${user.firstName} ${user.lastName}` 
                              : 'N/A'
                            }
                          </td>
                          <td className="px-4">{user.email || 'N/A'}</td>
                          <td className="px-4">{user.phoneNumber || 'N/A'}</td>
                          <td className="px-4">
                            <span className={getRoleBadgeClasses(user.role)}>
                              {user.role}
                            </span>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              )}
            </div>

            {/* Pagination */}
            <div className="card-footer bg-light py-3">
              <div className="d-flex justify-content-between align-items-center">
                <span className="text-muted">
                  Page {metadata.currentPage} of {metadata.totalPages}
                  <span className="ms-2">({metadata.totalRecords} records)</span>
                </span>
                <div className="btn-group">
                  <button
                    onClick={() => changePage(metadata.currentPage - 1)}
                    disabled={metadata.currentPage <= 1}
                    className="btn btn-outline-secondary px-3"
                  >
                    Previous
                  </button>
                  <button
                    onClick={() => changePage(metadata.currentPage + 1)}
                    disabled={metadata.currentPage >= metadata.totalPages}
                    className="btn btn-outline-secondary px-3"
                  >
                    Next
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserList;
