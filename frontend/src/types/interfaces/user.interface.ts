export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  username?: string;
  avatar?: string;
  phone?: string;
  dateOfBirth?: string;
  gender?: 'male' | 'female' | 'other';
  address?: Address;
  preferences?: UserPreferences;
  roles: UserRole[];
  permissions: string[];
  isActive: boolean;
  isEmailVerified: boolean;
  isPhoneVerified: boolean;
  lastLoginAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserRole {
  id: string;
  name: string;
  description?: string;
  permissions: string[];
}

export interface UserPreferences {
  language: string;
  timezone: string;
  currency: string;
  theme: 'light' | 'dark' | 'system';
  notifications: NotificationPreferences;
  privacy: PrivacySettings;
}

export interface NotificationPreferences {
  email: {
    marketing: boolean;
    orderUpdates: boolean;
    securityAlerts: boolean;
    newsletter: boolean;
  };
  push: {
    orderUpdates: boolean;
    promotions: boolean;
    reminders: boolean;
  };
  sms: {
    orderUpdates: boolean;
    securityAlerts: boolean;
  };
}

export interface PrivacySettings {
  profileVisibility: 'public' | 'private' | 'friends';
  showEmail: boolean;
  showPhone: boolean;
  allowDataCollection: boolean;
  allowPersonalization: boolean;
}

export interface Address {
  id?: string;
  type: 'home' | 'work' | 'billing' | 'shipping' | 'other';
  firstName: string;
  lastName: string;
  company?: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  phone?: string;
  isDefault: boolean;
  instructions?: string;
}

export interface UserProfile extends Omit<User, 'roles' | 'permissions'> {
  fullName: string;
  initials: string;
  addresses: Address[];
  defaultAddress?: Address;
  stats: UserStats;
}

export interface UserStats {
  totalOrders: number;
  totalSpent: number;
  averageOrderValue: number;
  favoriteCategories: string[];
  loyaltyPoints: number;
  memberSince: string;
}

export interface LoginCredentials {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface RegisterData {
  email: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  phone?: string;
  acceptTerms: boolean;
  subscribeNewsletter?: boolean;
}

export interface PasswordResetRequest {
  email: string;
}

export interface PasswordReset {
  token: string;
  password: string;
  confirmPassword: string;
}

export interface ChangePassword {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface UpdateProfile {
  firstName?: string;
  lastName?: string;
  phone?: string;
  dateOfBirth?: string;
  gender?: 'male' | 'female' | 'other';
  avatar?: File | string;
}

export interface UserSession {
  user: User;
  accessToken: string;
  refreshToken: string;
  expiresAt: string;
  permissions: string[];
  roles: string[];
}

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  session: UserSession | null;
}

// Type guards
export function isUser(obj: any): obj is User {
  return (
    obj &&
    typeof obj === 'object' &&
    typeof obj.id === 'string' &&
    typeof obj.email === 'string' &&
    typeof obj.firstName === 'string' &&
    typeof obj.lastName === 'string' &&
    Array.isArray(obj.roles) &&
    Array.isArray(obj.permissions)
  );
}

export function isUserProfile(obj: any): obj is UserProfile {
  return (
    isUser(obj) &&
    typeof obj.fullName === 'string' &&
    typeof obj.initials === 'string' &&
    Array.isArray(obj.addresses)
  );
}

export function isAddress(obj: any): obj is Address {
  return (
    obj &&
    typeof obj === 'object' &&
    typeof obj.type === 'string' &&
    typeof obj.firstName === 'string' &&
    typeof obj.lastName === 'string' &&
    typeof obj.addressLine1 === 'string' &&
    typeof obj.city === 'string' &&
    typeof obj.state === 'string' &&
    typeof obj.postalCode === 'string' &&
    typeof obj.country === 'string'
  );
}