import {SelectionShortcutsService} from './selection-shortcuts.service';

/**
 * The "All" button beside the attack chip (2026-09-30): quest 379 asks for a group, and on a phone
 * the box that selects one was found but not worked. One tap has to put every attack unit in the
 * hand, also those the camera does not show yet.
 */
describe('SelectionShortcutsService selectAll', () => {
  let rendered: number[];
  let selected: number[][];
  let byId: number[][];
  let aliveIds: () => ReadonlySet<number> | null;
  let centers: { x: number, y: number }[];
  let reported: string[];
  let service: SelectionShortcutsService;
  const vipers = [{id: 1, itemTypeId: 7, x: 10, y: 20}, {id: 2, itemTypeId: 7, x: 30, y: 40}, {id: 3, itemTypeId: 7, x: 50, y: 60}];

  beforeEach(() => {
    jasmine.clock().install();
    rendered = [];
    selected = [];
    byId = [];
    aliveIds = () => null;
    centers = [];
    reported = [];
    const renderer = {
      getBabylonBaseItemsByDiplomacy: () => rendered.map(id => ({getId: () => id})),
      setViewFieldCenter: (x: number, y: number) => centers.push({x, y})
    };
    const selection = {
      selectOwnItems: (items: { getId: () => number }[], _byGame?: boolean,
                       unrendered?: { units: { id: number }[], alive: () => ReadonlySet<number> | null }) => {
        selected.push(items.map(item => item.getId()));
        byId.push((unrendered?.units ?? []).map(unit => unit.id));
        if (unrendered) aliveIds = unrendered.alive;
      }
    };
    const facade = {
      baseItemUiService: {getMyOwnSyncItemTickInfos: () => vipers},
      itemTypeService: {
        getBaseItemTypeAngular: (id: number) => ({getId: () => id}),
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

  it('selects the ones off screen by id at once and takes the camera to the middle of them', () => {
    rendered = [1];
    service.selectAll('attack');
    expect(selected).toEqual([[1]]);
    expect(byId).toEqual([[2, 3]]);
    expect(centers).toEqual([{x: 30, y: 40}]);
  });

  // Quest 379: the Viper of 364 stays at the Extractor of 365, ~70 m from the two new ones, and
  // the old selection took only what the camera showed - two of three (2026-11-27).
  it('selects all three when none of them is on screen', () => {
    rendered = [];
    service.selectAll('attack');
    expect(selected).toEqual([[]]);
    expect(byId).toEqual([[1, 2, 3]]);
  });

  it('hands the selection the living own units, so a dead one can be dropped', () => {
    rendered = [1];
    service.selectAll('attack');
    expect([...aliveIds()!]).toEqual([1, 2, 3]);
  });
});
