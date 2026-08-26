import { postForm } from '../util/http.mjs';
import { requireEnv, saveToken, getToken, env } from '../config.mjs';
import { authorise, randomUrlSafe, pkcePair } from './loopback.mjs';
import { ok } from '../util/log.mjs';

const AUTH_URL = 'https://x.com/i/oauth2/authorize';
const TOKEN_URL = 'https://api.x.com/2/oauth2/token';
// media.write is a separate grant from tweet.write: a token without it gets a 403 the moment it
// touches the upload endpoint, even though it can post text fine.
const SCOPES = ['tweet.read', 'tweet.write', 'users.read', 'media.write', 'offline.access'];

const port = () => Number(env.X_REDIRECT_PORT || 8724);
const redirectUri = () => env.X_REDIRECT_URI || `http://127.0.0.1:${port()}`;

const basicAuth = (id, secret) => 'Basic ' + Buffer.from(`${id}:${secret}`).toString('base64');

export async function authoriseX() {
  const [clientId, clientSecret] = requireEnv('X_CLIENT_ID', 'X_CLIENT_SECRET');
  const state = randomUrlSafe(16);
  const { verifier, challenge } = pkcePair();

  const authUrl =
    AUTH_URL +
    '?' +
    new URLSearchParams({
      response_type: 'code',
      client_id: clientId,
      redirect_uri: redirectUri(),
      scope: SCOPES.join(' '),
      state,
      code_challenge: challenge,
      code_challenge_method: 'S256',
    });

  const code = await authorise({ authUrl, port: port(), state });
  const token = await postForm(
    TOKEN_URL,
    {
      code,
      grant_type: 'authorization_code',
      client_id: clientId,
      redirect_uri: redirectUri(),
      code_verifier: verifier,
    },
    { headers: { Authorization: basicAuth(clientId, clientSecret) } }
  );

  if (!token.refresh_token) {
    throw new Error('X returned no refresh token. Check that "offline.access" is in the app scopes.');
  }
  saveToken('x', { refresh_token: token.refresh_token });
  ok('X authorised. Refresh token stored in razarion-social/.tokens.json');
}

export async function xAccessToken() {
  const [clientId, clientSecret] = requireEnv('X_CLIENT_ID', 'X_CLIENT_SECRET');
  const { refresh_token } = getToken('x');
  const token = await postForm(
    TOKEN_URL,
    { refresh_token, grant_type: 'refresh_token', client_id: clientId },
    { headers: { Authorization: basicAuth(clientId, clientSecret) } }
  );
  // X rotates the refresh token on every use; keeping the old one means the next run fails.
  if (token.refresh_token) saveToken('x', { refresh_token: token.refresh_token });
  return token.access_token;
}
