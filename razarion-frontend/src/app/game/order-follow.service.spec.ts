import {NgZone} from '@angular/core';
import {OrderFollowService} from './order-follow.service';
import {OrderNote} from './action.service';
import {TipItemState} from '../gwtangular/GwtAngularFacade';

const HARVESTER = 6;
const VIPER = 7;

describe('OrderFollowService auto harvest', () => {
  let now: number;
  let items: TipItemState[];
  let tipActive: boolean;
  let inView: (x: number, y: number) => boolean;
  let follows: { target: () => { x: number, y: number } | null, onTakeOver: () => void, stop: jasmine.Spy }[];
  let reports: string[];
  let orderListener: (order: OrderNote) => void;

  function item(id: number, itemTypeId: number, x: number, y: number, extra: Partial<TipItemState> = {}): TipItemState {
    return {
      id, itemTypeId, own: true, x, y, idle: true, buildup: 1, factoryBuildQueue: [], constructingTypeId: 0,
      constructing: 0, cargo: [], ...extra
    };
  }

  /** In steps, so performance.now() moves along with the timers. */
  function advance(millis: number): void {
    for (let done = 0; done < millis; done += 100) {
      now += 100;
      jasmine.clock().tick(100);
    }
  }

  beforeEach(() => {
    jasmine.clock().install();
    now = 1000;
    spyOn(performance, 'now').and.callFake(() => now);
    items = [];
    tipActive = true;
    inView = () => true;
    follows = [];
    reports = [];
    const actionService = {addOrderListener: (listener: (order: OrderNote) => void) => orderListener = listener};
    const renderService = {
      setArrowJumpFraming: () => undefined,
      getCurrentViewField: () => ({contains: (p: any) => inView(p.getX(), p.getY())}),
      followViewFieldCenter: (target: () => { x: number, y: number } | null, onTakeOver: () => void) => {
        const stop = jasmine.createSpy('stop');
        follows.push({target, onTakeOver, stop});
        return stop;
      }
    };
    const gwtAngularService = {
      gwtAngularFacade: {
        baseItemUiService: {getTipItemStates: () => items},
        itemTypeService: {
          getBaseItemTypeAngular: (id: number) => ({getHarvesterType: () => id === HARVESTER ? {} : null})
        }
      }
    };
    const tipService = {isActive: () => tipActive};
    const tracker = {report: (kind: string, detail: string) => reports.push(kind + ' ' + detail)};
    const zone = {runOutsideAngular: (fn: () => any) => fn()} as unknown as NgZone;
    new OrderFollowService(actionService as any, renderService as any, gwtAngularService as any, tipService as any,
      {} as any, tracker as any, zone);
    // First look: whatever stands there already is not new
    advance(500);
  });

  afterEach(() => jasmine.clock().uninstall());

  it('follows a new harvester that drives off on its own until it has harvested a moment', () => {
    items.push(item(10, HARVESTER, 100, 100, {idle: false}));
    advance(500);
    // Still on screen: no camera yet
    advance(200);
    expect(follows.length).toBe(0);

    inView = (x) => x < 110;
    items[0] = item(10, HARVESTER, 120, 100, {idle: false});
    advance(200);
    expect(follows.length).toBe(1);
    expect(reports).toContain('ORDER_FOLLOW kind=autoHarvest');
    expect(follows[0].target()).toEqual({x: 120, y: 100});

    items[0] = item(10, HARVESTER, 130, 100, {idle: false, harvesting: true, harvestX: 134, harvestY: 100});
    advance(200);
    expect(follows[0].target()).toEqual({x: 132, y: 100});
    expect(follows[0].stop).not.toHaveBeenCalled();

    advance(2200);
    expect(follows[0].stop).toHaveBeenCalled();
  });

  it('leaves the camera alone when harvester and crystal stay in the picture', () => {
    items.push(item(10, HARVESTER, 100, 100, {idle: false}));
    advance(500);
    items[0] = item(10, HARVESTER, 105, 100, {idle: false, harvesting: true, harvestX: 108, harvestY: 100});
    advance(3000);
    expect(follows.length).toBe(0);
  });

  it('ignores a new harvester that stays idle', () => {
    inView = () => false;
    items.push(item(10, HARVESTER, 100, 100));
    advance(4000);
    items[0] = item(10, HARVESTER, 100, 100, {idle: false});
    advance(1000);
    expect(follows.length).toBe(0);
  });

  it('ignores harvesters that were there before and units that are no harvesters', () => {
    inView = () => false;
    items.push(item(11, VIPER, 100, 100, {idle: false}));
    advance(1000);
    expect(follows.length).toBe(0);
  });

  it('does nothing without a tip', () => {
    tipActive = false;
    inView = () => false;
    advance(500);
    items.push(item(10, HARVESTER, 100, 100, {idle: false}));
    advance(1000);
    expect(follows.length).toBe(0);
  });

  it('leaves a harvester the player gave an order to the order follow', () => {
    inView = () => false;
    items.push(item(10, HARVESTER, 100, 100, {idle: false}));
    tipActive = false;
    orderListener({kind: 'move', unitIds: [10], targetId: null, targetTypeId: null, target: {x: 200, y: 200}});
    tipActive = true;
    advance(1000);
    expect(reports).not.toContain('ORDER_FOLLOW kind=autoHarvest');
  });

  it('ends when the player moves the camera', () => {
    inView = () => false;
    items.push(item(10, HARVESTER, 100, 100, {idle: false}));
    advance(700);
    expect(follows.length).toBe(1);
    follows[0].onTakeOver();
    expect(reports).toContain('ORDER_FOLLOW_TAKEOVER kind=autoHarvest after=0-2');
  });
});
