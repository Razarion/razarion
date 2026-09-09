import {ShadowCasters, ShadowQuality} from './shadow-quality';

describe('ShadowQuality', () => {

  describe('arm', () => {
    afterEach(() => delete (window as any).RAZ_shadowArm);

    it('is the shipped behaviour when index.html said nothing', () => {
      // An index.html served from a stale cache has no arm. It must not silently join the
      // experiment - an unattributed session in the fast half would look like the slow half
      // getting faster.
      expect(ShadowQuality.arm()).toBe('hi');
    });

    it('is the shipped behaviour for any value that is not exactly lo', () => {
      (window as any).RAZ_shadowArm = 'low';
      expect(ShadowQuality.arm()).toBe('hi');
    });

    it('takes lo when index.html chose it', () => {
      (window as any).RAZ_shadowArm = 'lo';
      expect(ShadowQuality.arm()).toBe('lo');
    });
  });

  describe('size', () => {
    it('leaves the hi arm at exactly what ships today', () => {
      expect(ShadowQuality.size(640, 429, 'hi')).toBe(4096);
      expect(ShadowQuality.size(1920, 1080, 'hi')).toBe(4096);
    });

    it('gives the median PROD phone a sixteenth of the texels', () => {
      // 640x429 was the p50 backbuffer over two days of touch telemetry.
      expect(ShadowQuality.size(640, 429, 'lo')).toBe(1024);
    });

    it('leaves a 1920-wide desktop where it is, so it can serve as the negative control', () => {
      expect(ShadowQuality.size(1920, 1080, 'lo')).toBe(4096);
    });

    it('scales in between rather than switching on a device class', () => {
      expect(ShadowQuality.size(1280, 720, 'lo')).toBe(2048);
    });

    it('measures the long edge, so a landscape phone is not read as a tiny screen', () => {
      expect(ShadowQuality.size(429, 640, 'lo')).toBe(1024);
      expect(ShadowQuality.size(1080, 1920, 'lo')).toBe(4096);
    });

    it('never goes below the floor', () => {
      expect(ShadowQuality.size(200, 100, 'lo')).toBe(ShadowQuality.MIN_SIZE);
    });

    it('falls back to the window when the canvas has not been laid out yet', () => {
      // Babylon reports 300x150 for a canvas without a layout. Taken at face value that would
      // hand a desktop the floor - and the arm would then be measuring a bug, not a shadow map.
      const size = ShadowQuality.size(0, 0, 'lo');
      const expected = Math.max(window.innerWidth, window.innerHeight) * (window.devicePixelRatio || 1);
      expect(size).toBe(ShadowQuality.size(expected, 0, 'lo'));
    });
  });
});

describe('ShadowCasters', () => {
  afterEach(() => delete (window as any).RAZ_casterArm);

  it('is the shipped behaviour when index.html said nothing', () => {
    expect(ShadowCasters.arm()).toBe('all');
  });

  it('keeps the scenery on the caster list in the all arm', () => {
    expect(ShadowCasters.sceneryCastsShadows('all')).toBeTrue();
  });

  it('acts only where the frame budget is the problem', () => {
    // On a desktop the shadow is picture, not cost. The coin is still flipped there so the
    // tracking stays uniform, but nothing acts on it - which is what makes desktop the null
    // control for this arm.
    const touch = navigator.maxTouchPoints > 0;
    expect(ShadowCasters.sceneryCastsShadows('units')).toBe(!touch);
  });
});
