import { env, requireEnv } from '../../src/config.mjs';
import { sleep } from '../../src/util/http.mjs';
import { warn } from '../../src/util/log.mjs';
import { FACEBOOK_TOKEN_FILE, readJson } from './paths.mjs';

// Pages live on graph.facebook.com, not on the graph.instagram.com host the Instagram side uses.
const VERSION = () => env.FACEBOOK_API_VERSION || 'v25.0';
export const base = () => `https://graph.facebook.com/${VERSION()}`;

/**
 * Same shape of unhelpful answer as the Instagram side, so the same treatment: a short sentence
 * about what usually causes it, with Meta's own message kept alongside.
 */
const ERROR_HINTS = {
  4: 'Application request limit reached. Wait for the window to move.',
  10: 'Permission denied. The token is missing pages_manage_posts, or it is a user token where a page token is needed.',
  100: 'Invalid parameter. Often a media URL Facebook could not fetch, or a malformed backdated_time.',
  190: 'Access token invalid or expired. Re-run fb_token.mjs.',
  200: 'Permission error. The token lacks pages_manage_posts, or you are not an admin of the Page.',
  368: 'The Page is temporarily blocked from posting. This is Facebook rate-limiting the Page itself.',
  1609005: 'Facebook could not fetch the link in the post - it is unreachable or blocked.',
};

export function explainError(body) {
  const err = (body && body.error) || {};
  const parts = [];
  if (err.message) parts.push(err.message);
  const hint = ERROR_HINTS[err.code] || ERROR_HINTS[err.error_subcode];
  if (hint) parts.push('-> ' + hint);
  if (err.code) {
    parts.push(
      `(code ${err.code}${err.error_subcode ? ', subcode ' + err.error_subcode : ''}; ` +
        'reference: developers.facebook.com/docs/graph-api/guides/error-handling)'
    );
  }
  return parts.join('\n     ') || JSON.stringify(body);
}

export class FacebookError extends Error {
  constructor(status, body) {
    super(`Facebook HTTP ${status}\n     ${explainError(body)}`);
    this.name = 'FacebookError';
    this.status = status;
    this.body = body;
    this.code = body && body.error ? body.error.code : undefined;
  }
}

async function call(method, path, params, attempt = 0) {
  const url = new URL(base() + path);
  const options = { method };

  if (method === 'GET') {
    for (const [k, v] of Object.entries(params)) url.searchParams.set(k, String(v));
  } else {
    options.headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
    options.body = new URLSearchParams(params).toString();
  }

  // Same rule as on the Instagram side: only repeat what cannot create something twice. A GET is
  // safe; a POST that publishes is not, because a lost reply is indistinguishable from a refusal.
  const repeatable = method === 'GET';

  let res;
  try {
    res = await fetch(url.toString(), options);
  } catch (err) {
    if (repeatable && attempt < 3) {
      const backoff = 5000 * 2 ** attempt;
      warn(`Network error talking to Facebook (${err.message}). Retrying in ${backoff / 1000}s.`);
      await sleep(backoff);
      return call(method, path, params, attempt + 1);
    }
    const wrapped = new Error(
      `Network error on ${method} ${path}: ${err.message}` +
        (repeatable ? '' : '\n     This was a publish call. Check the Page before re-running.')
    );
    wrapped.networkFailure = true;
    wrapped.duringPublish = !repeatable;
    throw wrapped;
  }

  const text = await res.text();
  let body = text;
  try {
    body = text ? JSON.parse(text) : null;
  } catch {
    /* keep the raw text for the error */
  }

  if (res.ok) return body;

  if (res.status >= 500 && attempt < 3) {
    const backoff = 2000 * 2 ** attempt;
    warn(`Facebook returned ${res.status}. Retrying in ${backoff / 1000}s.`);
    await sleep(backoff);
    return call(method, path, params, attempt + 1);
  }

  throw new FacebookError(res.status, body);
}

export const post = (path, params) => call('POST', path, params);
export const get = (path, params) => call('GET', path, params);

/**
 * The Page id and its access token.
 *
 * A page token derived from a long-lived user token does not expire, so unlike the Instagram one
 * this is written once by fb_token.mjs and then left alone. .env is not involved.
 */
export function pageCredentials() {
  const stored = readJson(FACEBOOK_TOKEN_FILE);
  if (!stored || !stored.access_token) {
    throw new Error(
      'No Facebook page token. Run: node fb_token.mjs\n' +
        '(it needs FB_APP_ID, FB_APP_SECRET and FB_USER_TOKEN in razarion-social/.env)'
    );
  }
  return [stored.page_id, stored.access_token, stored.page_name];
}

export function apiCredentials() {
  return requireEnv('FB_APP_ID', 'FB_APP_SECRET');
}
