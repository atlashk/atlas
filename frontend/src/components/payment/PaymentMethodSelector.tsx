'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Label } from '@/components/ui/label';
import { CreditCard, Wallet } from 'lucide-react';

interface PaymentMethodSelectorProps {
  selectedMethod: string;
  onMethodChange: (method: string) => void;
}

export default function PaymentMethodSelector({
  selectedMethod,
  onMethodChange
}: PaymentMethodSelectorProps) {
  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="text-lg flex items-center gap-2">
          <Wallet className="w-5 h-5" />
          Payment Method
        </CardTitle>
      </CardHeader>
      <CardContent>
        <RadioGroup value={selectedMethod} onValueChange={onMethodChange}>
          <div className="flex items-center space-x-2 p-3 border rounded-lg hover:bg-gray-50 transition-colors">
            <RadioGroupItem value="STRIPE" id="stripe" />
            <Label htmlFor="stripe" className="flex items-center gap-2 cursor-pointer flex-1">
              <CreditCard className="w-4 h-4" />
              <div>
                <div className="font-medium">Credit/Debit Card</div>
                <div className="text-sm text-gray-500">Pay securely with Stripe</div>
              </div>
            </Label>
          </div>
          
          <div className="flex items-center space-x-2 p-3 border rounded-lg hover:bg-gray-50 transition-colors opacity-50">
            <RadioGroupItem value="COD" id="cod" disabled />
            <Label htmlFor="cod" className="flex items-center gap-2 cursor-pointer flex-1">
              <Wallet className="w-4 h-4" />
              <div>
                <div className="font-medium">Cash on Delivery</div>
                <div className="text-sm text-gray-500">Coming soon</div>
              </div>
            </Label>
          </div>
        </RadioGroup>
      </CardContent>
    </Card>
  );
}