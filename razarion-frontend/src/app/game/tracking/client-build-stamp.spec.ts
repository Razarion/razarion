import {clientBuildStamp, reportClientBuild} from './client-build-stamp';

/**
 * A phone in the Meta in-app browser ran the previous bundle for minutes after a deploy, and only
 * picked it up once the cache was cleared by hand - although /game/index.html goes out with
 * `Cache-Control: no-cache, must-revalidate`. Nobody clears a cache in an ad funnel, so without
 * this we cannot say which build a session was measuring.
 */
describe('Client build stamp', () => {
  let page: HTMLElement;

  beforeEach(() => {
    // Karma's own page carries a main.js, so the real document cannot answer these questions.
    page = document.createElement('div');
    document.body.appendChild(page);
  });

  afterEach(() => page.remove());

  function addScript(src: string): void {
    const script = document.createElement('script');
    // Not .src: assigning it would make the browser fetch a script that does not exist.
    script.setAttribute('src', src);
    script.type = 'text/plain';
    page.appendChild(script);
  }

  it('names the hashed production bundle', () => {
    addScript('/game/main-SMERQXAU.js');

    expect(clientBuildStamp(page)).toBe('main-SMERQXAU.js');
  });

  it('names the unhashed dev bundle too', () => {
    addScript('/main.js');

    // The dev server serves one name forever, which is itself the answer: this is not a build we
    // deployed.
    expect(clientBuildStamp(page)).toBe('main.js');
  });

  it('is not fooled by another bundle that merely starts the same way', () => {
    addScript('/game/mainframe-ABC.js');
    addScript('/game/main-REAL123.js');

    expect(clientBuildStamp(page)).toBe('main-REAL123.js');
  });

  it('says so rather than inventing one when no bundle is found', () => {
    expect(clientBuildStamp(page)).toBe('unknown');
  });

  it('reports it once, as a build and not as a player action', () => {
    addScript('/game/main-XYZ789.js');
    const reported: { kind: string, detail?: string }[] = [];
    const tracker: any = {report: (kind: string, detail?: string) => reported.push({kind, detail})};

    reportClientBuild(tracker, page);

    expect(reported).toEqual([{kind: 'CLIENT_BUILD', detail: 'build=main-XYZ789.js'}]);
  });

  it('never lets telemetry stop a game from starting', () => {
    const throwing: any = {
      report: () => {
        throw new Error('tracker down');
      }
    };

    expect(() => reportClientBuild(throwing, page)).not.toThrow();
  });
});
