import {ShadowQuality} from './shadow-quality';

describe('ShadowQuality', () => {
  describe('size', () => {
    it('gives the median PROD phone a sixteenth of the texels', () => {
      // 640x429 was the p50 backbuffer over two days of touch telemetry.
      expect(ShadowQuality.size(640, 429)).toBe(1024);
    });

    it('leaves a 1920-wide desktop exactly where it was before any of this', () => {
      expect(ShadowQuality.size(1920, 1080)).toBe(4096);
    });

    it('scales in between rather than switching on a device class', () => {
      expect(ShadowQuality.size(1280, 720)).toBe(2048);
    });

    it('measures the long edge, so a landscape phone is not read as a tiny screen', () => {
      expect(ShadowQuality.size(429, 640)).toBe(1024);
      expect(ShadowQuality.size(1080, 1920)).toBe(4096);
    });

    it('never goes below the floor', () => {
      expect(ShadowQuality.size(200, 100)).toBe(ShadowQuality.MIN_SIZE);
    });

    it('falls back to the window when the canvas has not been laid out yet', () => {
      // Babylon reports 300x150 for a canvas without a layout. Taken at face value that would
      // hand a desktop the floor - which would be measuring a layout bug, not a shadow map.
      const size = ShadowQuality.size(0, 0);
      const expected = Math.max(window.innerWidth, window.innerHeight) * (window.devicePixelRatio || 1);
      expect(size).toBe(ShadowQuality.size(expected, 0));
    });
  });
});


