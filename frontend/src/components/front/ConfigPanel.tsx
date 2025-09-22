'use client';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Separator } from '@/components/ui/separator';
import { Switch } from '@/components/ui/switch';
import { configStore } from '@/lib/config';
import { notificationService } from '@/services/notificationService';
import { Bell, Save, Settings } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'sonner';

export default function ConfigPanel() {
  const [notificationConfig, setNotificationConfig] = useState(configStore.getNotificationConfig());
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    // Subscribe to config changes
    const unsubscribe = configStore.subscribe((config) => {
      setNotificationConfig(config.notification);
    });

    return unsubscribe;
  }, []);

  const handleNotificationMethodChange = (method: 'polling' | 'sse' | 'ws') => {
    setNotificationConfig(prev => ({
      ...prev,
      defaultMethod: method
    }));
  };

  const handleSave = async () => {
    setIsSaving(true);
    try {
      // Update notification configuration
      configStore.setNotificationMethod(notificationConfig.defaultMethod);

      // Apply changes to services
      notificationService.changeNotificationMethod(notificationConfig.defaultMethod);

      toast.success('Configuration saved successfully!');
    } catch (error) {
      console.error('Failed to save configuration:', error);
      toast.error('Failed to save configuration. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  const availableNotificationMethods = ['polling', 'sse', 'ws'] as const;

  return (
    <Card className="w-full max-w-2xl mx-auto">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Settings className="h-5 w-5" />
          Application Settings
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Notification Settings */}
        <div className="space-y-4">
          <div className="flex items-center gap-2">
            <Bell className="h-4 w-4" />
            <h3 className="text-lg font-semibold">Notification Settings</h3>
          </div>
          
          <div className="space-y-3">
            <Label className="text-sm font-medium">Default Notification Method</Label>
            <RadioGroup
              value={notificationConfig.defaultMethod}
              onValueChange={handleNotificationMethodChange}
            >
              {availableNotificationMethods.map((method) => (
                <div key={method} className="flex items-center space-x-2">
                  <RadioGroupItem value={method} id={`default-${method}`} />
                  <Label htmlFor={`default-${method}`} className="capitalize">
                    {method === 'sse' ? 'Server-Sent Events' : method === 'ws' ? 'WebSocket' : method}
                  </Label>
                </div>
              ))}
            </RadioGroup>
          </div>
        </div>

        {/* Save Button */}
        <div className="flex justify-end">
          <Button onClick={handleSave} disabled={isSaving} className="flex items-center gap-2">
            {isSaving ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                Saving...
              </>
            ) : (
              <>
                <Save className="h-4 w-4" />
                Save Configuration
              </>
            )}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}