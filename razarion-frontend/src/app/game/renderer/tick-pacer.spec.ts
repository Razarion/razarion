import {TickPacer} from './tick-pacer';

describe('TickPacer', () => {
  function feed(pacer: TickPacer, gaps: number[]): void {
    let now = 1000;
    pacer.onTick(now);
    for (const gap of gaps) {
      now += gap;
      pacer.onTick(now);
    }
  }

  it('glides a little longer than the tick while ticks arrive on time', () => {
    const pacer = new TickPacer();
    feed(pacer, Array(50).fill(100));
    expect(pacer.glideMillis()).toBeCloseTo(125, 0);
  });

  it('follows ticks that arrive late, so a unit does not stop between them', () => {
    const pacer = new TickPacer();
    feed(pacer, Array(50).fill(150));
    // longer than the gap: the next tick comes in while the unit is still moving
    expect(pacer.glideMillis()).toBeGreaterThan(150);
    expect(pacer.glideMillis()).toBeCloseTo(187.5, 0);
  });

  it('is never shorter than the engine tick', () => {
    const pacer = new TickPacer();
    feed(pacer, Array(50).fill(30));
    expect(pacer.glideMillis()).toBe(TickPacer.MIN_GLIDE_MILLIS);
  });

  it('is capped, so a unit never trails far behind', () => {
    const pacer = new TickPacer();
    feed(pacer, Array(50).fill(900));
    expect(pacer.glideMillis()).toBe(TickPacer.MAX_GLIDE_MILLIS);
  });

  it('does not count a stall or a hidden tab as the pace', () => {
    const pacer = new TickPacer();
    feed(pacer, [...Array(50).fill(100), 30000, 100]);
    expect(pacer.glideMillis()).toBeCloseTo(125, 0);
  });

  it('counts the driving frames spent waiting, and starts anew once taken', () => {
    const pacer = new TickPacer();
    [false, false, true, false].forEach(waiting => pacer.recordUnitFrame(waiting));
    expect(pacer.takeUnitFrames()).toEqual({frames: 4, waiting: 1});
    expect(pacer.takeUnitFrames()).toEqual({frames: 0, waiting: 0});
    pacer.recordUnitFrame(false);
    pacer.recordUnitWaitFrames(3);
    expect(pacer.takeUnitFrames()).toEqual({frames: 4, waiting: 3});
  });

  it('starts at the engine tick before any gap is known', () => {
    expect(new TickPacer().glideMillis()).toBeCloseTo(125, 0);
  });
});
