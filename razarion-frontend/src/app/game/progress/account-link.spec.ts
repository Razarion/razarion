import {adoptAccountTokenFromLink} from './account-link';

describe('adoptAccountTokenFromLink', () => {
  function fakeWindow(hash: string) {
    const stored: { [key: string]: string } = {};
    const replaced: string[] = [];
    return {
      stored, replaced,
      win: {
        location: {hash, pathname: '/game', search: '?progress=account'},
        localStorage: {setItem: (k: string, v: string) => stored[k] = v},
        history: {state: null, replaceState: (_s: any, _t: string, url: string) => replaced.push(url)}
      }
    };
  }

  it('stores the token of the link and takes it off the address bar', () => {
    const f = fakeWindow('#account=abc.def.ghi');
    adoptAccountTokenFromLink(f.win);
    expect(f.stored['app.token']).toBe('abc.def.ghi');
    expect(f.replaced).toEqual(['/game?progress=account']);
  });

  it('leaves any other page alone', () => {
    const f = fakeWindow('#something');
    adoptAccountTokenFromLink(f.win);
    expect(f.stored['app.token']).toBeUndefined();
    expect(f.replaced).toEqual([]);
  });
});
