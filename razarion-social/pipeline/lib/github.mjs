import { env, requireEnv } from '../../src/config.mjs';
import { step } from '../../src/util/log.mjs';

const API = 'https://api.github.com';
const UPLOADS = 'https://uploads.github.com';

export function githubConfig() {
  const [token] = requireEnv('GITHUB_TOKEN');
  return {
    token,
    repo: env.GITHUB_REPO || 'Razarion/razarion',
    tag: env.GITHUB_RELEASE_TAG || 'instagram-media',
  };
}

function headers(config, extra = {}) {
  return {
    Authorization: 'Bearer ' + config.token,
    Accept: 'application/vnd.github+json',
    'X-GitHub-Api-Version': '2022-11-28',
    'User-Agent': 'razarion-pipeline/1.0',
    ...extra,
  };
}

async function gh(config, path, options = {}) {
  const res = await fetch(API + path, { ...options, headers: headers(config, options.headers) });
  const text = await res.text();
  let body = text;
  try {
    body = text ? JSON.parse(text) : null;
  } catch {
    /* keep the raw text for the error */
  }
  if (!res.ok) {
    const detail = body && body.message ? body.message : text;
    const hint =
      res.status === 401
        ? ' -> GITHUB_TOKEN is invalid or expired.'
        : res.status === 403
          ? ' -> the token lacks "Contents: read and write" on this repository.'
          : res.status === 404
            ? ' -> repository or release not found, or the token cannot see it.'
            : '';
    const error = new Error(`GitHub ${res.status} on ${path}: ${detail}${hint}`);
    error.status = res.status;
    throw error;
  }
  return body;
}

/**
 * Finds the release the media hangs off, creating it once if it is not there.
 *
 * A release rather than a commit, so 57 MB of screenshots never enters the git history of a source
 * repository. It is marked as a prerelease so it does not present itself as a version of the game.
 */
export async function ensureRelease(config) {
  try {
    return await gh(config, `/repos/${config.repo}/releases/tags/${config.tag}`);
  } catch (err) {
    if (err.status !== 404) throw err;
  }

  step(`creating release "${config.tag}" in ${config.repo}`);
  return gh(config, `/repos/${config.repo}/releases`, {
    method: 'POST',
    body: JSON.stringify({
      tag_name: config.tag,
      name: 'Instagram media',
      body:
        'Media referenced by Instagram posts. Instagram fetches files from a URL rather than ' +
        'accepting uploads, so the images and videos of the X backfill live here.\n\n' +
        'Not a release of the game.',
      prerelease: true,
    }),
  });
}

export async function listAssets(config, releaseId) {
  const assets = await gh(config, `/repos/${config.repo}/releases/${releaseId}/assets?per_page=100`);
  return new Map(assets.map((a) => [a.name, a]));
}

/**
 * Uploads one file and returns the URL Instagram will be handed.
 *
 * The content type matters: GitHub stores whatever is sent here and serves it back on download,
 * and Instagram rejects media it cannot identify.
 */
export async function uploadAsset(config, releaseId, name, body, contentType) {
  const url = `${UPLOADS}/repos/${config.repo}/releases/${releaseId}/assets?name=${encodeURIComponent(name)}`;
  const res = await fetch(url, {
    method: 'POST',
    headers: headers(config, { 'Content-Type': contentType, 'Content-Length': String(body.length) }),
    body,
  });

  const text = await res.text();
  if (!res.ok) {
    throw new Error(`GitHub asset upload failed (${res.status}) for ${name}: ${text}`);
  }
  const asset = JSON.parse(text);
  return { url: asset.browser_download_url, name: asset.name, id: asset.id };
}
