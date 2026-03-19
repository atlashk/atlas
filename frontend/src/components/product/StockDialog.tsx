"use client";

import { inventoryApi, type RetrieveStockResponse } from "@/api/index.api";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Spinner } from "@/components/ui/spinner";
import React, { useCallback, useEffect, useState } from "react";
import { toast } from "sonner";

interface StockDialogProps {
  isVisible: boolean;
  productId: string | null;
  onClose: () => void;
}

const StockDialog: React.FC<StockDialogProps> = ({
  isVisible,
  productId,
  onClose,
}) => {
  const [stockData, setStockData] = useState<RetrieveStockResponse | null>(null);
  const [availableQuantityInput, setAvailableQuantityInput] = useState("");
  const [isLoadingStock, setIsLoadingStock] = useState(false);
  const [isUpdatingStock, setIsUpdatingStock] = useState(false);

  const resetLocalState = useCallback(() => {
    setStockData(null);
    setAvailableQuantityInput("");
    setIsLoadingStock(false);
    setIsUpdatingStock(false);
  }, []);

  const handleClose = useCallback(() => {
    resetLocalState();
    onClose();
  }, [onClose, resetLocalState]);

  useEffect(() => {
    if (!isVisible || !productId) {
      resetLocalState();
      return;
    }

    let isActive = true;

    const fetchStock = async () => {
      setIsLoadingStock(true);
      try {
        const response = await inventoryApi.retrieveStock(productId);
        if (!response.success) {
          toast.error(response.errorMessage || "Failed to load stock");
          if (isActive) {
            handleClose();
          }
          return;
        }

        if (isActive) {
          const data = response.data;
          setStockData(data);
          setAvailableQuantityInput(String(data?.availableQuantity ?? 0));
        }
      } catch {
        toast.error("Failed to load stock");
        if (isActive) {
          handleClose();
        }
      } finally {
        if (isActive) {
          setIsLoadingStock(false);
        }
      }
    };

    fetchStock();

    return () => {
      isActive = false;
    };
  }, [handleClose, isVisible, productId, resetLocalState]);

  const handleUpdateAvailableQuantity = useCallback(async () => {
    if (!productId) return;

    const availableQuantity = Number.parseInt(availableQuantityInput, 10);
    if (!Number.isFinite(availableQuantity) || availableQuantity < 0) {
      toast.error("Available quantity must be zero or positive");
      return;
    }

    setIsUpdatingStock(true);
    try {
      const response = await inventoryApi.updateAvailableQuantity(productId, {
        availableQuantity,
      });

      if (!response.success) {
        toast.error(response.errorMessage || "Failed to update stock");
        return;
      }

      toast.success("Stock updated successfully");

      const refreshed = await inventoryApi.retrieveStock(productId);
      if (refreshed.success) {
        setStockData(refreshed.data);
        setAvailableQuantityInput(
          String(refreshed.data?.availableQuantity ?? availableQuantity)
        );
      } else {
        setStockData((prev) => (prev ? { ...prev, availableQuantity } : prev));
      }
    } catch {
      toast.error("Failed to update stock");
    } finally {
      setIsUpdatingStock(false);
    }
  }, [availableQuantityInput, productId]);

  return (
    <Dialog
      open={isVisible}
      onOpenChange={(open) => {
        if (!open) {
          handleClose();
        }
      }}
    >
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Stock</DialogTitle>
          <DialogDescription>
            {productId ? `Product ID: ${productId}` : ""}
          </DialogDescription>
        </DialogHeader>

        {isLoadingStock ? (
          <div className="flex justify-center py-8">
            <Spinner className="text-blue-600" />
          </div>
        ) : stockData ? (
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="availableQuantity">Available Quantity</Label>
              <Input
                id="availableQuantity"
                type="number"
                min="0"
                value={availableQuantityInput}
                onChange={(e) => setAvailableQuantityInput(e.target.value)}
                disabled={isUpdatingStock}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="reservedQuantity">Reserved Quantity</Label>
              <Input
                id="reservedQuantity"
                value={String(stockData.reservedQuantity ?? 0)}
                disabled
              />
            </div>
          </div>
        ) : (
          <div className="text-sm text-muted-foreground">
            No stock data available
          </div>
        )}

        <DialogFooter>
          <Button
            variant="outline"
            onClick={handleClose}
            disabled={isUpdatingStock}
          >
            Cancel
          </Button>
          <Button
            onClick={handleUpdateAvailableQuantity}
            disabled={!productId || !stockData || isLoadingStock || isUpdatingStock}
          >
            {isUpdatingStock && <Spinner className="mr-2" />}
            Save
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default StockDialog;
