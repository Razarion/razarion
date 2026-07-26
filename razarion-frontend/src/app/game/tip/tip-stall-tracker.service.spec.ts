import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {provideHttpClient} from '@angular/common/http';
import {TipStallTrackerService} from './tip-stall-tracker.service';
import {TipStallReason, TipStallSource, TipTaskName} from './tip-stall';

describe('TipStallTrackerService', () => {
  const URL = '/rest/tracker/tipStall';
  let service: TipStallTrackerService;
  let httpTestingController: HttpTestingController;

  function source(reason: string = TipStallReason.ACTOR_NOT_FOUND): TipStallSource {
    return {
      getTaskName: () => TipTaskName.SELECT,
      getStallReason: () => reason
    };
  }

  /** Bodies of all tipStall posts so far; flushed so the promises settle. */
  function postedBodies(): any[] {
    const requests = httpTestingController.match(URL);
    return requests.map(request => {
      request.flush(null);
      return request.request.body;
    });
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(TipStallTrackerService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    service.stop();
    httpTestingController.verify();
  });

  it('says nothing while the task is inside its patience', fakeAsync(() => {
    service.taskStarted(358, source());
    tick(TipStallTrackerService.STALL_MILLIS - 1);
    expect(postedBodies().length).toBe(0);
    service.stop();
  }));

  it('reports quest, task and reason once the task overruns', fakeAsync(() => {
    service.taskStarted(358, source(TipStallReason.BUTTON_DISABLED));
    tick(TipStallTrackerService.STALL_MILLIS);

    const bodies = postedBodies();
    expect(bodies.length).toBe(1);
    expect(bodies[0].questId).toBe(358);
    expect(bodies[0].tipTaskName).toBe(TipTaskName.SELECT);
    // Pulled when the watchdog fires, so it names the state the player is actually stuck in.
    expect(bodies[0].reason).toBe(TipStallReason.BUTTON_DISABLED);
    expect(bodies[0].resolved).toBeFalse();
    expect(bodies[0].waitMillis).toBeGreaterThanOrEqual(TipStallTrackerService.STALL_MILLIS);
    expect(bodies[0].stallUuid).toBeTruthy();
    service.stop();
  }));

  it('reports the resolution against the same stall when the player gets past it', fakeAsync(() => {
    service.taskStarted(358, source());
    tick(TipStallTrackerService.STALL_MILLIS);
    const stall = postedBodies()[0];

    tick(4000);
    service.taskEnded(true);

    const resolution = postedBodies();
    expect(resolution.length).toBe(1);
    expect(resolution[0].stallUuid).toBe(stall.stallUuid);
    expect(resolution[0].resolved).toBeTrue();
    expect(resolution[0].resolvedMillis).toBeGreaterThanOrEqual(TipStallTrackerService.STALL_MILLIS + 4000);
  }));

  it('does not resolve a stall the chain backtracked out of', fakeAsync(() => {
    service.taskStarted(358, source());
    tick(TipStallTrackerService.STALL_MILLIS);
    expect(postedBodies().length).toBe(1);

    service.taskEnded(false);
    expect(postedBodies().length).toBe(0);
  }));

  it('reports nothing for a task that ended in time', fakeAsync(() => {
    service.taskStarted(358, source());
    tick(5000);
    service.taskEnded(true);
    tick(TipStallTrackerService.STALL_MILLIS);
    expect(postedBodies().length).toBe(0);
  }));

  it('drops the watch on the previous task when the next one starts', fakeAsync(() => {
    service.taskStarted(358, source());
    tick(TipStallTrackerService.STALL_MILLIS - 1000);
    // The next task gets the full patience of its own; the old timer must not fire for it.
    service.taskStarted(358, source());
    tick(1000);
    expect(postedBodies().length).toBe(0);

    tick(TipStallTrackerService.STALL_MILLIS - 1000);
    expect(postedBodies().length).toBe(1);
    service.stop();
  }));

  it('reports a chain that keeps failing and restarting', fakeAsync(() => {
    // Every restart arms a fresh watchdog, so a loop like this never reaches the stall timeout.
    // Without this check a tip that fails itself produces no record at all.
    for (let i = 0; i < TipStallTrackerService.THRASHING_FAILURES; i++) {
      service.taskStarted(358, source());
      tick(1500);
      service.taskEnded(false);
    }

    const bodies = postedBodies();
    expect(bodies.length).toBe(1);
    expect(bodies[0].reason).toBe(TipStallReason.CHAIN_THRASHING);
    expect(bodies[0].questId).toBe(358);

    // Once per burst, not once per backtrack.
    service.taskStarted(358, source());
    tick(1500);
    service.taskEnded(false);
    expect(postedBodies().length).toBe(0);
    service.stop();
  }));

  it('catches a loop pacing itself at the selection grace', fakeAsync(() => {
    // 1500 ms per turn is under 8 restarts per 10 s - the start-counting version never saw it,
    // and a build tip looping between "place" and "select" reported nothing at all.
    for (let i = 0; i < 3; i++) {
      service.taskStarted(358, source());
      tick(1500);
      service.taskEnded(false);
    }
    expect(postedBodies().length).toBe(1);
    service.stop();
  }));

  it('does not call a normally advancing chain a restart loop', fakeAsync(() => {
    for (let i = 0; i < TipStallTrackerService.THRASHING_FAILURES + 4; i++) {
      service.taskStarted(358, source());
      tick(3000);
      service.taskEnded(true);
    }
    expect(postedBodies().length).toBe(0);
  }));

  it('does not add up backtracks that are far apart', fakeAsync(() => {
    // A player who gives up on a step now and then is not a broken chain.
    for (let i = 0; i < 4; i++) {
      service.taskStarted(358, source());
      tick(8000);
      service.taskEnded(false);
    }
    expect(postedBodies().length).toBe(0);
    service.stop();
  }));

  it('reports a chain that threw instead of leaving it silent', fakeAsync(() => {
    // The chain runs on timers and renderer callbacks - an exception there reaches the console
    // and nothing else, while the player waits for a tip that is never coming back.
    service.taskStarted(358, source());
    tick(4000);
    service.reportChainError(358, 'onSucceed');

    const bodies = postedBodies();
    expect(bodies.length).toBe(1);
    expect(bodies[0].reason).toBe(TipStallReason.CHAIN_ERROR);
    expect(bodies[0].questId).toBe(358);

    // The watchdog of the dead task must not fire on top of it.
    tick(TipStallTrackerService.STALL_MILLIS);
    expect(postedBodies().length).toBe(0);
  }));

  it('blames nothing while the tab is in the background', fakeAsync(() => {
    spyOnProperty(document, 'visibilityState').and.returnValue('hidden');
    service.taskStarted(358, source());
    tick(TipStallTrackerService.STALL_MILLIS);
    expect(postedBodies().length).toBe(0);
    service.stop();
  }));
});
