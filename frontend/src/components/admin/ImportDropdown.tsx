"use client";

import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Upload,
  FileText,
  FileSpreadsheet,
  Loader2,
  ChevronDown,
} from "lucide-react";

interface ImportDropdownProps {
  isImporting?: boolean;
  variant?:
    | "primary"
    | "secondary"
    | "success"
    | "danger"
    | "warning"
    | "info"
    | "light"
    | "dark";
  outline?: boolean;
  onImport: (fileType: "csv" | "excel") => void;
  onOpen?: () => void;
  onClose?: () => void;
}

const ImportDropdown: React.FC<ImportDropdownProps> = ({
  isImporting = false,
  variant = "primary",
  outline = true,
  onImport,
  onOpen,
  onClose,
}) => {
  const [isOpen, setIsOpen] = useState(false);

  const getVariant = () => {
    if (outline) {
      return "outline";
    }
    switch (variant) {
      case "danger":
        return "destructive";
      case "secondary":
        return "secondary";
      default:
        return "default";
    }
  };

  const handleImport = (fileType: "csv" | "excel") => {
    onImport(fileType);
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
        <Button variant={getVariant()} disabled={isImporting} className="gap-2">
          {isImporting ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Importing...
            </>
          ) : (
            <>
              <Upload className="h-4 w-4" />
              Import
              <ChevronDown className="h-4 w-4" />
            </>
          )}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent>
        <DropdownMenuItem
          onClick={() => handleImport("csv")}
          disabled={isImporting}
          className="gap-2"
        >
          <FileText className="h-4 w-4" />
          Import from CSV
        </DropdownMenuItem>
        <DropdownMenuItem
          onClick={() => handleImport("excel")}
          disabled={isImporting}
          className="gap-2"
        >
          <FileSpreadsheet className="h-4 w-4" />
          Import from Excel
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export default ImportDropdown;
