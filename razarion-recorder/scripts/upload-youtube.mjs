// Upload a rendered time-lapse to YouTube (private first, as razarion-social does), then set it public.
// Run with cwd = razarion2/razarion-social (its .env / .tokens.json):
//   node ../../razarion/razarion-recorder/scripts/upload-youtube.mjs <video.mp4> <thumbnail.png> "<title>"
import { statSync } from 'node:fs';
import { pathToFileURL } from 'node:url';
import path from 'node:path';

const social = process.cwd();
const { postToYouTube } = await import(pathToFileURL(path.join(social, 'src/platforms/youtube.mjs')));
const { googleAccessToken } = await import(pathToFileURL(path.join(social, 'src/auth/google.mjs')));

const [video, thumbnail, title] = process.argv.slice(2);
const description = `A whole day on the Razarion live server, 64 times faster.

Every dot is a unit, every square a building: bots in red, real players in colour, yellow lines are shots. You are looking at the beginner area - the island where every new player starts, the river band and the first regions on the mainland.

Razarion is a free, open-source real-time strategy MMO that runs in the browser. One planet for everyone, and it never stops.
Play: https://www.razarion.com`;

const result = await postToYouTube({
  video, videoSize: statSync(video).size, thumbnail,
  youtube: { title, description, tags: ['Razarion', 'RTS', 'MMO', 'browser game', 'timelapse', 'strategy game'] },
}, { dryRun: false });

// Same as reference_youtube_public_via_api: GET the status, flip privacy, PUT it back whole.
const token = await googleAccessToken();
const got = await (await fetch(`https://www.googleapis.com/youtube/v3/videos?part=status&id=${result.id}`, { headers: { Authorization: `Bearer ${token}` } })).json();
const status = { ...got.items[0].status, privacyStatus: 'public' };
const put = await fetch('https://www.googleapis.com/youtube/v3/videos?part=status', {
  method: 'PUT', headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
  body: JSON.stringify({ id: result.id, status }),
});
console.log(`public: HTTP ${put.status} ${put.ok ? '' : await put.text()}`);
console.log(result.url);
