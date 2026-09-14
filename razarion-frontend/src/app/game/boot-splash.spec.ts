import {removeSplash} from './boot-splash';

/**
 * Taking the page's loading screen away.
 *
 * Small, and it stays tested: a splash that is never removed leaves the player looking at a
 * loading screen with the running game behind it, and nothing else in the application removes it.
 *
 * Five of the cases here used to cover the animated arm - progress reports, the terrain
 * announcement, the page's own fade - and went with it when the experiment ended.
 */
describe('Boot splash bridge', () => {
  afterEach(() => {
    document.getElementById('raz-boot')?.remove();
  });

  it('removes the splash', () => {
    const splash = document.createElement('div');
    splash.id = 'raz-boot';
    document.body.appendChild(splash);

    removeSplash();

    expect(document.getElementById('raz-boot')).toBeNull();
  });

  it('survives being asked to remove a splash that is already gone', () => {
    // The backend and the auth pages drop it themselves, in index.html, before Angular starts.
    expect(() => removeSplash()).not.toThrow();
  });
});
