import {TipItemState} from '../../../gwtangular/GwtAngularFacade';
import {inProgressOf, questMeter} from './quest-meter';

function item(fields: Partial<TipItemState>): TipItemState {
  return {
    id: 1, itemTypeId: 1, own: true, x: 0, y: 0, idle: true, buildup: 1,
    factoryBuildQueue: [], constructingTypeId: 0, constructing: 0, cargo: [], ...fields
  };
}

describe('questMeter', () => {
  it('has no meter without a target', () => {
    expect(questMeter(0, 0)).toBeNull();
  });

  it('gives each item a cell, the counted ones full', () => {
    expect(questMeter(1, 3)!.cells).toEqual([1, 0, 0]);
  });

  it('fills the next cell with what is under construction, the furthest first', () => {
    const meter = questMeter(1, 3, [0.25, 0.5])!;
    expect(meter.cells).toEqual([1, 0.5, 0.25]);
    expect(meter.fraction).toBeCloseTo(1.75 / 3);
  });

  it('ignores construction beyond the target', () => {
    expect(questMeter(2, 3, [0.4, 0.9])!.cells).toEqual([1, 1, 0.9]);
    expect(questMeter(3, 3, [0.5])!.cells).toEqual([1, 1, 1]);
  });

  it('draws one segment for a large target', () => {
    const meter = questMeter(7, 20)!;
    expect(meter.cells).toEqual([0.35]);
    expect(meter.fraction).toBeCloseTo(0.35);
  });

  it('caps a count above the target', () => {
    expect(questMeter(5, 3)!.fraction).toBe(1);
  });
});

describe('inProgressOf', () => {
  const FACTORY = 4, VIPER = 3, BUILDER = 1;

  it('reads a building from its site, not also from the builder working on it', () => {
    const items = [
      item({id: 1, itemTypeId: FACTORY, buildup: 0.4}),
      item({id: 2, itemTypeId: BUILDER, constructingTypeId: FACTORY, constructing: 0.4})
    ];
    expect(inProgressOf(FACTORY, items)).toEqual([0.4]);
  });

  it('reads a unit from the factory producing it', () => {
    const items = [item({id: 1, itemTypeId: FACTORY, constructingTypeId: VIPER, constructing: 0.6})];
    expect(inProgressOf(VIPER, items)).toEqual([0.6]);
  });

  it('leaves out finished items and those of others', () => {
    const items = [
      item({id: 1, itemTypeId: FACTORY, buildup: 1}),
      item({id: 2, itemTypeId: FACTORY, buildup: 0.3, own: false})
    ];
    expect(inProgressOf(FACTORY, items)).toEqual([]);
  });
});
