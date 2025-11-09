// Simple EJS-based generator for rendering templates using app-stack.cfg
// Usage:
//  - Render single file: node generate.mjs --template <path> --out <path> [--cfg <path>] [--json <path>]
//  - Render directory:   node generate.mjs --dir <path> --out-dir <path> [--cfg <path>] [--json <path>]

import fs from 'fs';
import path from 'path';
import ejs from 'ejs';

function parseArgs(argv) {
  const args = {};
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a.startsWith('--')) {
      const key = a.slice(2);
      const val = argv[i + 1] && !argv[i + 1].startsWith('--') ? argv[++i] : true;
      args[key] = val;
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

async function renderFile(templatePath, outPath, stack) {
  // Render with whitespace trimming to avoid blank lines from EJS control tags
  const rendered = await ejs.renderFile(
    templatePath,
    { stack, env: process.env },
    { async: true }
  );
  let content = rendered.trim();

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
    } else if (e.isFile() && e.name.endsWith('.ejs')) {
      files.push(full);
    }
  }
  return files;
}

(async () => {
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

  if (args.template && args.out) {
    const templatePath = path.resolve(cwd, args.template);
    const outPath = path.resolve(cwd, args.out);
    await renderFile(templatePath, outPath, stack);
    console.log(`Generated: ${outPath}`);
    return;
  }

  const templateDir = path.resolve(cwd, args['dir']);
  const outDir = path.resolve(cwd, args['out-dir']);
  const templates = collectTemplates(templateDir);
  let count = 0;

  for (const t of templates) {
    const outRel = path.relative(templateDir, t).replace(/\.ejs$/, '');
    const outPath = path.join(outDir, outRel);
    await renderFile(t, outPath, stack);
    count++;
  }
  console.log(`Rendered ${count} file(s)`);
})();
