import {prefetchWorker, readWorkerBuild, workerFileUrls} from './worker-prefetch';

const BOOTSTRAP_SOURCE = "(function() {\n    'use strict';\n    var BUILD = '20260928175221';\n    importScripts('classes.wasm-runtime.js?v=' + BUILD);\n})();";

function fakeWindow(bootstrapSource: string) {
  const fetched: string[] = [];
  const read: string[] = [];
  const win = {
    fetch: (url: string) => {
      fetched.push(url.replace(/\?t=\d+$/, '?t=*'));
      if (url.indexOf('worker-bootstrap.js') >= 0) {
        return Promise.resolve({ok: true, text: () => Promise.resolve(bootstrapSource)});
      }
      return Promise.resolve({ok: true, arrayBuffer: () => { read.push(url); return Promise.resolve(new ArrayBuffer(0)); }});
    }
  };
  return {win, fetched, read};
}

async function settle(): Promise<void> {
  for (let i = 0; i < 6; i++) {
    await Promise.resolve();
  }
}

describe('prefetchWorker', () => {
  it('reads the build stamp the worker will use', () => {
    expect(readWorkerBuild(BOOTSTRAP_SOURCE)).toBe('20260928175221');
  });

  it('takes an unsubstituted stamp for none - its urls would never be asked for', () => {
    expect(readWorkerBuild("var BUILD = '${razarion.build}';")).toBeNull();
    expect(readWorkerBuild('no stamp here')).toBeNull();
  });

  it('names the files exactly as worker-bootstrap.js does, so the worker finds them in the cache', () => {
    expect(workerFileUrls('42')).toEqual([
      '/teavm-worker/classes.wasm-runtime.js?v=42',
      '/teavm-worker/razarion-worker.wasm?v=42'
    ]);
  });

  it('fetches and reads the runtime and the module of the running build', async () => {
    const {win, fetched, read} = fakeWindow(BOOTSTRAP_SOURCE);
    prefetchWorker(win);
    await settle();
    expect(fetched).toEqual([
      '/teavm-worker/worker-bootstrap.js?t=*',
      '/teavm-worker/classes.wasm-runtime.js?v=20260928175221',
      '/teavm-worker/razarion-worker.wasm?v=20260928175221'
    ]);
    expect(read.length).toBe(2);
  });

  it('fetches nothing more when the stamp cannot be read', async () => {
    const {win, fetched} = fakeWindow('garbage');
    prefetchWorker(win);
    await settle();
    expect(fetched).toEqual(['/teavm-worker/worker-bootstrap.js?t=*']);
  });
});
