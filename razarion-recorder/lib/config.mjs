import { readFileSync, existsSync, mkdirSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const here = dirname(fileURLToPath(import.meta.url));
export const ROOT = join(here, '..');
export const STATE_DIR = join(ROOT, 'state');
const ENV_FILE = join(ROOT, '.env');

// The same five-line parser as razarion-social/src/config.mjs: KEY=value, comments, quotes.
function loadEnvFile() {
  if (!existsSync(ENV_FILE)) return {};
  const out = {};
  for (const raw of readFileSync(ENV_FILE, 'utf8').split(/\r?\n/)) {
    const line = raw.trim();
    if (!line || line.startsWith('#')) continue;
    const eq = line.indexOf('=');
    if (eq < 0) continue;
    let value = line.slice(eq + 1).trim();
    if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
      value = value.slice(1, -1);
    }
    out[line.slice(0, eq).trim()] = value;
  }
  return out;
}

// Real environment wins over the file.
export const env = { ...loadEnvFile(), ...process.env };

export function requireEnv(...keys) {
  const missing = keys.filter((k) => !env[k]);
  if (missing.length) {
    throw new Error(`Missing in razarion-recorder/.env: ${missing.join(', ')} - see razarion-recorder/README.md`);
  }
  return keys.map((k) => env[k]);
}

export function ensureStateDir() {
  mkdirSync(STATE_DIR, { recursive: true });
  return STATE_DIR;
}

const number = (key, fallback) => (env[key] ? Number(env[key]) : fallback);

export const settings = {
  baseUrl: (env.RAZARION_BASE_URL || 'https://www.razarion.com').replace(/\/$/, ''),
  // How much of the past is held. Whatever is older, or over the budget, is dropped oldest first.
  keepHours: number('RECORDER_KEEP_HOURS', 6),
  maxMegabytes: number('RECORDER_MAX_MB', 512),
  // A fresh picture of the whole world, so a replay can start anywhere and not only at a connect.
  keyframeMinutes: number('RECORDER_KEYFRAME_MINUTES', 5),
  feedSeconds: number('RECORDER_FEED_SECONDS', 5),
  // The planet's chat (lib/chat.mjs); RECORDER_CHAT_ANSWER=0 keeps reading it but answers nothing.
  chatSeconds: number('RECORDER_CHAT_SECONDS', 5),
  chatAnswer: env.RECORDER_CHAT_ANSWER !== '0',
  port: number('RECORDER_PORT', 4712),
  // The game client watching a replay (lib/replay-server.mjs): the game server's pages, the tape's game connection.
  replayPort: number('RECORDER_REPLAY_PORT', 4713),
  // Where the replay client's pages come from: the game server, or an Angular dev server in front
  // of it (http://127.0.0.1:4201), which picks up a frontend change without a server build.
  replayUi: (env.RECORDER_REPLAY_UI || '').replace(/\/$/, ''),
  // The social engine the shorts go to (razarion-social/pipeline/engine.mjs, POST /api/moments).
  engineUrl: (env.RECORDER_ENGINE_URL || 'http://127.0.0.1:4711').replace(/\/$/, ''),
};
