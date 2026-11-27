import {SelectionService} from './selection.service';

/**
 * Units selected by id without ever being rendered (the "All" button, 2026-11-27). The engine
 * reports the death only of units that were rendered once, so these must be dropped by the
 * selection itself - one dead id fails the whole order.
 */
describe('SelectionService units selected off screen', () => {
  let service: SelectionService;
  let alive: Set<number>;
  let changes: number;
  const viperType = {
    getId: () => 3,
    getWeaponType: () => ({checkItemTypeDisallowed: () => false}),
    getPhysicalAreaConfig: () => ({fulfilledMovable: () => true}),
    getHarvesterType: () => null,
    getName: () => 'Viper'
  } as any;
  const rendered = (id: number) => ({getId: () => id, getBaseItemType: () => viperType, select: () => undefined}) as any;
  let reported: string[];

  beforeEach(() => {
    reported = [];
    service = new SelectionService({} as any, {report: (kind: string, detail?: string) => reported.push(kind + '|' + detail)} as any);
    (service as any).speakOwnSelection = () => undefined;
    alive = new Set([1, 2, 3]);
    changes = 0;
    service.addSelectionListener(() => changes++);
    service.selectOwnItems([rendered(1)], false, {units: [{id: 2, type: viperType}, {id: 3, type: viperType}], alive: () => alive});
  });

  it('gives orders to all of them and counts them as one group', () => {
    expect(service.getAttackerIds(5)).toEqual([1, 2, 3]);
    expect(service.getMovableIds()).toEqual([1, 2, 3]);
    expect(reported).toContain('SELECT_GROUP|units=3');
  });

  it('drops one that died before it was ever seen', () => {
    alive.delete(3);
    const before = changes;
    expect(service.getAttackerIds(5)).toEqual([1, 2]);
    expect(service.getSelectedOwnItemIds()).toEqual([1, 2]);
    expect(changes).toBe(before + 1);
  });

  it('leaves them alone when the living units are not known', () => {
    service.selectOwnItems([rendered(1)], false, {units: [{id: 2, type: viperType}], alive: () => null});
    expect(service.getMovableIds()).toEqual([1, 2]);
  });

  it('shows one in the cockpit once it comes into view, and stops checking it', () => {
    const before = changes;
    service.tryReattachItem(rendered(2));
    expect(changes).toBe(before + 1);
    expect(service.getSelectedOwnItems().map(item => item.getId())).toEqual([1, 2]);
    alive.delete(2); // from now on the engine reports its death (disposeItem)
    expect(service.getMovableIds()).toEqual([1, 2, 3]);
  });

  it('forgets them with the next selection', () => {
    service.clearSelection();
    alive.clear();
    expect(service.hasOwnSelection()).toBeFalse();
    service.selectOwnItems([rendered(1)]);
    expect(service.getMovableIds()).toEqual([1]);
  });
});
