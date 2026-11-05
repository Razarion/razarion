// node --test test/
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createServer } from 'node:http';
import { WebSocketServer } from 'ws';
import { GameStream } from '../lib/connection.mjs';

test('a handshake the server turns away is tried again, and connects once the server is back', async () => {
  let refuse = true;
  const games = new WebSocketServer({ noServer: true });
  const server = createServer();
  server.on('upgrade', (req, socket, head) => {
    if (refuse) {
      refuse = false;
      socket.end('HTTP/1.1 502 Bad Gateway\r\nContent-Length: 0\r\nConnection: close\r\n\r\n');
      return;
    }
    games.handleUpgrade(req, socket, head, (ws) => ws.send('TICK_INFO#{}'));
  });
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  const stream = new GameStream(`http://127.0.0.1:${server.address().port}`, { token: async () => 't', invalidate: () => {} });
  const states = [];
  stream.on('state', (state) => states.push(state));
  const packet = new Promise((resolve) => stream.once('packet', resolve));
  stream.start();
  try {
    assert.equal((await packet).name, 'TICK_INFO');
    assert.ok(states.indexOf('rejected') < states.lastIndexOf('connected'), states.join(' '));
  } finally {
    stream.stop();
    games.close();
    server.close();
  }
});
