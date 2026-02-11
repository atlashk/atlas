import { useUserStore } from './user.store';

export const useAuth = () => useUserStore(state => ({
  isAuthenticated: state.isAuthenticated(),
  isAdmin: state.isAdmin(),
  user: state.profile,
  loading: state.loading || state.profileLoading,
  error: state.error,
}));

export const useAuthActions = () => useUserStore(state => ({
  login: state.login,
  logout: state.logout,
  fetchProfile: state.fetchProfile,
  clearError: state.clearError,
}));

export const useUserProfile = () => useUserStore(state => state.profile);

export const useAuthLoading = () => useUserStore(state => state.loading || state.profileLoading);

export const useIsAuthenticated = () => useUserStore(state => state.isAuthenticated());

export const useIsAdmin = () => useUserStore(state => state.isAdmin());

export const useUserRole = () => useUserStore(state => state.profile?.role);
