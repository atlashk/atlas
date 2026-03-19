export interface User {
  id: number | string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  role: string;
}

export interface RegisterRequest {
  password: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
}

export interface RetrieveUserListFilter {
  id?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phoneNumber?: string;
  role?: string;
  page: number;
  size: number;
}

export interface CreateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  password: string;
  role: string;
}

export interface UpdateUserRequest {
  firstName: string;
  lastName: string;
  role: string;
}
