import { postForm } from '../util/http.mjs';
import { requireEnv, saveToken, getToken, env } from '../config.mjs';
import { authorise, randomUrlSafe } from './loopback.mjs';
import { ok } from '../util/log.mjs';

const AUTH_URL = 'https://accounts.google.com/o/oauth2/v2/auth';
const TOKEN_URL = 'https://oauth2.googleapis.com/token';
// youtube.upload alone cannot set a thumbnail, so the broader scope comes along for that one call.
const SCOPES = ['https://www.googleapis.com/auth/youtube.upload', 'https://www.googleapis.com/auth/youtube'];

const port = () => Number(env.GOOGLE_REDIRECT_PORT || 8723);
const redirectUri = () => `http://127.0.0.1:${port()}`;

export async function authoriseGoogle() {
  const [clientId, clientSecret] = requireEnv('GOOGLE_CLIENT_ID', 'GOOGLE_CLIENT_SECRET');
  const state = randomUrlSafe(16);

  const authUrl =
    AUTH_URL +
    '?' +
    new URLSearchParams({
      client_id: clientId,
      redirect_uri: redirectUri(),
      response_type: 'code',
      scope: SCOPES.join(' '),
      // Without both of these Google hands back no refresh token on re-authorisation, and the
      // whole point of this step is getting one that outlives the session.
      access_type: 'offline',
      prompt: 'consent',
      state,
    });

  const code = await authorise({ authUrl, port: port(), state });
  const token = await postForm(TOKEN_URL, {
    code,
    client_id: clientId,
    client_secret: clientSecret,
    redirect_uri: redirectUri(),
    grant_type: 'authorization_code',
  });

  if (!token.refresh_token) {
    throw new Error('Google returned no refresh token. Revoke the app at myaccount.google.com/permissions and retry.');
  }
  saveToken('google', { refresh_token: token.refresh_token });
  ok('Google authorised. Refresh token stored in razarion-social/.tokens.json');
}

export async function googleAccessToken() {
  const [clientId, clientSecret] = requireEnv('GOOGLE_CLIENT_ID', 'GOOGLE_CLIENT_SECRET');
  const { refresh_token } = getToken('google');
  const token = await postForm(TOKEN_URL, {
    client_id: clientId,
    client_secret: clientSecret,
    refresh_token,
    grant_type: 'refresh_token',
  });
  return token.access_token;
}
