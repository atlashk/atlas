'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useUserStore } from '@/stores/user.store'

export default function AdminProductListPage() {
  const router = useRouter()
  const { isAuthenticated, isAdmin, logout } = useUserStore()
  
  useEffect(() => {
    // Redirect if not authenticated or not admin
    if (!isAuthenticated() || !isAdmin()) {
      router.push('/login')
      return
    }
  }, [isAuthenticated, isAdmin, router])
  
  const handleLogout = () => {
    logout()
    router.push('/login')
  }
  
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div className="flex items-center space-x-4">
              <button
                onClick={() => router.push('/admin/dashboard')}
                className="text-indigo-600 hover:text-indigo-500"
              >
                ← Back to Dashboard
              </button>
              <h1 className="text-3xl font-bold text-gray-900">Product Management</h1>
            </div>
            <div className="flex items-center space-x-4">
              <button
                onClick={() => router.push('/admin/product/add')}
                className="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 transition-colors"
              >
                Add Product
              </button>
              <button
                onClick={handleLogout}
                className="bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 transition-colors"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Products</h2>
          <p className="text-gray-600">
            Product management functionality will be implemented here.
          </p>
          <div className="mt-4">
            <div className="text-sm text-gray-500">
              Features to be implemented:
              <ul className="list-disc list-inside mt-2 space-y-1">
                <li>View all products</li>
                <li>Search and filter products</li>
                <li>Edit product details</li>
                <li>Delete products</li>
                <li>Import/Export products</li>
                <li>Manage product categories and brands</li>
              </ul>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}