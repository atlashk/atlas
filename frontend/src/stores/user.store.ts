import { authApi, userApi } from "@/api";
import { Role } from "@/constants";
import type { LoginRequest } from "@/interfaces/auth.interface";
import type { RegisterRequest, User } from "@/interfaces/user.interface";
import { clearAuthCookies, setCookie, getCookie, isValidToken } from "@/utils/cookies";
import { create } from "zustand";
import { persist } from "zustand/middleware";

interface UserState {
  profile: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  loading: boolean;
  error: string | null;
  profileLoading: boolean;
}

interface UserActions {
  // Getters
  isAuthenticated: () => boolean;
  isAdmin: () => boolean;
  fullName: () => string;
  hasRole: (role: string) => boolean;
  authState: () => {
    isAuthenticated: boolean;
    isAdmin: boolean;
    user: User | null;
  };

  // Actions
  login: (
    credentials: LoginRequest
  ) => Promise<{ success: boolean; errorMessage?: string; userRole?: Role }>;
  register: (userData: RegisterRequest) => Promise<void>;
  fetchProfile: () => Promise<void>;
  setTokens: (accessToken: string, refreshToken: string) => void;
  logout: () => void;
  clearError: () => void;
  clearAuthState: () => void;
  initializeFromCookies: () => void;
}

type UserStore = UserState & UserActions;

// Use centralized cookie utilities
const clearAuthTokens = () => {
  clearAuthCookies();
};

export const useUserStore = create<UserStore>()(
  persist(
    (set, get) => ({
      // Initial state
      profile: null,
      accessToken: null,
      refreshToken: null,
      loading: false,
      error: null,
      profileLoading: false,

      // Getters
      isAuthenticated: () => {
        const { accessToken } = get();
        // Check both store state and cookies for better reliability
        const cookieToken = getCookie('accessToken');
        const tokenToCheck = accessToken || cookieToken;
        return isValidToken(tokenToCheck);
      },

      isAdmin: () => {
        const { profile } = get();
        return profile?.role === "ADMIN";
      },

      fullName: () => {
        const { profile } = get();
        if (!profile) return "";
        return `${profile.firstName} ${profile.lastName}`.trim();
      },

      hasRole: (role: string) => {
        const { profile } = get();
        return profile?.role === role;
      },

      authState: () => {
        const state = get();
        return {
          isAuthenticated: state.isAuthenticated(),
          isAdmin: state.isAdmin(),
          user: state.profile,
        };
      },

      // Actions
      login: async (request: LoginRequest) => {
        set({ loading: true, error: null });
        try {
          const response = await authApi.login(request);
          if (response.success && response.data) {
            const { accessToken, refreshToken } = response.data;

            // Store tokens in cookies for better security and middleware access
            console.log('Setting cookies with tokens:', { accessToken: accessToken.substring(0, 20) + '...', refreshToken: refreshToken.substring(0, 20) + '...' });
            setCookie('accessToken', accessToken);
            setCookie('refreshToken', refreshToken);
            console.log('Cookies after setting:', document.cookie);

            set({
              accessToken,
              refreshToken,
              profile: null, // Clear any existing profile
              loading: false,
            });

            // Try to fetch user profile after successful login
            try {
              await get().fetchProfile();
              const { profile } = get();
              console.log('Profile after login:', profile);

              return {
                success: true,
                userRole: profile?.role,
              };
            } catch (profileError) {
              // If profile fetch fails, still consider login successful
              // Keep tokens but warn about profile fetch failure
              console.warn(
                "Login successful but failed to fetch user profile. User will remain authenticated:",
                profileError
              );
              return {
                success: true,
                userRole: undefined, // Unknown role due to profile fetch failure
              };
            }
          } else {
            set({
              error: response.errorMessage || "Login failed",
              loading: false,
            });
            return {
              success: false,
              errorMessage: response.errorMessage || "Login failed",
            };
          }
        } catch (error) {
          const errorMessage =
            error instanceof Error ? error.message : "Login failed";
          set({
            error: errorMessage,
            loading: false,
          });
          return { success: false, errorMessage };
        }
      },

      register: async (userData: RegisterRequest) => {
        set({ loading: true, error: null });
        try {
          const response = await userApi.register(userData);
          if (response.success) {
            set({ loading: false });
          } else {
            set({
              error: response.errorMessage || "Registration failed",
              loading: false,
            });
          }
        } catch (error) {
          set({
            error:
              error instanceof Error ? error.message : "Registration failed",
            loading: false,
          });
        }
      },

      fetchProfile: async () => {
        const { accessToken, profile } = get();
        if (!accessToken) return;
        
        // Don't fetch if we already have a profile
        if (profile) return;

        set({ profileLoading: true, error: null });
        try {
          const response = await userApi.getProfile();
          if (response.success && response.data) {
            set({
              profile: response.data,
              profileLoading: false,
            });
          } else {
            // Don't set error for profile fetch failures - keep user authenticated
            console.warn('Failed to fetch profile but keeping user authenticated:', response.errorMessage);
            set({ profileLoading: false });
          }
        } catch (error) {
          // Don't set error for profile fetch failures - keep user authenticated
          console.warn('Failed to fetch profile but keeping user authenticated:', error);
          set({ profileLoading: false });
        }
      },

      setTokens: (accessToken: string, refreshToken: string) => {
        // Store in cookies for middleware access
        setCookie('accessToken', accessToken);
        setCookie('refreshToken', refreshToken);
        set({ accessToken, refreshToken });
      },

      logout: () => {
        console.log('Logout initiated');
        authApi.logout();
        clearAuthTokens();
        set({
          profile: null,
          accessToken: null,
          refreshToken: null,
          loading: false,
          error: null,
          profileLoading: false,
        });
        console.log('Logout completed, redirecting to login');
        
        // Force redirect to login page without any query parameters
        if (typeof window !== 'undefined') {
          window.location.href = '/login';
        }
      },

      clearError: () => {
        set({ error: null });
      },

      clearAuthState: () => {
        clearAuthTokens();

        set({
          profile: null,
          accessToken: null,
          refreshToken: null,
          loading: false,
          error: null,
          profileLoading: false,
        });
      },

      initializeFromCookies: () => {
        const cookieAccessToken = getCookie('accessToken');
        const cookieRefreshToken = getCookie('refreshToken');
        const { accessToken, refreshToken } = get();
        
        // If cookies have valid tokens but store doesn't, update store
        if (isValidToken(cookieAccessToken) && !accessToken) {
          set({
            accessToken: cookieAccessToken,
            refreshToken: cookieRefreshToken,
          });
        }
        // If store has tokens but cookies don't, clear store
        else if (!isValidToken(cookieAccessToken) && accessToken) {
          set({
            accessToken: null,
            refreshToken: null,
            profile: null,
          });
        }
      },
    }),
    {
      name: "user-store",
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        profile: state.profile,
      }),
    }
  )
);
