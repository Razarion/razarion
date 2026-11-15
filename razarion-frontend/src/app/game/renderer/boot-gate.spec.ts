import {isStartModelsGateOpen, markStartModelsReady, TERRAIN_READY_TASK, whenStartModelsReady, whenTerrainReady} from './boot-gate';

class FakeWindow extends EventTarget {
  RAZ_boot: unknown = {};
  RAZ_startModelsReady: boolean | undefined;
  RAZ_finishedTasks: string[] | undefined;

  finish(task: string): void {
    (this.RAZ_finishedTasks = this.RAZ_finishedTasks || []).push(task);
    this.dispatchEvent(new CustomEvent('raz-startup-task-finished', {detail: task}));
  }
}

async function isResolved(promise: Promise<void>): Promise<boolean> {
  let resolved = false;
  promise.then(() => resolved = true);
  await Promise.resolve();
  await Promise.resolve();
  return resolved;
}

describe('whenTerrainReady', () => {
  it('is open at once where no engine boots (studio, editor, mock)', async () => {
    const win = new FakeWindow();
    win.RAZ_boot = undefined;
    expect(await isResolved(whenTerrainReady(win))).toBeTrue();
  });

  it('is open at once when the terrain was reported before anybody asked', async () => {
    const win = new FakeWindow();
    win.finish(TERRAIN_READY_TASK);
    expect(await isResolved(whenTerrainReady(win))).toBeTrue();
  });

  it('holds until INIT_WORKER, not for the steps before it', async () => {
    const win = new FakeWindow();
    const gate = whenTerrainReady(win);
    win.finish('LOAD_THREE_JS_MODELS');
    expect(await isResolved(gate)).toBeFalse();
    win.finish(TERRAIN_READY_TASK);
    expect(await isResolved(gate)).toBeTrue();
  });

  it('opens after the timeout when the step never comes', async () => {
    jasmine.clock().install();
    try {
      const win = new FakeWindow();
      const gate = whenTerrainReady(win, 1000);
      jasmine.clock().tick(999);
      expect(await isResolved(gate)).toBeFalse();
      jasmine.clock().tick(1);
      expect(await isResolved(gate)).toBeTrue();
    } finally {
      jasmine.clock().uninstall();
    }
  });
});

describe('whenStartModelsReady', () => {
  it('is open at once where no engine boots (studio, editor, mock)', async () => {
    const win = new FakeWindow();
    win.RAZ_boot = undefined;
    expect(isStartModelsGateOpen(win)).toBeTrue();
    expect(await isResolved(whenStartModelsReady(win))).toBeTrue();
  });

  it('holds past the terrain until the start models are reported', async () => {
    const win = new FakeWindow();
    const gate = whenStartModelsReady(win);
    win.finish(TERRAIN_READY_TASK);
    expect(await isResolved(gate)).toBeFalse();
    markStartModelsReady(win);
    expect(await isResolved(gate)).toBeTrue();
    expect(isStartModelsGateOpen(win)).toBeTrue();
  });

  it('is open at once when the start models were reported before anybody asked', async () => {
    const win = new FakeWindow();
    markStartModelsReady(win);
    expect(await isResolved(whenStartModelsReady(win))).toBeTrue();
  });

  it('opens on its timeout, counted from the terrain, when the start models never come', async () => {
    jasmine.clock().install();
    try {
      const win = new FakeWindow();
      const gate = whenStartModelsReady(win, 1000);
      jasmine.clock().tick(5000);
      expect(await isResolved(gate)).toBeFalse();
      win.finish(TERRAIN_READY_TASK);
      await Promise.resolve();
      jasmine.clock().tick(999);
      expect(await isResolved(gate)).toBeFalse();
      jasmine.clock().tick(1);
      expect(await isResolved(gate)).toBeTrue();
    } finally {
      jasmine.clock().uninstall();
    }
  });
});
