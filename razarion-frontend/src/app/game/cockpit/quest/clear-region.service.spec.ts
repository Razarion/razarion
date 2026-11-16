import {NgZone} from '@angular/core';
import {ClearRegionService} from './clear-region.service';
import {GwtAngularService} from '../../../gwtangular/GwtAngularService';
import {FirstInteractionTrackerService} from '../../tracking/first-interaction-tracker.service';
import {PlaceConfig} from '../../../gwtangular/GwtAngularFacade';

describe('ClearRegionService (automatic sale of the noob island)', () => {
  /** The noob island of level 9: a square 0-340. */
  const island = {
    getPolygon2D: () => ({
      toCornersAngular: () => [[0, 0], [340, 0], [340, 340], [0, 340]].map(([x, y]) => ({getX: () => x, getY: () => y})),
    }),
    getPosition: () => null,
    getRadius: () => null,
  } as unknown as PlaceConfig;

  let items: { id: number, own: boolean, x: number, y: number, itemTypeId: number, buildup: number }[];
  let sold: number[][];
  let reported: string[];
  let service: ClearRegionService;

  beforeEach(() => {
    jasmine.clock().install();
    jasmine.clock().mockDate(new Date(2026, 10, 15, 12, 0, 0));
    items = [
      {id: 1, own: true, x: 100, y: 100, itemTypeId: 4, buildup: 1},
      {id: 2, own: true, x: 120, y: 90, itemTypeId: 4, buildup: 1},
      {id: 3, own: true, x: 466, y: 485, itemTypeId: 1, buildup: 1}, // the builder, across
      {id: 4, own: false, x: 50, y: 50, itemTypeId: 4, buildup: 1},  // a bot's
    ];
    sold = [];
    reported = [];
    const gwt = {
      gwtAngularFacade: {
        baseItemUiService: {getTipItemStates: () => items},
        itemTypeService: {getBaseItemTypeAngular: () => ({getPrice: () => 20})},
        itemCockpitBridge: {
          sellItems: (ids: number[]) => {
            sold.push(ids);
          }
        },
      }
    } as unknown as GwtAngularService;
    const tracker = {report: (kind: string, detail?: string) => reported.push(kind + (detail ? ' ' + detail : ''))} as unknown as FirstInteractionTrackerService;
    const zone = {run: (fn: () => unknown) => fn(), runOutsideAngular: (fn: () => unknown) => fn()} as unknown as NgZone;
    service = new ClearRegionService(gwt, tracker, zone);
  });

  afterEach(() => {
    service.deactivate();
    service.dismissNotice();
    jasmine.clock().uninstall();
  });

  it('sells what is left on the island by itself and says what it brought', () => {
    service.activate(island);

    expect(sold).toEqual([[1, 2]]);
    expect(reported).toEqual(['CLEAR_REGION_AUTO_SOLD count=2']);
    expect(service.sold()).toEqual({count: 2, refund: 20});
  });

  it('waits until the level-up splash is gone', () => {
    let splash = true;
    service.setBlocked(() => splash);
    service.activate(island);
    jasmine.clock().tick(2000);
    expect(sold).toEqual([]);
    expect(service.sold()).toBeNull();

    splash = false;
    jasmine.clock().tick(500);
    expect(sold).toEqual([[1, 2]]);
    expect(service.sold()).not.toBeNull();
  });

  it('sends the sale again when something is still there after 5 s, with one notice', () => {
    service.activate(island);
    jasmine.clock().tick(4500);
    expect(sold.length).toBe(1);

    jasmine.clock().tick(1000);
    expect(sold.length).toBe(2);
    expect(reported.length).toBe(2);
  });

  it('does not sell again once the island is empty', () => {
    service.activate(island);
    items = items.filter(item => item.id > 2);
    jasmine.clock().tick(10000);

    expect(sold.length).toBe(1);
    expect(service.offer()).toBeNull();
  });

  it('keeps the notice for 6 s after the quest has passed, or until tapped', () => {
    service.activate(island);
    service.deactivate(); // the quest passes with the next tick
    jasmine.clock().tick(5000);
    expect(service.sold()).not.toBeNull();
    jasmine.clock().tick(1000);
    expect(service.sold()).toBeNull();
  });

  it('a tap takes the notice away', () => {
    service.activate(island);
    service.dismissNotice();

    expect(service.sold()).toBeNull();
  });

  it('the buttons stay as the fallback', () => {
    service.setBlocked(() => true);
    service.activate(island);
    service.sellAll('strip');

    expect(sold).toEqual([[1, 2]]);
    expect(reported).toEqual(['CLEAR_REGION_SOLD count=2 from=strip']);
  });
});
