'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useUserStore } from '@/stores/user.store'

export default function AdminProductAddPage() {
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
                onClick={() => router.push('/admin/product')}
                className="text-indigo-600 hover:text-indigo-500"
              >
                ← Back to Products
              </button>
              <h1 className="text-3xl font-bold text-gray-900">Add Product</h1>
            </div>
            <button
              onClick={handleLogout}
              className="bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="bg-white shadow rounded-lg p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Create New Product</h2>
          <p className="text-gray-600">
            Product creation form will be implemented here.
          </p>
          <div className="mt-4">
            <div className="text-sm text-gray-500">
              Form fields to be implemented:
              <ul className="list-disc list-inside mt-2 space-y-1">
                <li>Product name and description</li>
                <li>Price and SKU</li>
                <li>Category and brand selection</li>
                <li>Product images upload</li>
                <li>Inventory management</li>
                <li>Product attributes and specifications</li>
              </ul>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}