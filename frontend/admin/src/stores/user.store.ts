import type { ChangePasswordRequest, LoginRequest, User } from "@/interfaces/identity.interface";
import { createLogger } from "@/utils/logger";
import { clearAuthCookies, getCookie, isValidToken, setCookie } from "@/utils/cookies";
import { create } from "zustand";
import { persist } from "zustand/middleware";
import { identityApi } from "@/api/authorization.api";

const AUTH_STORAGE_KEYS = {
  USER_STORE: 'user-store',
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
} as const;

const logger = createLogger('UserStore');

interface UserState {
  profile: User | null;
  accessToken: string | null;
  loading: boolean;
  error: string | null;
  profileLoading: boolean;
}

interface UserActions {
  isAuthenticated: () => boolean;

  login: (
    credentials: LoginRequest
  ) => Promise<{ success: boolean; errorMessage?: string }>;
  fetchProfile: () => Promise<void>;
  changePassword: (
    request: ChangePasswordRequest
  ) => Promise<{ success: boolean; errorMessage?: string }>;
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
      loading: false,
      error: null,
      profileLoading: false,

      isAuthenticated: () => {
        const { accessToken } = get();
        return isValidToken(accessToken);
      },

      login: async (request: LoginRequest) => {
        set({ loading: true, error: null });
        
        try {
          const response = await identityApi.login(request);

          if (response.success && response.data) {
            setCookie(AUTH_STORAGE_KEYS.ACCESS_TOKEN, response.data.accessToken);
            setCookie(AUTH_STORAGE_KEYS.REFRESH_TOKEN, response.data.refreshToken);

            set({
              accessToken: response.data.accessToken,
              profile: null,
              loading: false,
            });

            try {
              await get().fetchProfile();
              return { success: true };
            } catch (profileError) {
              logger.warn('Login successful but failed to fetch user profile', profileError);
              return {
                success: true,
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
          const errorMessage = error instanceof Error ? error.message : "Login failed";
          logger.error('Login failed', error);
          set({
            error: errorMessage,
            loading: false,
          });
          return { success: false, errorMessage };
        }
      },

      fetchProfile: async () => {
        const { profileLoading, accessToken } = get();
        
        if (!isValidToken(accessToken)) {
          logger.info('No valid token, skipping profile fetch');
          return;
        }
        
        if (profileLoading) {
          logger.info('Profile fetch already in progress');
          return;
        }

        logger.info('Fetching user profile...');
        set({ profileLoading: true, error: null });
        
        try {
          const result = await identityApi.retrieveProfile();
          
          if (result.success && result.data) {
            set({
              profile: result.data,
              profileLoading: false,
              error: null,
            });
          } else {
            logger.error('Failed to fetch profile', result.errorMessage);
            set({ 
              profileLoading: false,
              error: result.errorMessage,
            });
            clearAuthCookies();
            set({
              accessToken: null,
              profile: null,
            });
          }
        } catch (error) {
          logger.error('Profile fetch error', error);
          set({ 
            profileLoading: false,
            error: error instanceof Error ? error.message : 'Failed to load user profile',
          });
          clearAuthCookies();
          set({
            accessToken: null,
            profile: null,
          });
        }
      },

      changePassword: async (request: ChangePasswordRequest) => {
        logger.info("Change password initiated");
        try {
          const result = await identityApi.changePassword(request);
          if (result.success) {
            logger.info("Change password completed");
            return { success: true };
          }
          logger.warn("Change password failed", result.errorMessage);
          return { success: false, errorMessage: result.errorMessage || "Change password failed" };
        } catch (error) {
          logger.error("Change password error", error);
          return {
            success: false,
            errorMessage: error instanceof Error ? error.message : "Change password failed",
          };
        }
      },

      logout: async () => {
        logger.info('Logout initiated');
        try {
          await identityApi.logout();
          logger.info('Logout completed');
        } catch (error) {
          logger.error('Logout error', error);
        }
        clearAuthCookies();
        
        set({
          profile: null,
          accessToken: null,
          loading: false,
          error: null,
          profileLoading: false,
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
        clearAuthCookies();
        set({
          profile: null,
          accessToken: null,
          loading: false,
          error: null,
          profileLoading: false,
        });
      },

      initializeFromCookies: () => {
        logger.info('Initializing from cookies...');
        
        const cookieAccessToken = getCookie(AUTH_STORAGE_KEYS.ACCESS_TOKEN);
        const { accessToken } = get();
        
        if (isValidToken(cookieAccessToken) && !accessToken) {
          logger.info('Syncing valid tokens from cookies to store');
          set({
            accessToken: cookieAccessToken,
          });
        } else if (!isValidToken(cookieAccessToken) && accessToken) {
          logger.info('Cookies invalid, clearing store');
          set({
            accessToken: null,
            profile: null,
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
        profile: state.profile,
      }),
    }
  )
);
