/**
 * How big the shadow map has to be for the screen it will be seen on.
 *
 * <h3>What this was, and what the experiment said</h3>
 *
 * The shadow generator used to be created with a hardcoded 4096 for every device that ever opened
 * the game: 16.8 M texels against a 274'560 pixel backbuffer, 61x the visible area. Three arms ran
 * against that over two days — the map size, the caster list, and how often the pass runs. All
 * three came back null.
 *
 * The trap they were built for sprang every time. Read without matching the hardware, this arm
 * showed renderP50 62.8 ms against 50.2 — a 20 % win. Matched on Mali-G52/G57, the device class
 * present in both arms (n = 25 each): 68.7 against 72.7. Nothing, and the sign flips. The apparent
 * win was six of the fastest devices in the sample — an Adreno 840, an Adreno 830, two Apple GPUs,
 * a Mali-G710 — landing in the small-map arm by chance.
 *
 * The other two are in git and were reverted: taking the scenery off the caster list removed 90 %
 * of the casters and 15 % of the draw calls and changed renderP50 by nothing (the vegetation is
 * hardware-instanced, so thousands of trees are a handful of draw calls); halving the refresh rate
 * did the same. Shadows are not what the frame time is spent on.
 *
 * <h3>Why the sizing stayed anyway</h3>
 *
 * Not for speed — that was measured and is not there. For memory, which is the one axis still
 * open. A 4096 RGBA map is 64 MB in a single allocation; sized for a 640-pixel phone it is 4 MB.
 * PROD reports a median around 1.2 GB of estimated texture bytes per session, so 60 MB back is
 * worth having on a device that has 3-4 GB in total.
 *
 * Stated plainly: <b>appearance was never measured.</b> The argument that 1024 looks the same on a
 * 640-pixel screen is arithmetic — roughly one shadow texel per screen pixel — plus the
 * observation that half of all touch players ran on it for a day and nobody said anything. That is
 * weak evidence. If a shadow edge ever looks wrong on a phone, this is the first place to look and
 * {@link #MIN_SIZE} is the knob.
 *
 * <h3>The rule</h3>
 *
 * {@code directionalLight.shadowFrustumSize} is 150 world units and the camera sees roughly half of
 * that across, so the shadow map covers about twice the view width. Sizing it at
 * {@code 2 x renderWidth} lands near one shadow texel per screen pixel — the point where more
 * texels stop being visible. Clamped to [1024, 4096]: the ceiling is the old value, so no device
 * gets a worse picture than it had, and the floor keeps the edge from going blocky.
 *
 * On a 1920-wide desktop the rule returns 4096, i.e. exactly what shipped before. Desktop is
 * untouched by all of this.
 *
 * <p>The size is fixed when the ShadowGenerator is constructed and cannot be changed afterwards
 * without rebuilding it and re-registering every caster.
 */
export class ShadowQuality {
  /** The old value for every device, and the ceiling. */
  static readonly MAX_SIZE = 4096;
  /** Below this the shadow edge starts to read as stair-steps rather than as softness. */
  static readonly MIN_SIZE = 1024;
  /**
   * Shadow map texels per backbuffer pixel across. See the class comment: the shadow frustum
   * covers about twice the visible width, so this factor is what makes one texel land on one pixel.
   */
  static readonly FRUSTUM_TO_VIEW = 2;

  /**
   * @param renderWidth  engine.getRenderWidth() - device pixels, hardware scaling already applied
   * @param renderHeight engine.getRenderHeight()
   * @return the edge length to hand to {@code new ShadowGenerator(size, light)}
   */
  static size(renderWidth: number, renderHeight: number): number {
    // A canvas that has not been laid out yet reports 300x150, and one that reports nothing at all
    // would size the shadow map at the floor for a desktop. Neither is a device measurement, so
    // fall back to the window instead of trusting the number.
    const longest = Math.max(renderWidth, renderHeight);
    const usable = longest >= 64
      ? longest
      : Math.max(window.innerWidth, window.innerHeight) * (window.devicePixelRatio || 1);
    return ShadowQuality.nearestPowerOfTwo(usable * ShadowQuality.FRUSTUM_TO_VIEW);
  }

  /**
   * Nearest, not next-lower: 3840 (a 1920-wide desktop) rounding down to 2048 would halve the
   * resolution of the one device class that has no performance problem. Rounded in the log domain
   * so "nearest" means nearest by ratio - 1280 is 1.25x of 1024 and 1.6x of 2048, and picking 1024
   * there is right even though 2048 is closer on a number line.
   */
  private static nearestPowerOfTwo(value: number): number {
    const clamped = Math.max(ShadowQuality.MIN_SIZE, Math.min(ShadowQuality.MAX_SIZE, value));
    const exponent = Math.round(Math.log2(clamped));
    return Math.max(ShadowQuality.MIN_SIZE,
      Math.min(ShadowQuality.MAX_SIZE, Math.pow(2, exponent)));
  }
}
