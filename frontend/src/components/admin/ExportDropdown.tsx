'use client';

import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import { Download, FileText, FileSpreadsheet, FileImage, Loader2, ChevronDown } from 'lucide-react';

interface ExportDropdownProps {
  isExporting?: boolean;
  variant?: 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'light' | 'dark';
  outline?: boolean;
  onExport: (fileType: 'csv' | 'excel' | 'pdf') => void;
  onOpen?: () => void;
  onClose?: () => void;
}

const ExportDropdown: React.FC<ExportDropdownProps> = ({
  isExporting = false,
  variant = 'success',
  outline = true,
  onExport,
  onOpen,
  onClose
}) => {
  const [isOpen, setIsOpen] = useState(false);

  const getVariant = () => {
    if (outline) {
      return 'outline';
    }
    switch (variant) {
      case 'danger':
        return 'destructive';
      case 'secondary':
        return 'secondary';
      case 'success':
        return 'default';
      default:
        return 'default';
    }
  };

  const handleExport = (fileType: 'csv' | 'excel' | 'pdf') => {
    onExport(fileType);
  };

  const handleOpenChange = (open: boolean) => {
    setIsOpen(open);
    if (open) {
      onOpen?.();
    } else {
      onClose?.();
    }
  };

  return (
    <DropdownMenu open={isOpen} onOpenChange={handleOpenChange}>
      <DropdownMenuTrigger asChild>
        <Button
          variant={getVariant()}
          disabled={isExporting}
          className="gap-2"
        >
          {isExporting ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Exporting...
            </>
          ) : (
            <>
              <Download className="h-4 w-4" />
              Export
              <ChevronDown className="h-4 w-4" />
            </>
          )}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent>
        <DropdownMenuItem 
          onClick={() => handleExport('csv')} 
          disabled={isExporting}
          className="gap-2"
        >
          <FileText className="h-4 w-4" />
          Export as CSV
        </DropdownMenuItem>
        <DropdownMenuItem 
          onClick={() => handleExport('excel')} 
          disabled={isExporting}
          className="gap-2"
        >
          <FileSpreadsheet className="h-4 w-4" />
          Export as Excel
        </DropdownMenuItem>
        <DropdownMenuItem 
          onClick={() => handleExport('pdf')} 
          disabled={isExporting}
          className="gap-2"
        >
          <FileImage className="h-4 w-4" />
          Export as PDF
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export default ExportDropdown;