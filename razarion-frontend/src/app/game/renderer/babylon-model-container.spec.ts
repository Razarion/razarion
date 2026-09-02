import {Scene} from '@babylonjs/core';
import {BabylonModelContainer} from './babylon-model-container';
import {BabylonModelService} from './babylon-model.service';
import {BaseEntity} from '../../generated/razarion-share';

/**
 * When the game is allowed to start.
 *
 * The container used to hold the door for its whole set, which cost a first-time player 9.5 MB of
 * waiting - every material and every particle system, the largest a 4.8 MB vehicle material for a
 * player who owns no vehicles. Now only a named subset holds it. Two ways to get that wrong: open
 * too early and the ground renders red, never open and everybody stays on the splash screen
 * forever. The second is the one a failed download would cause, so it is tested explicitly.
 */
describe('Model container start gate', () => {
  class TestContainer extends BabylonModelContainer<BaseEntity, string> {
    started: number[] = [];
    private pendingEntities = new Map<number, BaseEntity>();

    protected loadBabylonModel(entity: BaseEntity): void {
      this.started.push(entity.id);
      this.pendingEntities.set(entity.id, entity);
    }

    /** The load finished and produced something. */
    finish(id: number): void {
      this.setBabylonModel(this.pendingEntities.get(id)!, 'model-' + id);
      this.handleBabylonModelLaded(id);
    }

    /** The load finished and produced nothing - a 404, a parse error. */
    fail(id: number): void {
      this.handleBabylonModelLaded(id);
    }
  }

  let container: TestContainer;
  let opened: number;

  function load(ids: number[]): void {
    container.load(ids.map(id => ({id}) as BaseEntity),
      {handleLoaded: () => opened++} as unknown as BabylonModelService,
      undefined as unknown as Scene);
  }

  beforeEach(() => {
    container = new TestContainer();
    opened = 0;
  });

  it('opens once the required ones are in, with the rest still loading', () => {
    container.setRequired([1, 2]);
    load([1, 2, 3, 4]);

    container.finish(1);
    expect(container.isStartRequirementMet()).toBeFalse();

    container.finish(2);
    expect(container.isStartRequirementMet()).toBeTrue();
    expect(opened).toBe(1);
    // The set as a whole is not done, and that is the point.
    expect(container.isLoaded()).toBeFalse();
  });

  it('opens on a required load that failed, rather than never', () => {
    // A material that 404s must not keep every player on the splash screen. Wrong beats invisible.
    container.setRequired([1]);
    load([1, 2]);

    container.fail(1);

    expect(container.isStartRequirementMet()).toBeTrue();
    expect(opened).toBe(1);
  });

  it('loads what the gate waits for first', () => {
    // Without this the required ones sit behind whatever order the content happened to have -
    // which is the wait this whole change exists to remove.
    container.setRequired([5]);
    load([1, 2, 3, 4, 5]);

    expect(container.started[0]).toBe(5);
  });

  it('opens immediately when nothing is required', () => {
    // The particle systems: both are effects, neither belongs in front of the first frame.
    container.setRequired([]);
    load([1, 2]);

    expect(container.isStartRequirementMet()).toBeTrue();
    expect(opened).toBe(1);
  });

  it('waits for the whole set when no subset was named', () => {
    // The old behaviour, kept for any container that has not been thought about.
    load([1, 2]);

    container.finish(1);
    expect(container.isStartRequirementMet()).toBeFalse();

    container.finish(2);
    expect(container.isStartRequirementMet()).toBeTrue();
  });

  it('wakes a waiter for one entity and reports whether it arrived', () => {
    // What a glb model now uses to wait for its own materials.
    container.setRequired([]);
    load([1, 2]);
    const seen: boolean[] = [];
    container.whenEntityLoaded(1, loaded => seen.push(loaded));
    container.whenEntityLoaded(2, loaded => seen.push(loaded));

    container.finish(1);
    container.fail(2);

    expect(seen).toEqual([true, false]);
  });
});
