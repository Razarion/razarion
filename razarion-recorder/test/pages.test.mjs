// node --test test/
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { Script } from 'node:vm';

// The pages carry their script inline; a slip in it shows as nothing working at all, with the cause
// only in the browser's console.
for (const page of ['page.html', 'player.html']) {
  test(`the script of ${page} compiles`, () => {
    const html = readFileSync(new URL(`../dashboard/${page}`, import.meta.url), 'utf8');
    const script = html.slice(html.indexOf('<script>') + '<script>'.length, html.lastIndexOf('</script>'));
    assert.doesNotThrow(() => new Script(script));
  });
}
