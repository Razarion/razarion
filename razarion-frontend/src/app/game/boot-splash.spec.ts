import {isAnimatedSplash, removeSplash, reportBootProgress, reportTerrainVisible} from './boot-splash';

/**
 * Talking to a loading screen that may not be listening.
 *
 * The functions live in index.html and exist only for the half of the players who get the animated
 * build-up. For the other half every call here has to do nothing at all - they are the control
 * group, and a control group that behaves differently measures nothing. The other direction
 * matters just as much: a splash that is never told to go away leaves the player on a loading
 * screen forever, which is worse than the abrupt handover this replaced.
 */
describe('Boot splash bridge', () => {
  const raz = window as any;
  const names = ['RAZ_bootProgress', 'RAZ_bootTerrainReady', 'RAZ_bootFadeOut'];
  let saved: { [name: string]: unknown };

  beforeEach(() => {
    saved = {};
    names.forEach(name => {
      saved[name] = raz[name];
      delete raz[name];
    });
  });

  afterEach(() => {
    names.forEach(name => {
      if (saved[name] === undefined) {
        delete raz[name];
      } else {
        raz[name] = saved[name];
      }
    });
    document.getElementById('raz-boot')?.remove();
  });

  it('says nothing is listening when the page drew no animation', () => {
    expect(isAnimatedSplash()).toBeFalse();
  });

  it('stays silent rather than throwing at a page that offers nothing', () => {
    // The control arm runs this code too. It must be a no-op, not an error in the console.
    expect(() => reportBootProgress(0.5)).not.toThrow();
    expect(() => reportTerrainVisible()).not.toThrow();
  });

  it('reports progress once the page is drawing', () => {
    const seen: number[] = [];
    raz.RAZ_bootProgress = (fraction: number) => seen.push(fraction);

    expect(isAnimatedSplash()).toBeTrue();
    reportBootProgress(0.42);

    expect(seen).toEqual([0.42]);
  });

  it('keeps the fraction inside its range', () => {
    // percent / 100 is the caller, and a boot task that overshoots must not push the build past
    // the end of the plane.
    const seen: number[] = [];
    raz.RAZ_bootProgress = (fraction: number) => seen.push(fraction);

    reportBootProgress(-3);
    reportBootProgress(1.4);

    expect(seen).toEqual([0, 1]);
  });

  it('announces the terrain', () => {
    let announced = 0;
    raz.RAZ_bootTerrainReady = () => announced++;

    reportTerrainVisible();

    expect(announced).toBe(1);
  });

  it('removes the splash itself where the page offers no fade', () => {
    // Every page that never ran the animation: the backend, an older cached index.
    const splash = document.createElement('div');
    splash.id = 'raz-boot';
    document.body.appendChild(splash);

    removeSplash();

    expect(document.getElementById('raz-boot')).toBeNull();
  });

  it('lets the page fade it out where it does', () => {
    let faded = 0;
    raz.RAZ_bootFadeOut = () => faded++;
    const splash = document.createElement('div');
    splash.id = 'raz-boot';
    document.body.appendChild(splash);

    removeSplash();

    expect(faded).toBe(1);
    // Left to the page, which takes it away after its own transition.
    expect(document.getElementById('raz-boot')).not.toBeNull();
  });

  it('survives being asked to remove a splash that is already gone', () => {
    expect(() => removeSplash()).not.toThrow();
  });
});
