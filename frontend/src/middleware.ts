import { NextRequest, NextResponse } from 'next/server';
import { jwtDecode } from 'jwt-decode';

interface JWTPayload {
  sub: string;
  roles: string[];
  exp: number;
  iat: number;
}

// Define protected routes and their required roles
const PROTECTED_ROUTES = {
  '/admin': ['ADMIN'],
  '/admin/dashboard': ['ADMIN'],
  '/admin/users': ['ADMIN'],
  '/admin/settings': ['ADMIN'],
  '/profile': ['USER', 'ADMIN'],
  '/dashboard': ['USER', 'ADMIN']
};

// Public routes that don't require authentication
const PUBLIC_ROUTES = [
  '/',
  '/login',
  '/register',
  '/about',
  '/contact',
  '/api/auth/login',
  '/api/auth/register',
  '/api/auth/refresh-token'
];

function isTokenExpired(token: string): boolean {
  try {
    const decoded = jwtDecode<JWTPayload>(token);
    const currentTime = Date.now() / 1000;
    return decoded.exp < currentTime;
  } catch {
    return true;
  }
}

function getUserRoles(token: string): string[] {
  try {
    const decoded = jwtDecode<JWTPayload>(token);
    return decoded.roles || [];
  } catch {
    return [];
  }
}

function hasRequiredRole(userRoles: string[], requiredRoles: string[]): boolean {
  return requiredRoles.some(role => userRoles.includes(role));
}

function isPublicRoute(pathname: string): boolean {
  return PUBLIC_ROUTES.some(route => {
    if (route === '/') {
      return pathname === '/';
    }
    return pathname.startsWith(route);
  });
}

function getRequiredRoles(pathname: string): string[] | null {
  // Check exact matches first
  if (PROTECTED_ROUTES[pathname as keyof typeof PROTECTED_ROUTES]) {
    return PROTECTED_ROUTES[pathname as keyof typeof PROTECTED_ROUTES];
  }
  
  // Check for prefix matches (e.g., /admin/anything should require ADMIN role)
  for (const [route, roles] of Object.entries(PROTECTED_ROUTES)) {
    if (pathname.startsWith(route + '/') || pathname === route) {
      return roles;
    }
  }
  
  return null;
}

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  
  // Skip middleware for static files and API routes (except auth)
  if (
    pathname.startsWith('/_next/') ||
    pathname.startsWith('/static/') ||
    pathname.includes('.') ||
    (pathname.startsWith('/api/') && !pathname.startsWith('/api/auth/'))
  ) {
    return NextResponse.next();
  }
  
  // Allow public routes
  if (isPublicRoute(pathname)) {
    return NextResponse.next();
  }
  
  // Get access token from cookies or headers
  const accessToken = request.cookies.get('accessToken')?.value || 
                     request.headers.get('authorization')?.replace('Bearer ', '');
  
  // Check if route requires authentication
  const requiredRoles = getRequiredRoles(pathname);
  
  if (requiredRoles) {
    // Route requires authentication
    if (!accessToken) {
      const loginUrl = new URL('/login', request.url);
      loginUrl.searchParams.set('redirect', pathname);
      return NextResponse.redirect(loginUrl);
    }
    
    // Check if token is expired
    if (isTokenExpired(accessToken)) {
      const loginUrl = new URL('/login', request.url);
      loginUrl.searchParams.set('redirect', pathname);
      loginUrl.searchParams.set('expired', 'true');
      return NextResponse.redirect(loginUrl);
    }
    
    // Check if user has required roles
    const userRoles = getUserRoles(accessToken);
    if (!hasRequiredRole(userRoles, requiredRoles)) {
      // Redirect to appropriate page based on user roles
      if (userRoles.includes('ADMIN')) {
        return NextResponse.redirect(new URL('/admin/dashboard', request.url));
      } else if (userRoles.includes('USER')) {
        return NextResponse.redirect(new URL('/dashboard', request.url));
      } else {
        return NextResponse.redirect(new URL('/', request.url));
      }
    }
  }
  
  // If user is authenticated and trying to access login/register, redirect to appropriate dashboard
  if (accessToken && !isTokenExpired(accessToken) && (pathname === '/login' || pathname === '/register')) {
    const userRoles = getUserRoles(accessToken);
    if (userRoles.includes('ADMIN')) {
      return NextResponse.redirect(new URL('/admin/dashboard', request.url));
    } else if (userRoles.includes('USER')) {
      return NextResponse.redirect(new URL('/dashboard', request.url));
    }
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
