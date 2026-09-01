import {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';

/**
 * The engine's own failures, on their way to the server instead of a console.
 *
 * The worker answers "could not build a tick" and logs it once, into the browser console. On a
 * phone in an in-app browser there is no console, and the tick stream is the only channel that
 * carries units, buildings and bots - so the game renders terrain, resources and the bot ground
 * area, never a single item, and nothing anywhere says why. That is the state the Meta cohort has
 * been in, and it is what this exists to end.
 *
 * Reaching into the prototype rather than constructing one: a real render service needs a scene, a
 * camera and the whole Babylon engine to say something this small.
 */
describe('Engine error report', () => {
  function serviceReporting(into: { kind: string, detail?: string }[],
                            report?: (kind: string, detail?: string) => void) {
    const service: any = Object.create(BabylonRenderServiceAccessImpl.prototype);
    service.firstInteractionTrackerService = {
      report: report ?? ((kind: string, detail?: string) => into.push({kind, detail}))
    };
    return service;
  }

  it('sends the reason, because the reason is what is missing', () => {
    const reported: { kind: string, detail?: string }[] = [];

    serviceReporting(reported).reportEngineError('tick update failed');

    expect(reported).toEqual([{kind: 'ENGINE_ERROR', detail: 'tick update failed'}]);
  });

  it('keeps a worker message that names its own origin', () => {
    const reported: { kind: string, detail?: string }[] = [];

    serviceReporting(reported).reportEngineError('worker: GameEngineWorker.onPostTick() failed null');

    expect(reported[0].detail).toBe('worker: GameEngineWorker.onPostTick() failed null');
  });

  it('caps a runaway message rather than posting a stack trace', () => {
    const reported: { kind: string, detail?: string }[] = [];

    serviceReporting(reported).reportEngineError('x'.repeat(5000));

    expect(reported[0].detail!.length).toBe(300);
  });

  it('says unknown rather than nothing when there is no reason', () => {
    const reported: { kind: string, detail?: string }[] = [];

    serviceReporting(reported).reportEngineError(null as unknown as string);

    expect(reported[0].detail).toBe('unknown');
  });

  it('never lets a failed report break the game it is reporting on', () => {
    const service = serviceReporting([], () => {
      throw new Error('tracker down');
    });

    expect(() => service.reportEngineError('tick update failed')).not.toThrow();
  });
});
