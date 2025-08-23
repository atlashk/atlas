import { authApi, userApi } from "@/api";
import type { LoginRequest } from "@/interfaces/auth.interface";
import type { RegisterRequest, User } from "@/interfaces/user.interface";
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
  ) => Promise<{ success: boolean; errorMessage?: string; isAdmin?: boolean }>;
  register: (userData: RegisterRequest) => Promise<void>;
  fetchProfile: () => Promise<void>;
  setTokens: (accessToken: string, refreshToken: string) => void;
  logout: () => void;
  clearError: () => void;
  clearAuthState: () => void;
}

type UserStore = UserState & UserActions;

// Utility function to clear authentication tokens
const clearAuthTokens = () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
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
        return !!accessToken;
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

            // Store tokens in both localStorage and Zustand store
            localStorage.setItem("accessToken", accessToken);
            localStorage.setItem("refreshToken", refreshToken);

            set({
              accessToken,
              refreshToken,
              loading: false,
            });
            // Fetch profile after successful login
            await get().fetchProfile();
            const { isAdmin } = get();
            return { success: true, isAdmin: isAdmin() };
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
        const { accessToken } = get();
        if (!accessToken) return;

        set({ profileLoading: true });
        try {
          const response = await userApi.getProfile();
          if (response.success && response.data) {
            set({
              profile: response.data,
              profileLoading: false,
            });
          } else {
            set({
              error: response.errorMessage || "Failed to fetch profile",
              profileLoading: false,
            });
          }
        } catch (error) {
          set({
            error:
              error instanceof Error
                ? error.message
                : "Failed to fetch profile",
            profileLoading: false,
          });
        }
      },

      setTokens: (accessToken: string, refreshToken: string) => {
        set({ accessToken, refreshToken });
      },

      logout: () => {
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
