// Cookie utilities - extracted to avoid circular dependencies

export const getCookie = (name: string): string | null => {
  if (typeof window === 'undefined') return null;
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop()?.split(';').shift() || null;
  return null;
};

export const setCookie = (name: string, value: string, options: { secure?: boolean; sameSite?: string; path?: string } = {}) => {
  if (typeof window === 'undefined') return;
  const { secure = window.location.protocol === 'https:', sameSite = 'strict', path = '/' } = options;
  document.cookie = `${name}=${value}; path=${path}; ${secure ? 'secure;' : ''} samesite=${sameSite}`;
};

export const deleteCookie = (name: string) => {
  if (typeof window === 'undefined') return;
  document.cookie = `${name}=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT`;
};

export const clearAuthCookies = () => {
  deleteCookie('accessToken');
  deleteCookie('refreshToken');
};

export const isValidToken = (token: string | null): boolean => {
  return !!token && token.length > 0;
};
