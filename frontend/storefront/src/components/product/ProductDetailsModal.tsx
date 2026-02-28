import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Table, TableBody, TableCell, TableRow } from "@/components/ui/table";
import { Product } from "@/interfaces";
import { useCartStore, useUserStore } from "@/stores";
import { getProductImageUrl } from "@/utils/productImage.util";
import { formatCurrency } from "@/utils/formatter.util";
import { Loader2 } from "lucide-react";
import Image from "next/image";
import React from "react";
import { toast } from "sonner";
import { useRouter } from "next/navigation";

interface ProductDetailsModalProps {
  isOpen: boolean;
  product?: Product | null;
  isLoading?: boolean;
  onClose: () => void;
}

const ProductDetailsModal: React.FC<ProductDetailsModalProps> = ({
  isOpen,
  product,
  isLoading = false,
  onClose,
}) => {
  const { addToCart } = useCartStore();
  const { isAuthenticated } = useUserStore();
  const router = useRouter();

  const handleAddToCart = async () => {
    // Check if user is authenticated
    if (!isAuthenticated()) {
      toast.info("Please login to add items to cart");
      router.push("/login");
      onClose();
      return;
    }

    if (product) {
      try {
        const success = await addToCart(product.id, 1);
        
        if (success) {
          toast.success(`${product.name} added to cart`);
          onClose();
        } else {
          toast.error("Failed to add product to cart");
        }
      } catch (error) {
        toast.error("Failed to add product to cart");
        console.error(error);
      }
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent 
        className="max-h-[90vh] overflow-y-auto" 
        style={{ maxWidth: '45vw', width: '45vw' }}
      >
        <DialogHeader>
          <DialogTitle>Product Details</DialogTitle>
        </DialogHeader>
        <div className="mt-4">
          {isLoading ? (
            <div className="flex justify-center py-8">
              <Loader2 className="h-8 w-8 animate-spin" />
            </div>
          ) : product ? (
            <div className="grid grid-cols-1 md:grid-cols-5 gap-6">
              <div className="md:col-span-2">
                <Image
                  src={getProductImageUrl(product.image)}
                  className="w-full h-96 object-cover object-center rounded-lg"
                  alt={product.name}
                  width={400}
                  height={384}
                  unoptimized
                />
              </div>
              <div className="space-y-4 md:col-span-3">
                <h3 className="text-2xl font-bold">{product.name}</h3>
                <p className="text-2xl font-semibold text-primary">
                  {formatCurrency(product.price)}
                </p>

                <div>
                  <h4 className="text-lg font-semibold mb-2">Description</h4>
                  <p className="text-muted-foreground">
                    {product.details?.description ||
                      "No description available."}
                  </p>
                </div>

                {product.attributes && product.attributes.length > 0 ? (
                  <div>
                    <h4 className="text-lg font-semibold mb-2">Attributes</h4>
                    <Table>
                      <TableBody>
                        {product.attributes.map((attribute) => (
                          <TableRow key={attribute.id}>
                            <TableCell className="font-medium text-muted-foreground">
                              {attribute.name}
                            </TableCell>
                            <TableCell>{attribute.value}</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>
                ) : (
                  <p className="text-muted-foreground">
                    No attributes available.
                  </p>
                )}

                <div>
                  <h4 className="text-lg font-semibold mb-2">Brand</h4>
                  {product.brand?.name ? (
                    <Badge variant="secondary">
                      {product.brand.name}
                    </Badge>
                  ) : (
                    <Badge variant="outline">
                      N/A
                    </Badge>
                  )}
                </div>

                <div>
                  <h4 className="text-lg font-semibold mb-2">Categories</h4>
                  {product.categories && product.categories.length > 0 ? (
                    <div className="flex flex-wrap gap-2">
                      {product.categories.map((category) => (
                        <Badge key={category.id} variant="secondary">
                          {category.name}
                        </Badge>
                      ))}
                    </div>
                  ) : (
                    <p className="text-muted-foreground">N/A</p>
                  )}
                </div>

                <Button onClick={handleAddToCart} className="w-full" size="lg">
                  Add to Cart
                </Button>
              </div>
            </div>
          ) : (
            <div className="text-center text-muted-foreground py-8">
              No product details available.
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ProductDetailsModal;
