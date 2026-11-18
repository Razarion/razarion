// Once a day: renders the last 24 h of the tape as a 64x time-lapse and puts it public on YouTube.
// Run by the Windows task "Razarion Daily Video" (scheduled/install-daily-video.ps1), or by hand:
//
//   node scripts/daily-video.mjs            # render + upload, unless today's is already up
//   node scripts/daily-video.mjs --dry      # render + thumbnail only, no upload
//
// Skips when the tape holds less than a day (the recorder restarted: the tape lives only in its
// memory) or when a video for this day is already in state/daily-video.json. Writes its log to
// state/daily-video.log. The upload goes through razarion2/razarion-social (its .env and tokens),
// see upload-youtube.mjs.
import { spawn } from 'node:child_process';
import { appendFileSync, existsSync, mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const here = path.dirname(fileURLToPath(import.meta.url));
const recorder = path.resolve(here, '..');
const stateDir = path.join(recorder, 'state');
const videoDir = path.join(stateDir, 'videos');
const ledgerFile = path.join(stateDir, 'daily-video.json');
const logFile = path.join(stateDir, 'daily-video.log');
const social = path.resolve(recorder, '../../razarion2/razarion-social');
const ffmpeg = path.join(social, 'pipeline/node_modules/ffmpeg-static/ffmpeg.exe');
const BASE = 'http://127.0.0.1:4712';
const MINUTES = 1440, SPEED = 64, MIN_TAPE_HOURS = 23.5, MAX_WAIT_HOURS = 6;
const dry = process.argv.includes('--dry');

mkdirSync(videoDir, { recursive: true });
const log = (msg) => {
  const line = `${new Date().toISOString()} ${msg}`;
  console.log(line);
  appendFileSync(logFile, line + '\n');
};
const run = (cmd, args, cwd) => new Promise((resolve, reject) => {
  let out = '';
  const child = spawn(cmd, args, { cwd, windowsHide: true });
  child.stdout.on('data', (d) => { out += d; });
  child.stderr.on('data', (d) => { out += d; });
  child.on('close', (code) => (code === 0 ? resolve(out) : reject(new Error(`${path.basename(cmd)} ${args[0] ?? ''} exit ${code}: ${out.slice(-600)}`))));
});

const zurich = (opts) => new Intl.DateTimeFormat('en-GB', { timeZone: 'Europe/Zurich', ...opts });
const dayKey = (t) => new Intl.DateTimeFormat('sv-SE', { timeZone: 'Europe/Zurich' }).format(t); // 2026-11-16

// "15–16 Nov 2026", "30 Nov – 1 Dec 2026", "31 Dec 2026 – 1 Jan 2027"
function dateRange(from, to) {
  const part = (t) => Object.fromEntries(zurich({ day: 'numeric', month: 'short', year: 'numeric' }).formatToParts(t).map((p) => [p.type, p.value]));
  const a = part(from), b = part(to);
  if (a.year !== b.year) return `${a.day} ${a.month} ${a.year} – ${b.day} ${b.month} ${b.year}`;
  if (a.month !== b.month) return `${a.day} ${a.month} – ${b.day} ${b.month} ${b.year}`;
  if (a.day !== b.day) return `${a.day}–${b.day} ${b.month} ${b.year}`;
  return `${b.day} ${b.month} ${b.year}`;
}

try {
  // The tape is in memory only: after a restart of the recorder (or the PC) it starts empty. Up to
  // MAX_WAIT_HOURS short of a full day, wait for it rather than miss the day - the video comes later.
  let hours = 0;
  for (;;) {
    const status = await (await fetch(`${BASE}/api/status`)).json();
    const oldest = status.tape?.held?.oldest;
    hours = oldest ? (Date.now() - oldest) / 3600000 : 0;
    if (hours >= MIN_TAPE_HOURS) break;
    if (MIN_TAPE_HOURS - hours > MAX_WAIT_HOURS) {
      log(`skip: tape holds only ${hours.toFixed(1)} h (recorder restarted?)`);
      process.exit(0);
    }
    const waitMs = (MIN_TAPE_HOURS - hours) * 3600000 + 60000;
    log(`wait: tape holds ${hours.toFixed(1)} h, rendering in ${Math.round(waitMs / 60000)} min`);
    await new Promise((r) => setTimeout(r, waitMs));
  }
  const now = Date.now();

  const today = dayKey(now);
  const ledger = existsSync(ledgerFile) ? JSON.parse(readFileSync(ledgerFile, 'utf8')) : [];
  if (!dry && ledger.some((v) => v.day === today)) {
    log(`skip: ${today} is already up (${ledger.find((v) => v.day === today).url})`);
    process.exit(0);
  }

  const from = now - MINUTES * 60000;
  const minutes = Math.floor(MINUTES / SPEED);
  const title = `Razarion – 24 hours on the live server in ${minutes} minutes (${SPEED}×) · ${dateRange(from, now)}`;
  const mp4 = path.join(videoDir, `razarion-24h-${today}.mp4`);
  const png = path.join(videoDir, `razarion-24h-${today}.png`);

  log(`render: ${title}`);
  const started = Date.now();
  await run(process.execPath, [path.join(here, 'render-video.mjs'), '--minutes', String(MINUTES), '--speed', String(SPEED), '--out', mp4], recorder);
  log(`rendered in ${Math.round((Date.now() - started) / 60000)} min`);

  // Thumbnail from the middle of the video.
  const mid = Math.round(MINUTES * 60 / SPEED / 2);
  await run(ffmpeg, ['-hide_banner', '-loglevel', 'error', '-y', '-ss', String(mid), '-i', mp4, '-frames:v', '1', '-vf', 'scale=1280:720', png], recorder);

  if (dry) {
    log(`dry run: ${mp4}`);
    process.exit(0);
  }

  const out = await run(process.execPath, [path.join(here, 'upload-youtube.mjs'), mp4, png, title], social);
  const url = out.match(/https:\/\/www\.youtube\.com\/watch\?v=[\w-]+/g)?.pop();
  if (!url || !/public: HTTP 200/.test(out)) throw new Error(`upload unclear: ${out.slice(-600)}`);

  // Anonymous check: oEmbed answers only for a public (or unlisted) video.
  const oembed = await fetch(`https://www.youtube.com/oembed?url=${encodeURIComponent(url)}&format=json`);
  ledger.push({ day: today, url, title, uploadedAt: new Date().toISOString(), oembed: oembed.status });
  writeFileSync(ledgerFile, JSON.stringify(ledger, null, 2));
  log(`uploaded: ${url} (oEmbed ${oembed.status})`);

  rmSync(mp4, { force: true }); // ~40 MB a day; YouTube keeps it
} catch (error) {
  log(`FAILED: ${error.message}`);
  process.exit(1);
}
