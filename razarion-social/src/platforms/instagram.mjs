import { postForm, getJson, sleep } from '../util/http.mjs';
import { requireEnv, env } from '../config.mjs';
import { step, ok, warn } from '../util/log.mjs';

const API_VERSION = () => env.INSTAGRAM_API_VERSION || 'v23.0';
const base = () => `https://graph.instagram.com/${API_VERSION()}`;

/**
 * Publishes the clip as a Reel.
 *
 * Two things make this the only platform here that cannot work today:
 *   1. instagram_business_content_publish has to clear Meta's app review, and
 *   2. Instagram never accepts an upload - it fetches the file from a public URL you provide,
 *      which has to still be reachable while the container is being created.
 * Hence instagram.videoUrl in the post spec rather than a local path.
 */
export async function postToInstagram(spec, { dryRun }) {
  const ig = spec.instagram;

  if (dryRun) {
    step(`Instagram: would publish a Reel from ${ig.videoUrl}`);
    return { platform: 'instagram', dryRun: true };
  }

  const [userId, token] = requireEnv('INSTAGRAM_USER_ID', 'INSTAGRAM_ACCESS_TOKEN');

  step('Instagram: creating media container');
  const container = await postForm(`${base()}/${userId}/media`, {
    media_type: 'REELS',
    video_url: ig.videoUrl,
    caption: ig.caption || '',
    share_to_feed: String(ig.shareToFeed ?? true),
    access_token: token,
  });

  // Instagram downloads and transcodes the file before it will publish it; publishing early
  // returns a "media not ready" error rather than waiting.
  step('Instagram: waiting for the container to finish');
  let ready = false;
  for (let i = 0; i < 60; i++) {
    const status = await getJson(
      `${base()}/${container.id}?fields=status_code,status&access_token=${encodeURIComponent(token)}`
    );
    if (status.status_code === 'FINISHED') {
      ready = true;
      break;
    }
    if (status.status_code === 'ERROR' || status.status_code === 'EXPIRED') {
      throw new Error(`Instagram container ${status.status_code}: ${status.status || ''}`);
    }
    await sleep(5000);
  }
  if (!ready) throw new Error('Instagram container did not finish within five minutes.');

  step('Instagram: publishing');
  const published = await postForm(`${base()}/${userId}/media_publish`, {
    creation_id: container.id,
    access_token: token,
  });

  ok(`Instagram: published - media id ${published.id}`);
  warn('Instagram: the access token expires after 60 days. Refresh it before then (see README).');
  return { platform: 'instagram', id: published.id };
}
