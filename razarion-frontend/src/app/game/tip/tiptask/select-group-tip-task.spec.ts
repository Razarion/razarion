import {fakeAsync, tick} from '@angular/core/testing';
import {SelectGroupTipTask} from './select-group-tip-task';
import {TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {TipConfig} from '../../../gwtangular/GwtAngularFacade';
import {TipStallReason} from '../tip-stall';

/**
 * The only task in the chain that asks for more than one unit.
 *
 * Quest 379 needs a group and nothing before it ever mentioned one: the refinery it asks for dies
 * to three shots from a single viper, but the tesla on the way out-ranges a viper (15 against 10)
 * and kills it in two hits. Measured on PROD over 21 days: the 52 players who failed killed 2403
 * teslas, lost 193 vipers, and destroyed no refinery at all.
 */
describe('SelectGroupTipTask', () => {
  const VIPER_TYPE_ID = 3;

  function viper(id: number) {
    return {
      getId: () => id,
      getBaseItemType: () => ({getId: () => VIPER_TYPE_ID})
    } as unknown as BabylonBaseItemImpl;
  }

  /** A unit of another type, to prove the count is per type and not per selection. */
  function harvester(id: number) {
    return {
      getId: () => id,
      getBaseItemType: () => ({getId: () => 2})
    } as unknown as BabylonBaseItemImpl;
  }

  function createTask(owned: number, selection: { current: BabylonBaseItemImpl[] },
                      sichtbar: { current: BabylonBaseItemImpl | null } = {current: viper(1)}) {
    const asked: boolean[] = [];
    const gemeldet: string[] = [];
    // setAsked, nicht asked.set: der Poll-Timer laeuft ausserhalb von Angulars Zone, und ein
    // direkt geschriebenes Signal blockiert die Kette korrekt, ohne dass die Zeile erscheint,
    // die dem Spieler den Grund nennt.
    const touchSelectionMode = {
      setAsked: (value: boolean) => asked.push(value)
    };
    const zielMarker: (string | null)[] = [];
    const renderService = {
      touchSelectionMode,
      getBabylonBaseItemById: () => null,
      // Sichtbar oder nicht - das entscheidet, ob der Richtungsmarker gesetzt wird.
      getBabylonBaseItemByDiplomacyItemType: () => sichtbar.current
    } as unknown as BabylonRenderServiceAccessImpl;
    const onSucceed = jasmine.createSpy('onSucceed');
    const tipService = {
      renderService,
      onSucceed,
      selectionService: {
        addSelectionListener: () => {
        },
        removeSelectionListener: () => {
        },
        getSelectedOwnItems: () => selection.current
      },
      gwtAngularFacade: {
        baseItemUiService: {getMyItemCount: () => owned}
      },
      setOutOfViewTarget: (p: any) => zielMarker.push(p === null ? null : "gesetzt"),
      firstInteractionTracker: {report: (_kind: string, detail?: string) => gemeldet.push(detail ?? '')}
    } as unknown as TipService;
    const tipConfig = {getActorItemTypeId: () => VIPER_TYPE_ID} as unknown as TipConfig;
    const context = new TipTaskContext(renderService);
    return {task: new SelectGroupTipTask(tipConfig, tipService, context), onSucceed, asked, gemeldet, zielMarker, context};
  }

  it('asks for the box while only one unit is held', fakeAsync(() => {
    const selection = {current: [viper(1)]};
    const {task, onSucceed, asked} = createTask(3, selection);

    task.start();

    expect(onSucceed).not.toHaveBeenCalled();
    expect(task.getStallReason()).toBe(TipStallReason.AWAIT_GROUP);
    // The icon bar is the only thing on screen that can point at the button.
    expect(asked[asked.length - 1]).toBeTrue();
    task.cleanup();
    tick(2000);
  }));

  it('is done as soon as a second unit joins the selection', fakeAsync(() => {
    const selection = {current: [viper(1)]};
    const {task, onSucceed, asked} = createTask(3, selection);

    task.start();
    expect(onSucceed).not.toHaveBeenCalled();

    selection.current = [viper(1), viper(2), viper(3)];
    tick(600);

    expect(onSucceed).toHaveBeenCalled();
    // The prompt has to go with the task, or it keeps asking for a group while the next tip asks
    // for something else.
    expect(asked[asked.length - 1]).toBeFalse();
    task.cleanup();
    tick(2000);
  }));

  it('counts the actor type, not the size of the selection', fakeAsync(() => {
    // A viper and a harvester in one box is not the group this quest is about.
    const selection = {current: [viper(1), harvester(7)]};
    const {task, onSucceed} = createTask(3, selection);

    task.start();

    expect(onSucceed).not.toHaveBeenCalled();
    expect(task.getStallReason()).toBe(TipStallReason.AWAIT_GROUP);
    task.cleanup();
    tick(2000);
  }));

  it('lets a player through who has nothing to group', fakeAsync(() => {
    // Two of the three vipers died on the way. Asking for a group of one is a tip that cannot be
    // obeyed, and standing on it would strand the player on the quest for good.
    const selection = {current: [viper(1)]};
    const {task, onSucceed, asked} = createTask(1, selection);

    task.start();

    expect(onSucceed).toHaveBeenCalled();
    expect(task.getStallReason()).toBe(TipStallReason.TOO_FEW_TO_GROUP);
    expect(asked).not.toContain(true);
    task.cleanup();
    tick(2000);
  }));

  it('counts what the player owns over the whole planet, not what is on screen', fakeAsync(() => {
    // getMyItemCount answers for the base, not the view field. Counting rendered instances instead
    // would call a player with three vipers off screen a player with nothing to group.
    const selection = {current: [viper(1)]};
    const {task, onSucceed} = createTask(3, selection);

    task.start();

    expect(onSucceed).not.toHaveBeenCalled();
    task.cleanup();
    tick(2000);
  }));

  /**
   * Without this the three group kinds cannot be read at all: a session with no group in it looks
   * identical whether the tip taught one and was ignored or never opened its mouth. The skip has no
   * other record anywhere - a task that succeeds at once never waits the thirty seconds the stall
   * watchdog needs before it looks.
   */
  it('says what it decided, both ways', fakeAsync(() => {
    const selection = {current: [viper(1)]};
    const {task, gemeldet} = createTask(3, selection);

    task.start();
    expect(gemeldet).toContain('state=asked');
    task.cleanup();
    tick(2000);

    const leer = {current: [viper(1)]};
    const zweite = createTask(1, leer);
    zweite.task.start();
    expect(zweite.gemeldet).toContain('state=skipped');
    zweite.task.cleanup();
    tick(2000);
  }));

  /**
   * A tip asking for a box around units the player cannot see, without saying where they are, is
   * the same silence the attack tip was fixed for the day before - repeated in new code. Measured
   * on PROD in the first twenty hours after it shipped: 0.36 stalls per player on quest 379
   * reporting ACTOR_OUT_OF_VIEW.
   */
  it('points at the units when they are off screen', fakeAsync(() => {
    const selection = {current: [viper(1)]};
    const sichtbar: { current: BabylonBaseItemImpl | null } = {current: null};
    const {task, zielMarker, context} = createTask(3, selection, sichtbar);
    context.rememberActorPosition({getX: () => 148, getY: () => 25} as any);

    task.start();

    expect(task.getStallReason()).toBe(TipStallReason.ACTOR_OUT_OF_VIEW);
    expect(zielMarker[zielMarker.length - 1]).toBe('gesetzt');
    task.cleanup();
    tick(2000);
  }));

  it('takes the marker down again once they are on screen', fakeAsync(() => {
    const selection = {current: [viper(1)]};
    const sichtbar: { current: BabylonBaseItemImpl | null } = {current: null};
    const {task, zielMarker, context} = createTask(3, selection, sichtbar);
    context.rememberActorPosition({getX: () => 148, getY: () => 25} as any);

    task.start();
    sichtbar.current = viper(1);
    tick(600);

    expect(task.getStallReason()).toBe(TipStallReason.AWAIT_GROUP);
    expect(zielMarker[zielMarker.length - 1]).toBeNull();
    task.cleanup();
    tick(2000);
  }));

  it('leaves no timer behind', fakeAsync(() => {
    const selection = {current: [viper(1)]};
    const {task, onSucceed} = createTask(3, selection);

    task.start();
    task.cleanup();
    selection.current = [viper(1), viper(2)];

    // A poll that outlives the task would succeed a task nobody is showing any more.
    tick(5000);
    expect(onSucceed).not.toHaveBeenCalled();
  }));
});
