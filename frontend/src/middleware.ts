import { NextRequest, NextResponse } from 'next/server';
import { userApi } from './api/user';

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Define public routes that don't require authentication
  const PUBLIC_ROUTES = [
    '/',
    '/login',
    '/register'
  ];

  // Define protected routes that require authentication
  const PROTECTED_ROUTES_PREFIXES = [
    '/admin',
    '/admin/dashboard',
  ];

  const isPublicRoute = PUBLIC_ROUTES.some(route => {
    if (route === '/') {
      return pathname === '/';
    }
    return pathname.startsWith(route);
  });

  const isProtectedRoute = PROTECTED_ROUTES_PREFIXES.some(prefix => pathname.startsWith(prefix));

  const accessToken = request.cookies.get('accessToken')?.value;

  // If the user is trying to access a public route but is already authenticated, redirect based on user role
  if (isPublicRoute && accessToken) {
    try {
      const response = await userApi.getProfile();

      if (response.success) {
        const userProfile = response.data;
        if (userProfile.role === 'ADMIN') {
          return NextResponse.redirect(new URL('/admin/dashboard', request.url));
        } else if (userProfile.role === 'USER') {
          return NextResponse.redirect(new URL('/', request.url));
        }
      } else {
        // If profile fetch fails, redirect to login to re-authenticate or handle error
        const loginUrl = new URL('/login', request.url);
        loginUrl.searchParams.set('redirect', pathname);
        return NextResponse.redirect(loginUrl);
      }
    } catch (error: any) {
      console.error('Error fetching user profile:', error.message);
      const loginUrl = new URL('/login', request.url);
      loginUrl.searchParams.set('redirect', pathname);
      return NextResponse.redirect(loginUrl);
    }
  }

  // Skip middleware for static files and API routes (except auth)
  if (
    pathname.startsWith('/_next/') ||
    pathname.startsWith('/static/') ||
    pathname.includes('.') ||
    (pathname.startsWith('/api/') &&
      !pathname.startsWith('/api/auth/login') &&
      !pathname.startsWith('/api/auth/register') &&
      !pathname.startsWith('/api/auth/refresh-token'))
  ) {
    return NextResponse.next();
  }

  // If the user is trying to access a protected route and is not authenticated, redirect to login
  if (isProtectedRoute && !accessToken) {
    const loginUrl = new URL('/login', request.url);
    loginUrl.searchParams.set('redirect', pathname);
    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
};
