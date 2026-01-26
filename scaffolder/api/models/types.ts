
export type DatabaseType = string;
export type AuthType = string;
export type MigrationType = string;
export type DeploymentType = string;
export type CIType = string;

export interface StackConfig {
  database: DatabaseType;
  authentication: AuthType;
  migration: MigrationType;
  deployment: DeploymentType[];
  ci: CIType;
}

export type JwtSigningAlgorithm = 'RS256' | 'HS256';

export interface JwtEntityFieldRef {
  entityName: string;
  fieldName: string;
}

export interface JwtAuthFieldMapping {
  username: JwtEntityFieldRef;
  password: JwtEntityFieldRef;
  role: JwtEntityFieldRef;
}

export type JwtClientTokenTransport =
  | {
      type: 'authorization_header';
      headerName: string;
      prefix: string;
    }
  | {
      type: 'cookie';
      accessTokenCookieName: string;
      refreshTokenCookieName: string;
    };

export interface JwtConfig {
  roles: string[];
  accessTokenTtlSeconds: number;
  refreshTokenTtlSeconds: number;
  signingAlgorithm: JwtSigningAlgorithm;
  fieldMapping: JwtAuthFieldMapping;
  clientTokenTransport: JwtClientTokenTransport;
  hs256Secret?: string;
}

export type FieldType = 'string' | 'integer' | 'long' | 'boolean' | 'decimal' | 'date' | 'datetime';

export interface ValidationRules {
  required?: boolean;
  minLength?: number;
  maxLength?: number;
  min?: number;
  max?: number;
}

export interface FieldConfig {
  name: string;
  type: FieldType;
  nullable: boolean;
  unique: boolean;
  primaryKey: boolean;
  validation?: ValidationRules;
}

export type RelationshipType = 'OneToMany' | 'ManyToOne' | 'ManyToMany';

export interface RelationshipConfig {
  type: RelationshipType;
  targetEntity: string;
  sourceField: string;
  targetField: string;
  joinTable?: string;
}

export interface EntityConfig {
  name: string;
  tableName: string;
  fields: FieldConfig[];
  relationships: RelationshipConfig[];
}

export interface ProjectConfig {
  projectName: string;
  groupId: string;
  artifactId: string;
  version: string;
  description: string;
  basePackage: string;
  stack: StackConfig;
  jwt?: JwtConfig;
  entities: EntityConfig[];
}

export interface FileNode {
  name: string;
  type: 'file' | 'directory';
  path: string;
  content?: string;
  children?: FileNode[];
  size?: number;
}

export interface ValidationResult {
  valid: boolean;
  errors: string[];
  warnings: string[];
}

export interface StackOptionItem<TId extends string = string> {
  id: TId;
  label: string;
}

export interface DeploymentModeOption {
  id: string;
  label: string;
  deployment: DeploymentType[];
}

export interface StackOptionsResponse {
  databases: StackOptionItem<DatabaseType>[];
  authentication: StackOptionItem<AuthType>[];
  migration: StackOptionItem<MigrationType>[];
  deployment: DeploymentModeOption[];
  cicd: StackOptionItem<CIType>[];
  defaults: StackConfig;
}
