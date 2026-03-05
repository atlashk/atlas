// Handlebars-based generator for rendering templates using app-stack.cfg
// Usage:
//  - Render directory:   node generate.mjs --dir <path> --out-dir <path> [--app-stack <name>]

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import Handlebars from 'handlebars';

function parseArgs(argv) {
  const args = {};
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a.startsWith('--')) {
      // Handle --key=value format
      if (a.includes('=')) {
        const [key, ...valueParts] = a.slice(2).split('=');
        args[key] = valueParts.join('='); // Join back in case value contains '='
      } else {
        // Handle --key value format
        const key = a.slice(2);
        const val = argv[i + 1] && !argv[i + 1].startsWith('--') ? argv[++i] : true;
        args[key] = val;
      }
    }
  }
  return args;
}

function setDeep(obj, keys, val) {
  let cur = obj;
  for (let i = 0; i < keys.length - 1; i++) {
    const k = keys[i];
    if (!(k in cur)) cur[k] = {};
    cur = cur[k];
  }
  cur[keys[keys.length - 1]] = val;
}

function parseCfg(content) {
  const obj = {};
  content.split(/\r?\n/).forEach(line => {
    const s = line.trim();
    if (!s || s.startsWith('#') || s.startsWith('//')) return;
    const idx = s.indexOf('=');
    if (idx === -1) return;
    const key = s.slice(0, idx).trim();
    const value = s.slice(idx + 1).trim();
    setDeep(obj, key.split('.'), value);
  });
  return obj;
}

function parseEnv(content) {
  const obj = {};
  content.split(/\r?\n/).forEach(line => {
    const s = line.trim();
    // Skip empty lines and comments
    if (!s || s.startsWith('#')) return;
    const idx = s.indexOf('=');
    if (idx === -1) return;
    const key = s.slice(0, idx).trim();
    let value = s.slice(idx + 1).trim();
    // Remove surrounding quotes if present
    if ((value.startsWith('"') && value.endsWith('"')) || 
        (value.startsWith("'") && value.endsWith("'"))) {
      value = value.slice(1, -1);
    }
    obj[key] = value;
  });
  return obj;
}

function parseYaml(content) {
  const obj = {};
  const lines = content.split(/\r?\n/);
  const stack = [];
  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;
    const indent = line.match(/^\s*/)[0].length;
    const level = Math.floor(indent / 2);
    stack.length = level;
    const colonIndex = trimmed.indexOf(':');
    if (colonIndex === -1) continue;
    const key = trimmed.substring(0, colonIndex).trim();
    const value = trimmed.substring(colonIndex + 1).trim();
    if (value) {
      setDeep(obj, [...stack, key], value);
    } else {
      stack.push(key);
    }
  }
  return obj;
}

function createStackAccessor(stack) {
  const cache = new Map();
  return (stackPath) => {
    if (cache.has(stackPath)) return cache.get(stackPath);
    const parts = stackPath.split('.');
    let value = stack;
    for (const part of parts) {
      value = value?.[part];
      if (value === undefined) {
        cache.set(stackPath, '');
        return '';
      }
    }
    const normalized = (value || '').toString().toLowerCase();
    cache.set(stackPath, normalized);
    return normalized;
  };
}

// Register Handlebars helpers
function registerHelpers() {
  // Equality check
  Handlebars.registerHelper('eq', function(a, b) {
    return a === b;
  });

  // OR operation
  Handlebars.registerHelper('or', function(...args) {
    // Last argument is Handlebars options object
    const values = args.slice(0, -1);
    return values.some(v => !!v);
  });

  // AND operation
  Handlebars.registerHelper('and', function(...args) {
    const values = args.slice(0, -1);
    return values.every(v => !!v);
  });

  // NOT operation
  Handlebars.registerHelper('not', function(value) {
    return !value;
  });

  // Get nested value from object
  Handlebars.registerHelper('get', function(obj, path) {
    if (!obj || !path) return '';
    const parts = path.split('.');
    let value = obj;
    for (const part of parts) {
      value = value?.[part];
      if (value === undefined) return '';
    }
    return (value || '').toString().toLowerCase();
  });

  // Check if service matches based on stack config
  // Usage: {{#hasService "datasource" "mysql"}}...{{/hasService}}
  Handlebars.registerHelper('hasService', function(serviceType, serviceName, options) {
    const { stack, getStackValue } = this;
    const stackValue = getStackValue || createStackAccessor(stack);
    
    // Get the value from stack using serviceType as the key
    const actualValue = stackValue(serviceType);
    
    // Compare case-insensitive
    const result = actualValue === (serviceName || '').toString().toLowerCase();
    
    return result ? options.fn(this) : options.inverse(this);
  });

  // Special helper for Grafana - shows when any observability service is enabled
  // Usage: {{#hasGrafana}}...{{/hasGrafana}}
  Handlebars.registerHelper('hasGrafana', function(options) {
    const { stack, getStackValue } = this;
    const stackValue = getStackValue || createStackAccessor(stack);

    // Grafana is shown when any of these services are enabled
    const hasPrometheus = stackValue('observability.metrics') === 'prometheus';
    const hasLoki = stackValue('observability.logging.stack') === 'loki';
    const hasZipkin = stackValue('observability.tracing') === 'zipkin';

    const result = hasPrometheus || hasLoki || hasZipkin;
    return result ? options.fn(this) : options.inverse(this);
  });
}

async function renderFile(templatePath, outPath, context) {
  // Read template file
  const templateContent = fs.readFileSync(templatePath, 'utf8');
  
  // Compile and render with Handlebars
  const template = Handlebars.compile(templateContent, { 
    noEscape: true,
    strict: false 
  });
  
  let content = template(context).trim();

  // Extract embedded file blocks from the rendered output.
  // Block syntax:
  //   # @@file: <relative/path>
  //   ... file content ...
  //   # @@endfile
  // These blocks will be written relative to the output file directory.
  const outDir = path.dirname(outPath);
  const fileBlockRegex = /^#\s*@@file:\s*(.+?)\s*$[\r\n]+([\s\S]*?)^#\s*@@endfile\s*$/gm;
  let match;
  while ((match = fileBlockRegex.exec(content)) !== null) {
    const relTarget = match[1].trim();
    const body = match[2];
    const targetPath = path.resolve(outDir, relTarget);
    fs.mkdirSync(path.dirname(targetPath), { recursive: true });
    // Preserve exact body as written in the template
    fs.writeFileSync(targetPath, body, 'utf8');
  }
  // Remove all embedded blocks from the final compose content
  content = content.replace(fileBlockRegex, '');

  // Normalize whitespace in the final output:
  // - Strip trailing spaces at end of lines
  // - Remove whitespace-only lines
  // - Collapse multiple blank lines to a single blank line
  content = content.replace(/[ \t]+(\r?\n)/g, '$1');
  content = content.replace(/^\s+$/gm, '');
  content = content.replace(/(\r?\n){2,}/g, '\n\n');

  // Skip writing if content is empty after normalization
  if (!content.trim()) {
    if (fs.existsSync(outPath)) {
      try {
        fs.unlinkSync(outPath);
      } catch (_) {
        // ignore deletion errors
      }
    }
    return false;
  }

  fs.mkdirSync(path.dirname(outPath), { recursive: true });
  fs.writeFileSync(outPath, content, 'utf8');
  return true;
}

function collectTemplates(dir) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  const files = [];
  for (const e of entries) {
    const full = path.join(dir, e.name);
    if (e.isDirectory()) {
      files.push(...collectTemplates(full));
    } else if (e.isFile()) {
      files.push(full);
    }
  }
  return files;
}

function shouldSkipFileByPath(filePath, context) {
  const { stack, getStackValue } = context;
  const stackValue = getStackValue || createStackAccessor(stack);
  
  // Normalize path separators
  const normalizedPath = filePath.replace(/\\/g, '/');
  
  // Skip specific service configs based on stack
  const datasource = stackValue('datasource');
  if (normalizedPath.includes('/config/mysql/') && datasource !== 'mysql') return true;
  if (normalizedPath.includes('/config/postgres/') && datasource !== 'postgres' && datasource !== 'postgresql') return true;
  
  const kvStore = stackValue('kv-store');
  if (normalizedPath.includes('/config/redis/') && kvStore !== 'redis') return true;
  
  const messaging = stackValue('messaging');
  if (normalizedPath.includes('/config/kafka/') && messaging !== 'kafka') return true;
  if (normalizedPath.includes('/config/rabbitmq/') && messaging !== 'rabbitmq') return true;
  
  const storage = stackValue('storage');
  if (normalizedPath.includes('/config/minio/') && storage !== 'minio') return true;
  
  const fullTextSearch = stackValue('full-text-search');
  if (normalizedPath.includes('/config/elasticsearch/') && fullTextSearch !== 'elasticsearch') return true;
  
  const identity = stackValue('identity');
  if (normalizedPath.includes('/config/keycloak/') && identity !== 'keycloak') return true;

  const notificationEmail = stackValue('notification.email');
  if (normalizedPath.includes('/config/smtp4dev/') && notificationEmail !== 'spring') return true;
  
  return false;
}

(async () => {
  // Register all helpers before processing
  registerHelpers();
  
  const args = parseArgs(process.argv.slice(2));
  const cwd = process.cwd();

  if (!args['dir'] || !args['out-dir']) {
    console.error('Usage: node generate.mjs --dir <path> --out-dir <path> [--app-stack <name>]');
    process.exit(1);
  }

  let stack = {};
  let dotenv = {};
  
  // Load .env file from generator directory
  const scriptDir = path.dirname(fileURLToPath(import.meta.url));
  const envPath = path.resolve(scriptDir, '.env');
  if (fs.existsSync(envPath)) {
    dotenv = parseEnv(fs.readFileSync(envPath, 'utf8'));
    console.log(`Loaded ${Object.keys(dotenv).length} variable(s) from .env`);
  }
  
  if (args['app-stack']) {
    const appStackName = args['app-stack'];
    const configPath = path.resolve(scriptDir, '..', 'config', `app-stack.${appStackName}.yml`);
    if (fs.existsSync(configPath)) {
      stack = { ...stack, ...parseYaml(fs.readFileSync(configPath, 'utf8')) };
    } else {
      console.error(`Config file not found: ${configPath}`);
      process.exit(1);
    }
  }
  if (args.cfg) {
    const cfgPath = path.resolve(cwd, args.cfg);
    if (fs.existsSync(cfgPath)) {
      stack = { ...stack, ...parseCfg(fs.readFileSync(cfgPath, 'utf8')) };
    }
  }

  // Parse additional flags for template context
  const infraOnly = args['infra-only'] === 'true' ? true : false;

  // App stack name (e.g., dev, local.compose, local.k8s.native)
  const appStack = args['app-stack'] || '';

  // Build template context with stack config + flags
  const getStackValue = createStackAccessor(stack);
  const templateContext = {
    stack,
    env: process.env,
    dotenv,
    infraOnly,
    appStack,
    getStackValue
  };

  const templateDir = path.resolve(cwd, args['dir']);
  const outDir = path.resolve(cwd, args['out-dir']);
  const templates = collectTemplates(templateDir);
  let count = 0;
  let skipped = 0;

  for (const t of templates) {
    // Check if file should be skipped based on path
    if (shouldSkipFileByPath(t, templateContext)) {
      skipped++;
      continue;
    }
    
    const relPath = path.relative(templateDir, t);
    const isHandlebarsTemplate = t.endsWith('.hbs') || t.endsWith('.handlebars');
    const outRel = isHandlebarsTemplate ? relPath.replace(/\.(hbs|handlebars)$/, '') : relPath;
    const outPath = path.join(outDir, outRel);

    if (isHandlebarsTemplate) {
      if (await renderFile(t, outPath, templateContext)) {
        count++;
      }
    } else {
      fs.mkdirSync(path.dirname(outPath), { recursive: true });
      fs.copyFileSync(t, outPath);
      count++;
    }
  }
  console.log(`Rendered ${count} file(s), skipped ${skipped} file(s)`);
})();
