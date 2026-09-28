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
