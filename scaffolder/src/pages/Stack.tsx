import React, { useCallback, useMemo, useState } from 'react';
import { Folder, File, ChevronRight, ChevronDown, RefreshCw, X, Eye, Download } from 'lucide-react';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { oneLight } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { generateProject, previewProject, validateProject } from '../api/client';
import { FileNode } from '../models/types';
import { useProjectStore } from '../store/projectStore';
import { StackSection } from './project/StackSection';
import { useJwtConfigController, useStackOptionsLoader } from './project/hooks';

const syntaxTheme = {
  ...oneLight,
  linenumber: { fontStyle: 'normal', color: '#9ca3af' },
  'comment.linenumber': { fontStyle: 'normal', color: '#9ca3af' },
  'react-syntax-highlighter-line-number': { fontStyle: 'normal', color: '#9ca3af' },
};

const languageFromPath = (filePath: string) => {
  const fileName = filePath.split(/[\\/]/).pop() ?? '';
  if (fileName.toLowerCase() === 'dockerfile') return 'docker';
  const ext = fileName.includes('.') ? fileName.split('.').pop()?.toLowerCase() : undefined;

  switch (ext) {
    case 'ts':
    case 'tsx':
      return 'typescript';
    case 'js':
      return 'javascript';
    case 'jsx':
      return 'jsx';
    case 'json':
      return 'json';
    case 'yml':
    case 'yaml':
      return 'yaml';
    case 'md':
      return 'markdown';
    case 'java':
      return 'java';
    case 'kt':
      return 'kotlin';
    case 'gradle':
      return 'groovy';
    case 'xml':
      return 'xml';
    case 'html':
      return 'html';
    case 'css':
      return 'css';
    case 'scss':
      return 'scss';
    case 'properties':
      return 'properties';
    case 'sql':
      return 'sql';
    case 'sh':
      return 'bash';
    case 'dockerfile':
      return 'docker';
    case 'hbs':
      return 'handlebars';
    default:
      return 'text';
  }
};

const FileTreeItem: React.FC<{ node: FileNode; onSelect: (node: FileNode) => void; selectedPath?: string }> = ({
  node,
  onSelect,
  selectedPath,
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const isSelected = node.path === selectedPath;

  const handleClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    onSelect(node);
    if (node.type === 'directory') {
      setIsOpen(!isOpen);
    }
  };

  return (
    <div className="ml-4">
      <div
        className={`flex items-center gap-2 py-1 px-2 rounded cursor-pointer hover:bg-gray-100 ${
          isSelected ? 'bg-blue-50 text-blue-600' : 'text-gray-700'
        }`}
        onClick={handleClick}
      >
        {node.type === 'directory' && (isOpen ? <ChevronDown size={14} /> : <ChevronRight size={14} />)}
        {node.type === 'directory' ? (
          <Folder size={16} className="text-blue-400" />
        ) : (
          <File size={16} className="text-gray-400" />
        )}
        <span className="text-sm truncate">{node.name}</span>
      </div>
      {isOpen && node.children && (
        <div className="border-l border-gray-200 ml-2">
          {node.children.map((child) => (
            <FileTreeItem key={child.path} node={child} onSelect={onSelect} selectedPath={selectedPath} />
          ))}
        </div>
      )}
    </div>
  );
};

export const Stack: React.FC = () => {
  const { config, setProjectInfo, setStack, stackOptions, setStackOptions } = useProjectStore();
  const { isLoadingStackOptions, stackOptionsError, loadStackOptions } = useStackOptionsLoader(
    stackOptions,
    setStackOptions,
  );
  const { jwt, setJwtConfig } = useJwtConfigController(config, setProjectInfo);

  const [validationErrors, setValidationErrors] = useState<string[]>([]);
  const [validationWarnings, setValidationWarnings] = useState<string[]>([]);
  const [isValidating, setIsValidating] = useState(false);

  const [isPreviewOpen, setIsPreviewOpen] = useState(false);
  const [fileTree, setFileTree] = useState<FileNode | null>(null);
  const [selectedFile, setSelectedFile] = useState<FileNode | null>(null);
  const [isPreviewLoading, setIsPreviewLoading] = useState(false);
  const [previewError, setPreviewError] = useState<string | null>(null);

  const [isDownloading, setIsDownloading] = useState(false);

  const canInteract = useMemo(
    () => !isValidating && !isPreviewLoading && !isDownloading,
    [isDownloading, isPreviewLoading, isValidating],
  );

  const runValidation = useCallback(async () => {
    if (isValidating) return false;
    setIsValidating(true);
    try {
      const result = await validateProject(config);
      setValidationErrors(result.errors ?? []);
      setValidationWarnings(result.warnings ?? []);
      return result.valid;
    } catch {
      setValidationErrors(['Failed to validate project']);
      setValidationWarnings([]);
      return false;
    } finally {
      setIsValidating(false);
    }
  }, [config, isValidating]);

  const fetchPreview = useCallback(async () => {
    setIsPreviewLoading(true);
    setPreviewError(null);
    try {
      const validation = await validateProject(config);
      setValidationErrors(validation.errors ?? []);
      setValidationWarnings(validation.warnings ?? []);
      if (!validation.valid) {
        setPreviewError((validation.errors ?? []).join('\n') || 'Invalid project config');
        return;
      }
      const tree = await previewProject(config);
      setFileTree(tree);
      setSelectedFile(tree);
    } catch (e) {
      setPreviewError(e instanceof Error ? e.message : 'Failed to load project preview');
    } finally {
      setIsPreviewLoading(false);
    }
  }, [config]);

  const handleOpenPreview = useCallback(() => {
    if (!canInteract) return;
    void (async () => {
      const ok = await runValidation();
      if (!ok) return;
      setIsPreviewOpen(true);
      await fetchPreview();
    })();
  }, [canInteract, fetchPreview, runValidation]);

  const handleDownload = useCallback(() => {
    if (!canInteract) return;
    void (async () => {
      const ok = await runValidation();
      if (!ok) return;
      setIsDownloading(true);
      try {
        await generateProject(config);
      } catch (e) {
        setValidationErrors([e instanceof Error ? e.message : 'Failed to generate project']);
        setValidationWarnings([]);
      } finally {
        setIsDownloading(false);
      }
    })();
  }, [canInteract, config, runValidation]);

  const closePreview = useCallback(() => {
    setIsPreviewOpen(false);
    setPreviewError(null);
  }, []);

  return (
    <div className="space-y-8">
      <StackSection
        stack={config.stack}
        stackOptions={stackOptions}
        isLoadingStackOptions={isLoadingStackOptions}
        stackOptionsError={stackOptionsError}
        onRetryLoadStackOptions={loadStackOptions}
        setStack={setStack}
        jwt={jwt}
        setJwtConfig={setJwtConfig}
      />

      {(validationErrors.length > 0 || validationWarnings.length > 0) && (
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-4">
          {validationErrors.length > 0 && (
            <div>
              <div className="text-sm font-medium text-red-700 mb-2">Validation errors</div>
              <ul className="list-disc pl-5 space-y-1 text-sm text-red-700">
                {validationErrors.map((err, idx) => (
                  <li key={`${err}-${idx}`}>{err}</li>
                ))}
              </ul>
            </div>
          )}
          {validationWarnings.length > 0 && (
            <div>
              <div className="text-sm font-medium text-amber-700 mb-2">Warnings</div>
              <ul className="list-disc pl-5 space-y-1 text-sm text-amber-700">
                {validationWarnings.map((w, idx) => (
                  <li key={`${w}-${idx}`}>{w}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      <div className="flex justify-center gap-3">
        <button
          type="button"
          onClick={handleOpenPreview}
          disabled={!canInteract}
          className="px-4 py-2 rounded-md border border-gray-200 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          <Eye size={16} />
          Preview
        </button>
        <button
          type="button"
          onClick={handleDownload}
          disabled={!canInteract}
          className="px-4 py-2 rounded-md bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          {isDownloading ? <RefreshCw size={16} className="animate-spin" /> : <Download size={16} />}
          {isDownloading ? 'Downloading...' : 'Download'}
        </button>
      </div>

      {isPreviewOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-6">
          <button
            type="button"
            className="absolute inset-0 bg-black/40"
            onClick={closePreview}
            aria-label="Close preview"
          />
          <div className="relative w-full max-w-6xl h-[min(80vh,900px)] bg-white rounded-xl shadow-xl border border-gray-200 overflow-hidden flex flex-col">
            <div className="flex items-center justify-between px-4 py-3 border-b border-gray-200 bg-gray-50">
              <div className="text-lg font-semibold text-gray-800">Project Preview</div>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => void fetchPreview()}
                  disabled={isPreviewLoading}
                  className="flex items-center gap-2 text-sm text-gray-600 hover:text-blue-600 bg-white px-3 py-1.5 rounded-md border shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <RefreshCw size={14} className={isPreviewLoading ? 'animate-spin' : ''} />
                  Refresh
                </button>
                <button
                  type="button"
                  onClick={closePreview}
                  className="p-2 rounded-md hover:bg-gray-100 text-gray-600"
                  aria-label="Close"
                >
                  <X size={18} />
                </button>
              </div>
            </div>

            <div className="flex-1 flex overflow-hidden">
              <div className="w-1/3 border-r border-gray-200 overflow-y-auto p-4 bg-gray-50">
                {isPreviewLoading && !fileTree && (
                  <div className="text-center text-gray-500 mt-4">Generating structure...</div>
                )}
                {previewError && <div className="text-red-600 text-sm whitespace-pre-wrap">{previewError}</div>}
                {fileTree && (
                  <div className="-ml-4">
                    <FileTreeItem node={fileTree} onSelect={setSelectedFile} selectedPath={selectedFile?.path} />
                  </div>
                )}
              </div>

              <div className="w-2/3 flex flex-col overflow-hidden">
                {selectedFile ? (
                  <>
                    <div className="px-4 py-3 border-b border-gray-200 bg-gray-50 font-mono text-sm text-gray-600 flex items-center gap-2">
                      <File size={14} />
                      {selectedFile.path}
                    </div>
                    <div className="flex-1 overflow-auto p-4 bg-white">
                      {selectedFile.type === 'file' ? (
                        <SyntaxHighlighter
                          language={languageFromPath(selectedFile.path)}
                          style={syntaxTheme}
                          showLineNumbers
                          wrapLongLines
                          customStyle={{
                            margin: 0,
                            background: 'transparent',
                            fontSize: '0.875rem',
                            lineHeight: '1.25rem',
                          }}
                          lineNumberStyle={{
                            minWidth: '3em',
                            paddingRight: '1em',
                            color: '#9ca3af',
                            fontStyle: 'normal',
                            userSelect: 'none',
                          }}
                          codeTagProps={{
                            style: {
                              fontFamily:
                                'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace',
                              fontStyle: 'normal',
                            },
                          }}
                        >
                          {selectedFile.content || '(Empty file)'}
                        </SyntaxHighlighter>
                      ) : (
                        <div className="flex flex-col items-center justify-center h-full text-gray-400">
                          <Folder size={48} className="mb-2 opacity-50" />
                          <p>Select a file to view content</p>
                        </div>
                      )}
                    </div>
                  </>
                ) : (
                  <div className="flex flex-col items-center justify-center h-full text-gray-400">
                    <p>Select a file to preview</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
