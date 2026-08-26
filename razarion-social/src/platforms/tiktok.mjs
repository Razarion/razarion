import { postJson, sleep } from '../util/http.mjs';
import { readRange } from '../util/chunks.mjs';
import { tiktokAccessToken } from '../auth/tiktok.mjs';
import { step, ok, warn, progress } from '../util/log.mjs';

const INIT_URL = 'https://open.tiktokapis.com/v2/post/publish/inbox/video/init/';
const STATUS_URL = 'https://open.tiktokapis.com/v2/post/publish/status/fetch/';
// TikTok wants chunks of at least 5 MB; the final one may run up to 128 MB.
const CHUNK = 10 * 1024 * 1024;

/**
 * Uploads the clip into the account's TikTok drafts.
 *
 * Direct publishing exists (the video.publish scope) but needs TikTok to audit the app first,
 * and until they do, everything an unaudited client posts is visible to nobody but you. Drafts
 * need no audit and cost one tap in the app - the better trade for a one-person project.
 */
export async function postToTikTok(spec, { dryRun }) {
  const size = spec.videoSize;

  if (dryRun) {
    step(`TikTok: would upload ${(size / 1024 / 1024).toFixed(1)} MB to your drafts`);
    return { platform: 'tiktok', dryRun: true };
  }

  const token = await tiktokAccessToken();
  const totalChunks = Math.max(1, Math.floor(size / CHUNK));
  const chunkSize = totalChunks === 1 ? size : CHUNK;

  step('TikTok: initialising upload');
  const init = await postJson(
    INIT_URL,
    { source_info: { source: 'FILE_UPLOAD', video_size: size, chunk_size: chunkSize, total_chunk_count: totalChunks } },
    { headers: { Authorization: `Bearer ${token}` } }
  );
  if (init.error && init.error.code !== 'ok') {
    throw new Error(`TikTok init failed: ${init.error.code} - ${init.error.message}`);
  }
  const { publish_id: publishId, upload_url: uploadUrl } = init.data;

  // The chunk count is fixed at init, so the last chunk carries the remainder instead of
  // becoming an extra chunk of its own.
  for (let i = 0; i < totalChunks; i++) {
    const start = i * chunkSize;
    const end = i === totalChunks - 1 ? size - 1 : start + chunkSize - 1;
    const chunk = await readRange(spec.video, start, end - start + 1);
    const res = await fetch(uploadUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': 'video/mp4',
        'Content-Length': String(chunk.length),
        'Content-Range': `bytes ${start}-${end}/${size}`,
      },
      body: chunk,
    });
    if (!res.ok) throw new Error(`TikTok chunk upload failed: HTTP ${res.status}\n${await res.text()}`);
    progress('TikTok', end + 1, size);
  }

  step('TikTok: waiting for processing');
  for (let i = 0; i < 60; i++) {
    const status = await postJson(STATUS_URL, { publish_id: publishId }, { headers: { Authorization: `Bearer ${token}` } });
    const s = status.data?.status;
    if (s === 'SEND_TO_USER_INBOX' || s === 'PUBLISH_COMPLETE') break;
    if (s === 'FAILED') throw new Error(`TikTok processing failed: ${JSON.stringify(status.data)}`);
    await sleep(5000);
  }

  ok('TikTok: the clip is in your drafts.');
  warn('TikTok: open the app, tap the inbox notification, add the caption and post.');
  return { platform: 'tiktok', publishId };
}
