export interface User {
  id: number;
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
