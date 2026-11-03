// node --test test/
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createServer } from 'node:http';
import { startReplayProxy } from '../lib/replay-server.mjs';

const listen = (server) => new Promise((resolve) => server.listen(0, '127.0.0.1', () => resolve(server.address().port)));

test('the proxy passes the game through and keeps the watcher out of the game', async () => {
  const seen = [];
  const upstream = createServer((req, res) => {
    seen.push({ method: req.method, url: req.url, auth: req.headers.authorization ?? null });
    res.end('ok');
  });
  const upstreamPort = await listen(upstream);
  const proxyPort = 47130 + Math.floor(Math.random() * 50);
  const proxy = startReplayProxy({
    port: proxyPort,
    target: `http://127.0.0.1:${upstreamPort}`,
    auth: { token: async () => 'recorder-token' },
    getSession: () => ({ status: () => ({ baseId: 5, playhead: 1, connected: false }) }),
    log: () => {},
  });
  await new Promise((resolve) => proxy.once('listening', resolve));
  const call = (path, method = 'GET') => fetch(`http://127.0.0.1:${proxyPort}${path}`, { method });
  try {
    // The page loses its query: the server would count it as a visit
    await call('/game/replay?utm_source=x');
    // A dev server's module request keeps it
    await call('/game/@ng/component?c=app');
    // REST goes with the recorder's token, the context load too
    await call('/rest/terrainHeightMap/1');
    await call('/rest/game-ui-context-control/cold', 'POST');
    // Tracking and every other write never leave
    assert.equal(await (await call('/rest/tracker/startupTask', 'POST')).text(), 'null');
    assert.equal(await (await call('/rest/quest/activateNextPossibleQuest', 'POST')).text(), 'null');
    assert.deepEqual(await (await call('/replay/status')).json(), { baseId: 5, playhead: 1, connected: false });

    assert.deepEqual(seen, [
      { method: 'GET', url: '/game/replay', auth: null },
      { method: 'GET', url: '/game/@ng/component?c=app', auth: null },
      { method: 'GET', url: '/rest/terrainHeightMap/1', auth: 'Bearer recorder-token' },
      { method: 'POST', url: '/rest/game-ui-context-control/cold', auth: 'Bearer recorder-token' },
    ]);
  } finally {
    proxy.close();
    upstream.close();
  }
});
