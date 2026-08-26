#!/usr/bin/env node
import { loadSpec, selectedPlatforms, PLATFORMS } from './spec.mjs';
import { env, hasEnv, readTokens, ENV_FILE_PATH } from './config.mjs';
import { authoriseGoogle } from './auth/google.mjs';
import { authoriseX } from './auth/x.mjs';
import { authoriseTikTok } from './auth/tiktok.mjs';
import { postToYouTube } from './platforms/youtube.mjs';
import { postToX, estimateCost } from './platforms/x.mjs';
import { postToTikTok } from './platforms/tiktok.mjs';
import { postToInstagram } from './platforms/instagram.mjs';
import { info, step, ok, warn, fail } from './util/log.mjs';

const HANDLERS = {
  youtube: postToYouTube,
  x: postToX,
  tiktok: postToTikTok,
  instagram: postToInstagram,
};

const USAGE = `
razarion-social - one clip, four platforms

  node src/cli.mjs check
      Show which platforms are configured and authorised.

  node src/cli.mjs auth <google|x|tiktok> [--manual]
      Run the OAuth flow once per platform and store the refresh token.
      --manual  paste the redirect URL by hand (needed for TikTok, which
                refuses http://localhost redirect URIs).

  node src/cli.mjs post <spec.json> [--live] [--only youtube,x]
      Without --live this is a dry run: it validates everything and prints
      what would be sent, but posts nothing. Posting is not undoable and X
      charges per post, so the safe path is the default one.

Platforms: ${PLATFORMS.join(', ')}
`;

function parseArgs(argv) {
  const positional = [];
  const flags = {};
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a.startsWith('--')) {
      const [key, inline] = a.slice(2).split('=');
      flags[key] = inline ?? (argv[i + 1] && !argv[i + 1].startsWith('--') ? argv[++i] : true);
    } else {
      positional.push(a);
    }
  }
  return { positional, flags };
}

function check() {
  const tokens = readTokens();
  const rows = [
    ['youtube', hasEnv('GOOGLE_CLIENT_ID', 'GOOGLE_CLIENT_SECRET'), !!tokens.google, 'uploads as private; you publish in Studio'],
    ['x', hasEnv('X_CLIENT_ID', 'X_CLIENT_SECRET'), !!tokens.x, 'fully automatic, charged per post'],
    ['tiktok', hasEnv('TIKTOK_CLIENT_KEY', 'TIKTOK_CLIENT_SECRET'), !!tokens.tiktok, 'uploads to drafts; you publish in the app'],
    ['instagram', hasEnv('INSTAGRAM_USER_ID', 'INSTAGRAM_ACCESS_TOKEN'), hasEnv('INSTAGRAM_ACCESS_TOKEN'), 'needs Meta app review + a public video URL'],
  ];

  info(`\nConfig file: ${ENV_FILE_PATH}\n`);
  for (const [name, configured, authorised, note] of rows) {
    const state = !configured ? 'no credentials' : !authorised ? 'not authorised' : 'ready';
    info(`  ${name.padEnd(10)} ${state.padEnd(15)} ${note}`);
  }
  info('');
}

async function post(specPath, flags) {
  if (!specPath) throw new Error('Which post spec? Usage: post <spec.json>');
  const spec = loadSpec(specPath);
  const platforms = selectedPlatforms(spec, flags.only);
  if (!platforms.length) throw new Error('The post spec contains no platform sections.');

  const dryRun = !flags.live;
  const sizeMb = (spec.videoSize / 1024 / 1024).toFixed(1);
  info(`\n${dryRun ? 'DRY RUN' : 'POSTING'}  ${spec.video}  (${sizeMb} MB)`);
  info(`Platforms: ${platforms.join(', ')}\n`);

  if (spec.x && platforms.includes('x')) {
    const cost = estimateCost(spec.x.text) * (1 + (spec.x.thread?.length || 0));
    info(`Estimated X cost for this run: $${cost.toFixed(3)}\n`);
  }

  const results = [];
  for (const platform of platforms) {
    try {
      results.push(await HANDLERS[platform](spec, { dryRun }));
    } catch (err) {
      // One platform failing must not strand the others - a half-published clip is recoverable,
      // an aborted run that already posted to X is not.
      fail(`${platform}: ${err.message}`);
      results.push({ platform, error: err.message });
    }
  }

  info('\nSummary');
  for (const r of results) {
    if (r.error) fail(`${r.platform}: failed`);
    else if (r.dryRun) step(`${r.platform}: dry run only`);
    else ok(`${r.platform}: ${r.url || 'done'}`);
  }
  if (dryRun) warn('\nNothing was posted. Add --live to actually publish.');

  if (results.some((r) => r.error)) process.exitCode = 1;
}

async function main() {
  const { positional, flags } = parseArgs(process.argv.slice(2));
  const [command, arg] = positional;

  switch (command) {
    case 'check':
      return check();
    case 'auth': {
      if (arg === 'google' || arg === 'youtube') return authoriseGoogle();
      if (arg === 'x') return authoriseX();
      if (arg === 'tiktok') return authoriseTikTok({ manual: !!flags.manual });
      if (arg === 'instagram') {
        throw new Error(
          'Instagram uses a long-lived token from the Meta app dashboard, not this flow.\n' +
            'Put it in .env as INSTAGRAM_ACCESS_TOKEN - see README.md.'
        );
      }
      throw new Error('Usage: auth <google|x|tiktok>');
    }
    case 'post':
      return post(arg, flags);
    default:
      info(USAGE);
      if (command) process.exitCode = 1;
  }
}

main().catch((err) => {
  fail(err.message);
  if (env.DEBUG) console.error(err);
  process.exit(1);
});
