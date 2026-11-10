import { requireEnv } from './config.mjs';

// The server signs tokens for 36 000 s (UserController.auth). A new one is fetched when less than
// this is left, so a reconnect never goes out with a token about to lapse.
const RENEW_BEFORE_MS = 30 * 60 * 1000;

/**
 * The recorder's own account, never an admin and never anonymous: without a token the server would
 * create a new anonymous player for every connect, and those land in the very funnel the recorder
 * is there to measure. The server gives this account the RECORDER role (razarion.recorder.email).
 */
export function createAuth(baseUrl) {
  let token = null;
  let expiresAt = 0;
  let roles = [];

  async function login() {
    const [email, password] = requireEnv('RAZARION_RECORDER_EMAIL', 'RAZARION_RECORDER_PASSWORD');
    const res = await fetch(`${baseUrl}/rest/user/auth`, {
      method: 'POST',
      headers: { Authorization: 'Basic ' + Buffer.from(`${email}:${password}`).toString('base64') },
    });
    const body = await res.text();
    if (!res.ok) {
      throw new Error(`Login failed (HTTP ${res.status}). Check RAZARION_RECORDER_EMAIL and RAZARION_RECORDER_PASSWORD.`);
    }
    token = body.replace(/^"|"$/g, '').trim();
    const claims = JSON.parse(Buffer.from(token.split('.')[1], 'base64url').toString('utf8'));
    expiresAt = claims.exp * 1000;
    roles = String(claims.scope || '').split(' ').filter(Boolean);
  }

  return {
    async token() {
      if (!token || Date.now() > expiresAt - RENEW_BEFORE_MS) await login();
      return token;
    },
    get roles() {
      return roles;
    },
    get expiresAt() {
      return expiresAt;
    },
    invalidate() {
      token = null;
    },
  };
}

export async function getJson(baseUrl, auth, path) {
  const res = await fetch(`${baseUrl}${path}`, { headers: { Authorization: 'Bearer ' + (await auth.token()) } });
  if (res.status === 401) auth.invalidate();
  if (!res.ok) {
    const error = new Error(`GET ${path}: HTTP ${res.status}`);
    error.status = res.status;
    throw error;
  }
  return res.json();
}

/** The recorder's one write: the chat assistant's answer (RecorderController.chatAnswer). */
export async function postText(baseUrl, auth, path, text) {
  const res = await fetch(`${baseUrl}${path}`, {
    method: 'POST',
    headers: { Authorization: 'Bearer ' + (await auth.token()), 'Content-Type': 'text/plain; charset=utf-8' },
    body: text,
  });
  if (res.status === 401) auth.invalidate();
  if (!res.ok) {
    const error = new Error(`POST ${path}: HTTP ${res.status}`);
    error.status = res.status;
    throw error;
  }
}

/** A file's bytes as the body, e.g. a clip for the chat; answers the server's JSON. */
export async function postBytes(baseUrl, auth, path, bytes, contentType) {
  const res = await fetch(`${baseUrl}${path}`, {
    method: 'POST',
    headers: { Authorization: 'Bearer ' + (await auth.token()), 'Content-Type': contentType },
    body: bytes,
  });
  if (res.status === 401) auth.invalidate();
  if (!res.ok) {
    const error = new Error(`POST ${path}: HTTP ${res.status}`);
    error.status = res.status;
    throw error;
  }
  return res.json();
}
