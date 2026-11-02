import {pointInPolygon} from './tip-region';

describe('pointInPolygon', () => {
  // The harbour zone of the Noob Island band: a long rectangle along the coast.
  const BAND = [{x: 0, y: 140}, {x: 1245, y: 140}, {x: 1245, y: 225}, {x: 0, y: 225}];

  it('takes the point of a long band straight across from the builder', () => {
    expect(pointInPolygon(BAND, {x: 400, y: 120})).toEqual({x: 400, y: 182.5});
  });

  it('keeps a margin from the ends of the band', () => {
    expect(pointInPolygon(BAND, {x: -50, y: 120})).toEqual({x: 20, y: 182.5});
    expect(pointInPolygon(BAND, {x: 2000, y: 120})).toEqual({x: 1225, y: 182.5});
  });

  it('takes the middle of the widest part without a builder', () => {
    expect(pointInPolygon(BAND, null)).toEqual({x: 622.5, y: 182.5, width: 1245} as any);
  });

  it('takes the middle of a narrow strip along a wavy coast', () => {
    // 13 m of water off a coast that waves by 10 m, corners every 15 m: like quest 386 on the band.
    const coast = (x: number) => 160 + 10 * Math.sin(x / 40);
    const strip: { x: number, y: number }[] = [];
    for (let x = 0; x <= 600; x += 15) strip.push({x, y: coast(x) + 1});
    for (let x = 600; x >= 0; x -= 15) strip.push({x, y: coast(x) + 14});
    const point = pointInPolygon(strip, {x: 300, y: 120})!;
    expect(point.y - coast(point.x)).toBeGreaterThan(4);
    expect(point.y - coast(point.x)).toBeLessThan(11);
    expect(Math.abs(point.x - 300)).toBeLessThan(60);
  });

  it('stays inside a bent region', () => {
    // An L: the lower bar is wide, the upright narrow. From above the upright the point lands in it.
    const L = [{x: 0, y: 0}, {x: 100, y: 0}, {x: 100, y: 10}, {x: 10, y: 10}, {x: 10, y: 100}, {x: 0, y: 100}];
    const point = pointInPolygon(L, {x: 5, y: 200})!;
    expect(point.x).toBe(5);
    expect(point.y).toBe(55);
  });
});
