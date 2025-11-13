import { NextRequest, NextResponse } from 'next/server';
import { isValidToken } from '@/utils/cookies';

export async function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Skip middleware for static files and API routes
  if (
    pathname.startsWith('/_next/') ||
    pathname.startsWith('/static/') ||
    pathname.includes('.') ||
    pathname.startsWith('/api/')
  ) {
    return NextResponse.next();
  }

  // Define protected routes that require authentication
  const ADMIN_ROUTES = ['/admin'];
  const isAdminRoute = ADMIN_ROUTES.some(route => pathname.startsWith(route));

  const accessToken = request.cookies.get('accessToken')?.value;
  const hasValidToken = isValidToken(accessToken ?? null);

  // Redirect unauthenticated users from admin routes to login
  if (!hasValidToken && isAdminRoute) {
    const loginUrl = new URL('/login', request.url);
    loginUrl.searchParams.set('redirect', pathname);
    return NextResponse.redirect(loginUrl);
  }

  // Let client-side handle guest-only route redirects for better UX
  // This allows proper role-based redirects after authentication
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
