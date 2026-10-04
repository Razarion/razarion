import {QuestCockpitComponent} from './quest-cockpit.component';

/**
 * The glow on the quest panel (2026-09-27): it has to answer real progress and nothing else, and
 * it must not turn into a strobe while harvest progress arrives once a second.
 */
describe('QuestCockpitComponent flash', () => {
  let cockpit: any;

  function progress(...rows: [number, boolean][]): void {
    cockpit.progressRows = rows.map(([actual, done]) => ({text: '', actual, done}));
    cockpit.flashOnProgress();
  }

  beforeEach(() => {
    jasmine.clock().install();
    jasmine.clock().mockDate(new Date(2026, 8, 27, 12, 0, 0));
    cockpit = Object.create(QuestCockpitComponent.prototype);
    cockpit.flashClass = '';
    cockpit.flashAlternate = false;
    cockpit.lastFlashTime = 0;
    cockpit.pendingTickFlash = null;
    cockpit.clearFlashTimer = null;
    cockpit.knownProgress = [{actual: 0, done: false}];
  });

  afterEach(() => jasmine.clock().uninstall());

  it('glows faintly when a counter goes up', () => {
    progress([1, false]);
    expect(cockpit.flashClass).toContain('quest-flash-tick');
  });

  it('glows stronger when a row is done', () => {
    progress([3, true]);
    expect(cockpit.flashClass).toContain('quest-flash-done');
  });

  it('does not glow for the same count sent again, or a count going down', () => {
    cockpit.knownProgress = [{actual: 2, done: false}];
    progress([2, false]);
    expect(cockpit.flashClass).toBe('');
    progress([1, false]);
    expect(cockpit.flashClass).toBe('');
  });

  it('folds counter steps inside the interval into one later glow', () => {
    progress([1, false]);
    jasmine.clock().tick(1300);
    expect(cockpit.flashClass).toBe('');
    progress([2, false]);
    jasmine.clock().tick(500);
    progress([3, false]);
    expect(cockpit.flashClass).toBe('');
    jasmine.clock().tick(1200);
    expect(cockpit.flashClass).toContain('quest-flash-tick');
  });

  it('lets a done row through the interval at once', () => {
    progress([1, false]);
    jasmine.clock().tick(1300);
    progress([2, true]);
    expect(cockpit.flashClass).toContain('quest-flash-done');
  });

  it('alternates the animation so a new glow restarts a fading one', () => {
    progress([1, true], [0, false]);
    const first = cockpit.flashClass;
    progress([1, true], [1, true]);
    expect(first).toContain('quest-flash-a');
    expect(cockpit.flashClass).toContain('quest-flash-b');
  });
});

/**
 * A tap on the quest line (2026-09-30): the camera goes to the quest's target instead of the quest
 * list opening - players looking for help there left the guided quest.
 */
describe('QuestCockpitComponent goToQuestTarget', () => {
  let cockpit: any;
  let centers: { x: number, y: number }[];
  let reported: [string, string | undefined][];

  beforeEach(() => {
    centers = [];
    reported = [];
    cockpit = Object.create(QuestCockpitComponent.prototype);
    cockpit.flashClass = '';
    cockpit.flashAlternate = false;
    cockpit.pendingTickFlash = null;
    cockpit.clearFlashTimer = null;
    cockpit.renderService = {
      getCurrentViewField: () => ({getScreenCenter: () => ({getX: () => 0, getY: () => 0})}),
      setViewFieldCenter: (x: number, y: number) => centers.push({x, y}),
      reportFirstInteraction: (kind: string, detail?: string) => reported.push([kind, detail])
    };
  });

  afterEach(() => clearTimeout(cockpit.clearFlashTimer));

  it('takes the camera to where the tip points', () => {
    cockpit.questMarkerService = {get: () => ({kind: 'point', x: 120, y: 340})};
    cockpit.goToQuestTarget();
    expect(centers).toEqual([{x: 120, y: 340}]);
    expect(reported).toEqual([['QUEST_JUMP', undefined]]);
    expect(cockpit.flashClass).toContain('quest-flash-quest');
  });

  it('without a target it leaves the camera and still answers the tap', () => {
    cockpit.questMarkerService = {get: () => null};
    cockpit.goToQuestTarget();
    expect(centers).toEqual([]);
    expect(reported).toEqual([['QUEST_JUMP', 'target=none']]);
    expect(cockpit.flashClass).toContain('quest-flash-quest');
  });
});
