'use client';

import { useEffect, useState } from 'react';
import { PaymentMethod } from '@/interfaces';
import { paymentApi, PaymentMethodResponse } from '@/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Label } from '@/components/ui/label';
import { CreditCard, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

interface PaymentMethodSelectorProps {
  selectedMethod: PaymentMethod;
  onMethodChange: (method: PaymentMethod) => void;
}

export default function PaymentMethodSelector({ 
  selectedMethod, 
  onMethodChange 
}: PaymentMethodSelectorProps) {
  const [paymentMethods, setPaymentMethods] = useState<PaymentMethodResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchPaymentMethods = async () => {
      try {
        setLoading(true);
        const response = await paymentApi.getPaymentMethods();
        
        if (response.success && response.data) {
          const enabledMethods = response.data.filter(method => method.enabled);
          setPaymentMethods(enabledMethods);
          
          // Auto-select first available method if none selected
          if (enabledMethods.length > 0 && !selectedMethod) {
            onMethodChange(enabledMethods[0].type as PaymentMethod);
          }
        } else {
          toast.error('Failed to load payment methods');
        }
      } catch (error) {
        console.error('Error fetching payment methods:', error);
        toast.error('Failed to load payment methods');
      } finally {
        setLoading(false);
      }
    };

    fetchPaymentMethods();
  }, [selectedMethod, onMethodChange]);

  const getMethodIcon = (type: string) => {
    switch (type.toLowerCase()) {
      case 'card':
      case 'credit_card':
      case 'debit_card':
        return <CreditCard className="h-4 w-4" />;
      default:
        return <CreditCard className="h-4 w-4" />;
    }
  };

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <CreditCard className="h-5 w-5" />
          Payment Method
        </CardTitle>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="flex items-center justify-center p-4">
            <Loader2 className="h-6 w-6 animate-spin" />
            <span className="ml-2">Loading payment methods...</span>
          </div>
        ) : paymentMethods.length === 0 ? (
          <div className="text-center p-4 text-gray-500">
            No payment methods available
          </div>
        ) : (
          <RadioGroup 
            value={selectedMethod} 
            onValueChange={(value) => onMethodChange(value as PaymentMethod)}
          >
            {paymentMethods.map((method) => (
              <div 
                key={method.id} 
                className="flex items-center space-x-2 p-3 border rounded-lg hover:bg-gray-50"
              >
                <RadioGroupItem value={method.type} id={method.id} />
                <Label 
                  htmlFor={method.id} 
                  className="flex items-center gap-2 cursor-pointer flex-1"
                >
                  {getMethodIcon(method.type)}
                  <div className="flex flex-col">
                    <span>{method.name}</span>
                    {method.description && (
                      <span className="text-sm text-gray-500">{method.description}</span>
                    )}
                  </div>
                </Label>
              </div>
            ))}
          </RadioGroup>
        )}
      </CardContent>
    </Card>
  );
}