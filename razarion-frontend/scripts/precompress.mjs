// Writes a brotli copy (.br) next to every JavaScript and CSS file of a finished Angular build.
//
// The game's start is bandwidth-bound on a phone, and 2.5 MB of it is this JavaScript - all of it
// needed before the engine can run. It went out as gzip only, although every browser we see asks
// for brotli too. The server hands out the .br copy to those that ask (EncodedResourceResolver in
// WebMvcConfiguration) and the original to the rest; the content hash in the file name stays the
// same, so nothing about caching changes.
//
// Quality 11 is slow and only paid once per build. Usage: node scripts/precompress.mjs <dir>...
import {readdirSync, readFileSync, statSync, writeFileSync} from 'node:fs';
import {join} from 'node:path';
import {brotliCompressSync, constants} from 'node:zlib';

const EXTENSIONS = /\.(js|css|mjs)$/;

function walk(dir) {
  return readdirSync(dir).flatMap(name => {
    const path = join(dir, name);
    return statSync(path).isDirectory() ? walk(path) : [path];
  });
}

for (const dir of process.argv.slice(2)) {
  let files = 0, before = 0, after = 0;
  for (const path of walk(dir).filter(p => EXTENSIONS.test(p))) {
    const content = readFileSync(path);
    const compressed = brotliCompressSync(content, {
      params: {
        [constants.BROTLI_PARAM_QUALITY]: 11,
        [constants.BROTLI_PARAM_SIZE_HINT]: content.length
      }
    });
    writeFileSync(path + '.br', compressed);
    files++;
    before += content.length;
    after += compressed.length;
  }
  console.log(`precompress ${dir}: ${files} files, ${(before / 1048576).toFixed(2)} MB -> ${(after / 1048576).toFixed(2)} MB brotli`);
}
