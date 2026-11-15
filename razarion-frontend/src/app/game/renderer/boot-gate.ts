/**
 * Holds downloads the start does not wait for until the engine has its terrain.
 *
 * A phone's start is bandwidth-bound. INIT_WORKER, the step that releases the game, waits for the
 * terrain shape and the height map, 3.1 MB that the worker asks for only once it runs - and they
 * used to share the line with the 3.8 MB glb, the 1 MB environment texture and the particle
 * systems, none of which the first frame waits for. Chrome against PROD with a phone's network
 * (150 ms, 5 Mbit/s, CPU x4), 2026-09-27: RUN_GAME 22.1 s as shipped, 15.7-16.2 s with those held
 * until INIT_WORKER; at 1.6 Mbit/s 65 s against 44 s. The glb arrived equally late either way -
 * it already came about eleven seconds after the start.
 * <p>
 * Open at once where there is no engine start to wait for: the studio, the editors and the mock
 * build use the same renderer without one, and would otherwise wait out the timeout.
 */
const FINISHED_EVENT = 'raz-startup-task-finished';
/** The task the terrain belongs to; see TeaVMClientTrackerService.publishTaskFinished. */
export const TERRAIN_READY_TASK = 'INIT_WORKER';
/** Longer than any start we measured waits for INIT_WORKER on a slow line, as long as the boot's own timeout. */
export const BOOT_GATE_TIMEOUT_MILLIS = 60000;

/**
 * The second gate, after the terrain: the start models - builder, factory, harvester, crystals.
 * <p>
 * Measured 2026-11-15, Chrome against PROD with a phone's network (150 ms, 5 Mbit/s, CPU x4): the
 * 1.1 MB start glb and the two materials it paints itself with started at RUN_GAME and were usable
 * 14.4 s later, because they shared the line with 5.5 MB nobody needed in those seconds - the
 * environment map, particle systems, explosion and lightning sheets, the minimap, bot ground and
 * foam textures. Players put their base down about 7 s after RUN_GAME, so in 68% of the starts the
 * builder they had just placed was invisible (PLACER_CONFIRMED unit=0). With those held back until
 * the start glb was in: 8-9 s; at 2 Mbit/s 37 s against 18 s.
 * <p>
 * Opens when {@link markStartModelsReady} is called, and in any case {@link START_MODELS_TIMEOUT_MILLIS}
 * after the terrain gate opened - effects held back for good would be worse than a late builder.
 */
const START_MODELS_EVENT = 'raz-start-models-ready';
export const START_MODELS_TIMEOUT_MILLIS = 20000;

/** Whether what waits for the start models may load right now. */
export function isStartModelsGateOpen(win: any = window): boolean {
  return !win.RAZ_boot || !!win.RAZ_startModelsReady;
}

/** The start glb is usable (or failed - that is an answer too). Idempotent. */
export function markStartModelsReady(win: any = window): void {
  if (win.RAZ_startModelsReady) {
    return;
  }
  win.RAZ_startModelsReady = true;
  win.dispatchEvent(new CustomEvent(START_MODELS_EVENT));
}

export function whenStartModelsReady(win: any = window, timeoutMillis: number = START_MODELS_TIMEOUT_MILLIS): Promise<void> {
  if (isStartModelsGateOpen(win)) {
    return Promise.resolve();
  }
  return whenTerrainReady(win).then(() => new Promise<void>(resolve => {
    if (isStartModelsGateOpen(win)) {
      resolve();
      return;
    }
    const done = () => {
      win.removeEventListener(START_MODELS_EVENT, done);
      clearTimeout(timer);
      resolve();
    };
    const timer = setTimeout(done, timeoutMillis);
    win.addEventListener(START_MODELS_EVENT, done);
  }));
}

export function whenTerrainReady(win: any = window, timeoutMillis: number = BOOT_GATE_TIMEOUT_MILLIS): Promise<void> {
  return new Promise<void>(resolve => {
    // No engine booting on this page (studio, editor, mock): nothing to make room for.
    if (!win.RAZ_boot) {
      resolve();
      return;
    }
    const finished: string[] | undefined = win.RAZ_finishedTasks;
    if (finished && finished.indexOf(TERRAIN_READY_TASK) >= 0) {
      resolve();
      return;
    }
    let timer: any = null;
    const listener = (event: Event) => {
      if ((event as CustomEvent).detail === TERRAIN_READY_TASK) {
        done();
      }
    };
    const done = () => {
      win.removeEventListener(FINISHED_EVENT, listener);
      if (timer !== null) {
        clearTimeout(timer);
      }
      resolve();
    };
    win.addEventListener(FINISHED_EVENT, listener);
    // Never withheld for good: a start that fails or renames the step still gets its models.
    timer = setTimeout(done, timeoutMillis);
  });
}
