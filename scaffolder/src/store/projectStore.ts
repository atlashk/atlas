import { create } from 'zustand';
import { ProjectConfig, EntityConfig, StackConfig, StackOptionsResponse } from '../models/types';

function normalizePackageGroupId(groupId: string): string {
  return groupId
    .toLowerCase()
    .replace(/\s+/g, '')
    .replace(/[^a-z0-9.]/g, '')
    .replace(/\.+/g, '.')
    .replace(/^\./, '')
    .replace(/\.$/, '');
}

function normalizePackageArtifactId(artifactId: string): string {
  return artifactId
    .toLowerCase()
    .replace(/\s+/g, '')
    .replace(/[^a-z0-9]/g, '');
}

function deriveBasePackage(groupId: string, artifactId: string): string {
  const group = normalizePackageGroupId(groupId);
  const artifact = normalizePackageArtifactId(artifactId);
  if (group && artifact) return `${group}.${artifact}`;
  return group || artifact;
}

interface ProjectState {
  config: ProjectConfig;
  stackOptions: StackOptionsResponse | null;
  setProjectInfo: (info: Partial<ProjectConfig>) => void;
  setStack: (stack: Partial<StackConfig>) => void;
  setStackOptions: (stackOptions: StackOptionsResponse) => void;
  addEntity: (entity: EntityConfig) => void;
  updateEntity: (index: number, entity: EntityConfig) => void;
  removeEntity: (index: number) => void;
  reset: () => void;
}

const initialConfig: ProjectConfig = {
  projectName: 'demo',
  groupId: 'com.example',
  artifactId: 'demo',
  version: '0.0.1-SNAPSHOT',
  description: 'Demo project',
  basePackage: deriveBasePackage('com.example', 'demo'),
  stack: {
    database: 'postgresql',
    authentication: 'none',
    migration: 'none',
    deployment: [],
    ci: 'none',
  },
  jwt: {
    roles: ['USER', 'ADMIN'],
    accessTokenTtlSeconds: 86400,
    refreshTokenTtlSeconds: 2592000,
    signingAlgorithm: 'HS256',
    fieldMapping: {
      username: { entityName: '', fieldName: '' },
      password: { entityName: '', fieldName: '' },
      role: { entityName: '', fieldName: '' },
    },
    clientTokenTransport: {
      type: 'authorization_header',
      headerName: 'Authorization',
      prefix: 'Bearer ',
    },
    hs256Secret: '',
  },
  entities: [],
};

export const useProjectStore = create<ProjectState>((set) => ({
  config: initialConfig,
  stackOptions: null,
  setProjectInfo: (info) =>
    set((state) => {
      const nextConfig = { ...state.config, ...info };
      const groupOrArtifactChanged =
        Object.prototype.hasOwnProperty.call(info, 'groupId') ||
        Object.prototype.hasOwnProperty.call(info, 'artifactId');
      const basePackageExplicitlySet = Object.prototype.hasOwnProperty.call(info, 'basePackage');

      if (groupOrArtifactChanged && !basePackageExplicitlySet) {
        nextConfig.basePackage = deriveBasePackage(nextConfig.groupId, nextConfig.artifactId);
      }

      return { config: nextConfig };
    }),
  setStack: (stack) =>
    set((state) => ({
      config: {
        ...state.config,
        stack: { ...state.config.stack, ...stack },
      },
    })),
  setStackOptions: (stackOptions) => set({ stackOptions }),
  addEntity: (entity) =>
    set((state) => ({
      config: {
        ...state.config,
        entities: [...state.config.entities, entity],
      },
    })),
  updateEntity: (index, entity) =>
    set((state) => {
      const newEntities = [...state.config.entities];
      newEntities[index] = entity;
      return {
        config: { ...state.config, entities: newEntities },
      };
    }),
  removeEntity: (index) =>
    set((state) => ({
      config: {
        ...state.config,
        entities: state.config.entities.filter((_, i) => i !== index),
      },
    })),
  reset: () => set({ config: initialConfig }),
}));
