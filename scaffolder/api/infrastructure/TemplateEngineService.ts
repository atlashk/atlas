import Handlebars from 'handlebars';
import fs from 'fs-extra';
import path from 'path';

export class TemplateEngineService {
  private templatesDir: string;

  constructor(templatesDir: string) {
    this.templatesDir = templatesDir;
    this.registerHelpers();
  }

  private registerHelpers() {
    const toJavaType = (type: unknown): string => {
      switch (type) {
        case 'string': return 'String';
        case 'integer': return 'Integer';
        case 'long': return 'Long';
        case 'boolean': return 'Boolean';
        case 'decimal': return 'BigDecimal';
        case 'date': return 'LocalDate';
        case 'datetime': return 'LocalDateTime';
        default: return 'String';
      }
    };

    Handlebars.registerHelper('lower', (str) => str.toLowerCase());
    Handlebars.registerHelper('upper', (str) => str.toUpperCase());
    Handlebars.registerHelper('capitalize', (str) => {
        if (!str) return '';
        return str.charAt(0).toUpperCase() + str.slice(1);
    });
    Handlebars.registerHelper('lowerCamel', (value) => {
      if (value === null || value === undefined) return '';
      const str = String(value).trim();
      if (!str) return '';

      const parts = str
        .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
        .split(/[^A-Za-z0-9]+/)
        .filter(Boolean);

      if (parts.length === 0) return '';

      const [first, ...rest] = parts;
      const firstPart = first.charAt(0).toLowerCase() + first.slice(1);
      const restParts = rest.map((p) => (p ? p.charAt(0).toUpperCase() + p.slice(1).toLowerCase() : ''));
      return `${firstPart}${restParts.join('')}`;
    });
    Handlebars.registerHelper('eq', (a, b) => a === b);
    Handlebars.registerHelper('contains', (collection, value) => {
      if (!collection) return false;
      if (Array.isArray(collection)) return collection.includes(value);
      if (typeof collection === 'string') return collection.includes(String(value));
      return false;
    });
    Handlebars.registerHelper('packagePath', (pkg) => pkg.replace(/\./g, '/'));
    Handlebars.registerHelper('javaType', (type) => {
        return toJavaType(type);
    });
    Handlebars.registerHelper('entityIdJavaType', (entity: unknown) => {
      const fields = (entity as { fields?: Array<{ primaryKey?: boolean; type?: unknown }> })?.fields;
      const pk = fields?.find((f) => f?.primaryKey);
      const pkType = pk?.type ?? 'long';
      if (pkType !== 'integer' && pkType !== 'long' && pkType !== 'string') {
        throw new Error(`Unsupported primary key type: ${String(pkType)}. Supported: integer, long, string.`);
      }
      return toJavaType(pkType);
    });
  }

  public async render(templatePath: string, context: unknown): Promise<string> {
    const fullPath = path.join(this.templatesDir, templatePath);
    try {
        const templateContent = await fs.readFile(fullPath, 'utf-8');
        const template = Handlebars.compile(templateContent);
        return template(context);
    } catch (error) {
        console.error(`Error rendering template ${templatePath}:`, error);
        throw error;
    }
  }

  public async renderString(templateContent: string, context: unknown): Promise<string> {
      const template = Handlebars.compile(templateContent);
      return template(context);
  }
}
