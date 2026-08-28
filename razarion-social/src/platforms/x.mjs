import { basename } from 'node:path';
import { request, postJson, getJson, sleep } from '../util/http.mjs';
import { fileChunks } from '../util/chunks.mjs';
import { xAccessToken } from '../auth/x.mjs';
import { step, ok, warn, progress } from '../util/log.mjs';

const MEDIA_BASE = 'https://api.x.com/2/media/upload';
const TWEETS_URL = 'https://api.x.com/2/tweets';
// X rejects append segments over 5 MB.
const CHUNK = 4 * 1024 * 1024;

const hasLink = (text) => /https?:\/\//i.test(text);

// Pay-per-use since February 2026. A link multiplies the price of a post by more than ten, and
// almost every post here carries one, so the cost is worth stating out loud before it is spent.
export function estimateCost(text) {
  return hasLink(text) ? 0.2 : 0.015;
}

// X wants the real MIME type, and rejects a JPEG announced as video/mp4. The spec only ever
// carried clips, so this was hardcoded; composed posts can attach a screenshot too.
const MEDIA_TYPES = {
  '.mp4': 'video/mp4',
  '.mov': 'video/quicktime',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.png': 'image/png',
  '.gif': 'image/gif',
};

function mediaTypeFor(path) {
  const ext = path.slice(path.lastIndexOf('.')).toLowerCase();
  return MEDIA_TYPES[ext] || 'video/mp4';
}

async function uploadMedia(token, path, size, category) {
  step('X: initialising media upload');
  const init = await postJson(
    `${MEDIA_BASE}/initialize`,
    { media_type: mediaTypeFor(path), total_bytes: size, media_category: category },
    { headers: { Authorization: `Bearer ${token}` } }
  );
  const mediaId = init.data?.id || init.data?.media_id_string || init.media_id_string;
  if (!mediaId) throw new Error(`X did not return a media id:\n${JSON.stringify(init, null, 2)}`);

  let segment = 0;
  for await (const { chunk, end } of fileChunks(path, CHUNK)) {
    const form = new FormData();
    form.set('segment_index', String(segment));
    form.set('media', new Blob([chunk]), basename(path));
    await request(`${MEDIA_BASE}/${mediaId}/append`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
      body: form,
    });
    progress('X', end + 1, size);
    segment += 1;
  }

  step('X: finalising media');
  let state = await postJson(`${MEDIA_BASE}/${mediaId}/finalize`, {}, { headers: { Authorization: `Bearer ${token}` } });

  // Video is transcoded server-side; posting before it finishes fails with a vague error.
  let info = state.data?.processing_info;
  while (info && ['pending', 'in_progress'].includes(info.state)) {
    await sleep((info.check_after_secs || 5) * 1000);
    const status = await getJson(`${MEDIA_BASE}?command=STATUS&media_id=${mediaId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    info = status.data?.processing_info;
    step(`X: transcoding ${info?.progress_percent ?? '?'}%`);
  }
  if (info?.state === 'failed') throw new Error(`X media processing failed: ${JSON.stringify(info.error)}`);

  return mediaId;
}

export async function postToX(spec, { dryRun }) {
  const x = spec.x;
  const withVideo = x.attachVideo !== false;
  const cost = estimateCost(x.text);

  if (dryRun) {
    step(`X: would post ${withVideo ? 'with video' : 'text only'} (~$${cost.toFixed(3)})`);
    console.log('    ' + x.text.split('\n').join('\n    '));
    return { platform: 'x', dryRun: true, cost };
  }

  warn(`X: this post costs about $${cost.toFixed(3)}${hasLink(x.text) ? ' (contains a link)' : ''}.`);
  const token = await xAccessToken();

  let mediaIds;
  if (withVideo) {
    mediaIds = [await uploadMedia(token, spec.video, spec.videoSize, x.mediaCategory || 'tweet_video')];
  }

  const payload = { text: x.text };
  if (mediaIds) payload.media = { media_ids: mediaIds };
  if (x.replyTo) payload.reply = { in_reply_to_tweet_id: x.replyTo };

  const res = await postJson(TWEETS_URL, payload, { headers: { Authorization: `Bearer ${token}` } });
  const id = res.data?.id;
  const url = `https://x.com/${spec.x.handle || 'razariongame'}/status/${id}`;
  ok(`X: posted - ${url}`);

  // A thread is just replies chained onto the previous id; the video rides on the first post only.
  let previous = id;
  for (const line of x.thread || []) {
    const reply = await postJson(
      TWEETS_URL,
      { text: line, reply: { in_reply_to_tweet_id: previous } },
      { headers: { Authorization: `Bearer ${token}` } }
    );
    previous = reply.data?.id;
    ok('X: replied in thread');
  }

  return { platform: 'x', url, id, cost };
}
