import { Role } from "@/constants";
import { AUTH_STORAGE_KEYS } from "@/constants/auth.constants";
import type { LoginRequest } from "@/interfaces/auth.interface";
import type { RegisterRequest, User } from "@/interfaces/iam.interface";
import { authService } from "@/services/auth.service";
import { tokenManager } from "@/services/token.service";
import { createLogger } from "@/utils/logger";
import { isValidToken } from "@/utils/cookies";
import { create } from "zustand";
import { persist } from "zustand/middleware";
import { useCartStore } from "./cart.store";

const logger = createLogger('UserStore');

interface UserState {
  profile: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  loading: boolean;
  error: string | null;
  profileLoading: boolean;
  profileLastFetched: number | null;
}

interface UserActions {
  isAuthenticated: () => boolean;
  isAdmin: () => boolean;
  fullName: () => string;
  hasRole: (role: string) => boolean;
  authState: () => {
    isAuthenticated: boolean;
    isAdmin: boolean;
    user: User | null;
  };

  login: (
    credentials: LoginRequest
  ) => Promise<{ success: boolean; errorMessage?: string; userRole?: Role }>;
  register: (userData: RegisterRequest) => Promise<void>;
  fetchProfile: (options?: { force?: boolean; skipCache?: boolean }) => Promise<void>;
  setTokens: (accessToken: string, refreshToken: string) => void;
  logout: () => void;
  clearError: () => void;
  clearAuthState: () => void;
  initializeFromCookies: () => void;
}

type UserStore = UserState & UserActions;

export const useUserStore = create<UserStore>()(
  persist(
    (set, get) => ({
      profile: null,
      accessToken: null,
      refreshToken: null,
      loading: false,
      error: null,
      profileLoading: false,
      profileLastFetched: null,

      isAuthenticated: () => {
        const { accessToken } = get();
        return isValidToken(accessToken);
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

      login: async (request: LoginRequest) => {
        set({ loading: true, error: null });
        
        try {
          const result = await authService.login(request);
          
          if (result.success && result.accessToken && result.refreshToken) {
            tokenManager.setTokens({
              accessToken: result.accessToken,
              refreshToken: result.refreshToken,
            });

            set({
              accessToken: result.accessToken,
              refreshToken: result.refreshToken,
              profile: null,
              loading: false,
            });

            try {
              await get().fetchProfile();
              const { profile } = get();
              
              return {
                success: true,
                userRole: profile?.role,
              };
            } catch (profileError) {
              logger.warn('Login successful but failed to fetch user profile', profileError);
              return {
                success: true,
                userRole: undefined,
              };
            }
          } else {
            set({
              error: result.errorMessage || "Login failed",
              loading: false,
            });
            return {
              success: false,
              errorMessage: result.errorMessage || "Login failed",
            };
          }
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : "Login failed";
          logger.error('Login failed', error);
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
          const { iamFrontApi } = await import('@/api/index.api');
          const response = await iamFrontApi.register(userData);
          
          if (response.success) {
            set({ loading: false });
          } else {
            set({
              error: response.errorMessage || "Registration failed",
              loading: false,
            });
          }
        } catch (error) {
          logger.error('Registration failed', error);
          set({
            error: error instanceof Error ? error.message : "Registration failed",
            loading: false,
          });
        }
      },

      fetchProfile: async (options?: { force?: boolean; skipCache?: boolean }) => {
        const { profile, profileLoading, accessToken, profileLastFetched } = get();
        
        if (!isValidToken(accessToken)) {
          logger.info('No valid token, skipping profile fetch');
          return;
        }
        
        if (profileLoading) {
          logger.info('Profile fetch already in progress');
          return;
        }

        const isCacheValid = authService.isProfileCacheValid(profileLastFetched);

        if (!options?.skipCache && !options?.force && profile && isCacheValid) {
          logger.info('Using cached profile');
          return;
        }

        logger.info('Fetching user profile...');
        set({ profileLoading: true, error: null });
        
        try {
          const result = await authService.fetchProfile(options);
          
          if (result.success && result.profile) {
            set({
              profile: result.profile,
              profileLoading: false,
              profileLastFetched: Date.now(),
              error: null,
            });
          } else {
            logger.error('Failed to fetch profile', result.errorMessage);
            set({ 
              profileLoading: false,
              error: result.errorMessage,
            });
            tokenManager.clearTokens();
            set({
              accessToken: null,
              refreshToken: null,
              profile: null,
              profileLastFetched: null,
            });
          }
        } catch (error) {
          logger.error('Profile fetch error', error);
          set({ 
            profileLoading: false,
            error: error instanceof Error ? error.message : 'Failed to load user profile',
          });
          tokenManager.clearTokens();
          set({
            accessToken: null,
            refreshToken: null,
            profile: null,
            profileLastFetched: null,
          });
        }
      },

      setTokens: (accessToken: string, refreshToken: string) => {
        tokenManager.setTokens({ accessToken, refreshToken });
        set({ accessToken, refreshToken });
      },

      logout: async () => {
        logger.info('Logout initiated');
        await authService.logout();
        tokenManager.clearTokens();
        
        try {
          const { clearCartState } = useCartStore.getState();
          clearCartState();
        } catch (error) {
          logger.warn('Failed to clear cart state during logout', error);
        }
        
        set({
          profile: null,
          accessToken: null,
          refreshToken: null,
          loading: false,
          error: null,
          profileLoading: false,
          profileLastFetched: null,
        });
        
        logger.info('Logout completed, redirecting to login');
        
        if (typeof window !== 'undefined') {
          window.location.href = '/login';
        }
      },

      clearError: () => {
        set({ error: null });
      },

      clearAuthState: () => {
        tokenManager.clearTokens();
        set({
          profile: null,
          accessToken: null,
          refreshToken: null,
          loading: false,
          error: null,
          profileLoading: false,
          profileLastFetched: null,
        });
      },

      initializeFromCookies: () => {
        logger.info('Initializing from cookies...');
        
        const cookieAccessToken = tokenManager.getAccessToken();
        const cookieRefreshToken = tokenManager.getRefreshToken();
        const { accessToken } = get();
        
        if (isValidToken(cookieAccessToken) && !accessToken) {
          logger.info('Syncing valid tokens from cookies to store');
          set({
            accessToken: cookieAccessToken,
            refreshToken: cookieRefreshToken,
          });
        } else if (!isValidToken(cookieAccessToken) && accessToken) {
          logger.info('Cookies invalid, clearing store');
          set({
            accessToken: null,
            refreshToken: null,
            profile: null,
            profileLastFetched: null,
          });
        } else if (isValidToken(cookieAccessToken) && isValidToken(accessToken)) {
          logger.info('Store and cookies are in sync');
        } else {
          logger.info('No valid tokens found');
        }
      },
    }),
    {
      name: AUTH_STORAGE_KEYS.USER_STORE,
      skipHydration: true,
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        profile: state.profile,
        profileLastFetched: state.profileLastFetched,
      }),
    }
  )
);
