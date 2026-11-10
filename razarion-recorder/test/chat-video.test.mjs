import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { chatClipArgs, momentToChat } from '../lib/chat-video.mjs';

const dir = mkdtempSync(join(tmpdir(), 'chatvideo-'));
const take = join(dir, 'm-portrait.mp4');
const make = async (_take, out) => writeFileSync(out, Buffer.alloc(1000));

test('a filmed moment goes into the chat as the small portrait clip', async () => {
  const posted = [];
  const moment = { id: 'm', userId: 'u1', takes: { portrait: take, landscape: 'x.mp4' } };
  await momentToChat(moment, { make, log: () => {}, post: async (userId, bytes) => { posted.push([userId, bytes.length]); return 7; } });
  assert.deepEqual(posted, [['u1', 1000]]);
  assert.equal(moment.chat.status, 'posted');
  assert.equal(moment.chat.videoId, 7);
});

test('no player or no take: nothing posted; a refused post is kept as failed', async () => {
  const none = { id: 'm', userId: null, takes: { portrait: take } };
  await momentToChat(none, { make, log: () => {}, post: async () => assert.fail('posted') });
  assert.equal(none.chat.status, 'skipped');
  const refused = { id: 'm', userId: 'u1', takes: { portrait: take } };
  await momentToChat(refused, { make, log: () => {}, post: async () => { throw new Error('HTTP 400'); } });
  assert.deepEqual(refused.chat, { status: 'failed', error: 'HTTP 400' });
});

test('the clip is small and starts before it is loaded', () => {
  const args = chatClipArgs('in.mp4', 'out.mp4');
  assert.ok(args.includes('+faststart'));
  assert.ok(args.includes('-an'));
  assert.equal(args.at(-1), 'out.mp4');
});
