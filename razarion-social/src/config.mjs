import { readFileSync, writeFileSync, existsSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const here = dirname(fileURLToPath(import.meta.url));
export const ROOT = join(here, '..');
const ENV_FILE = join(ROOT, '.env');
const TOKEN_FILE = join(ROOT, '.tokens.json');

// A five-line parser instead of a dependency. Handles KEY=value, comments, blank lines and
// surrounding quotes - which is the whole of what a credentials file needs.
function loadEnvFile() {
  if (!existsSync(ENV_FILE)) return {};
  const out = {};
  for (const raw of readFileSync(ENV_FILE, 'utf8').split(/\r?\n/)) {
    const line = raw.trim();
    if (!line || line.startsWith('#')) continue;
    const eq = line.indexOf('=');
    if (eq < 0) continue;
    const key = line.slice(0, eq).trim();
    let value = line.slice(eq + 1).trim();
    if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
      value = value.slice(1, -1);
    }
    out[key] = value;
  }
  return out;
}

// Real environment wins over the file, so CI can inject secrets without one existing.
const fileEnv = loadEnvFile();
export const env = { ...fileEnv, ...process.env };

export function requireEnv(...keys) {
  const missing = keys.filter((k) => !env[k]);
  if (missing.length) {
    throw new Error(
      `Missing in razarion-social/.env: ${missing.join(', ')}\n` +
        `See razarion-social/README.md for where each value comes from.`
    );
  }
  return keys.map((k) => env[k]);
}

export function hasEnv(...keys) {
  return keys.every((k) => !!env[k]);
}

// Refresh tokens live next to the config rather than in it: .env is hand-edited, this file is
// rewritten by the auth flow, and mixing the two loses comments every time a token rotates.
export function readTokens() {
  if (!existsSync(TOKEN_FILE)) return {};
  return JSON.parse(readFileSync(TOKEN_FILE, 'utf8'));
}

export function saveToken(provider, data) {
  const all = readTokens();
  all[provider] = { ...(all[provider] || {}), ...data };
  writeFileSync(TOKEN_FILE, JSON.stringify(all, null, 2) + '\n', 'utf8');
}

export function getToken(provider) {
  const t = readTokens()[provider];
  if (!t) {
    throw new Error(`Not authorised for "${provider}". Run: node razarion-social/src/cli.mjs auth ${provider}`);
  }
  return t;
}

export const TOKEN_FILE_PATH = TOKEN_FILE;
export const ENV_FILE_PATH = ENV_FILE;
