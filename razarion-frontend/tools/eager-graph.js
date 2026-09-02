/*
 * Which source files land in the first page load, and which of them import a barrel.
 *
 * Why this exists: @babylonjs/core declares sideEffects ["**\/*"], so a bundler may not drop
 * anything it pulls in. A single barrel import - `from "@babylonjs/core"` - anywhere in the eager
 * graph therefore drags the whole library into the initial bundle, and 2057 KB of the 5432 KB the
 * game loads before it starts is exactly that. It is all or nothing: converting forty files to deep
 * imports and missing the forty-first buys nothing at all.
 *
 * So before that work is worth starting, the question is how big it actually is. This walks static
 * imports from the entry point and stops at every dynamic import(), which is where Angular's lazy
 * routes and every deliberately deferred component sit. What comes back is the set that has to be
 * clean, and the list of files that are not.
 *
 * Run it again afterwards: zero barrel importers is the finish line, and anything else means the
 * refactor bought nothing.
 *
 * A limit worth knowing: this reads TypeScript imports, and Angular's @defer works in the
 * template. A component used only inside a @defer block is still imported statically here - the
 * compiler turns it into a dynamic import at build time - so this still counts it as eager. It is
 * therefore pessimistic about deferred components, and the build's own "Initial total" is the
 * authority whenever the two disagree.
 *
 *   node tools/eager-graph.js [bereich ...]
 */
const fs = require('fs');
const path = require('path');

const ROOT = path.join(__dirname, '..', 'src');
const ENTRY = path.join(ROOT, 'main.ts');

/* Barrels worth naming. A deep path into the same package is fine and is not matched. */
const BARRELS = [
  '@babylonjs/core',
  '@babylonjs/loaders',
  '@babylonjs/materials',
  '@babylonjs/gui',
  'primeng',
  'rxjs'
];

/*
 * Static import and re-export only. `import("x")` is deliberately not matched: a dynamic import is
 * a chunk boundary, and treating it as one is the whole point of the measurement.
 */
const STATIC_IMPORT = /(?:^|\n)\s*(?:import|export)\s+(?:type\s+)?(?:[^'"()]*?\sfrom\s+)?['"]([^'"]+)['"]/g;
/* `import type` and `export type` are erased by TypeScript and cost nothing at runtime. */
const TYPE_ONLY = /(?:^|\n)\s*(?:import|export)\s+type\s/;

function resolve(spec, fromFile) {
  if (!spec.startsWith('.')) {
    return null; // a package, not one of ours
  }
  const base = path.resolve(path.dirname(fromFile), spec);
  for (const candidate of [base + '.ts', base + '.tsx', path.join(base, 'index.ts')]) {
    if (fs.existsSync(candidate) && fs.statSync(candidate).isFile()) {
      return candidate;
    }
  }
  return null;
}

const eager = new Set();
const pulledInBy = new Map();   // file -> the file that imported it first
const barrelUsers = new Map();   // file -> [barrel, ...]
const packages = new Map();      // bare specifier -> count
const queue = [ENTRY];

while (queue.length) {
  const file = queue.shift();
  if (eager.has(file) || !fs.existsSync(file)) {
    continue;
  }
  eager.add(file);
  const source = fs.readFileSync(file, 'utf8');

  for (const match of source.matchAll(STATIC_IMPORT)) {
    const spec = match[1];
    const statement = match[0];

    if (spec.startsWith('.')) {
      const target = resolve(spec, file);
      // A type-only import is erased, so it pulls nothing into the bundle.
      if (target && !TYPE_ONLY.test(statement)) {
        if (!pulledInBy.has(target)) {
          pulledInBy.set(target, file);
        }
        queue.push(target);
      }
      continue;
    }

    packages.set(spec, (packages.get(spec) || 0) + 1);
    if (BARRELS.includes(spec) && !TYPE_ONLY.test(statement)) {
      const rel = path.relative(ROOT, file).replace(/\\/g, '/');
      if (!barrelUsers.has(rel)) {
        barrelUsers.set(rel, []);
      }
      barrelUsers.get(rel).push(spec);
    }
  }
}

const byArea = new Map();
for (const [file, barrels] of barrelUsers) {
  const area = file.split('/').slice(0, 2).join('/');
  if (!byArea.has(area)) {
    byArea.set(area, {files: 0, barrels: new Set()});
  }
  byArea.get(area).files++;
  barrels.forEach(b => byArea.get(area).barrels.add(b));
}

console.log('Eager geladener Quellgraph ab src/main.ts');
console.log('  Dateien:', eager.size);
console.log('  davon mit Barrel-Import:', barrelUsers.size);
console.log('');

if (barrelUsers.size) {
  console.log('Nach Bereich:');
  [...byArea.entries()].sort((a, b) => b[1].files - a[1].files)
    .forEach(([area, v]) => console.log('  ' + String(v.files).padStart(4) + '  ' + area.padEnd(28)
      + [...v.barrels].join(', ')));
  console.log('');
  console.log('Alle betroffenen Dateien:');
  [...barrelUsers.entries()].sort().forEach(([f, b]) => console.log('  ' + f + '  <- ' + b.join(', ')));
} else {
  console.log('Kein Barrel-Import im eager geladenen Graphen. Das ist die Ziellinie.');
}

/**
 * The shortest way in. Cutting an area out of the first page load means cutting one edge - putting
 * a dynamic import() where a static one is now - and this says which one, rather than leaving it to
 * be guessed at across two hundred files.
 */
function chainInto(prefix) {
  const first = [...eager]
    .map(f => path.relative(ROOT, f).replace(/\\/g, '/'))
    .filter(f => f.startsWith(prefix))
    .map(f => path.join(ROOT, f))
    .sort((a, b) => depth(a) - depth(b))[0];
  if (!first) {
    return null;
  }
  const chain = [];
  for (let at = first; at; at = pulledInBy.get(at)) {
    chain.unshift(path.relative(ROOT, at).replace(/\\/g, '/'));
    if (chain.length > 20) {
      break;
    }
  }
  return chain;
}

function depth(file) {
  let n = 0;
  for (let at = file; at && n < 50; at = pulledInBy.get(at)) {
    n++;
  }
  return n;
}

const areas = process.argv.slice(2).length ? process.argv.slice(2) : ['app/editor', 'app/backend'];

for (const area of areas) {
  const chain = chainInto(area);
  console.log('');
  if (!chain) {
    console.log('Kein eager Pfad nach ' + area + ' - bereits lazy.');
  } else {
    console.log('Kürzester eager Pfad nach ' + area + ':');
    chain.forEach((f, i) => console.log('  ' + '  '.repeat(i) + (i ? '-> ' : '') + f));
  }
}

console.log('');
console.log('Meistgenutzte Pakete im eager Graphen:');
[...packages.entries()].sort((a, b) => b[1] - a[1]).slice(0, 12)
  .forEach(([p, n]) => console.log('  ' + String(n).padStart(4) + '  ' + p));
