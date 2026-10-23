// Cliff Lab: node tools/cliff-lab/serve.mjs -> http://localhost:4210 (the game's textures from public/)
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
const pub = path.join(here, '..', '..', 'public');
const TYPES = {'.html': 'text/html; charset=utf-8', '.webp': 'image/webp', '.jpg': 'image/jpeg', '.png': 'image/png'};
const PORT = 4210;

http.createServer((req, res) => {
  const url = decodeURIComponent(new URL(req.url, 'http://x').pathname);
  const file = url === '/' ? path.join(here, 'index.html') : path.join(pub, path.normalize(url));
  if (!file.startsWith(here) && !file.startsWith(pub)) {
    res.writeHead(403).end();
    return;
  }
  fs.readFile(file, (err, data) => {
    if (err) {
      res.writeHead(404).end();
      return;
    }
    res.writeHead(200, {'Content-Type': TYPES[path.extname(file)] ?? 'application/octet-stream', 'Cache-Control': 'no-store'});
    res.end(data);
  });
}).listen(PORT, () => console.log(`Cliff Lab: http://localhost:${PORT}`));
