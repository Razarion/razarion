import { basename } from 'node:path';
import { request } from '../util/http.mjs';
import { fileChunks, readWhole } from '../util/chunks.mjs';
import { googleAccessToken } from '../auth/google.mjs';
import { step, ok, warn, progress } from '../util/log.mjs';

const UPLOAD_URL = 'https://www.googleapis.com/upload/youtube/v3/videos';
const THUMBNAIL_URL = 'https://www.googleapis.com/upload/youtube/v3/thumbnails/set';
const CHUNK = 8 * 1024 * 1024;

/**
 * Uploads the clip with its metadata and returns the video URL.
 *
 * The default privacy is "private" on purpose. An OAuth client that has not passed Google's
 * compliance audit may only create private videos - a public upload is silently forced back to
 * private - so the honest workflow is: this uploads everything, you flip the switch in Studio.
 */
export async function postToYouTube(spec, { dryRun }) {
  const yt = spec.youtube;
  const privacy = yt.privacy || 'private';

  if (dryRun) {
    step(`YouTube: would upload ${basename(spec.video)} as "${yt.title}" (${privacy})`);
    return { platform: 'youtube', dryRun: true };
  }

  const token = await googleAccessToken();
  const metadata = {
    snippet: {
      title: yt.title,
      description: yt.description || '',
      tags: yt.tags || [],
      // 20 is "Gaming". Overridable, but it is the right answer for every clip this repo ships.
      categoryId: String(yt.categoryId || 20),
    },
    status: {
      privacyStatus: privacy,
      selfDeclaredMadeForKids: yt.madeForKids ?? false,
    },
  };

  step('YouTube: starting resumable upload session');
  const init = await request(`${UPLOAD_URL}?uploadType=resumable&part=snippet,status`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json; charset=UTF-8',
      'X-Upload-Content-Length': String(spec.videoSize),
      'X-Upload-Content-Type': yt.mimeType || 'video/mp4',
    },
    body: JSON.stringify(metadata),
  });

  const session = init.headers.get('location');
  if (!session) throw new Error('YouTube did not return a resumable upload URL.');

  let video = null;
  for await (const { chunk, start, end } of fileChunks(spec.video, CHUNK)) {
    const res = await fetch(session, {
      method: 'PUT',
      headers: {
        'Content-Length': String(chunk.length),
        'Content-Range': `bytes ${start}-${end}/${spec.videoSize}`,
      },
      body: chunk,
    });
    progress('YouTube', end + 1, spec.videoSize);
    // 308 means "send the next chunk"; 200/201 means the last one landed and the body is the video.
    if (res.status === 308) continue;
    if (!res.ok) throw new Error(`YouTube upload failed: HTTP ${res.status}\n${await res.text()}`);
    video = await res.json();
  }
  if (!video?.id) throw new Error('YouTube upload finished without returning a video id.');

  if (spec.thumbnail) {
    step('YouTube: setting thumbnail');
    await request(`${THUMBNAIL_URL}?videoId=${video.id}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'image/png' },
      body: await readWhole(spec.thumbnail),
    });
  }

  const url = `https://www.youtube.com/watch?v=${video.id}`;
  ok(`YouTube: uploaded as ${privacy} - ${url}`);
  if (privacy === 'private') {
    warn('YouTube: set it to public in Studio when you are ready. Automating that step needs a Google compliance audit.');
  }
  return { platform: 'youtube', url, id: video.id };
}
