import { postForm } from '../util/http.mjs';
import { requireEnv, saveToken, getToken, env } from '../config.mjs';
import { authorise, randomUrlSafe, pkcePair } from './loopback.mjs';
import { ok } from '../util/log.mjs';

const AUTH_URL = 'https://www.tiktok.com/v2/auth/authorize/';
const TOKEN_URL = 'https://open.tiktokapis.com/v2/oauth/token/';
// video.upload puts clips in your drafts and needs no app audit. video.publish would post
// publicly, but until TikTok audits the app everything it posts is private anyway.
const SCOPES = ['user.info.basic', 'video.upload'];

const port = () => Number(env.TIKTOK_REDIRECT_PORT || 8725);
// TikTok validates the redirect URI against the one registered in the developer portal, and it
// will not accept a bare http://localhost. Register an https URL, then use --manual to paste it.
const redirectUri = () => env.TIKTOK_REDIRECT_URI || `http://127.0.0.1:${port()}`;

export async function authoriseTikTok({ manual = false } = {}) {
  const [clientKey, clientSecret] = requireEnv('TIKTOK_CLIENT_KEY', 'TIKTOK_CLIENT_SECRET');
  const state = randomUrlSafe(16);
  const { verifier, challenge } = pkcePair();

  const authUrl =
    AUTH_URL +
    '?' +
    new URLSearchParams({
      client_key: clientKey,
      scope: SCOPES.join(','),
      response_type: 'code',
      redirect_uri: redirectUri(),
      state,
      code_challenge: challenge,
      code_challenge_method: 'S256',
    });

  const code = await authorise({ authUrl, port: port(), state, manual: manual || !!env.TIKTOK_REDIRECT_URI });
  const token = await postForm(TOKEN_URL, {
    client_key: clientKey,
    client_secret: clientSecret,
    code: decodeURIComponent(code),
    grant_type: 'authorization_code',
    redirect_uri: redirectUri(),
    code_verifier: verifier,
  });

  if (token.error) throw new Error(`${token.error}: ${token.error_description}`);
  if (!token.refresh_token) throw new Error('TikTok returned no refresh token.');
  saveToken('tiktok', { refresh_token: token.refresh_token });
  ok('TikTok authorised. Refresh token stored in razarion-social/.tokens.json');
}

export async function tiktokAccessToken() {
  const [clientKey, clientSecret] = requireEnv('TIKTOK_CLIENT_KEY', 'TIKTOK_CLIENT_SECRET');
  const { refresh_token } = getToken('tiktok');
  const token = await postForm(TOKEN_URL, {
    client_key: clientKey,
    client_secret: clientSecret,
    grant_type: 'refresh_token',
    refresh_token,
  });
  if (token.error) throw new Error(`${token.error}: ${token.error_description}`);
  if (token.refresh_token) saveToken('tiktok', { refresh_token: token.refresh_token });
  return token.access_token;
}
