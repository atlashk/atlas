// Handlebars-based generator for rendering templates using app-stack.cfg
// Usage:
//  - Render single file: node generate.mjs --template <path> --out <path> [--cfg <path>] [--json <path>]
//  - Render directory:   node generate.mjs --dir <path> --out-dir <path> [--cfg <path>] [--json <path>]

import fs from 'fs';
import path from 'path';
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

function parseCfg(content) {
  const obj = {};
  const setDeep = (o, keys, val) => {
    let cur = o;
    for (let i = 0; i < keys.length - 1; i++) {
      const k = keys[i];
      if (!(k in cur)) cur[k] = {};
      cur = cur[k];
    }
    cur[keys[keys.length - 1]] = val;
  };
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

  // Check if service should be enabled based on stack config
  Handlebars.registerHelper('hasService', function(serviceName, options) {
    const { stack, enableObservability } = this;
    
    const getStackValue = (path) => {
      const parts = path.split('.');
      let value = stack;
      for (const part of parts) {
        value = value?.[part];
        if (value === undefined) return '';
      }
      return (value || '').toString().toLowerCase();
    };

    const checks = {
      'mysql': getStackValue('datasource') === 'mysql',
      'postgres': getStackValue('datasource') === 'postgres' || getStackValue('datasource') === 'postgresql',
      'redis': getStackValue('kv-store') === 'redis',
      'kafka': getStackValue('messaging') === 'kafka',
      'rabbitmq': getStackValue('messaging') === 'rabbitmq',
      'elasticsearch': getStackValue('full-text-search') === 'elasticsearch',
      'minio': getStackValue('storage') === 'minio',
      'smtp4dev': getStackValue('notification.email') === 'spring',
      'jwt': getStackValue('iam') === 'jwt',
      'keycloak': getStackValue('iam') === 'keycloak',
      'nginx': getStackValue('reverse-proxy') === 'nginx',
      'prometheus': enableObservability && getStackValue('observability.metrics') === 'prometheus',
      'loki': enableObservability && getStackValue('observability.logging.stack') === 'loki',
      'promtail': enableObservability && getStackValue('observability.logging.stack') === 'loki',
      'zipkin': enableObservability && getStackValue('observability.tracing') === 'zipkin',
      'grafana': enableObservability && (
        getStackValue('observability.logging.stack') === 'loki' ||
        getStackValue('observability.metrics') === 'prometheus' ||
        getStackValue('observability.tracing') === 'zipkin'
      )
    };

    const result = checks[serviceName] || false;
    return result ? options.fn(this) : options.inverse(this);
  });

  // Check multiple conditions with observability
  Handlebars.registerHelper('ifObservability', function(service, options) {
    if (!this.enableObservability) {
      return options.inverse(this);
    }
    
    const { stack } = this;
    const getStackValue = (path) => {
      const parts = path.split('.');
      let value = stack;
      for (const part of parts) {
        value = value?.[part];
        if (value === undefined) return '';
      }
      return (value || '').toString().toLowerCase();
    };

    const checks = {
      'prometheus': getStackValue('observability.metrics') === 'prometheus',
      'loki': getStackValue('observability.logging.stack') === 'loki',
      'zipkin': getStackValue('observability.tracing') === 'zipkin',
      'grafana': (
        getStackValue('observability.logging.stack') === 'loki' ||
        getStackValue('observability.metrics') === 'prometheus' ||
        getStackValue('observability.tracing') === 'zipkin'
      )
    };

    const result = checks[service] || false;
    return result ? options.fn(this) : options.inverse(this);
  });

  // Lowercase helper
  Handlebars.registerHelper('lower', function(str) {
    return (str || '').toLowerCase();
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
    } else if (e.isFile() && (e.name.endsWith('.hbs') || e.name.endsWith('.handlebars'))) {
      files.push(full);
    }
  }
  return files;
}

function shouldSkipFileByPath(filePath, context) {
  const { stack, enableObservability } = context;
  
  const getStackValue = (path) => {
    const parts = path.split('.');
    let value = stack;
    for (const part of parts) {
      value = value?.[part];
      if (value === undefined) return '';
    }
    return (value || '').toString().toLowerCase();
  };
  
  // Normalize path separators
  const normalizedPath = filePath.replace(/\\/g, '/');
  
  // Skip observability configs if observability is disabled
  if (!enableObservability) {
    if (normalizedPath.includes('/config/grafana/')) return true;
    if (normalizedPath.includes('/config/prometheus/')) return true;
    if (normalizedPath.includes('/config/promtail/')) return true;
    if (normalizedPath.includes('/config/loki/')) return true;
    if (normalizedPath.includes('/config/zipkin/')) return true;
  }
  
  // Skip specific service configs based on stack
  const datasource = getStackValue('datasource');
  if (normalizedPath.includes('/config/mysql/') && datasource !== 'mysql') return true;
  if (normalizedPath.includes('/config/postgres/') && datasource !== 'postgres' && datasource !== 'postgresql') return true;
  
  const kvStore = getStackValue('kv-store');
  if (normalizedPath.includes('/config/redis/') && kvStore !== 'redis') return true;
  
  const messaging = getStackValue('messaging');
  if (normalizedPath.includes('/config/kafka/') && messaging !== 'kafka') return true;
  if (normalizedPath.includes('/config/rabbitmq/') && messaging !== 'rabbitmq') return true;
  
  const storage = getStackValue('storage');
  if (normalizedPath.includes('/config/minio/') && storage !== 'minio') return true;
  
  const fullTextSearch = getStackValue('full-text-search');
  if (normalizedPath.includes('/config/elasticsearch/') && fullTextSearch !== 'elasticsearch') return true;
  
  const reverseProxy = getStackValue('reverse-proxy');
  if (normalizedPath.includes('/config/nginx/') && reverseProxy !== 'nginx') return true;
  
  const iam = getStackValue('iam');
  if (normalizedPath.includes('/config/keycloak/') && iam !== 'keycloak') return true;
  
  const notificationEmail = getStackValue('notification.email');
  if (normalizedPath.includes('/config/smtp4dev/') && notificationEmail !== 'spring') return true;
  
  return false;
}

(async () => {
  // Register all helpers before processing
  registerHelpers();
  
  const args = parseArgs(process.argv.slice(2));
  const cwd = process.cwd();

  if ((!args.template || !args.out) && (!args['dir'] || !args['out-dir'])) {
    console.error('Usage: node generate.mjs --template <path> --out <path> [--cfg <path>] | --dir <path> --out-dir <path> [--cfg <path>]');
    process.exit(1);
  }

  let stack = {};
  if (args.cfg) {
    const cfgPath = path.resolve(cwd, args.cfg);
    if (fs.existsSync(cfgPath)) {
      stack = { ...stack, ...parseCfg(fs.readFileSync(cfgPath, 'utf8')) };
    }
  }
  if (args.json) {
    const jsonPath = path.resolve(cwd, args.json);
    if (fs.existsSync(jsonPath)) {
      stack = { ...stack, ...JSON.parse(fs.readFileSync(jsonPath, 'utf8')) };
    }
  }

  // Parse additional flags for template context
  const infraOnly = args['infra-only'] === 'true' ? true : false;
  
  // Observability is infrastructure only - force to false if infra-only is false
  let enableObservability = args['enable-observability'] === 'false' ? false : 
                            args['enable-observability'] === 'true' ? true : true;

  // Build template context with stack config + flags
  const templateContext = {
    stack,
    env: process.env,
    enableObservability,
    infraOnly
  };

  if (args.template && args.out) {
    const templatePath = path.resolve(cwd, args.template);
    const outPath = path.resolve(cwd, args.out);
    const written = await renderFile(templatePath, outPath, templateContext);
    console.log(written ? `Generated: ${outPath}` : `Skipped empty output: ${outPath}`);
    return;
  }

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
    
    const outRel = path.relative(templateDir, t).replace(/\.(hbs|handlebars)$/, '');
    const outPath = path.join(outDir, outRel);
    if (await renderFile(t, outPath, templateContext)) {
      count++;
    }
  }
  console.log(`Rendered ${count} file(s), skipped ${skipped} file(s)`);
})();
