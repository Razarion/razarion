import {RenderTelemetry, RenderTelemetrySceneStats} from './render-telemetry';

describe('RenderTelemetry', () => {
  const STATS: RenderTelemetrySceneStats = {
    meshes: 400, activeMeshes: 180, activeIndices: 1234, materials: 88,
    disabledMeshes: 210, instancedMeshes: 380, shadowCasters: 395,
    meshTop: 'Rock:200,Palm:120,ground:20',
    parkedMeshes: 205, parkingFilter: true,
    renderWidth: 1280, renderHeight: 720, hardwareScaling: 1, gpu: 'Test "GPU"'
  };

  let lines: string[];

  beforeEach(() => {
    lines = [];
    spyOn(console, 'warn').and.callFake((...args: any[]) => lines.push(String(args[0])));
  });

  function telemetry(stats: RenderTelemetrySceneStats = STATS): RenderTelemetry {
    return new RenderTelemetry(() => stats);
  }

  /** Feeds frames at a fixed interval starting at t=0, returning the timestamp of the last one. */
  function feed(rt: RenderTelemetry, count: number, intervalMs: number, renderMs = 5, from = 0,
                drawCalls = 120): number {
    let now = from;
    for (let i = 0; i < count; i++) {
      rt.recordFrame(now, renderMs, drawCalls);
      now += intervalMs;
    }
    return now - intervalMs;
  }

  /**
   * Consumes the short first period and returns a timestamp to start the next feed from. Every
   * test below that is about the steady ten-second window needs this: the first window is
   * deliberately different, and without burning it those tests would read the warm-up line and
   * then a second line built from the frames left over.
   *
   * Sixteen frames 200 ms apart end exactly on the three-second boundary with fifteen intervals,
   * so nothing spills into the period that follows.
   */
  function skipFirstPeriod(rt: RenderTelemetry): number {
    feed(rt, 16, 200);
    expect(lines.length).toBe(1);
    lines.length = 0;
    return 3_200;
  }

  function field(line: string, key: string): string {
    const match = line.match(new RegExp(`(?:^|\\s)${key}=("[^"]*"|\\S+)`));
    return match ? match[1] : '';
  }

  /**
   * The first line has to arrive early. A mobile player who reaches a running game stays about
   * twenty seconds, and most of that is spent getting there - at ten seconds a window they were
   * gone before the renderer ever said anything, which is why seven days of PROD telemetry held
   * almost no mobile session at all.
   */
  it('says something within three seconds, before a mobile session is over', () => {
    // 16 frames 200 ms apart: three seconds exactly, 15 intervals.
    feed(telemetry(), 16, 200);

    expect(lines.length).toBe(1);
    expect(field(lines[0], 'frames')).toBe('15');
    expect(field(lines[0], 'periodS')).toBe('3.0');
    expect(field(lines[0], 'seq')).toBe('1');
  });

  it('emits one summary line once the period is full', () => {
    const rt = telemetry();
    const from = skipFirstPeriod(rt);
    // 21 frames 500 ms apart = 10.0 s of wall time and 20 frame intervals.
    feed(rt, 21, 500, 5, from);

    expect(lines.length).toBe(1);
    expect(field(lines[0], 'frames')).toBe('20');
    expect(field(lines[0], 'periodS')).toBe('10.0');
    expect(field(lines[0], 'fps')).toBe('2.0');
    expect(field(lines[0], 'seq')).toBe('2');
  });

  /**
   * Why the frame floor ends the period instead of discarding it. This phone draws four frames a
   * second; under the old rule it produced sixteen frames in ten seconds, missed the twenty-frame
   * bar and reported nothing - every period, for its whole session.
   */
  it('waits for a slow device rather than throwing its period away', () => {
    const rt = telemetry();
    const from = skipFirstPeriod(rt);
    // 4 fps: the twenty-frame floor is not met until ten seconds have passed anyway, and the
    // period closes on the frame that satisfies both.
    feed(rt, 41, 250, 5, from);

    expect(lines.length).toBe(1);
    expect(field(lines[0], 'frames')).toBe('40');
    expect(field(lines[0], 'periodS')).toBe('10.0');
    expect(field(lines[0], 'fps')).toBe('4.0');
  });

  /**
   * A tab opened in the background never fires visibilitychange, so the reset that handles a tab
   * being switched away never runs for it. Its frames are throttled to about 1 Hz, and a slow
   * period is no longer dropped for being slow - so without this it would arrive as a phone
   * drawing one frame a second.
   */
  it('says nothing at all from a tab that was never on screen', () => {
    spyOnProperty(document, 'visibilityState').and.returnValue('hidden');

    feed(telemetry(), 16, 200);

    expect(lines.length).toBe(0);
  });

  it('reports the worst frame instead of hiding it in an average', () => {
    const rt = telemetry();
    const from = skipFirstPeriod(rt);
    // 601 frames at a steady 60 fps, then one 400 ms freeze that closes the period. The mean
    // barely moves (16.6 ms); the max and the long-frame counters are the whole point.
    feed(rt, 601, 16, 5, from);
    rt.recordFrame(from + 600 * 16 + 400, 5, 120);

    expect(lines.length).toBe(1);
    expect(field(lines[0], 'frameP50')).toBe('16.0');
    expect(field(lines[0], 'frameMax')).toBe('400.0');
    expect(field(lines[0], 'long50')).toBe('1');
    expect(field(lines[0], 'long100')).toBe('1');
    expect(field(lines[0], 'long250')).toBe('1');
  });

  it('says nothing about a period too short to mean anything', () => {
    // 6 frames a second apart: 5 intervals, under the floor even for the short first period. Six
    // seconds of wall time is well past the three-second mark and still says nothing, which is
    // the point - the frame count is the floor, not the clock.
    feed(telemetry(), 6, 1000);

    expect(lines.length).toBe(0);
  });

  it('drops the frames around a visibility change rather than reporting 1 fps', () => {
    const rt = telemetry();
    const from = skipFirstPeriod(rt);
    // A backgrounded tab: rAF throttled to ~1 Hz for 9 s...
    feed(rt, 10, 1000, 5, from);
    document.dispatchEvent(new Event('visibilitychange'));
    // ...then a normal foreground period, which must not carry the 1000 ms frames.
    feed(rt, 21, 500, 5, 100_000);

    expect(lines.length).toBe(1);
    expect(field(lines[0], 'frameMax')).toBe('500.0');
  });

  it('carries the session uuid so a lagging period can be joined to its device', () => {
    (window as any).RAZ_gameSessionUuid = 'abc-123';
    try {
      const rt = telemetry();
      feed(rt, 21, 500, 5, skipFirstPeriod(rt));
      expect(field(lines[0], 'session')).toBe('abc-123');
    } finally {
      delete (window as any).RAZ_gameSessionUuid;
    }
  });

  it('keeps a quote in the GPU string from breaking the key="value" shape', () => {
    const rt = telemetry();
    feed(rt, 21, 500, 5, skipFirstPeriod(rt));

    expect(field(lines[0], 'gpu')).toBe(`"Test 'GPU'"`);
  });

  it('names the owners of scene.meshes, not just how many there are', () => {
    // The whole point of the census: 400 meshes of which 210 are switched off says "cached
    // scenery", and the bucket names say whose scenery it is.
    const rt = telemetry();
    feed(rt, 21, 500, 5, skipFirstPeriod(rt));

    expect(field(lines[0], 'disabledMeshes')).toBe('210');
    expect(field(lines[0], 'instanced')).toBe('380');
    expect(field(lines[0], 'shadowCasters')).toBe('395');
    expect(field(lines[0], 'meshTop')).toBe('"Rock:200,Palm:120,ground:20"');
  });

  it('carries the A/B state so a period can be attributed to the parked-mesh filter', () => {
    const rt = telemetry({...STATS, parkedMeshes: -1, parkingFilter: false});
    feed(rt, 21, 500, 5, skipFirstPeriod(rt));

    expect(field(lines[0], 'parked')).toBe('-1');
    expect(field(lines[0], 'parkingFilter')).toBe('false');
  });

  it('keeps a quote in a mesh name from breaking the key="value" shape', () => {
    const rt = telemetry({...STATS, meshTop: 'TerrainObject "palm":900'});
    feed(rt, 21, 500, 5, skipFirstPeriod(rt));

    expect(field(lines[0], 'meshTop')).toBe(`"TerrainObject 'palm':900"`);
  });

  it('reports scene counters instead of failing when a counter throws', () => {
    const rt = new RenderTelemetry(() => {
      throw new Error('scene disposed');
    });
    feed(rt, 21, 500, 5, skipFirstPeriod(rt));

    expect(lines.length).toBe(1);
    expect(field(lines[0], 'meshes')).toBe('-1');
  });

  it('reports draw calls per frame, so the per-visible-mesh cost can be attributed', () => {
    const rt = telemetry();
    // A steady 140 draw calls with one spike: the median says what a normal frame submits, the
    // max catches the frame that also rebuilt the shadow map.
    const from = skipFirstPeriod(rt);
    feed(rt, 20, 500, 5, from, 140);
    rt.recordFrame(from + 10_000, 5, 900);

    expect(field(lines[0], 'drawP50')).toBe('140');
    expect(field(lines[0], 'drawMax')).toBe('900');
  });

  it('carries -1 rather than a wrong number when the engine keeps no draw-call counter', () => {
    const rt = telemetry();
    feed(rt, 21, 500, 5, skipFirstPeriod(rt), -1);

    expect(field(lines[0], 'drawP50')).toBe('-1');
  });

  it('counts the engine ticks that arrived during the period', () => {
    const rt = telemetry();
    // After the warm-up, not before: a tick recorded during the first period belongs to it and is
    // cleared with it.
    const from = skipFirstPeriod(rt);
    rt.recordTick(1);
    rt.recordTick(2);
    rt.recordTick(3);
    feed(rt, 21, 500, 5, from);

    // Three ticks produce two gaps between them.
    expect(field(lines[0], 'ticks')).toBe('2');
    expect(field(lines[0], 'tickApplyMax')).toBe('3.0');
  });
});
