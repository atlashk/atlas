
import { Request, Response } from 'express';
import { ProjectOrchestrator } from '../services/ProjectOrchestrator';
import { ZipGeneratorService } from '../services/ZipGeneratorService';
import { ProjectConfig } from '../models/types';
import { stackOptions } from '../config/stackOptions';
import path from 'path';

export class ScaffoldController {
  private orchestrator: ProjectOrchestrator;
  private zipGenerator: ZipGeneratorService;

  constructor() {
    // Assuming templates are in 'templates' directory at project root
    const templatesDir = path.resolve(process.cwd(), 'templates');
    this.orchestrator = new ProjectOrchestrator(templatesDir);
    this.zipGenerator = new ZipGeneratorService();
  }

  private getValidationErrors(config: ProjectConfig): string[] {
    const errors: string[] = [];
    const normalizeValue = (value: string) => value.trim().toLowerCase();

    if (!config?.projectName) errors.push('Project name is required');
    if (!config?.basePackage) errors.push('Base package is required');

    const { databases, authentication, migration, deployment, cicd } = stackOptions;
    const stack = config?.stack;

    if (!stack) {
      errors.push('Stack is required');
    } else {
      if (!stack.database) {
        errors.push('Database is required');
      } else if (!databases.some((o) => o.id === stack.database)) {
        errors.push(`Database must be one of: ${databases.map((o) => o.id).join(', ')}`);
      }

      if (!stack.authentication) {
        errors.push('Authentication is required');
      } else if (!authentication.some((o) => o.id === stack.authentication)) {
        errors.push(`Authentication must be one of: ${authentication.map((o) => o.id).join(', ')}`);
      }

      if (!stack.migration) {
        errors.push('Migration is required');
      } else if (!migration.some((o) => o.id === stack.migration)) {
        errors.push(`Migration must be one of: ${migration.map((o) => o.id).join(', ')}`);
      }

      if (!stack.ci) {
        errors.push('CI is required');
      } else if (!cicd.some((o) => o.id === stack.ci)) {
        errors.push(`CI must be one of: ${cicd.map((o) => o.id).join(', ')}`);
      }

      const normalizeDeployment = (deployment: string[] | undefined) =>
        JSON.stringify([...(deployment ?? [])].slice().sort());
      const allowedModes = new Set(deployment.map((m) => normalizeDeployment(m.deployment)));
      if (!allowedModes.has(normalizeDeployment(stack.deployment))) {
        errors.push('Deployment mode is invalid');
      }
    }

    if (stack?.authentication === 'jwt') {
      const jwt = config.jwt;
      if (!jwt) {
        errors.push('JWT config is required');
      } else {
        if (!Number.isFinite(jwt.accessTokenTtlSeconds) || jwt.accessTokenTtlSeconds <= 0) {
          errors.push('JWT access token TTL must be > 0');
        }
        if (!Number.isFinite(jwt.refreshTokenTtlSeconds) || jwt.refreshTokenTtlSeconds <= 0) {
          errors.push('JWT refresh token TTL must be > 0');
        }

        const alg = jwt.signingAlgorithm;
        if (alg !== 'RS256' && alg !== 'HS256') {
          errors.push('JWT signing algorithm must be RS256 or HS256');
        }
        if (alg === 'HS256' && !jwt.hs256Secret?.trim()) {
          errors.push('JWT HS256 secret is required');
        }

        const fieldMapping = jwt.fieldMapping;
        if (!fieldMapping) {
          errors.push('JWT field mapping is required');
        } else {
          const mappings = [
            { label: 'username', value: fieldMapping.username },
            { label: 'password', value: fieldMapping.password },
            { label: 'role', value: fieldMapping.role },
          ] as const;

          for (const mapping of mappings) {
            if (!mapping.value?.entityName?.trim()) errors.push(`JWT ${mapping.label} mapping entity is required`);
            if (!mapping.value?.fieldName?.trim()) errors.push(`JWT ${mapping.label} mapping field is required`);
          }

          const entities = config.entities ?? [];
          for (const mapping of mappings) {
            const entityName = mapping.value?.entityName?.trim();
            const fieldName = mapping.value?.fieldName?.trim();
            if (!entityName || !fieldName) continue;

            const entity = entities.find((e) => normalizeValue(e.name) === normalizeValue(entityName));
            if (!entity) {
              errors.push(`JWT ${mapping.label} mapping entity does not exist: ${entityName}`);
              continue;
            }
            const fieldExists = (entity.fields ?? []).some((f) => normalizeValue(f.name) === normalizeValue(fieldName));
            if (!fieldExists) {
              errors.push(`JWT ${mapping.label} mapping field does not exist: ${entityName}.${fieldName}`);
            }
          }
        }

        const transport = jwt.clientTokenTransport;
        if (!transport) {
          errors.push('JWT client token transport is required');
        } else if (transport.type === 'authorization_header') {
          if (!transport.headerName?.trim()) errors.push('JWT header name is required');
          if (transport.prefix === undefined) errors.push('JWT header prefix is required');
        } else if (transport.type === 'cookie') {
          if (!transport.accessTokenCookieName?.trim()) errors.push('JWT access token cookie name is required');
          if (!transport.refreshTokenCookieName?.trim()) errors.push('JWT refresh token cookie name is required');
        } else {
          errors.push('JWT client token transport type is invalid');
        }
      }
    }

    const supportedPkTypes = new Set(['integer', 'long', 'string']);
    for (const entity of config?.entities ?? []) {
      const pkFields = (entity.fields ?? []).filter((f) => f.primaryKey);
      if (pkFields.length !== 1) {
        errors.push(`Entity ${entity.name} must have exactly 1 primary key field`);
        continue;
      }
      const pkType = pkFields[0]?.type;
      if (!supportedPkTypes.has(pkType)) {
        errors.push(`Entity ${entity.name} primary key type must be integer, long, or string`);
      }
    }

    return errors;
  }

  public stackOptions = async (_req: Request, res: Response) => {
    res.json(stackOptions);
  };

  public validate = async (req: Request, res: Response) => {
    const config = req.body as ProjectConfig;
    const errors = this.getValidationErrors(config);
    if (errors.length > 0) {
      return res.status(400).json({ valid: false, errors, warnings: [] });
    }
    res.json({ valid: true, errors: [], warnings: [] });
  };

  public generate = async (req: Request, res: Response) => {
    try {
      const config = req.body as ProjectConfig;
      const errors = this.getValidationErrors(config);
      if (errors.length > 0) {
        return res.status(400).json({ error: 'Invalid config', errors });
      }
      
      const fileTree = await this.orchestrator.generateProject(config);
      
      res.setHeader('Content-Type', 'application/zip');
      res.setHeader('Content-Disposition', `attachment; filename=${config.projectName}.zip`);
      
      await this.zipGenerator.generateZip(fileTree, res);
    } catch (error) {
      console.error('Error generating project:', error);
      res.status(500).json({ error: 'Failed to generate project' });
    }
  };

  public preview = async (req: Request, res: Response) => {
    try {
      const config = req.body as ProjectConfig;
      const errors = this.getValidationErrors(config);
      if (errors.length > 0) {
        return res.status(400).json({ error: 'Invalid config', errors });
      }
      const fileTree = await this.orchestrator.generateProject(config);
      res.json({ tree: fileTree });
    } catch (error) {
      console.error('Error previewing project:', error);
      res.status(500).json({ error: 'Failed to preview project' });
    }
  };
}
