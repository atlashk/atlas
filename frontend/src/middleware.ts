import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

// Define protected routes
const adminRoutes = [
  '/admin/dashboard',
  '/admin/user',
  '/admin/product',
  '/admin/order'
]

const authRoutes = ['/login', '/register']

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl
  
  // Get tokens from cookies
  const accessToken = request.cookies.get('user-store')?.value
  let userStore = null
  
  try {
    if (accessToken) {
      userStore = JSON.parse(accessToken)
    }
  } catch (error) {
    console.error('Failed to parse user store from cookies:', error)
  }
  
  const isAuthenticated = !!(userStore?.state?.accessToken)
  const isAdmin = userStore?.state?.profile?.role === 'ADMIN'
  
  // Redirect authenticated users away from auth pages
  if (authRoutes.includes(pathname) && isAuthenticated) {
    return NextResponse.redirect(new URL('/', request.url))
  }
  
  // Check admin routes
  if (adminRoutes.some(route => pathname.startsWith(route))) {
    if (!isAuthenticated) {
      return NextResponse.redirect(new URL('/login', request.url))
    }
    if (!isAdmin) {
      return NextResponse.redirect(new URL('/', request.url))
    }
  }
  
  // Auto-redirect admin users from home to dashboard
  if (pathname === '/' && isAuthenticated && isAdmin) {
    return NextResponse.redirect(new URL('/admin/dashboard', request.url))
  }
  
  return NextResponse.next()
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
}