// Renders a stretch of the tape as an MP4 time-lapse: the 2D player, frame by frame, in a headless
// Chrome, piped into ffmpeg. Frame by frame rather than screen-recorded, so a slow machine makes a
// slow render instead of a jerky video.
//
//   node scripts/render-video.mjs --minutes 1440 --speed 64 --out day.mp4
//
// The view is the noob part of planet 117 (south-west corner), the stretch the dashboard shows when
// zoomed onto the island, the river band and the first regions. --box x0,y0,x1,y1 (metres) for
// another. ffmpeg comes from razarion-social's ffmpeg-static (--ffmpeg to point elsewhere).
import { chromium } from 'playwright-core';
import { spawn } from 'node:child_process';
import { existsSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const here = path.dirname(fileURLToPath(import.meta.url));
const args = Object.fromEntries(process.argv.slice(2).reduce((acc, a, i, all) => {
  if (a.startsWith('--')) acc.push([a.slice(2), all[i + 1]?.startsWith('--') || all[i + 1] === undefined ? 'true' : all[i + 1]]);
  return acc;
}, []));

const URL_BASE = args.url || 'http://127.0.0.1:4712';
const MINUTES = Number(args.minutes || 1440);
const SPEED = Number(args.speed || 64);
const FPS = Number(args.fps || 30);
const OUT = path.resolve(args.out || `replay-${MINUTES}min-${SPEED}x.mp4`);
const [BX0, BY0, BX1, BY1] = (args.box || '0,0,3100,2330').split(',').map(Number);
const W = 1920, H = 1080, PANEL = 480;
const FFMPEG = args.ffmpeg || path.resolve(here, '../../../razarion2/razarion-social/pipeline/node_modules/ffmpeg-static/ffmpeg.exe');
if (!existsSync(FFMPEG)) throw new Error(`ffmpeg not found: ${FFMPEG}`);

const browser = await chromium.launch({ channel: 'chrome', headless: true });
const page = await browser.newPage({ viewport: { width: W, height: H }, deviceScaleFactor: 1, colorScheme: 'dark' });
await page.goto(`${URL_BASE}/player`);
await page.addStyleTag({ content: `
  header, footer, aside, #status, #tip { display: none !important; }
  body { display: block !important; margin: 0; overflow: hidden; background: #0f1316; }
  #mapWrap { position: fixed !important; left: 0; top: 0; width: ${W - PANEL}px; height: ${H}px; }
  #map { width: 100%; height: 100%; display: block; }
  #vp { position: fixed; left: ${W - PANEL}px; top: 0; width: ${PANEL}px; height: ${H}px; box-sizing: border-box;
        padding: 56px 40px; background: #161615; color: #e9e9e4; font: 22px/1.45 system-ui, sans-serif; border-left: 1px solid #33332f; }
  #vp h1 { font-size: 38px; margin: 0 0 6px; } #vp .sub { color: #9a9a93; margin-bottom: 48px; }
  #vp .day { font-size: 28px; color: #9a9a93; } #vp .time { font-size: 76px; font-weight: 600; font-variant-numeric: tabular-nums; margin-bottom: 40px; }
  #vp .bar { height: 8px; background: #33332f; border-radius: 4px; margin-bottom: 48px; } #vp .bar i { display: block; height: 100%; background: #8fb5ea; border-radius: 4px; }
  #vp .row { display: flex; align-items: center; gap: 14px; margin: 8px 0; } #vp .sw { width: 18px; height: 18px; border-radius: 50%; border: 1px solid rgba(0,0,0,.6); }
  #vp .foot { position: absolute; bottom: 48px; left: 40px; right: 40px; color: #9a9a93; font-size: 20px; }
` });

const meta = await page.evaluate(async ([minutes, box, speed]) => {
  await load(`minutes=${minutes}`);
  setPlaying(false);
  await new Promise((resolve) => { const wait = () => (image ? resolve() : setTimeout(wait, 100)); wait(); });
  resize();
  const [x0, y0, x1, y1] = box;
  view = { cx: (x0 + x1) / 2, cy: (y0 + y1) / 2, scale: Math.min(canvas.width / (x1 - x0), canvas.height / (y1 - y0)) };
  const panel = document.createElement('div');
  panel.id = 'vp';
  panel.innerHTML = `<h1>Razarion</h1><div class="sub">The live server, ${speed}× faster</div>
    <div class="day" id="vpDay"></div><div class="time" id="vpTime"></div><div class="bar"><i id="vpBar"></i></div>
    <div class="row"><span class="sw" style="background:${BOT_COLOR}"></span>Bots</div>
    <div class="row"><span class="sw" style="background:${HUMAN_COLORS[0]}"></span><span class="sw" style="background:${HUMAN_COLORS[1]}"></span><span class="sw" style="background:${HUMAN_COLORS[2]}"></span>Players</div>
    <div class="row"><span style="display:inline-block;width:18px;height:3px;background:rgb(255,230,120)"></span>Shots</div>
    <div class="foot">Circles move, squares are buildings.<br>Free to play at razarion.com</div>`;
  document.body.appendChild(panel);
  window.__fmtDay = new Intl.DateTimeFormat('en-GB', { weekday: 'long', day: 'numeric', month: 'long', timeZone: 'Europe/Zurich' });
  window.__fmtTime = new Intl.DateTimeFormat('en-GB', { hour: '2-digit', minute: '2-digit', timeZone: 'Europe/Zurich', hour12: false });
  return { from: track.from, to: track.to, items: track.items.length };
}, [MINUTES, [BX0, BY0, BX1, BY1], SPEED]);

const step = SPEED * 1000 / FPS;
const frames = Math.floor((meta.to - meta.from) / step) + 1;
console.log(`${meta.items} items, ${Math.round((meta.to - meta.from) / 60000)} min tape -> ${frames} frames, ${(frames / FPS / 60).toFixed(1)} min video -> ${OUT}`);

const ff = spawn(FFMPEG, ['-y', '-loglevel', 'error', '-f', 'image2pipe', '-framerate', String(FPS), '-i', '-',
  '-c:v', 'libx264', '-preset', 'medium', '-crf', '20', '-pix_fmt', 'yuv420p', '-movflags', '+faststart', OUT],
{ stdio: ['pipe', 'inherit', 'inherit'] });
const ffDone = new Promise((resolve, reject) => ff.on('close', (code) => (code === 0 ? resolve() : reject(new Error(`ffmpeg exit ${code}`)))));

const started = Date.now();
for (let i = 0; i < frames; i++) {
  const t = Math.min(meta.to, meta.from + i * step);
  await page.evaluate(([t, from, to]) => {
    seek(t);
    draw();
    const d = new Date(t);
    document.getElementById('vpDay').textContent = __fmtDay.format(d);
    document.getElementById('vpTime').textContent = __fmtTime.format(d);
    document.getElementById('vpBar').style.width = `${(100 * (t - from) / (to - from)).toFixed(2)}%`;
  }, [t, meta.from, meta.to]);
  const jpg = await page.screenshot({ type: 'jpeg', quality: 92 });
  if (!ff.stdin.write(jpg)) await new Promise((resolve) => ff.stdin.once('drain', resolve));
  if (i % 500 === 0) {
    const rate = (i + 1) / ((Date.now() - started) / 1000);
    console.log(`frame ${i}/${frames}  ${rate.toFixed(1)} fps  ~${Math.round((frames - i) / rate / 60)} min left`);
  }
}
ff.stdin.end();
await ffDone;
await browser.close();
console.log(`done in ${Math.round((Date.now() - started) / 60000)} min: ${OUT}`);
