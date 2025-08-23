import { Role } from "@/constants";

export interface User {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  role: Role;
}

export interface ListUserFilters {
  id?: string;
  keyword?: string;
  role?: Role;
  page: number;
  size: number;
}

export interface RegisterRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
}
