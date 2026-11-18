// Upload a rendered time-lapse to YouTube (private first, as razarion-social does), then set it public.
// Run with cwd = razarion2/razarion-social (its .env / .tokens.json):
//   node ../../razarion/razarion-recorder/scripts/upload-youtube.mjs <video.mp4> <thumbnail.png> "<title>"
//   node ../../razarion/razarion-recorder/scripts/upload-youtube.mjs --existing <videoId> <thumbnail.png>
// The second form finishes a video that is up already: thumbnail, then public.
//
// The thumbnail is set here and not by postToYouTube, and a refusal does not stop the run: on
// 2026-11-17 YouTube answered 403 "The thumbnail can't be set for the specified video" right after
// the upload, the script ended there, and the day's video stayed private. A video without its own
// thumbnail is better than one nobody can see.
import { readFileSync, statSync } from 'node:fs';
import { pathToFileURL } from 'node:url';
import path from 'node:path';

const social = process.cwd();
const { postToYouTube } = await import(pathToFileURL(path.join(social, 'src/platforms/youtube.mjs')));
const { googleAccessToken } = await import(pathToFileURL(path.join(social, 'src/auth/google.mjs')));

const existing = process.argv[2] === '--existing';
const [video, thumbnail, title] = existing ? [null, process.argv[4], null] : process.argv.slice(2);
const description = `A whole day on the Razarion live server, 64 times faster.

Every dot is a unit, every square a building: bots in red, real players in colour, yellow lines are shots. You are looking at the beginner area - the island where every new player starts, the river band and the first regions on the mainland.

Razarion is a free, open-source real-time strategy MMO that runs in the browser. One planet for everyone, and it never stops.
Play: https://www.razarion.com`;

let id;
let url;
if (existing) {
  id = process.argv[3];
  url = `https://www.youtube.com/watch?v=${id}`;
} else {
  const result = await postToYouTube({
    video, videoSize: statSync(video).size,
    youtube: { title, description, tags: ['Razarion', 'RTS', 'MMO', 'browser game', 'timelapse', 'strategy game'] },
  }, { dryRun: false });
  id = result.id;
  url = result.url;
}

// A fresh token per call: the retries below can outlast the hour a token lives.
const auth = async () => ({ Authorization: `Bearer ${await googleAccessToken()}` });
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// Same as reference_youtube_public_via_api: GET the status, flip privacy, PUT it back whole.
// YouTube refuses this too (403 "Forbidden", domain youtube.video) while the upload is still being
// processed: on 2026-11-18 the PUT right after the upload failed, the same PUT 16 minutes later
// passed. So: up to an hour, one try a minute.
let made = false;
for (let attempt = 1; attempt <= 60 && !made; attempt++) {
  const headers = await auth();
  const got = await (await fetch(`https://www.googleapis.com/youtube/v3/videos?part=status&id=${id}`, { headers })).json();
  const status = { ...got.items[0].status, privacyStatus: "public" };
  const put = await fetch("https://www.googleapis.com/youtube/v3/videos?part=status", {
    method: "PUT", headers: { ...headers, "Content-Type": "application/json" },
    body: JSON.stringify({ id, status }),
  });
  if (put.ok) {
    made = true;
    console.log(`public: HTTP ${put.status} (attempt ${attempt})`);
  } else {
    const text = await put.text();
    console.log(`public attempt ${attempt}: HTTP ${put.status} ${text.slice(0, 200).replace(/\s+/g, ' ')}`);
    if (attempt < 60) await sleep(60_000);
  }
}
if (!made) process.exit(1);

// Thumbnail afterwards, with patience: YouTube refuses it (403) while the video is still processing -
// a 22-minute video takes a few minutes. Giving up is a warning, the video is public already.
if (thumbnail) {
  let set = false;
  for (let attempt = 1; attempt <= 10 && !set; attempt++) {
    const res = await fetch(`https://www.googleapis.com/upload/youtube/v3/thumbnails/set?videoId=${id}`, {
      method: 'POST', headers: { ...(await auth()), 'Content-Type': 'image/png' }, body: readFileSync(thumbnail),
    });
    if (res.ok) {
      set = true;
    } else {
      const text = await res.text();
      console.log(`thumbnail attempt ${attempt}: HTTP ${res.status} ${text.slice(0, 200).replace(/\s+/g, ' ')}`);
      if (attempt < 10) await sleep(60_000);
    }
  }
  console.log(set ? 'thumbnail: set' : 'thumbnail: NOT set, YouTube keeps its own frame');
}
console.log(url);
