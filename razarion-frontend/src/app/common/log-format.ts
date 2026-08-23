/**
 * Turns the arguments of a `console.warn`/`console.error` call into the one string that
 * {@code AppComponent} forwards to the server.
 *
 * It exists because `JSON.stringify` is the wrong tool for the argument that matters most. An
 * `Error` carries `name`, `message` and `stack` as non-enumerable properties, so
 * `JSON.stringify(new Error('boom'))` is `{}` - and Angular's own `ErrorHandler` logs exactly that
 * shape: `console.error('ERROR', error)`. Every uncaught error in the app therefore reached the
 * server log as the four characters `ERROR {}`, which says that something failed and nothing else.
 *
 * The second failure was quieter. `JSON.stringify` throws on a circular structure and on a
 * `BigInt`, and the forwarding in `AppComponent` is wrapped in a `try`/`catch` that drops the
 * whole record - so logging a Babylon object or a DOM node lost the line instead of mangling it.
 * Nothing in here throws.
 */

/**
 * Longest text taken from a single argument. A circular object used to cost the line and now
 * serializes, so without a cap one `console.error(scene)` would POST megabytes and fill the log
 * with one entry. Generous enough for a stack trace, which is what this is for.
 */
const MAX_ARG_CHARS = 4000;

export function formatLogArgs(args: readonly unknown[]): string {
  return args.map(formatLogArg).join(' ');
}

export function formatLogArg(value: unknown): string {
  return cap(describe(value));
}

function describe(value: unknown): string {
  if (typeof value === 'string') {
    return value;
  }
  // Not just for tidiness: `[undefined].join(' ')` is the empty string, so an undefined argument
  // used to vanish from the message rather than be reported as absent.
  if (value === undefined) {
    return 'undefined';
  }
  if (value === null) {
    return 'null';
  }
  if (typeof value === 'bigint') {
    return value.toString() + 'n';
  }
  if (typeof value !== 'object' && typeof value !== 'function') {
    return String(value);
  }
  if (typeof value === 'function') {
    return '[Function ' + (value.name || 'anonymous') + ']';
  }
  if (isErrorLike(value)) {
    return describeError(value);
  }
  // Checked before the generic Event below, both because it is the interesting one and because an
  // ErrorEvent is an Event.
  if (isErrorEvent(value)) {
    return describeErrorEvent(value);
  }
  if (isPromiseRejectionEvent(value)) {
    return 'Unhandled rejection: ' + describe(value.reason);
  }
  if (isEvent(value)) {
    return 'Event(' + String(value.type) + ')';
  }
  return safeStringify(value);
}

/**
 * Duck-typed rather than `instanceof Error`: an error thrown inside the worker and passed through
 * `postMessage`, or one crossing any other realm, is not an instance of this realm's `Error`.
 */
function isErrorLike(value: object): value is { name?: unknown, message?: unknown, stack?: unknown, cause?: unknown } {
  if (Object.prototype.toString.call(value) === '[object Error]') {
    return true;
  }
  const candidate = value as { name?: unknown, message?: unknown, stack?: unknown };
  return typeof candidate.name === 'string'
    && typeof candidate.message === 'string'
    && typeof candidate.stack === 'string';
}

function describeError(error: { name?: unknown, message?: unknown, stack?: unknown, cause?: unknown }): string {
  const name = typeof error.name === 'string' && error.name ? error.name : 'Error';
  const message = typeof error.message === 'string' ? error.message : '';
  const head = message ? name + ': ' + message : name;
  const stack = typeof error.stack === 'string' ? error.stack.trim() : '';

  // V8 and Firefox disagree about whether the stack repeats the head line. Print it once.
  let text = !stack ? head : (stack.startsWith(name) ? stack : head + '\n' + stack);

  // Where the useful part of an Angular error lives: HttpErrorResponse keeps status and url as
  // own enumerable properties, and they are outside everything read above.
  const extras = ownExtras(error);
  if (extras) {
    text += '\n - Details: ' + extras;
  }
  if (error.cause !== undefined && error.cause !== null) {
    text += '\n - Caused by: ' + describe(error.cause);
  }
  return text;
}

/**
 * The own enumerable properties of an error other than the ones already printed, or null when
 * there are none - which is the normal case and must not add an empty line.
 */
function ownExtras(error: object): string | null {
  const rest: Record<string, unknown> = {};
  let found = false;
  for (const key of Object.keys(error)) {
    if (key === 'name' || key === 'message' || key === 'stack' || key === 'cause') {
      continue;
    }
    rest[key] = (error as Record<string, unknown>)[key];
    found = true;
  }
  return found ? safeStringify(rest) : null;
}

function describeErrorEvent(event: { message?: unknown, filename?: unknown, lineno?: unknown, colno?: unknown, error?: unknown }): string {
  const where = typeof event.filename === 'string' && event.filename
    ? ' (' + event.filename + ':' + String(event.lineno ?? '?') + ':' + String(event.colno ?? '?') + ')'
    : '';
  const text = (typeof event.message === 'string' ? event.message : 'ErrorEvent') + where;
  // The event's message is a one-liner; the error behind it is the one with the stack.
  return event.error != null ? text + '\n' + describe(event.error) : text;
}

function isErrorEvent(value: object): value is { message?: unknown, filename?: unknown, lineno?: unknown, colno?: unknown, error?: unknown } {
  const candidate = value as { message?: unknown, filename?: unknown, lineno?: unknown };
  return typeof candidate.message === 'string'
    && (typeof candidate.filename === 'string' || typeof candidate.lineno === 'number');
}

function isPromiseRejectionEvent(value: object): value is { reason: unknown } {
  return 'reason' in value && 'promise' in value;
}

function isEvent(value: object): value is { type: unknown } {
  const candidate = value as { type?: unknown };
  return typeof candidate.type === 'string' && 'target' in value;
}

/**
 * `JSON.stringify` with the three things that make it throw or lie taken out: cycles, `BigInt`,
 * and a nested error serializing as `{}` for the same reason the top-level one did.
 */
function safeStringify(value: unknown): string {
  const seen = new WeakSet<object>();
  try {
    const json = JSON.stringify(value, function (this: unknown, _key: string, current: unknown) {
      if (typeof current === 'bigint') {
        return current.toString() + 'n';
      }
      if (typeof current === 'function') {
        return '[Function ' + (current.name || 'anonymous') + ']';
      }
      if (typeof current !== 'object' || current === null) {
        return current;
      }
      if (seen.has(current)) {
        return '[Circular]';
      }
      seen.add(current);
      // Reached through a property rather than as an argument, so describe() never saw it.
      if (isErrorLike(current)) {
        return describeError(current);
      }
      return current;
    });
    // undefined comes back for a value JSON has no representation for at the top level.
    return json ?? String(value);
  } catch (e) {
    // A property getter that throws, or a Proxy that refuses to be read. Say so rather than
    // losing the record: the other arguments of the call may still carry the answer.
    try {
      return String(value);
    } catch {
      return '[unserializable]';
    }
  }
}

function cap(text: string): string {
  return text.length <= MAX_ARG_CHARS
    ? text
    : text.slice(0, MAX_ARG_CHARS) + '… [' + (text.length - MAX_ARG_CHARS) + ' more chars]';
}
