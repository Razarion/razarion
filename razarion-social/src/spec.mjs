import { readFileSync, existsSync, statSync } from 'node:fs';
import { resolve, dirname, isAbsolute } from 'node:path';

export const PLATFORMS = ['youtube', 'x', 'tiktok', 'instagram'];

// Caption ceilings as the platforms enforce them. Checked up front rather than at the API call,
// because finding out that the TikTok caption was too long after a 300 MB upload is a bad trade.
const LIMITS = {
  'youtube.title': 100,
  'youtube.description': 5000,
  'x.text': 280,
  'tiktok.title': 2200,
  'instagram.caption': 2200,
};

function fileMustExist(path, label) {
  if (!existsSync(path)) throw new Error(`${label} does not exist: ${path}`);
  if (!statSync(path).isFile()) throw new Error(`${label} is not a file: ${path}`);
}

export function loadSpec(specPath) {
  const abs = resolve(specPath);
  if (!existsSync(abs)) throw new Error(`Post spec not found: ${abs}`);
  const spec = JSON.parse(readFileSync(abs, 'utf8'));
  const base = dirname(abs);

  // Paths in the spec are relative to the spec file, so a spec can be moved together with its
  // clip without every path breaking.
  const resolvePath = (p) => (isAbsolute(p) ? p : resolve(base, p));

  if (!spec.video) throw new Error('Post spec needs a "video" path.');
  spec.video = resolvePath(spec.video);
  fileMustExist(spec.video, 'video');
  spec.videoSize = statSync(spec.video).size;

  if (spec.thumbnail) {
    spec.thumbnail = resolvePath(spec.thumbnail);
    fileMustExist(spec.thumbnail, 'thumbnail');
  }

  const unknown = Object.keys(spec).filter(
    (k) => !PLATFORMS.includes(k) && !['video', 'videoSize', 'thumbnail', 'notes'].includes(k)
  );
  if (unknown.length) throw new Error(`Unknown keys in post spec: ${unknown.join(', ')}`);

  if (spec.youtube && !spec.youtube.title) throw new Error('youtube.title is required.');
  if (spec.x && !spec.x.text) throw new Error('x.text is required.');
  if (spec.instagram && !spec.instagram.videoUrl) {
    throw new Error(
      'instagram.videoUrl is required: Instagram pulls the file from a public URL, it does not accept an upload.'
    );
  }

  for (const [path, max] of Object.entries(LIMITS)) {
    const [platform, field] = path.split('.');
    const value = spec[platform]?.[field];
    if (typeof value === 'string' && value.length > max) {
      throw new Error(`${path} is ${value.length} characters, the limit is ${max}.`);
    }
  }

  return spec;
}

export function selectedPlatforms(spec, only) {
  const present = PLATFORMS.filter((p) => spec[p]);
  if (!only) return present;
  const wanted = only.split(',').map((s) => s.trim().toLowerCase());
  const bad = wanted.filter((w) => !PLATFORMS.includes(w));
  if (bad.length) throw new Error(`Unknown platform(s): ${bad.join(', ')}`);
  const missing = wanted.filter((w) => !present.includes(w));
  if (missing.length) throw new Error(`Post spec has no section for: ${missing.join(', ')}`);
  return wanted;
}
