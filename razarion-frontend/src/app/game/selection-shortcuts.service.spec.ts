import {SelectionShortcutsService} from './selection-shortcuts.service';

/**
 * The "All" button beside the attack chip (2026-09-30): quest 379 asks for a group, and on a phone
 * the box that selects one was found but not worked. One tap has to put every attack unit in the
 * hand, also those the camera does not show yet.
 */
describe('SelectionShortcutsService selectAll', () => {
  let rendered: number[];
  let selected: number[][];
  let centers: { x: number, y: number }[];
  let reported: string[];
  let service: SelectionShortcutsService;
  const vipers = [{id: 1, itemTypeId: 7, x: 10, y: 20}, {id: 2, itemTypeId: 7, x: 30, y: 40}, {id: 3, itemTypeId: 7, x: 50, y: 60}];

  beforeEach(() => {
    jasmine.clock().install();
    rendered = [];
    selected = [];
    centers = [];
    reported = [];
    const renderer = {
      getBabylonBaseItemsByDiplomacy: () => rendered.map(id => ({getId: () => id})),
      setViewFieldCenter: (x: number, y: number) => centers.push({x, y})
    };
    const selection = {selectOwnItems: (items: { getId: () => number }[]) => selected.push(items.map(item => item.getId()))};
    const facade = {
      baseItemUiService: {getMyOwnSyncItemTickInfos: () => vipers},
      itemTypeService: {
        getAllBaseItemTypes: () => [{
          getId: () => 7, getBuilderType: () => null, getFactoryType: () => null, getHarvesterType: () => null,
          getWeaponType: () => ({}), getPhysicalAreaConfig: () => ({fulfilledMovable: () => true})
        }]
      }
    };
    const tracker = {report: (kind: string, detail?: string) => reported.push(kind + '|' + detail)};
    service = new SelectionShortcutsService(renderer as any, selection as any, {gwtAngularFacade: facade} as any, tracker as any);
  });

  afterEach(() => jasmine.clock().uninstall());

  it('selects every attack unit at once when all are on screen, without moving the camera', () => {
    rendered = [1, 2, 3];
    service.selectAll('attack');
    expect(selected).toEqual([[1, 2, 3]]);
    expect(centers).toEqual([]);
    expect(reported).toEqual(['SELECT_ALL|category=attack']);
  });

  it('takes the camera to the middle of them and selects them once they are rendered', () => {
    rendered = [1];
    service.selectAll('attack');
    expect(selected).toEqual([]);
    expect(centers).toEqual([{x: 30, y: 40}]);
    rendered = [1, 2, 3];
    jasmine.clock().tick(150);
    expect(selected).toEqual([[1, 2, 3]]);
  });

  it('takes what arrived when some never show up', () => {
    rendered = [2];
    service.selectAll('attack');
    jasmine.clock().tick(150 * 8);
    expect(selected).toEqual([[2]]);
  });
});
