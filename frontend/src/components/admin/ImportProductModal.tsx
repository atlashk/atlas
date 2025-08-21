"use client";

import React, { useState, useRef } from "react";
import { toast } from "sonner";

interface ImportProductModalProps {
  isVisible: boolean;
  onClose: () => void;
  onImportSuccess: () => void;
}

const ImportProductModal: React.FC<ImportProductModalProps> = ({
  isVisible,
  onClose,
  onImportSuccess,
}) => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isImporting, setIsImporting] = useState(false);
  const [importProgress, setImportProgress] = useState(0);
  const [dragActive, setDragActive] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const acceptedTypes = ".csv,.xlsx,.xls";
  const maxFileSize = 10 * 1024 * 1024; // 10MB

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    const files = e.dataTransfer.files;
    if (files && files[0]) {
      handleFileSelection(files[0]);
    }
  };

  const handleFileSelection = (file: File) => {
    // Validate file type
    const validTypes = [
      "text/csv",
      "application/vnd.ms-excel",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    ];
    if (
      !validTypes.includes(file.type) &&
      !file.name.match(/\.(csv|xlsx|xls)$/i)
    ) {
      toast.error("Please select a valid CSV or Excel file.");
      return;
    }

    // Validate file size
    if (file.size > maxFileSize) {
      toast.error("File size must be less than 10MB.");
      return;
    }

    setSelectedFile(file);
  };

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files[0]) {
      handleFileSelection(files[0]);
    }
  };

  const handleImport = async () => {
    if (!selectedFile) {
      toast.error("Please select a file to import.");
      return;
    }

    setIsImporting(true);
    setImportProgress(0);

    try {
      const formData = new FormData();
      formData.append("file", selectedFile);

      // Simulate progress
      const progressInterval = setInterval(() => {
        setImportProgress((prev) => {
          if (prev >= 90) {
            clearInterval(progressInterval);
            return prev;
          }
          return prev + 10;
        });
      }, 200);

      // TODO: Replace with actual API call
      // const response = await productService.importProducts(formData);

      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 2000));

      clearInterval(progressInterval);
      setImportProgress(100);

      toast.success("Products imported successfully!");
      onImportSuccess();
      handleClose();
    } catch (error) {
      console.error("Import error:", error);
      toast.error("Failed to import products. Please try again.");
    } finally {
      setIsImporting(false);
      setImportProgress(0);
    }
  };

  const handleClose = () => {
    setSelectedFile(null);
    setIsImporting(false);
    setImportProgress(0);
    setDragActive(false);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
    onClose();
  };

  const downloadTemplate = () => {
    // TODO: Implement template download
    toast.info("Template download feature will be implemented soon.");
  };

  if (!isVisible) return null;

  return (
    <div
      className="modal show d-block"
      tabIndex={-1}
      style={{ backgroundColor: "rgba(0,0,0,0.5)" }}
    >
      <div className="modal-dialog modal-lg">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">
              <i className="bi bi-upload me-2"></i>
              Import Products
            </h5>
            <button
              type="button"
              className="btn-close"
              onClick={handleClose}
              disabled={isImporting}
            ></button>
          </div>

          <div className="modal-body">
            {!isImporting ? (
              <>
                <div className="mb-3">
                  <p className="text-muted">
                    Upload a CSV or Excel file to import products. Make sure
                    your file follows the correct format.
                  </p>
                  <button
                    type="button"
                    className="btn btn-outline-info btn-sm"
                    onClick={downloadTemplate}
                  >
                    <i className="bi bi-download me-1"></i>
                    Download Template
                  </button>
                </div>

                <div
                  className={`border-2 border-dashed rounded p-4 text-center ${
                    dragActive ? "border-primary bg-light" : "border-secondary"
                  }`}
                  onDragEnter={handleDrag}
                  onDragLeave={handleDrag}
                  onDragOver={handleDrag}
                  onDrop={handleDrop}
                >
                  {selectedFile ? (
                    <div>
                      <i className="bi bi-file-earmark-check text-success fs-1"></i>
                      <p className="mt-2 mb-1">
                        <strong>{selectedFile.name}</strong>
                      </p>
                      <p className="text-muted small">
                        {(selectedFile.size / 1024 / 1024).toFixed(2)} MB
                      </p>
                      <button
                        type="button"
                        className="btn btn-outline-danger btn-sm"
                        onClick={() => setSelectedFile(null)}
                      >
                        Remove
                      </button>
                    </div>
                  ) : (
                    <div>
                      <i className="bi bi-cloud-upload fs-1 text-muted"></i>
                      <p className="mt-2 mb-1">Drag and drop your file here</p>
                      <p className="text-muted small mb-3">or</p>
                      <button
                        type="button"
                        className="btn btn-outline-primary"
                        onClick={() => fileInputRef.current?.click()}
                      >
                        Choose File
                      </button>
                      <p className="text-muted small mt-2">
                        Supported formats: CSV, Excel (.xlsx, .xls)
                        <br />
                        Maximum file size: 10MB
                      </p>
                    </div>
                  )}
                </div>

                <input
                  ref={fileInputRef}
                  type="file"
                  className="d-none"
                  accept={acceptedTypes}
                  onChange={handleFileInputChange}
                />
              </>
            ) : (
              <div className="text-center">
                <div className="spinner-border text-primary mb-3" role="status">
                  <span className="visually-hidden">Loading...</span>
                </div>
                <h6>Importing Products...</h6>
                <div className="progress mt-3">
                  <div
                    className="progress-bar progress-bar-striped progress-bar-animated"
                    role="progressbar"
                    style={{ width: `${importProgress}%` }}
                    aria-valuenow={importProgress}
                    aria-valuemin={0}
                    aria-valuemax={100}
                  >
                    {importProgress}%
                  </div>
                </div>
                <p className="text-muted mt-2 small">
                  Please wait while we process your file...
                </p>
              </div>
            )}
          </div>

          <div className="modal-footer">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={handleClose}
              disabled={isImporting}
            >
              Cancel
            </button>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleImport}
              disabled={!selectedFile || isImporting}
            >
              {isImporting ? (
                <>
                  <span
                    className="spinner-border spinner-border-sm me-2"
                    role="status"
                    aria-hidden="true"
                  ></span>
                  Importing...
                </>
              ) : (
                <>
                  <i className="bi bi-upload me-2"></i>
                  Import Products
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ImportProductModal;
