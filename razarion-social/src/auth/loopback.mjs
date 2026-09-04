import { createServer } from 'node:http';
import { spawn } from 'node:child_process';
import { createInterface } from 'node:readline/promises';
import { randomBytes, createHash } from 'node:crypto';
import { step, warn } from '../util/log.mjs';

export const randomUrlSafe = (bytes = 32) => randomBytes(bytes).toString('base64url');

export function pkcePair() {
  const verifier = randomUrlSafe(32);
  const challenge = createHash('sha256').update(verifier).digest('base64url');
  return { verifier, challenge };
}

function openBrowser(url) {
  // Windows needs the URL quoted, and the quotes have to survive Node. cmd.exe re-parses its own
  // command line and reads & as a command separator, so an unquoted authorisation URL is cut at
  // the first parameter - the browser then opens a request carrying only client_id and Google
  // answers "Required parameter is missing: response_type", which reads like a bug in the request
  // rather than in how it was opened. windowsVerbatimArguments stops Node from re-escaping the
  // quotes we add. The empty pair before it is start's window-title argument: without it, start
  // treats the quoted URL as the title and opens nothing.
  const [command, args, options] =
    process.platform === 'win32'
      ? ['cmd', ['/c', 'start', '""', `"${url}"`], { windowsVerbatimArguments: true }]
      : process.platform === 'darwin'
        ? ['open', [url], {}]
        : ['xdg-open', [url], {}];
  try {
    spawn(command, args, { detached: true, stdio: 'ignore', ...options }).unref();
  } catch {
    warn('Could not open a browser automatically.');
  }
}

// TikTok rejects http://localhost redirect URIs, and some setups run this over SSH where no
// browser can open. Both cases fall back to the same thing: paste the URL you were redirected to.
async function pasteFallback(expectedState) {
  const rl = createInterface({ input: process.stdin, output: process.stdout });
  const pasted = await rl.question('\nPaste the full URL you were redirected to: ');
  rl.close();
  const url = new URL(pasted.trim());
  const error = url.searchParams.get('error');
  if (error) throw new Error(`Authorisation denied: ${error} ${url.searchParams.get('error_description') || ''}`);
  const state = url.searchParams.get('state');
  if (expectedState && state !== expectedState) throw new Error('State mismatch - restart the authorisation.');
  const code = url.searchParams.get('code');
  if (!code) throw new Error('No "code" parameter in that URL.');
  return code;
}

/**
 * Runs the browser half of an authorisation-code flow and resolves with the code.
 * Starts a one-shot local server on `port` unless `manual` is set.
 */
export async function authorise({ authUrl, port, state, manual = false }) {
  step('Opening the authorisation page in your browser.');
  console.log('\nIf nothing opens, visit this URL yourself:\n' + authUrl + '\n');
  openBrowser(authUrl);

  if (manual) return pasteFallback(state);

  return new Promise((resolve, reject) => {
    const server = createServer((req, res) => {
      const url = new URL(req.url, `http://127.0.0.1:${port}`);
      if (url.pathname === '/favicon.ico') {
        res.writeHead(404).end();
        return;
      }
      const code = url.searchParams.get('code');
      const error = url.searchParams.get('error');
      const returnedState = url.searchParams.get('state');

      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      res.end(
        `<!doctype html><meta charset="utf-8"><title>Razarion</title>` +
          `<body style="font-family:system-ui;padding:3rem;background:#111;color:#eee">` +
          `<h1>${code ? 'Authorised' : 'Authorisation failed'}</h1>` +
          `<p>${code ? 'You can close this tab and return to the terminal.' : error || 'No code returned.'}</p>`
      );
      server.close();

      if (error) return reject(new Error(`Authorisation denied: ${error}`));
      if (state && returnedState !== state) return reject(new Error('State mismatch - possible CSRF, restart.'));
      if (!code) return reject(new Error('No authorisation code in the redirect.'));
      resolve(code);
    });

    server.on('error', reject);
    server.listen(port, '127.0.0.1');
    // Nobody sits at a terminal for five minutes; failing loudly beats hanging forever.
    setTimeout(() => {
      server.close();
      reject(new Error('Timed out waiting for the browser redirect (5 min).'));
    }, 5 * 60 * 1000).unref();
  });
}
