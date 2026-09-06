import {createBootLatch, shouldStartWasmEarly, startWasmDownload} from './wasm-boot';

/**
 * The order in which the engine and Angular become ready.
 *
 * Both orders happen in the field and neither is under our control: the WebAssembly module is now
 * fetched before Angular is bootstrapped, so on a fast connection it can be compiled first, and on
 * a slow one Angular can be up long before it. Getting this wrong is invisible in a build and
 * fatal at runtime - the engine either calls into a facade that has no cockpit adapters yet, or
 * never gets called at all and the player sits on the splash screen forever.
 */
describe('WebAssembly boot latch', () => {
  it('starts the engine when Angular is the second to arrive', () => {
    const latch = createBootLatch();
    let started = 0;

    latch.wasmReady(() => started++);
    expect(started).withContext('the facade is not wired yet').toBe(0);

    latch.angularReady();
    expect(started).toBe(1);
  });

  it('starts the engine when the module is the second to arrive', () => {
    const latch = createBootLatch();
    let started = 0;

    latch.angularReady();
    expect(started).withContext('there is nothing to start yet').toBe(0);

    latch.wasmReady(() => started++);
    expect(started).toBe(1);
  });

  it('starts the engine once, however often it is told', () => {
    // A warm restart re-enters GameComponent, and the router can construct it more than once.
    const latch = createBootLatch();
    let started = 0;

    latch.wasmReady(() => started++);
    latch.angularReady();
    latch.angularReady();
    latch.angularReady();

    expect(started).toBe(1);
  });

  it('keeps two latches apart', () => {
    // Guards the factory itself: shared state would make the order of one page decide the other.
    const a = createBootLatch();
    const b = createBootLatch();
    let startedA = 0;
    let startedB = 0;

    a.wasmReady(() => startedA++);
    b.wasmReady(() => startedB++);
    a.angularReady();

    expect(startedA).toBe(1);
    expect(startedB).withContext('b was never told about Angular').toBe(0);
  });
});

describe('Early WebAssembly start', () => {
  it('leaves the admin backend alone', () => {
    // No game there, and 613 KB of engine nobody asked for.
    expect(shouldStartWasmEarly('/backend')).toBeFalse();
    expect(shouldStartWasmEarly('/backend/planets')).toBeFalse();
  });

  it('fetches for the game and the director route', () => {
    expect(shouldStartWasmEarly('/')).toBeTrue();
    expect(shouldStartWasmEarly('/director')).toBeTrue();
  });

  it('asks for the client once, not once per call', () => {
    // Called from main.ts and again from notifyAngularReady, because a client-side navigation out
    // of /backend never passed the first one. The check used to compare an absolute src against a
    // path and therefore never matched - a second tag would have run a second engine.
    const before = document.getElementsByTagName('script').length;

    startWasmDownload();
    startWasmDownload();
    startWasmDownload();

    const added = Array.from(document.getElementsByTagName('script'))
      .filter(s => s.src.indexOf('/teavm-client/client-bootstrap.js') >= 0);
    expect(added.length).toBe(1);
    expect(document.getElementsByTagName('script').length).toBe(before + 1);
    added.forEach(s => s.remove());
  });
});
