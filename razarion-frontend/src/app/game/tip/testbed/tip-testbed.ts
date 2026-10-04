import {tick} from '@angular/core/testing';
import {BehaviorSubject, of} from 'rxjs';
import {TipService} from '../tip.service';
import {TipStallTrackerService} from '../tip-stall-tracker.service';
import {SelectionService} from '../../selection.service';
import {ActionService} from '../../action.service';
import {FirstInteractionTrackerService} from '../../tracking/first-interaction-tracker.service';
import {FakeBaseItem, FakeItemCockpit, FakeResourceItem, QuestCondition, Unit, World} from './fake-world';
import {TipStallReason} from '../tip-stall';
import {fakeBaseItemType, ItemTypeId, itemTypeSpec} from './fake-item-types';
import {QuestMarkerService} from '../../cockpit/main/radar/quest-marker.service';

/** A quest with a tip, as in catalog section 3. */
export interface TipQuest {
  id: number;
  tip: 'BUILD' | 'FABRICATE' | 'HARVEST' | 'ATTACK' | 'LOAD' | 'SAIL' | 'UNLOAD' | 'SELL';
  actorTypeId: number;
  /** The typed part of the condition, which is also what the tip reads its target from. */
  typeCount: { typeId: number, count: number } | null;
  /** More types after typeCount, for a condition that asks for several (396). */
  moreTypeCounts?: { typeId: number, count: number }[];
  /** ConditionConfig.conditionTrigger, where the tip reads it (SYNC_ITEM_POSITION without a region). */
  trigger?: string;
  condition: QuestCondition;
  /** The tip asks for a group before the attack (TipConfig.group). */
  group?: boolean;
  /** The build region of a SYNC_ITEM_POSITION quest (386), as a polygon. */
  region?: { x: number, y: number }[];
}

/**
 * Quest 386's coastal band in small: a strip 60 units east of the base, well outside the render
 * box while the camera is at home.
 */
export const REGION_386 = [{x: 55, y: -10}, {x: 65, y: -10}, {x: 65, y: 10}, {x: 55, y: 10}];

/**
 * The crossing off the noob island in small: the coast of the Phase 2 region 80 units north-east of
 * the base, and the circle of water off it the transporter has to reach - within the transporter's
 * unload range (20) of that coast, as on the planet.
 */
export const REGION_PHASE2 = [{x: 80, y: 40}, {x: 130, y: 40}, {x: 130, y: 90}, {x: 80, y: 90}];
export const REGION_COAST_WATER = octagon(72, 32, 6);

function octagon(x: number, y: number, radius: number): { x: number, y: number }[] {
  return [...Array(8).keys()].map(i => ({
    x: x + radius * Math.cos(i * Math.PI / 4),
    y: y + radius * Math.sin(i * Math.PI / 4)
  }));
}

export const QUESTS: Record<number, TipQuest> = {
  358: {id: 358, tip: 'BUILD', actorTypeId: ItemTypeId.BUILDER, typeCount: {typeId: ItemTypeId.FACTORY, count: 1},
    condition: {kind: 'created', typeId: ItemTypeId.FACTORY, count: 1, includeExisting: true}},
  359: {id: 359, tip: 'FABRICATE', actorTypeId: ItemTypeId.FACTORY, typeCount: {typeId: ItemTypeId.HARVESTER, count: 1},
    condition: {kind: 'created', typeId: ItemTypeId.HARVESTER, count: 1, includeExisting: true}},
  363: {id: 363, tip: 'HARVEST', actorTypeId: ItemTypeId.HARVESTER, typeCount: null,
    condition: {kind: 'harvested', amount: 5}},
  364: {id: 364, tip: 'FABRICATE', actorTypeId: ItemTypeId.FACTORY, typeCount: {typeId: ItemTypeId.VIPER, count: 1},
    condition: {kind: 'created', typeId: ItemTypeId.VIPER, count: 1, includeExisting: true}},
  365: {id: 365, tip: 'ATTACK', actorTypeId: ItemTypeId.VIPER, typeCount: {typeId: ItemTypeId.BOT_EXTRACTOR, count: 1},
    condition: {kind: 'killed', typeId: ItemTypeId.BOT_EXTRACTOR, count: 1}},
  361: {id: 361, tip: 'BUILD', actorTypeId: ItemTypeId.BUILDER, typeCount: {typeId: ItemTypeId.RADAR, count: 1},
    condition: {kind: 'created', typeId: ItemTypeId.RADAR, count: 1, includeExisting: true}},
  362: {id: 362, tip: 'BUILD', actorTypeId: ItemTypeId.BUILDER, typeCount: {typeId: ItemTypeId.POWERPLANT, count: 1},
    condition: {kind: 'created', typeId: ItemTypeId.POWERPLANT, count: 1, includeExisting: true}},
  366: {id: 366, tip: 'HARVEST', actorTypeId: ItemTypeId.HARVESTER, typeCount: null,
    condition: {kind: 'harvested', amount: 10}},
  386: {id: 386, tip: 'BUILD', actorTypeId: ItemTypeId.BUILDER, typeCount: {typeId: ItemTypeId.DOCKYARD, count: 1},
    condition: {kind: 'createdIn', typeId: ItemTypeId.DOCKYARD, region: REGION_386}, region: REGION_386},
  369: {id: 369, tip: 'FABRICATE', actorTypeId: ItemTypeId.FACTORY, typeCount: {typeId: ItemTypeId.VIPER, count: 3},
    condition: {kind: 'created', typeId: ItemTypeId.VIPER, count: 3, includeExisting: true}},
  379: {id: 379, tip: 'ATTACK', actorTypeId: ItemTypeId.VIPER, typeCount: {typeId: ItemTypeId.BOT_REFINERY, count: 1},
    condition: {kind: 'killed', typeId: ItemTypeId.BOT_REFINERY, count: 1}, group: true},
  387: {id: 387, tip: 'FABRICATE', actorTypeId: ItemTypeId.DOCKYARD, typeCount: {typeId: ItemTypeId.HYDRA, count: 1},
    condition: {kind: 'created', typeId: ItemTypeId.HYDRA, count: 1, includeExisting: true}},
  388: {id: 388, tip: 'ATTACK', actorTypeId: ItemTypeId.HYDRA, typeCount: {typeId: ItemTypeId.BOT_HYDRA, count: 1},
    condition: {kind: 'killed', typeId: ItemTypeId.BOT_HYDRA, count: 1}},
  389: {id: 389, tip: 'FABRICATE', actorTypeId: ItemTypeId.DOCKYARD, typeCount: {typeId: ItemTypeId.TRANSPORTER, count: 1},
    condition: {kind: 'created', typeId: ItemTypeId.TRANSPORTER, count: 1, includeExisting: true}},
  // Quest 392 split in three (2026-09-22); 485 and 486 are the local ids.
  485: {id: 485, tip: 'LOAD', actorTypeId: ItemTypeId.BUILDER, typeCount: {typeId: ItemTypeId.BUILDER, count: 1},
    condition: {kind: 'loaded', typeId: ItemTypeId.BUILDER}},
  486: {id: 486, tip: 'SAIL', actorTypeId: ItemTypeId.BUILDER, typeCount: {typeId: ItemTypeId.TRANSPORTER, count: 1},
    condition: {kind: 'loadedIn', containerTypeId: ItemTypeId.TRANSPORTER, region: REGION_COAST_WATER}, region: REGION_COAST_WATER},
  392: {id: 392, tip: 'UNLOAD', actorTypeId: ItemTypeId.BUILDER, typeCount: {typeId: ItemTypeId.BUILDER, count: 1},
    condition: {kind: 'in', typeId: ItemTypeId.BUILDER, region: REGION_PHASE2}, region: REGION_PHASE2},
  // Level 9 moves the base: sell the factory, later the dockyard (2026-09-25).
  393: {id: 393, tip: 'SELL', actorTypeId: ItemTypeId.FACTORY, typeCount: {typeId: ItemTypeId.FACTORY, count: 1},
    condition: {kind: 'sold', typeId: ItemTypeId.FACTORY, count: 1}},
  // SYNC_ITEM_POSITION in the Phase 2 start region: what stands there counts, whenever it was built.
  395: {id: 395, tip: 'BUILD', actorTypeId: ItemTypeId.BUILDER, typeCount: {typeId: ItemTypeId.FACTORY, count: 1},
    trigger: 'SYNC_ITEM_POSITION', region: REGION_PHASE2,
    condition: {kind: 'owns', types: [{typeId: ItemTypeId.FACTORY, count: 1}], region: REGION_PHASE2}},
  396: {id: 396, tip: 'BUILD', actorTypeId: ItemTypeId.BUILDER, typeCount: {typeId: ItemTypeId.RADAR, count: 1},
    moreTypeCounts: [{typeId: ItemTypeId.POWERPLANT, count: 1}], trigger: 'SYNC_ITEM_POSITION', region: REGION_PHASE2,
    condition: {kind: 'owns', types: [{typeId: ItemTypeId.RADAR, count: 1}, {typeId: ItemTypeId.POWERPLANT, count: 1}],
      region: REGION_PHASE2}},
  // A harvester and six vipers on the Phase 2 island, from the factory built there (395).
  400: {id: 400, tip: 'FABRICATE', actorTypeId: ItemTypeId.FACTORY, typeCount: {typeId: ItemTypeId.HARVESTER, count: 1},
    moreTypeCounts: [{typeId: ItemTypeId.VIPER, count: 6}], trigger: 'SYNC_ITEM_POSITION', region: REGION_PHASE2,
    condition: {kind: 'owns', types: [{typeId: ItemTypeId.HARVESTER, count: 1}, {typeId: ItemTypeId.VIPER, count: 6}],
      region: REGION_PHASE2}},
  401: {id: 401, tip: 'SELL', actorTypeId: ItemTypeId.DOCKYARD, typeCount: {typeId: ItemTypeId.DOCKYARD, count: 1},
    condition: {kind: 'sold', typeId: ItemTypeId.DOCKYARD, count: 1}}
};

/** What is on the player's screen that a tip put there. */
export interface PlayerView {
  /** Prompts on items that are on screen. A prompt on a disposed or off-screen item is not seen. */
  prompts: { text: string, itemId: number, typeId: number }[];
  /** Direction arrow at the edge of the screen, as an angle; null when there is none. */
  arrowAngle: number | null;
  /** The "go there" chip beside the arrow is up. */
  jumpOffered: boolean;
  cockpitHintTypeId: number | null;
  placeMarker: boolean;
  placerActive: boolean;
  /** The group tip lights the selection-box button in the icon bar. */
  groupAsked: boolean;
  /** The hint stands on the Unload button of the item cockpit. */
  unloadHint: boolean;
  /** The hint stands on the sell button of the item cockpit. */
  sellHint: boolean;
}

export interface Violation {
  rule: string;
  atMillis: number;
  message: string;
}

/**
 * How long a rule may be broken before it counts. The tips poll at one second, so a correct chain
 * can be wrong for up to a second after the world changed (R6).
 */
const GRACE_MILLIS = 1500;
const STEP_MILLIS = 100;

/**
 * The tip test bed: the real TipService with its guide, SelectionService and ActionService running on
 * the fake world. Only renderer, worker, cockpit and network are faked.
 *
 * Must be used inside fakeAsync - run() advances the world and the timers of the tip code together.
 */
export class TipTestbed {
  readonly world = new World();
  readonly selection: SelectionService;
  readonly action: ActionService;
  readonly tipService: TipService;
  readonly cockpit: FakeItemCockpit;
  /** What the minimap would mark: the tip's target, as QuestMarkerService holds it. */
  readonly questMarker = new QuestMarkerService();
  readonly stallReports: any[] = [];
  readonly errors: string[] = [];
  readonly violations: Violation[] = [];
  /** Expectations of the case that did not hold; see check(). */
  readonly failures: string[] = [];
  /** How far {@link #checkRules} has read the stall reports (R8). */
  private checkedStallReports = 0;
  private readonly openViolations = new Map<string, { since: number, message: string, recorded: boolean }>();
  private readonly tipsVisible$ = new BehaviorSubject<boolean>(true);
  quest: TipQuest | null = null;
  questPassed = false;

  constructor() {
    const http = {request: () => of({})};
    const tracker = new FirstInteractionTrackerService(http as any);
    const stallTracker = new TipStallTrackerService(http as any, tracker);
    spyOn(stallTracker as any, 'report').and.callFake((json: any) => this.stallReports.push(json));
    spyOn(console, 'error').and.callFake((...args: any[]) => this.errors.push(args.map(String).join(' ')));
    const audio = {
      speakSelection: () => {
      }, speakCommand: () => {
      }
    };
    this.selection = new SelectionService(audio as any, tracker);
    this.world.renderer.selectionHooks = this.selection;
    const gwtAngularService = {gwtAngularFacade: this.createFacade()};
    this.cockpit = new FakeItemCockpit(this.world);
    this.cockpit.selection = this.selection;
    // First, like ItemCockpitService, which exists before any tip does.
    this.selection.addSelectionListener(() => this.cockpit.rebuild(this.selection.getSelectedOwnItems()));
    this.action = new ActionService(gwtAngularService as any, audio as any, this.selection, tracker);
    // Not setRendererService(): that also hooks the Escape key on the window, once per test.
    (this.action as any).rendererService = this.world.renderer;
    const uiSettings = {
      tipsVisible$: this.tipsVisible$,
      get tipsVisible() {
        return uiSettings.tipsVisible$.value;
      }
    };
    this.tipService = new TipService(this.world.renderer as any, gwtAngularService as any, this.selection,
      uiSettings as any, stallTracker, tracker, this.action, this.questMarker);
    this.tipService.setItemCockpit(this.cockpit as any);
    this.world.onQuestPassed = () => {
      this.questPassed = true;
      this.tipService.deactivate();
    };
  }

  // --- Scene and quest ----------------------------------------------------------------------------

  own(typeId: number, x: number, y: number): Unit {
    return this.world.addUnit(typeId, 'own', x, y);
  }

  bot(typeId: number, x: number, y: number, respawnDelayMillis: number | null = null): Unit {
    return this.world.addBotUnit(typeId, x, y, respawnDelayMillis);
  }

  /** Puts the world on screen as it is now. Call after setting the scene, before the quest. */
  lookAt(x: number, y: number): void {
    this.world.moveCamera(x, y);
  }

  lookAtUnit(unit: Unit): void {
    this.lookAt(unit.x, unit.y);
  }

  /**
   * Puts the camera so the unit lands at a given height in the picture: 0 is the top edge, 1 the
   * bottom. For the cases that ask what happens when a target is on screen but cannot carry a
   * prompt - behind the bottom HUD, or with no room for the label.
   */
  lookSoThat(unit: Unit, screenFraction: number): void {
    this.lookAt(unit.x, this.world.cameraYForScreenFraction(unit.y, screenFraction));
  }

  activateQuest(questId: number): void {
    const quest = QUESTS[questId];
    this.quest = quest;
    this.questPassed = false;
    this.world.setQuestCondition(quest.condition);
    try {
      this.tipService.activate(this.questConfig(quest) as any);
    } catch (error) {
      // QuestCockpitComponent catches this and logs a warning; the tip is dead from here on (R7).
      this.record('R7', `exception out of TipService.activate(): ${error}`);
    }
  }

  setTipsVisible(visible: boolean): void {
    this.tipsVisible$.next(visible);
  }

  // --- The player -----------------------------------------------------------------------------------

  /** A click on an item the player can see. Clicking something off screen is a mistake in the test. */
  click(target: Unit | { id: number }): void {
    const item = this.onScreenItem(target.id);
    item.click((itemType, id, diplomacy, babylonItem) =>
      this.action.onItemClicked(itemType, id, diplomacy, babylonItem));
  }

  /** A box around units on screen - the only way to select more than one. */
  boxSelect(...units: Unit[]): void {
    const items = units.map(unit => this.onScreenItem(unit.id) as FakeBaseItem);
    this.selection.selectOwnItems(items as any);
  }

  deselect(): void {
    this.selection.clearSelection();
  }

  clickTerrain(x: number, y: number): void {
    this.action.onTerrainClicked(x, y);
  }

  clickBuildButton(typeId: number): void {
    this.cockpit.click(typeId, this.world.renderer);
  }

  place(x: number, y: number): void {
    this.world.renderer.place(x, y);
  }

  clickUnload(): void {
    this.cockpit.clickUnload(this.world.renderer);
  }

  /** One tap on the sell button - the first arms it, the second sells. */
  /** The player taps the "go there" chip: the camera goes to the arrow's target, as setViewFieldCenter does. */
  clickJump(): void {
    const target = this.world.renderer.outOfViewTarget;
    if (!target) {
      throw new Error('There is no "go there" chip');
    }
    this.world.moveCamera(target.x, target.y);
  }

  clickSell(): void {
    this.cockpit.clickSell();
  }

  cancelPlacer(): void {
    this.world.renderer.cancelPlacer();
  }

  private onScreenItem(id: number): FakeBaseItem | FakeResourceItem {
    const item = [...this.world.renderer.liveBaseItems(), ...this.world.renderer.liveResourceItems()]
      .find(candidate => candidate.getId() === id);
    if (!item || !item.isOnScreen()) {
      throw new Error(`Test error: item ${id} is not on screen and cannot be clicked`);
    }
    return item;
  }

  // --- Time -----------------------------------------------------------------------------------------

  /** Lets the world and the tip code run for this long, checking the rules on every step. */
  run(millis: number): void {
    for (let elapsed = 0; elapsed < millis; elapsed += STEP_MILLIS) {
      this.world.step(STEP_MILLIS);
      try {
        tick(STEP_MILLIS);
      } catch (error) {
        // Thrown by a timer of the tip code. The timer is gone after this, which in the game means
        // the loop it drove is dead - a defect of the tips (R7), not an error of the case.
        this.record('R7', `exception out of a tip timer: ${error}`);
      }
      this.checkRules();
    }
  }

  /** Runs until the condition holds, failing the test when it does not within the limit. */
  runUntil(condition: () => boolean, limitMillis: number, what: string): void {
    for (let elapsed = 0; elapsed < limitMillis; elapsed += STEP_MILLIS) {
      if (condition()) {
        return;
      }
      this.run(STEP_MILLIS);
    }
    if (!condition()) {
      throw new Error(`Test error: ${what} did not happen within ${limitMillis} ms`);
    }
  }

  /** Stops the tip chain's timers so fakeAsync ends clean. */
  finish(): void {
    this.tipService.deactivate();
    tick(60000);
  }

  // --- Observation ------------------------------------------------------------------------------------

  view(): PlayerView {
    const renderer = this.world.renderer;
    const prompts: PlayerView['prompts'] = [];
    for (const item of renderer.liveBaseItems()) {
      const text = item.getPromptText();
      if (text !== null && item.promptReadable()) {
        prompts.push({text, itemId: item.getId(), typeId: item.itemType.getId()});
      }
    }
    for (const item of renderer.liveResourceItems()) {
      const text = item.getPromptText();
      if (text !== null && item.promptReadable()) {
        prompts.push({text, itemId: item.getId(), typeId: item.itemType.getId()});
      }
    }
    return {
      prompts,
      arrowAngle: renderer.outOfViewAngle,
      jumpOffered: renderer.outOfViewTarget !== null,
      cockpitHintTypeId: this.cockpit.hintTypeId,
      placeMarker: renderer.placeMarkerShown,
      placerActive: renderer.baseItemPlacerActive,
      groupAsked: renderer.touchSelectionMode.asked,
      unloadHint: this.cockpit.unloadHint,
      sellHint: this.cockpit.sellHint
    };
  }

  /** Whether the arrow on screen points at this spot, within 20 degrees. */
  arrowPointsAt(x: number, y: number): boolean {
    const angle = this.view().arrowAngle;
    if (angle === null) {
      return false;
    }
    return this.angleDifference(angle, this.angleTo(x, y)) < 20 * Math.PI / 180;
  }

  hasGuidance(view = this.view()): boolean {
    return view.prompts.length > 0 || view.arrowAngle !== null || view.cockpitHintTypeId !== null
      || view.placeMarker || view.placerActive || view.groupAsked || view.unloadHint || view.sellHint;
  }

  /** The one prompt on screen, if there is exactly one. */
  onlyPrompt(): PlayerView['prompts'][0] | null {
    const prompts = this.view().prompts;
    return prompts.length === 1 ? prompts[0] : null;
  }

  /** Whether exactly this prompt, and nothing else, is on screen. */
  showsOnly(text: string, itemId: number): boolean {
    const prompt = this.onlyPrompt();
    return prompt !== null && prompt.text === text && prompt.itemId === itemId;
  }

  /**
   * A soft expectation: collected rather than thrown, so a case can be run as a known defect and
   * still report everything that is wrong with it.
   */
  check(condition: boolean, message: string): void {
    if (!condition) {
      this.failures.push(`${message} (at ${this.world.time} ms, view: ${JSON.stringify(this.view())})`);
    }
  }

  /** Everything wrong with the case: its own expectations and the rules. */
  outcome(): string[] {
    return [...this.failures, ...this.violations.map(v => `${v.rule} at ${v.atMillis} ms: ${v.message}`)];
  }

  lastStallReason(): string | null {
    const last = this.stallReports[this.stallReports.length - 1];
    return last ? last.reason ?? null : null;
  }

  /** The reasons reported to the stall tracking, in order - what R8 is read from. */
  stallReasons(): string[] {
    return this.stallReports.filter(report => report.reason).map(report => report.reason);
  }

  // --- Rules R1-R7 (catalog section 1) ----------------------------------------------------------

  private checkRules(): void {
    const view = this.view();
    const quest = this.quest;
    const questOpen = quest !== null && !this.questPassed;
    const working = questOpen && this.actorIsWorking(quest!);
    const targetAbsent = questOpen && this.targetIsAbsent(quest!);

    this.rule('R1', questOpen && !working && !targetAbsent && !this.hasGuidance(view),
      'quest open, player can act, and nothing on screen tells him what to do');

    const selectedIds = new Set(this.selection.getSelectedOwnItemIds());
    const selectPromptOnSelected = view.prompts.find(prompt =>
      prompt.text === 'Click to select' && selectedIds.has(prompt.itemId));
    this.rule('R2', !!selectPromptOnSelected,
      `"Click to select" on item ${selectPromptOnSelected?.itemId}, which is selected`);

    const selectPrompt = view.prompts.find(prompt => prompt.text === 'Click to select');
    this.rule('R3', working && (selectPrompt !== undefined || view.arrowAngle !== null),
      `the actor is working and the tip shows ${selectPrompt ? '"Click to select"' : 'a direction arrow'}`);

    this.rule('R4', view.arrowAngle !== null && !this.arrowPointsAtSomething(view.arrowAngle),
      'the direction arrow points at nothing');

    this.rule('R5', view.prompts.length > 1,
      `${view.prompts.length} prompts at once: ${view.prompts.map(prompt => prompt.text).join(', ')}`);
    this.rule('R5', !questOpen && quest !== null && this.hasGuidance(view) && !view.placerActive,
      'quest is over and tip guidance is still on screen');

    if (this.errors.length > 0) {
      this.record('R7', `console.error: ${this.errors.join(' | ')}`);
      this.errors.length = 0;
    }

    // R8, the half of it that needs no case of its own: a tip that is doing its job never reports
    // itself as a restart loop. Every other reason is judged by the case, this one never holds.
    while (this.checkedStallReports < this.stallReports.length) {
      const report = this.stallReports[this.checkedStallReports++];
      if (report.reason === TipStallReason.CHAIN_THRASHING) {
        this.record('R8', `CHAIN_THRASHING reported on ${report.tipTaskName} after ${report.waitMillis} ms`);
      }
    }
  }

  /** Records a violation once it has lasted longer than the grace. */
  private rule(rule: string, broken: boolean, message: string): void {
    const key = rule + ':' + message.replace(/\d+/g, '#');
    const open = this.openViolations.get(key);
    if (!broken) {
      this.openViolations.delete(key);
      return;
    }
    if (!open) {
      this.openViolations.set(key, {since: this.world.time, message, recorded: false});
      return;
    }
    if (!open.recorded && this.world.time - open.since >= GRACE_MILLIS) {
      open.recorded = true;
      this.violations.push({rule, atMillis: open.since, message});
    }
  }

  private record(rule: string, message: string): void {
    this.violations.push({rule, atMillis: this.world.time, message});
  }

  /**
   * The units the quest is about are doing something for it, or have just been told to - R3
   * applies, R1 does not.
   */
  private actorIsWorking(quest: TipQuest): boolean {
    return [...this.world.units.values()].some(unit =>
      unit.owner === 'own' && (unit.spec.id === quest.actorTypeId || this.carriesActor(quest, unit)
        || (!!unit.spec.harvester && this.moneyShort(quest)))
      && (!this.world.isIdle(unit) || this.world.hasPendingOrder(unit.id)));
  }

  /** Too little Razarion for what the quest builds: a harvester at work works for the quest. */
  private moneyShort(quest: TipQuest): boolean {
    return (quest.tip === 'BUILD' || quest.tip === 'FABRICATE') && quest.typeCount !== null
      && this.world.razarion < itemTypeSpec(quest.typeCount.typeId).price;
  }

  /**
   * Crossing the water: the loaded container sails and unloads for the unit inside it, and a
   * dockyard building a container to replace a sunk one works for it too. So does a factory
   * building the actor itself, to replace one that was destroyed (2026-09-28).
   */
  private carriesActor(quest: TipQuest, unit: Unit): boolean {
    return unit.cargo.some(id => this.world.units.get(id)?.spec.id === quest.actorTypeId)
      || unit.queue.includes(quest.actorTypeId)
      || unit.queue.some(typeId => !!itemTypeSpec(typeId).container?.carries.includes(quest.actorTypeId));
  }

  /** No target on the whole planet: nothing to show until it is back (Q7). */
  private targetIsAbsent(quest: TipQuest): boolean {
    if (quest.tip === 'ATTACK') {
      return ![...this.world.units.values()].some(unit =>
        unit.owner === 'bot' && (quest.typeCount === null || unit.spec.id === quest.typeCount.typeId));
    }
    if (quest.tip === 'HARVEST') {
      return this.world.resources.size === 0;
    }
    return false;
  }

  /**
   * Anything the arrow could legitimately mean: own units, bot units, resources - and the build
   * region of a quest that has one, which R4 names explicitly ("a point inside a build region").
   */
  private arrowPointsAtSomething(angle: number): boolean {
    const region = this.quest?.region ?? [];
    const candidates: { x: number, y: number }[] = [
      ...[...this.world.units.values()].map(unit => ({x: unit.x, y: unit.y})),
      ...[...this.world.resources.values()].map(resource => ({x: resource.x, y: resource.y})),
      ...region,
      ...(region.length > 0 ? [{
        x: region.reduce((sum, corner) => sum + corner.x, 0) / region.length,
        y: region.reduce((sum, corner) => sum + corner.y, 0) / region.length
      }] : [])
    ];
    // Not onScreen: a target inside the view field but behind the HUD or in the band where the
    // label does not fit is exactly what the arrow is for, so it counts as something to point at.
    return candidates.some(candidate =>
      !this.world.promptReadable(candidate.x, candidate.y)
      && this.angleDifference(angle, this.angleTo(candidate.x, candidate.y)) < 20 * Math.PI / 180);
  }

  private angleTo(x: number, y: number): number {
    return this.world.viewField().getAngleTo({getX: () => x, getY: () => y} as any);
  }

  private angleDifference(one: number, other: number): number {
    const difference = Math.abs(one - other) % (2 * Math.PI);
    return Math.min(difference, 2 * Math.PI - difference);
  }

  // --- Fakes of the GWT facade ----------------------------------------------------------------

  private createFacade(): any {
    const world = this.world;
    let moveAck: (() => void) | null = null;
    return {
      baseItemUiService: {
        getResources: () => world.razarion,
        getNearestEnemyPosition: (x: number, y: number, typeId: number, typeIdUsed: boolean) => {
          let best: Unit | null = null;
          for (const unit of world.units.values()) {
            if (unit.owner !== 'bot' || (typeIdUsed && unit.spec.id !== typeId)) {
              continue;
            }
            if (!best || Math.hypot(unit.x - x, unit.y - y) < Math.hypot(best.x - x, best.y - y)) {
              best = unit;
            }
          }
          const found = best;
          return found ? {getX: () => found.x, getY: () => found.y} : null;
        },
        getMyItemCount: (typeId: number) => world.ownCount(typeId),
        // As BaseItemUiService.getTipItemStates(): every own item, the enemies of the given type.
        getTipItemStates: (enemyItemTypeId: number) => [...world.units.values()]
          .filter(unit => unit.containedIn === null)
          .filter(unit => unit.owner === 'own'
            || (enemyItemTypeId >= 0 && (enemyItemTypeId === 0 || unit.spec.id === enemyItemTypeId)))
          .map(unit => ({
            id: unit.id,
            itemTypeId: unit.spec.id,
            own: unit.owner === 'own',
            x: unit.x,
            y: unit.y,
            idle: world.isIdle(unit),
            buildup: unit.buildup,
            // The head is the unit in production. The engine keeps that one apart (toBeBuiltType)
            // and reports only what waits behind it - reporting the head hid a 15 s delay (FAB-06).
            factoryBuildQueue: unit.queue.slice(1),
            constructingTypeId: unit.queue[0] ?? 0,
            constructing: unit.queue.length > 0 ? Math.min(1, unit.queueProgress) : 0,
            cargo: unit.cargo.map(id => world.units.get(id)!.spec.id)
          }))
      },
      itemTypeService: {
        getBaseItemTypeAngular: (typeId: number) => fakeBaseItemType(typeId)
      },
      resourceUiService: {
        getNearestResourcePosition: (x: number, y: number) => {
          let best: { x: number, y: number } | null = null;
          for (const resource of world.resources.values()) {
            if (!best || Math.hypot(resource.x - x, resource.y - y) < Math.hypot(best.x - x, best.y - y)) {
              best = resource;
            }
          }
          const found = best;
          return found ? {getX: () => found.x, getY: () => found.y} : null;
        }
      },
      gameUiControl: {
        getMyLimitation4ItemType: (typeId: number) => world.itemLimits.get(typeId) ?? Number.MAX_SAFE_INTEGER,
        getColdGameUiContext: () => ({
          getInGameQuestVisualConfig: () => ({
            getRadius: () => 3,
            getNodesMaterialId: () => 1,
            getPlaceNodesMaterialId: () => 2,
            getOutOfViewNodesMaterialId: () => 12,
            getOutOfViewSize: () => 1,
            getOutOfViewDistanceFromCamera: () => 3
          })
        })
      },
      gameCommandService: {
        attackCmd: (ids: number[], targetId: number) => world.command(ids, {kind: 'attack', targetId}),
        harvestCmd: (ids: number[], resourceId: number) => world.command(ids, {kind: 'harvest', resourceId}),
        moveCmd: (ids: number[], x: number, y: number) => {
          world.command(ids, {kind: 'move', x, y});
          setTimeout(() => moveAck?.(), world.commandLatencyMillis);
        },
        finalizeBuildCmd: (ids: number[], siteId: number) => world.command(ids, {kind: 'finalize', siteId}),
        loadContainerCmd: (ids: number[], containerId: number) => world.command(ids, {kind: 'load', containerId}),
        setMoveCommandAckCallback: (callback: () => void) => moveAck = callback
      }
    };
  }

  private questConfig(quest: TipQuest): any {
    return {
      getId: () => quest.id,
      getInternalName: () => `quest ${quest.id}`,
      getTipConfig: () => ({
        getTipString: () => quest.tip,
        getActorItemTypeId: () => quest.actorTypeId,
        isGroup: () => !!quest.group
      }),
      getConditionConfig: () => ({
        getConditionTrigger: () => quest.trigger ?? null,
        getComparisonConfig: () => ({
          toTypeCountAngular: () => [...(quest.typeCount ? [quest.typeCount] : []), ...(quest.moreTypeCounts ?? [])]
            .map(typeCount => [typeCount.typeId, typeCount.count]),
          getPlaceConfig: () => quest.region ? {
            getPosition: () => null,
            toRadiusAngular: () => null,
            getPolygon2D: () => ({
              toCornersAngular: () => quest.region!.map(corner => ({getX: () => corner.x, getY: () => corner.y}))
            })
          } : null
        })
      })
    };
  }
}


