import React from 'react';
import { Card, CardContent, CardFooter, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Product } from '@/interfaces';
import { formatCurrency } from '@/utils/formatter.util';
import { getProductImageUrl } from '@/utils/productImage.util';
import Image from 'next/image';

interface ProductCardProps {
  product: Product;
  onProductClick: (product: Product) => void;
  onAddToCart: (product: Product) => void;
}

const ProductCard: React.FC<ProductCardProps> = ({
  product,
  onProductClick,
  onAddToCart
}) => {
  return (
    <Card className="hover:shadow-md transition-shadow duration-200 overflow-hidden p-0">
      <div className="aspect-[4/3] relative overflow-hidden">
        <Image
          src={getProductImageUrl(product.image)}
          alt={product.name}
          fill
          className="object-cover hover:scale-105 transition-transform duration-200"
          sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
        />
      </div>
      <CardContent className="p-3 py-0">
        <CardTitle
          className="text-base font-semibold text-gray-900 mb-1 line-clamp-2 cursor-pointer hover:text-blue-600 hover:underline transition-colors"
          onClick={() => onProductClick(product)}
        >
          {product.name}
        </CardTitle>
        <p className="text-lg font-bold text-blue-600">
          {formatCurrency(product.price)}
        </p>
      </CardContent>
      <CardFooter className="p-3 pt-0">
        <Button
          onClick={() => onAddToCart(product)}
          className="w-full"
        >
          Add to Cart
        </Button>
      </CardFooter>
    </Card>
  );
};

export default ProductCard;