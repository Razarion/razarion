import {formatLogArgs} from './log-format';

/**
 * The cases covered are the ones that silently cost information before: an Error that stringified
 * to {}, and the arguments that made JSON.stringify throw, which dropped the whole record because
 * the forwarding in AppComponent catches and ignores.
 */
describe('formatLogArgs', () => {

  /** What Angular's own ErrorHandler does: console.error('ERROR', error). */
  it('reports an Error instead of an empty object', () => {
    const error = new Error('boom');
    error.stack = 'Error: boom\n    at Planet.tick (planet.ts:42:7)';

    const message = formatLogArgs(['ERROR', error]);

    expect(message).toContain('ERROR');
    expect(message).toContain('Error: boom');
    expect(message).toContain('Planet.tick');
    expect(message).not.toContain('{}');
  });

  it('does not repeat the head line the stack already carries', () => {
    const error = new Error('boom');
    error.stack = 'Error: boom\n    at one (a.ts:1:1)';

    expect(formatLogArgs([error])).toBe('Error: boom\n    at one (a.ts:1:1)');
  });

  it('prints head plus stack where the stack omits the head', () => {
    const error = new Error('boom');
    error.stack = 'one@a.ts:1:1';

    expect(formatLogArgs([error])).toBe('Error: boom\none@a.ts:1:1');
  });

  /**
   * An error thrown in the worker and handed over by postMessage is not an instance of this
   * realm's Error, which is why the check is duck-typed.
   */
  it('reports a cross-realm error', () => {
    const foreign = {name: 'TypeError', message: 'not a function', stack: 'TypeError: not a function\n    at x'};

    expect(formatLogArgs([foreign])).toContain('TypeError: not a function');
  });

  it('keeps the own properties an Angular HttpErrorResponse carries', () => {
    const error = Object.assign(new Error('Http failure'), {status: 502, url: '/rest/tracker/tipStall'});
    error.stack = 'Error: Http failure\n    at http';

    const message = formatLogArgs([error]);

    expect(message).toContain('502');
    expect(message).toContain('/rest/tracker/tipStall');
  });

  it('follows the cause chain', () => {
    const root = new Error('socket closed');
    root.stack = 'Error: socket closed';
    const error = new Error('command lost', {cause: root});
    error.stack = 'Error: command lost';

    const message = formatLogArgs([error]);

    expect(message).toContain('command lost');
    expect(message).toContain('Caused by:');
    expect(message).toContain('socket closed');
  });

  it('serializes a circular structure rather than losing the line', () => {
    const node: any = {id: 7};
    node.parent = node;

    const message = formatLogArgs(['scene', node]);

    expect(message).toContain('"id":7');
    expect(message).toContain('[Circular]');
  });

  it('reports an error nested in a plain object', () => {
    const message = formatLogArgs([{step: 'INIT_WORKER', error: new Error('worker gone')}]);

    expect(message).toContain('INIT_WORKER');
    expect(message).toContain('worker gone');
  });

  it('does not throw on a BigInt', () => {
    expect(formatLogArgs(['count', 9007199254740993n])).toBe('count 9007199254740993n');
    expect(formatLogArgs([{ticks: 12n}])).toContain('12n');
  });

  it('names an absent argument instead of dropping it', () => {
    expect(formatLogArgs(['id', undefined, null])).toBe('id undefined null');
  });

  it('survives a property that throws when read', () => {
    const hostile = {
      get boom(): string {
        throw new Error('no');
      }
    };

    expect(() => formatLogArgs([hostile])).not.toThrow();
    expect(formatLogArgs([hostile]).length).toBeGreaterThan(0);
  });

  it('reports an ErrorEvent with the error behind it', () => {
    const inner = new Error('script failed');
    inner.stack = 'Error: script failed\n    at load';
    const event = {message: 'Uncaught', filename: '/game/main.js', lineno: 12, colno: 3, error: inner};

    const message = formatLogArgs([event]);

    expect(message).toContain('Uncaught (/game/main.js:12:3)');
    expect(message).toContain('script failed');
  });

  it('caps a huge argument so one call cannot fill the log', () => {
    const message = formatLogArgs([{blob: 'x'.repeat(20_000)}]);

    expect(message.length).toBeLessThan(4200);
    expect(message).toContain('more chars');
  });
});
