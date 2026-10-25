#!/usr/bin/env node
// The review page: every post that is not out yet, with its media and the text per network, to
// read, correct and approve in a browser instead of editing four JSON files by hand.
//
//   node review.mjs                 # http://127.0.0.1:4711
//   node review.mjs --port 4800
//   node review.mjs --open          # also opens the browser
//
// The server side is lib/review-routes.mjs. engine.mjs serves the same page together with its
// dashboard on the same port, so while the engine runs this finds the port taken and, with
// --open, opens the engine's page instead.

import { createServer } from 'node:http';
import { spawn } from 'node:child_process';
import { handleReview, collectPosts } from './lib/review-routes.mjs';
import { parseArgs } from './lib/args.mjs';
import { info, ok, fail } from '../src/util/log.mjs';

const args = parseArgs(process.argv.slice(2));
const PORT = Number(args.port || 4711);
const HOST = '127.0.0.1';

const server = createServer(async (req, res) => {
  const url = new URL(req.url, `http://${HOST}:${PORT}`);
  if (!(await handleReview(req, res, url))) res.writeHead(404).end();
});

const ADDRESS = `http://${HOST}:${PORT}`;

function openBrowser() {
  if (args.open && process.platform === 'win32') spawn('cmd', ['/c', 'start', '', ADDRESS], { detached: true, stdio: 'ignore' }).unref();
}

server.on('error', (err) => {
  if (err.code === 'EADDRINUSE') {
    // Most likely the page from an earlier double-click is still running: show that one.
    if (args.open) {
      ok(`The review page is already running at ${ADDRESS} - opening it.`);
      openBrowser();
      return;
    }
    fail(`Port ${PORT} is taken - is the review page already running? --port picks another.`);
  } else {
    fail(err.message);
  }
  process.exit(1);
});

server.listen(PORT, HOST, () => {
  const open = collectPosts(false);
  ok(`Review page at ${ADDRESS}`);
  info(`  ${open.filter((p) => p.waiting).length} post(s) waiting for review, ${open.length} not fully out yet.`);
  info('  Ctrl+C stops it.');
  openBrowser();
});
