import {LONGEST_PROMPT_TEXT, PROMPT_CLEARANCE_FRACTION, promptAssemblyWidthPx, promptFitsAt} from './prompt-geometry';

/**
 * Where the quest tips may put a prompt, on the screen it was getting wrong: a phone in portrait,
 * backbuffer 369x683, with about 180 px of HUD along the bottom. See
 * project_quest365_out_of_view_2026_09_22.
 */
describe('promptFitsAt', () => {
  const WIDTH = 369;
  const HEIGHT = 683;
  const HUD = 180;
  const FLOOR = HEIGHT - HUD;
  /** "Tap to attack" without the mouse icon, the phone's case. */
  const ASSEMBLY = promptAssemblyWidthPx('Tap to attack', false);
  const clearance = PROMPT_CLEARANCE_FRACTION * HEIGHT;
  const middle = WIDTH / 2;

  function fit(x: number, y: number, assembly = ASSEMBLY) {
    return promptFitsAt(x, y, WIDTH, HEIGHT, HUD, assembly);
  }

  it('scales the prompt so it never takes more than a third of the picture', () => {
    // The whole reason the phone case was broken: unscaled, the clearance was 290 px of 683.
    expect(clearance).toBeLessThan(HEIGHT / 3);
  });

  it('puts the label above an anchor in the lower half', () => {
    const above = fit(middle, FLOOR - 20);
    expect(above.readable).toBeTrue();
    expect(above.side).toBe('above');
  });

  it('turns the label round for an anchor at the top edge', () => {
    const below = fit(middle, 5);
    expect(below.readable).toBeTrue();
    expect(below.side).toBe('below');
  });

  it('puts the label beside an anchor near the right edge', () => {
    // What the phone showed on 2026-09-22: the extractor sat near the right edge and the green
    // bubble, centred on it, ran off the picture with its text cut in two.
    const beside = fit(WIDTH - 10, FLOOR - 100);
    expect(beside.readable).toBeTrue();
    expect(beside.side).toBe('left');
  });

  it('puts the label beside an anchor near the left edge', () => {
    const beside = fit(10, FLOOR - 100);
    expect(beside.readable).toBeTrue();
    expect(beside.side).toBe('right');
  });

  it('refuses an anchor behind the HUD', () => {
    expect(fit(middle, FLOOR + 1).readable).toBeFalse();
    expect(fit(middle, HEIGHT - 1).readable).toBeFalse();
  });

  it('refuses an anchor off the picture', () => {
    expect(fit(middle, -1).readable).toBeFalse();
    expect(fit(-1, 200).readable).toBeFalse();
    expect(fit(WIDTH + 1, 200).readable).toBeFalse();
  });

  it('leaves no band down the sides where neither centred nor beside fits', () => {
    // The defect this test exists for, measured on a Pixel 7 on 2026-09-22: the assembly width
    // was compared unscaled against the picture, so "centred" only held in a 74 px window in the
    // middle of a 411 px screen while "beside" needed the outer 86 px. Two bands of 40 % of the
    // width together showed the direction arrow at a target in plain sight, and panning a few
    // pixels made the prompt appear.
    for (const [width, height] of [[369, 683], [411, 782], [412, 800], [800, 600], [1920, 1080]]) {
      for (const text of ['Click to select', 'Click to attack', 'Click to harvest']) {
        const touch = width < height;
        const assembly = promptAssemblyWidthPx(touch ? text.replace('Click', 'Tap') : text, !touch);
        for (let x = 0; x <= width; x += 3) {
          expect(promptFitsAt(x, height / 2, width, height, 0, assembly).readable)
            .withContext(`${width}x${height}, "${text}", x ${x}`).toBeTrue();
        }
      }
    }
  });

  it('never says no in the middle of the picture, on any screen the game runs on', () => {
    for (const [width, height] of [[369, 683], [412, 800], [800, 600], [1440, 900], [1920, 1080]]) {
      for (const hud of [0, 100, 180, 250]) {
        const assembly = promptAssemblyWidthPx(LONGEST_PROMPT_TEXT, true);
        for (let y = 0; y <= height - hud; y += 5) {
          expect(promptFitsAt(width / 2, y, width, height, hud, assembly).readable)
            .withContext(`${width}x${height}, hud ${hud}, y ${y}`).toBeTrue();
        }
      }
    }
  });

  it('refuses everything when a panel covers the picture', () => {
    // The general form still has to answer: a phone with a panel open is the case that could one
    // day leave no room on any side.
    expect(promptFitsAt(20, 100, 369, 683, 600, ASSEMBLY).readable).toBeFalse();
  });

  it('measures a longer text as wider', () => {
    expect(promptAssemblyWidthPx('Click to continue building', false))
      .toBeGreaterThan(promptAssemblyWidthPx('Tap to attack', false));
    // The mouse icon is 60 px of picture a finger has no use for.
    expect(promptAssemblyWidthPx('Tap to attack', true) - promptAssemblyWidthPx('Tap to attack', false))
      .toBe(60);
  });
});
