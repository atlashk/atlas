
import { FileNode, ProjectConfig, StackOptionsResponse } from '../models/types';

const API_BASE = '/api/scaffold';

export type ValidationResult = {
  valid: boolean;
  errors: string[];
  warnings: string[];
};

const readJsonSafely = async <T>(response: Response): Promise<T | null> => {
  try {
    return (await response.json()) as T;
  } catch {
    return null;
  }
};

export const getStackOptions = async (): Promise<StackOptionsResponse> => {
  const response = await fetch(`${API_BASE}/stack-options`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  });
  if (!response.ok) throw new Error('Failed to fetch stack options');
  return (await response.json()) as StackOptionsResponse;
};

export const validateProject = async (config: ProjectConfig): Promise<ValidationResult> => {
  const response = await fetch(`${API_BASE}/validate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(config),
  });

  const data = await readJsonSafely<ValidationResult>(response);
  if (data) return data;
  throw new Error('Failed to validate project');
};

export const previewProject = async (config: ProjectConfig): Promise<FileNode> => {
  const response = await fetch(`${API_BASE}/preview`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(config),
  });
  const data = await readJsonSafely<{ tree?: FileNode; errors?: string[] }>(response);
  if (!response.ok) {
    const message = data?.errors?.length ? data.errors.join('\n') : 'Failed to fetch preview';
    throw new Error(message);
  }
  if (!data?.tree) throw new Error('Failed to fetch preview');
  return data.tree;
};

export const generateProject = async (config: ProjectConfig): Promise<void> => {
  const response = await fetch(`${API_BASE}/generate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(config),
  });
  if (!response.ok) {
    const data = await readJsonSafely<{ errors?: string[] }>(response);
    const message = data?.errors?.length ? data.errors.join('\n') : 'Failed to generate project';
    throw new Error(message);
  }

  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${config.projectName}.zip`;
  document.body.appendChild(a);
  a.click();
  window.URL.revokeObjectURL(url);
  document.body.removeChild(a);
};
