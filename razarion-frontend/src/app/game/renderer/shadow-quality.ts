/**
 * How big the shadow map has to be for the screen it will be seen on.
 *
 * Why this exists. The shadow generator was created with a hardcoded 4096 for every device that
 * ever opened the game. PROD telemetry over two days, touch devices only:
 *
 *   fps p50 = 10.4, frameP50 = 80.1 ms, renderP50 = 58.0 ms  (72 % of the frame)
 *   83 % of all measured periods below 20 fps, 92 % below 30
 *   backbuffer p50 = 640 x 429 = 274 560 pixels
 *   shadow map    = 4096 x 4096 = 16 777 216 texels          -> 61x the visible area
 *
 * A shadow map is a full depth pass: every caster is transformed and rasterised a second time,
 * into that 16 M texel target. Sixty-one shadow texels per visible pixel is not detail anybody can
 * see, it is fill rate spent on a resolution the screen cannot show.
 *
 * The rule. {@code directionalLight.shadowFrustumSize} is 150 world units and the camera sees
 * roughly half of that across, so the shadow map covers about twice the view width. Sizing it at
 * {@code 2 x renderWidth} therefore lands near one shadow texel per screen pixel in the part that
 * is actually visible - the point where more texels stop being visible at all. Clamped to
 * [1024, 4096]: the ceiling is today's value, so no device gets a worse picture than it has now,
 * and the floor keeps the shadow from going blocky on a small phone.
 *
 * On a 1920-wide desktop the rule returns 4096, i.e. exactly what ships today. That is deliberate
 * and it is what makes the A/B readable: desktop is the built-in negative control. If the two arms
 * differ on desktop, the measurement is wrong, not the shadow map.
 *
 * What the size does NOT touch, and why there is a second arm. 60 hours of PROD telemetry, 3161
 * touch periods with a loaded world, each measure centred within its own session so that a fast
 * phone with an empty map cannot fake a correlation against a slow one with a full map:
 *
 *   renderP50 against drawP50        R^2 = 0.31, 65.5 us per draw call   <- the strongest term
 *   renderP50 against activeIndices  R^2 = 0.13
 *   renderP50 against activeMeshes   R^2 = 0.12
 *   renderP50 against shadowCasters  R^2 = 0.03
 *
 * and the effect is clean and symmetric: 150 draw calls fewer than the session's own median is
 * -15.7 ms, 150 more is +16.5 ms. Draw calls per active mesh sit at p50 = 2.09 - the shadow pass
 * draws the scene a second time, in full. Meanwhile a bigger backbuffer goes with a *lower*
 * renderP50 (41.8 ms above 300k pixels against 66.7 ms below it), which is the opposite of what a
 * fill-bound renderer looks like.
 *
 * Shrinking the shadow map removes zero draw calls. It can still win, because a tile-based mobile
 * GPU bins every primitive across the target's tiles and a 4096 map has 65 536 of them where a
 * 1024 map has 4096 - a per-primitive cost that scales with area and would show up inside the
 * draw call, not beside it. That is a real mechanism and an uncertain one, which is exactly what
 * an arm is for. But the mass is in the second geometry pass, not in its resolution, which is why
 * {@link ShadowCasters} exists and flips its own independent coin.
 *
 * Two independent flips rather than one four-way choice: each main effect is then read at the full
 * sample size, averaged over the other, instead of splitting thin traffic into four cells.
 *
 * Still deliberately left alone: {@code refreshRate}. The map is rebuilt every single frame for a
 * sun that never moves, and at 10 fps a shadow that updates every second frame is unlikely to be
 * noticed - but that is a third arm, not a third of this one.
 *
 * The size is fixed when the ShadowGenerator is constructed and cannot be changed afterwards
 * without rebuilding it and re-registering every caster, so there is no F-key toggle here. The
 * arm is chosen once per browser in index.html and forced with {@code ?shadow=hi|lo}.
 */
export class ShadowQuality {
  /** Today's value, and the ceiling. Anything at or above this is the unchanged behaviour. */
  static readonly MAX_SIZE = 4096;
  /** Below this the shadow edge starts to read as stair-steps rather than as softness. */
  static readonly MIN_SIZE = 1024;
  /**
   * Shadow map texels per backbuffer pixel across. See the class comment: the shadow frustum
   * covers about twice the visible width, so this factor is what makes one texel land on one pixel.
   */
  static readonly FRUSTUM_TO_VIEW = 2;

  /**
   * The arm this session is in. Set in index.html, alongside the boot-screen arm and for the same
   * reason: it has to be readable by the tracking beacon that fires before anything else.
   *
   * "hi" is the old behaviour, "lo" is the sized one. Anything else - an old cached index.html, a
   * browser without storage - reads as "hi", so an unknown value can never quietly become the
   * experiment.
   */
  static arm(): 'hi' | 'lo' {
    return (window as any).RAZ_shadowArm === 'lo' ? 'lo' : 'hi';
  }

  /**
   * @param renderWidth  engine.getRenderWidth() - device pixels, hardware scaling already applied
   * @param renderHeight engine.getRenderHeight()
   * @param arm          which half of the experiment this session is in
   * @return the edge length to hand to {@code new ShadowGenerator(size, light)}
   */
  static size(renderWidth: number, renderHeight: number, arm: 'hi' | 'lo'): number {
    if (arm !== 'lo') {
      return ShadowQuality.MAX_SIZE;
    }
    // A canvas that has not been laid out yet reports 300x150, and one that reports nothing at all
    // would size the shadow map at the floor for a desktop. Neither is a device measurement, so
    // fall back to the window instead of trusting the number.
    const longest = Math.max(renderWidth, renderHeight);
    const usable = longest >= 64
      ? longest
      : Math.max(window.innerWidth, window.innerHeight) * (window.devicePixelRatio || 1);
    const target = usable * ShadowQuality.FRUSTUM_TO_VIEW;
    return ShadowQuality.nearestPowerOfTwo(target);
  }

  /**
   * Nearest, not next-lower: 3840 (a 1920-wide desktop) rounding down to 2048 would halve the
   * resolution of the one device that has no performance problem, which is a change the experiment
   * never asked for. Rounded in the log domain so "nearest" means nearest by ratio - 1280 is 1.25x
   * of 1024 and 1.6x of 2048, and picking 1024 there is the right answer even though 2048 is
   * closer on a number line.
   */
  private static nearestPowerOfTwo(value: number): number {
    const clamped = Math.max(ShadowQuality.MIN_SIZE, Math.min(ShadowQuality.MAX_SIZE, value));
    const exponent = Math.round(Math.log2(clamped));
    return Math.max(ShadowQuality.MIN_SIZE,
      Math.min(ShadowQuality.MAX_SIZE, Math.pow(2, exponent)));
  }
}

/**
 * Whether the scenery casts shadows at all, or only the things the player commands.
 *
 * The second, independent arm. See the measurement in {@link ShadowQuality}: the shadow pass draws
 * the scene a second time in full (2.09 draw calls per active mesh), and draw calls are what
 * renderP50 actually follows. The caster census says whose second pass it is - one PROD phone
 * reported {@code TerrainObject:1305, leaves:795, trunk:505, branches:290, dry_leaves:290,
 * palm:290} against 486 active meshes. It is the vegetation.
 *
 * So arm "units" takes the static terrain models off the caster list and leaves everything the
 * player owns or fights on it: bases, units, resources and boxes go through a different path
 * ({@code cloneModel3D} / render-object) and keep their shadows. Palms and rocks stop dropping
 * one. That is the trade every RTS on a phone has made, and it is visible - which is why it is an
 * arm and not a fix.
 *
 * Only on touch. On a desktop the frame budget is not the problem and the shadow is pure picture,
 * so the coin is still flipped there (the tracking stays uniform) but nothing acts on it. Desktop
 * is therefore the null control for this arm too: a difference there means the measurement is
 * wrong, not the shadows.
 *
 * The machinery already existed behind F11 - {@code BabylonModelService.setStaticModelsShadowCasting}
 * both walks the templates that exist and holds the flag that later instances are created under.
 * This only decides what it is set to at startup.
 */
export class ShadowCasters {
  /** "all" is the shipped behaviour. Anything unrecognised reads as "all". */
  static arm(): 'all' | 'units' {
    return (window as any).RAZ_casterArm === 'units' ? 'units' : 'all';
  }

  /** Whether the static terrain models should be on the caster list for this session. */
  static sceneryCastsShadows(arm: 'all' | 'units'): boolean {
    if (arm !== 'units') {
      return true;
    }
    return !(navigator.maxTouchPoints > 0);
  }
}
