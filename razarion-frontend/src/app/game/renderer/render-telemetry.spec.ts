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
  function feed(rt: RenderTelemetry, count: number, intervalMs: number, renderMs = 5, from = 0): number {
    let now = from;
    for (let i = 0; i < count; i++) {
      rt.recordFrame(now, renderMs);
      now += intervalMs;
    }
    return now - intervalMs;
  }

  function field(line: string, key: string): string {
    const match = line.match(new RegExp(`(?:^|\\s)${key}=("[^"]*"|\\S+)`));
    return match ? match[1] : '';
  }

  it('emits one summary line once the period is full', () => {
    // 21 frames 500 ms apart = 10.0 s of wall time and 20 frame intervals.
    feed(telemetry(), 21, 500);

    expect(lines.length).toBe(1);
    expect(field(lines[0], 'frames')).toBe('20');
    expect(field(lines[0], 'periodS')).toBe('10.0');
    expect(field(lines[0], 'fps')).toBe('2.0');
    expect(field(lines[0], 'seq')).toBe('1');
  });

  it('reports the worst frame instead of hiding it in an average', () => {
    const rt = telemetry();
    // 601 frames at a steady 60 fps, then one 400 ms freeze that closes the period. The mean
    // barely moves (16.6 ms); the max and the long-frame counters are the whole point.
    feed(rt, 601, 16);
    rt.recordFrame(600 * 16 + 400, 5);

    expect(lines.length).toBe(1);
    expect(field(lines[0], 'frameP50')).toBe('16.0');
    expect(field(lines[0], 'frameMax')).toBe('400.0');
    expect(field(lines[0], 'long50')).toBe('1');
    expect(field(lines[0], 'long100')).toBe('1');
    expect(field(lines[0], 'long250')).toBe('1');
  });

  it('says nothing about a period too short to mean anything', () => {
    // 11 frames a second apart: 10 intervals, well under the 20-frame floor.
    feed(telemetry(), 11, 1000);

    expect(lines.length).toBe(0);
  });

  it('drops the frames around a visibility change rather than reporting 1 fps', () => {
    const rt = telemetry();
    // A backgrounded tab: rAF throttled to ~1 Hz for 9 s...
    feed(rt, 10, 1000);
    document.dispatchEvent(new Event('visibilitychange'));
    // ...then a normal foreground period, which must not carry the 1000 ms frames.
    feed(rt, 21, 500, 5, 100_000);

    expect(lines.length).toBe(1);
    expect(field(lines[0], 'frameMax')).toBe('500.0');
  });

  it('carries the session uuid so a lagging period can be joined to its device', () => {
    (window as any).RAZ_gameSessionUuid = 'abc-123';
    try {
      feed(telemetry(), 21, 500);
      expect(field(lines[0], 'session')).toBe('abc-123');
    } finally {
      delete (window as any).RAZ_gameSessionUuid;
    }
  });

  it('keeps a quote in the GPU string from breaking the key="value" shape', () => {
    feed(telemetry(), 21, 500);

    expect(field(lines[0], 'gpu')).toBe(`"Test 'GPU'"`);
  });

  it('names the owners of scene.meshes, not just how many there are', () => {
    // The whole point of the census: 400 meshes of which 210 are switched off says "cached
    // scenery", and the bucket names say whose scenery it is.
    feed(telemetry(), 21, 500);

    expect(field(lines[0], 'disabledMeshes')).toBe('210');
    expect(field(lines[0], 'instanced')).toBe('380');
    expect(field(lines[0], 'shadowCasters')).toBe('395');
    expect(field(lines[0], 'meshTop')).toBe('"Rock:200,Palm:120,ground:20"');
  });

  it('carries the A/B state so a period can be attributed to the parked-mesh filter', () => {
    feed(telemetry({...STATS, parkedMeshes: -1, parkingFilter: false}), 21, 500);

    expect(field(lines[0], 'parked')).toBe('-1');
    expect(field(lines[0], 'parkingFilter')).toBe('false');
  });

  it('keeps a quote in a mesh name from breaking the key="value" shape', () => {
    feed(telemetry({...STATS, meshTop: 'TerrainObject "palm":900'}), 21, 500);

    expect(field(lines[0], 'meshTop')).toBe(`"TerrainObject 'palm':900"`);
  });

  it('reports scene counters instead of failing when a counter throws', () => {
    const rt = new RenderTelemetry(() => {
      throw new Error('scene disposed');
    });
    feed(rt, 21, 500);

    expect(lines.length).toBe(1);
    expect(field(lines[0], 'meshes')).toBe('-1');
  });

  it('counts the engine ticks that arrived during the period', () => {
    const rt = telemetry();
    rt.recordTick(1);
    rt.recordTick(2);
    rt.recordTick(3);
    feed(rt, 21, 500);

    // Three ticks produce two gaps between them.
    expect(field(lines[0], 'ticks')).toBe('2');
    expect(field(lines[0], 'tickApplyMax')).toBe('3.0');
  });
});
