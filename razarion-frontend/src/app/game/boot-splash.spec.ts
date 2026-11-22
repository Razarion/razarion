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
    document.getElementById('raz-land')?.remove();
    document.getElementById('cover')?.remove();
  });

  it('removes the splash', () => {
    const splash = document.createElement('div');
    splash.id = 'raz-boot';
    document.body.appendChild(splash);

    removeSplash();

    expect(document.getElementById('raz-boot')).toBeNull();
  });

  it('moves a shown landing picker into the cover, before the progress well', () => {
    const splash = document.createElement('div');
    splash.id = 'raz-boot';
    const picker = document.createElement('div');
    picker.id = 'raz-land';
    splash.appendChild(picker);
    document.body.appendChild(splash);
    const cover = document.createElement('div');
    cover.id = 'cover';
    const well = document.createElement('div');
    cover.appendChild(well);
    document.body.appendChild(cover);

    removeSplash(well);

    expect(document.getElementById('raz-boot')).toBeNull();
    expect(picker.parentElement).toBe(cover);
    expect(picker.nextElementSibling).toBe(well);
  });

  it('lets a hidden landing picker go with the splash', () => {
    // The plain arm, or no cells to offer: the picker was never shown.
    const splash = document.createElement('div');
    splash.id = 'raz-boot';
    const picker = document.createElement('div');
    picker.id = 'raz-land';
    picker.hidden = true;
    splash.appendChild(picker);
    document.body.appendChild(splash);
    const well = document.createElement('div');
    document.body.appendChild(well);

    removeSplash(well);

    expect(document.getElementById('raz-land')).toBeNull();
    well.remove();
  });

  it('survives being asked to remove a splash that is already gone', () => {
    // The backend and the auth pages drop it themselves, in index.html, before Angular starts.
    expect(() => removeSplash()).not.toThrow();
  });
});
