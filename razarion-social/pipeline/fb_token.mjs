#!/usr/bin/env node
// Turns the short-lived user token you copy out of the Graph API Explorer into a Page access token
// that does not expire, and stores it in state/facebook-token.json.
//
// Facebook needs three hops where Instagram needed one:
//   1. a short-lived user token          <- the only part you do by hand
//   2. exchanged for a long-lived one    <- here
//   3. exchanged for a Page token        <- here
//
// A Page token derived from a long-lived user token has no expiry, so this is a one-time errand.
//
//   node fb_token.mjs                 # uses FB_USER_TOKEN from .env
//   node fb_token.mjs --page 123456   # when the account administers several Pages

import { parseArgs } from './lib/args.mjs';
import { FACEBOOK_TOKEN_FILE, STATE_DIR, ensureDir, writeJson, toRelative } from './lib/paths.mjs';
import { get } from './lib/facebook.mjs';
import { env, requireEnv } from '../src/config.mjs';
import { info, step, ok, warn, fail } from '../src/util/log.mjs';

const NEEDED_SCOPES = ['pages_show_list', 'pages_manage_posts', 'pages_read_engagement'];

// A Meta app secret is 32 hex characters. Anything else in that variable is some other value that
// happened to be nearby in the dashboard, and sending it produces "Error validating client secret"
// two steps later instead of here.
const looksLikeAppSecret = (value) => /^[0-9a-f]{32}$/.test(value || '');

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const [userToken] = requireEnv('FB_USER_TOKEN');

  /**
   * The exchange is the convenient path, not the only one.
   *
   * It needs the app secret, which is awkward to find and worse to keep lying around. The Access
   * Token Debugger extends a token just as well with a button, so a token that is already
   * long-lived is accepted as it is - the goal is a Page token that does not expire, and where the
   * longevity came from does not matter.
   */
  let effectiveToken = userToken;
  if (looksLikeAppSecret(env.FB_APP_SECRET) && env.FB_APP_ID) {
    step('exchanging the short-lived token for a long-lived one');
    const longLived = await get('/oauth/access_token', {
      grant_type: 'fb_exchange_token',
      client_id: env.FB_APP_ID,
      client_secret: env.FB_APP_SECRET,
      fb_exchange_token: userToken,
    });
    if (!longLived.access_token) throw new Error(`No token came back: ${JSON.stringify(longLived)}`);
    effectiveToken = longLived.access_token;
  } else {
    warn('No usable FB_APP_SECRET - using FB_USER_TOKEN as it is.');
    warn('It has to be long-lived already, or the Page token will expire within the hour.');
  }

  // A token can debug itself, so this works without an app token - and it says outright whether the
  // thing is long-lived, rather than leaving it to be discovered when it stops working.
  const debug = await get('/debug_token', {
    input_token: effectiveToken,
    access_token: effectiveToken,
  });
  const expiresAt = debug.data && debug.data.expires_at;
  if (expiresAt === 0) {
    step('user token: no expiry');
  } else if (expiresAt) {
    const hours = Math.round((expiresAt * 1000 - Date.now()) / 3600000);
    if (hours < 24) {
      warn(`This user token expires in about ${hours} hour(s), so the Page token will too.`);
      warn('Extend it first at developers.facebook.com/tools/debug/accesstoken ("Extend Access Token").');
    } else {
      step(`user token valid for about ${Math.round(hours / 24)} day(s)`);
    }
  }

  const granted = (debug.data && debug.data.scopes) || [];
  const missing = NEEDED_SCOPES.filter((s) => !granted.includes(s));
  if (missing.length) {
    warn(`The token is missing: ${missing.join(', ')}`);
    warn('Generate it again in the Graph API Explorer with those permissions ticked.');
  }

  step('looking up the Pages this account administers');
  const accounts = await get('/me/accounts', {
    fields: 'id,name,access_token,tasks',
    access_token: effectiveToken,
  });
  const pages = (accounts && accounts.data) || [];
  if (!pages.length) {
    throw new Error(
      'No Pages came back. Either the account administers none, or the token is missing ' +
        'pages_show_list, or the Page was not ticked when the token was authorised.'
    );
  }

  const wanted = args.page ? String(args.page) : env.FB_PAGE_ID;
  let page = pages[0];
  if (wanted) {
    page = pages.find((p) => p.id === wanted || p.name === wanted);
    if (!page) {
      info('Pages this account administers:');
      for (const p of pages) info(`  ${p.id}  ${p.name}`);
      throw new Error(`No Page matching "${wanted}".`);
    }
  } else if (pages.length > 1) {
    info('Several Pages found:');
    for (const p of pages) info(`  ${p.id}  ${p.name}`);
    throw new Error('Pick one with --page <id>, or set FB_PAGE_ID in .env.');
  }

  // CREATE_CONTENT is the task that actually gates posting; being listed as an admin is not the
  // same thing, and the difference only shows up at publish time otherwise.
  const tasks = page.tasks || [];
  if (tasks.length && !tasks.includes('CREATE_CONTENT')) {
    warn(`Your role on "${page.name}" does not include CREATE_CONTENT (${tasks.join(', ')}).`);
    warn('Publishing will fail until that changes in the Page settings.');
  }

  // Ask the page token about itself rather than inferring from the user token it came from. A page
  // token derived from a long-lived user token reports expires_at 0, meaning no expiry - but the
  // user token it descends from still carries its own 60-day date, so reading that one would call
  // a permanent token short-lived.
  const pageDebug = await get('/debug_token', {
    input_token: page.access_token,
    access_token: page.access_token,
  });
  const pageExpiresAt = pageDebug.data && pageDebug.data.expires_at;
  const permanent = pageExpiresAt === 0;

  ensureDir(STATE_DIR);
  writeJson(FACEBOOK_TOKEN_FILE, {
    page_id: page.id,
    page_name: page.name,
    access_token: page.access_token,
    tasks,
    permanent,
    obtained_at: new Date().toISOString(),
  });

  ok(`Page token for "${page.name}" (${page.id}) stored in ${toRelative(FACEBOOK_TOKEN_FILE)}`);
  if (permanent) {
    info('  Derived from a long-lived user token, so it does not expire.');
    info('  FB_USER_TOKEN in .env has done its job and can be cleared.');
  } else {
    warn('  SHORT-LIVED: it came from a short-lived user token and dies with it, within the hour.');
    warn('  Extend the user token at developers.facebook.com/tools/debug/accesstoken,');
    warn('  put the extended one in FB_USER_TOKEN, and run this again for a permanent Page token.');
  }
}

main().catch((err) => {
  fail(err.message);
  process.exit(1);
});
