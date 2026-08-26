#!/usr/bin/env node
// Exchanges the current Instagram token for a fresh 60-day one and stores it in state/.
//
// Run it every few weeks, or let the cron job that runs sync_new.mjs call it first. The token in
// .env stays as it is - the refreshed one lives next to the other state and takes precedence
// while it is valid.
//
//   node refresh_token.mjs          # refresh if it expires within 14 days
//   node refresh_token.mjs --force  # refresh regardless

import { parseArgs } from './lib/args.mjs';
import { INSTAGRAM_TOKEN_FILE, STATE_DIR, ensureDir, readJson, writeJson, toRelative } from './lib/paths.mjs';
import { credentials, refreshLongLivedToken } from './lib/instagram.mjs';
import { info, ok, warn, fail } from '../src/util/log.mjs';

const REFRESH_WHEN_DAYS_LEFT = 14;
const DAY = 24 * 60 * 60 * 1000;

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const [, token] = credentials();

  const stored = readJson(INSTAGRAM_TOKEN_FILE);
  if (stored && !args.force) {
    const daysLeft = Math.floor((Date.parse(stored.expires_at) - Date.now()) / DAY);
    info(`Stored token expires in ${daysLeft} day(s).`);
    if (daysLeft > REFRESH_WHEN_DAYS_LEFT) {
      ok('Nothing to do. Use --force to refresh anyway.');
      return;
    }
  }

  const result = await refreshLongLivedToken(token);
  const expiresAt = new Date(Date.now() + (result.expires_in || 60 * 24 * 3600) * 1000).toISOString();

  ensureDir(STATE_DIR);
  writeJson(INSTAGRAM_TOKEN_FILE, {
    access_token: result.access_token,
    refreshed_at: new Date().toISOString(),
    expires_at: expiresAt,
  });

  ok(`Token refreshed. Valid until ${expiresAt.slice(0, 10)}.`);
  info(`Stored in ${toRelative(INSTAGRAM_TOKEN_FILE)}; .env was not touched.`);
  warn('A token can only be refreshed while it is still valid. Let this lapse and the only way');
  warn('back is generating a new one in the Meta dashboard by hand.');
}

main().catch((err) => {
  fail(err.message);
  process.exit(1);
});
