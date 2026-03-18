export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  role: string;
}

export interface RegisterRequest {
  password: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
}
