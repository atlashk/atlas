'use client';

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Gauge, Package, ShoppingCart, Users, DollarSign, Zap, Plus } from 'lucide-react';

const Dashboard: React.FC = () => {
  return (
    <div className="container mx-auto px-4 py-8">
      <div className="grid grid-cols-1 gap-6">
        <Card>
          <CardContent className="text-center py-8">
            <div className="flex items-center justify-center gap-3 mb-4">
              <Gauge className="h-8 w-8 text-blue-600" />
              <h1 className="text-3xl font-bold">
                Welcome to Admin Dashboard
              </h1>
            </div>
            <p className="text-gray-600 text-lg">
              Manage your e-commerce platform from this central dashboard.
            </p>
          </CardContent>
        </Card>
      </div>
      
      {/* Quick Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mt-6">
        <Card className="bg-blue-600 text-white border-blue-600">
          <CardContent className="p-6">
            <div className="flex justify-between items-center">
              <div>
                <h6 className="text-blue-100 text-sm font-medium">Total Products</h6>
                <h3 className="text-2xl font-bold mt-1">--</h3>
              </div>
              <Package className="h-8 w-8 text-blue-200" />
            </div>
          </CardContent>
        </Card>
        
        <Card className="bg-green-600 text-white border-green-600">
          <CardContent className="p-6">
            <div className="flex justify-between items-center">
              <div>
                <h6 className="text-green-100 text-sm font-medium">Total Orders</h6>
                <h3 className="text-2xl font-bold mt-1">--</h3>
              </div>
              <ShoppingCart className="h-8 w-8 text-green-200" />
            </div>
          </CardContent>
        </Card>
        
        <Card className="bg-cyan-600 text-white border-cyan-600">
          <CardContent className="p-6">
            <div className="flex justify-between items-center">
              <div>
                <h6 className="text-cyan-100 text-sm font-medium">Total Users</h6>
                <h3 className="text-2xl font-bold mt-1">--</h3>
              </div>
              <Users className="h-8 w-8 text-cyan-200" />
            </div>
          </CardContent>
        </Card>
        
        <Card className="bg-amber-600 text-white border-amber-600">
          <CardContent className="p-6">
            <div className="flex justify-between items-center">
              <div>
                <h6 className="text-amber-100 text-sm font-medium">Revenue</h6>
                <h3 className="text-2xl font-bold mt-1">$--</h3>
              </div>
              <DollarSign className="h-8 w-8 text-amber-200" />
            </div>
          </CardContent>
        </Card>
      </div>
      
      {/* Quick Actions */}
      <div className="mt-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Zap className="h-5 w-5" />
              Quick Actions
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <Button variant="outline" className="h-auto p-4 flex flex-col items-center gap-2" asChild>
                <a href="/admin/products">
                  <Package className="h-6 w-6" />
                  <span>Manage Products</span>
                </a>
              </Button>
              <Button variant="outline" className="h-auto p-4 flex flex-col items-center gap-2" asChild>
                <a href="/admin/orders">
                  <ShoppingCart className="h-6 w-6" />
                  <span>View Orders</span>
                </a>
              </Button>
              <Button variant="outline" className="h-auto p-4 flex flex-col items-center gap-2" asChild>
                <a href="/admin/users">
                  <Users className="h-6 w-6" />
                  <span>Manage Users</span>
                </a>
              </Button>
              <Button variant="outline" className="h-auto p-4 flex flex-col items-center gap-2" asChild>
                <a href="/admin/products/add">
                  <Plus className="h-6 w-6" />
                  <span>Add Product</span>
                </a>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Dashboard;