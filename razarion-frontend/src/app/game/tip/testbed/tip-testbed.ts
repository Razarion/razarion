import {tick} from '@angular/core/testing';
import {BehaviorSubject, of} from 'rxjs';
import {TipService} from '../tip.service';
import {TipStallTrackerService} from '../tip-stall-tracker.service';
import {SelectionService} from '../../selection.service';
import {ActionService} from '../../action.service';
import {FirstInteractionTrackerService} from '../../tracking/first-interaction-tracker.service';
import {FakeBaseItem, FakeItemCockpit, FakeResourceItem, QuestCondition, Unit, World} from './fake-world';
import {fakeBaseItemType, ItemTypeId} from './fake-item-types';

/** A quest with a tip, as in catalog section 3. */
export interface TipQuest {
  id: number;
  tip: 'BUILD' | 'FABRICATE' | 'HARVEST' | 'ATTACK';
  actorTypeId: number;
  /** The typed part of the condition, which is also what the tip reads its target from. */
  typeCount: { typeId: number, count: number } | null;
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
    condition: {kind: 'killed', typeId: ItemTypeId.BOT_HYDRA, count: 1}}
};

/** What is on the player's screen that a tip put there. */
export interface PlayerView {
  /** Prompts on items that are on screen. A prompt on a disposed or off-screen item is not seen. */
  prompts: { text: string, itemId: number, typeId: number }[];
  /** Direction arrow at the edge of the screen, as an angle; null when there is none. */
  arrowAngle: number | null;
  cockpitHintTypeId: number | null;
  placeMarker: boolean;
  placerActive: boolean;
  /** The group tip lights the selection-box button in the icon bar. */
  groupAsked: boolean;
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
  readonly stallReports: any[] = [];
  readonly errors: string[] = [];
  readonly violations: Violation[] = [];
  /** Expectations of the case that did not hold; see check(). */
  readonly failures: string[] = [];
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
      uiSettings as any, stallTracker, tracker, this.action);
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
      if (text !== null && item.isOnScreen()) {
        prompts.push({text, itemId: item.getId(), typeId: item.itemType.getId()});
      }
    }
    for (const item of renderer.liveResourceItems()) {
      const text = item.getPromptText();
      if (text !== null && item.isOnScreen()) {
        prompts.push({text, itemId: item.getId(), typeId: item.itemType.getId()});
      }
    }
    return {
      prompts,
      arrowAngle: renderer.outOfViewAngle,
      cockpitHintTypeId: this.cockpit.hintTypeId,
      placeMarker: renderer.placeMarkerShown,
      placerActive: renderer.baseItemPlacerActive,
      groupAsked: renderer.touchSelectionMode.asked
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
      || view.placeMarker || view.placerActive || view.groupAsked;
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
      unit.owner === 'own' && unit.spec.id === quest.actorTypeId
      && (!this.world.isIdle(unit) || this.world.hasPendingOrder(unit.id)));
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

  /** Anything the arrow could legitimately mean: own units, bot units, resources. */
  private arrowPointsAtSomething(angle: number): boolean {
    const candidates: { x: number, y: number }[] = [
      ...[...this.world.units.values()].map(unit => ({x: unit.x, y: unit.y})),
      ...[...this.world.resources.values()].map(resource => ({x: resource.x, y: resource.y}))
    ];
    return candidates.some(candidate =>
      !this.world.onScreen(candidate.x, candidate.y)
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
            factoryBuildQueue: [...unit.queue]
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
        loadContainerCmd: () => {
        },
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
        getComparisonConfig: () => ({
          toTypeCountAngular: () => quest.typeCount ? [[quest.typeCount.typeId, quest.typeCount.count]] : [],
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


