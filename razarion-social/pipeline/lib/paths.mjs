import { mkdirSync, readFileSync, writeFileSync, existsSync, renameSync } from 'node:fs';
import { dirname, join, relative, sep } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));

export const PIPELINE_ROOT = join(here, '..');
export const DATA_DIR = join(PIPELINE_ROOT, 'data');
export const MEDIA_DIR = join(DATA_DIR, 'media');
export const CARDS_DIR = join(DATA_DIR, 'cards');
export const STATE_DIR = join(PIPELINE_ROOT, 'state');

export const RAW_TIMELINE_FILE = join(DATA_DIR, 'raw-timeline.json');
export const POSTS_FILE = join(DATA_DIR, 'posts.json');
export const CAPTIONS_FILE = join(DATA_DIR, 'captions.json');
export const UPLOADS_FILE = join(DATA_DIR, 'uploads.json');
export const POSTED_FILE = join(STATE_DIR, 'posted.json');
export const SYNC_FILE = join(STATE_DIR, 'sync.json');
export const INSTAGRAM_TOKEN_FILE = join(STATE_DIR, 'instagram-token.json');
export const FACEBOOK_TOKEN_FILE = join(STATE_DIR, 'facebook-token.json');
export const FB_POSTS_FILE = join(DATA_DIR, 'fb_posts.json');
export const POSTED_FB_FILE = join(STATE_DIR, 'posted_fb.json');

export function ensureDir(dir) {
  mkdirSync(dir, { recursive: true });
}

export function readJson(file, fallback = null) {
  if (!existsSync(file)) return fallback;
  return JSON.parse(readFileSync(file, 'utf8'));
}

// Write to a sibling temp file and rename. captions.json carries hand-made edits, and a run
// interrupted mid-write would otherwise leave a truncated file where those edits used to be.
// rename() replaces atomically on both NTFS and ext4.
export function writeJson(file, value) {
  ensureDir(dirname(file));
  const tmp = file + '.tmp';
  writeFileSync(tmp, JSON.stringify(value, null, 2) + '\n', 'utf8');
  renameSync(tmp, file);
}

// Paths inside the JSON files are stored relative to razarion-social/pipeline, so the data directory
// stays movable and the files diff cleanly between machines.
export function toRelative(absolute) {
  return relative(PIPELINE_ROOT, absolute).split(sep).join('/');
}
