import {prefetchTerrain} from './terrain-prefetch';

function fakeWindow(token: string | null, urls: string[]) {
  const fetched: string[] = [];
  const read: string[] = [];
  const win = {
    localStorage: {getItem: (key: string) => key === 'app.token' ? token : null},
    fetch: (url: string) => {
      fetched.push(url);
      if (url === '/rest/terrainshape/prefetch') {
        return Promise.resolve({ok: true, json: () => Promise.resolve(urls)});
      }
      return Promise.resolve({ok: true, arrayBuffer: () => { read.push(url); return Promise.resolve(new ArrayBuffer(0)); }});
    }
  };
  return {win, fetched, read};
}

async function settle(): Promise<void> {
  for (let i = 0; i < 5; i++) {
    await Promise.resolve();
  }
}

describe('prefetchTerrain', () => {
  const urls = ['/rest/terrainshape/117', '/rest/terrainHeightMap/117/flat', '/rest/terrainHeightMap/117/region/0/0/32/32'];

  it('fetches and reads every url the server names for a visitor without a token', async () => {
    const {win, fetched, read} = fakeWindow(null, urls);
    prefetchTerrain(win);
    await settle();
    expect(fetched).toEqual(['/rest/terrainshape/prefetch', ...urls]);
    expect(read).toEqual(urls);
  });

  it('leaves a logged-in player alone - he may be on another planet', async () => {
    const {win, fetched} = fakeWindow('a.jwt.token', urls);
    prefetchTerrain(win);
    await settle();
    expect(fetched).toEqual([]);
  });

  it('never throws when the hint fails', async () => {
    const win = {localStorage: {getItem: () => null}, fetch: () => Promise.reject(new TypeError('Failed to fetch'))};
    expect(() => prefetchTerrain(win)).not.toThrow();
    await settle();
  });
});
