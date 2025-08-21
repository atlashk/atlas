'use client'

import { useRouter } from 'next/navigation'
import { useUserStore } from '@/stores/user.store'
import { Button } from '@/components/ui/button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Menu } from 'lucide-react'

export default function NavBar() {
  const router = useRouter()
  const { isAuthenticated, isAdmin, fullName, logout, loading } = useUserStore()
  
  const handleBrandClick = () => {
    if (!isAuthenticated()) {
      router.push('/')
    } else if (isAdmin()) {
      router.push('/admin/dashboard')
    } else {
      router.push('/')
    }
  }
  
  const handleLogout = async () => {
    logout()
    router.push('/')
  }
  
  return (
    <nav className="bg-primary border-b fixed top-0 left-0 right-0 z-50 mb-4">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Brand */}
          <div className="flex items-center">
            <Button
              variant="ghost"
              onClick={handleBrandClick}
              className="text-xl font-bold text-white hover:text-gray-300"
            >
              Atlas
            </Button>
          </div>

          {/* Navigation Links */}
          <div className="hidden md:block">
            <div className="ml-10 flex items-baseline space-x-2">
              {isAuthenticated() && isAdmin() && (
                <>
                  <Button
                    variant="ghost"
                    onClick={() => router.push('/admin/user')}
                    className="text-gray-300 hover:bg-gray-700 hover:text-white"
                  >
                    User Management
                  </Button>
                  <Button
                    variant="ghost"
                    onClick={() => router.push('/admin/product')}
                    className="text-gray-300 hover:bg-gray-700 hover:text-white"
                  >
                    Product Management
                  </Button>
                  <Button
                    variant="ghost"
                    onClick={() => router.push('/admin/order')}
                    className="text-gray-300 hover:bg-gray-700 hover:text-white"
                  >
                    Order Management
                  </Button>
                </>
              )}
            </div>
          </div>

          {/* Right side - User info & auth buttons */}
          <div className="flex items-center space-x-4">
            {isAuthenticated() ? (
              <>
                <span className="text-gray-300 text-sm">
                  Welcome, {fullName()}
                </span>
                <Button
                  variant="destructive"
                  onClick={handleLogout}
                  disabled={loading}
                  size="sm"
                >
                  {loading ? (
                    <div className="flex items-center">
                      <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                      Logging out...
                    </div>
                  ) : (
                    'Logout'
                  )}
                </Button>
              </>
            ) : (
              <>
                <Button
                  onClick={() => router.push('/login')}
                  size="sm"
                  className="bg-white text-gray-900 hover:bg-gray-100"
                >
                  Login
                </Button>
                <Button
                  variant="secondary"
                  onClick={() => router.push('/register')}
                  size="sm"
                  className="bg-blue-600 text-white hover:bg-blue-700"
                >
                  Register
                </Button>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Mobile menu */}
      {isAuthenticated() && isAdmin() && (
        <div className="md:hidden">
          <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3 bg-gray-700">
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="sm" className="text-gray-300">
                  <Menu className="h-4 w-4" />
                  Menu
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={() => router.push('/admin/user')}>
                  User Management
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => router.push('/admin/product')}>
                  Product Management
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => router.push('/admin/order')}>
                  Order Management
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>
      )}
    </nav>
  )
}
