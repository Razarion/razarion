/**
 * Guard for the console-to-server forwarding installed in {@code AppComponent}.
 *
 * Without it a server outage turns into a feedback loop: the WASM client logs a websocket or
 * game-engine error every few milliseconds, every log line becomes an HTTP POST, every POST fails
 * against the dead server and the browser prints that failure to the console again. The tab ends up
 * spending its time on dead requests instead of on the health poll that would reload it.
 */
export class RemoteLogging {
  private static readonly WINDOW_MS = 10_000;
  private static readonly MAX_PER_WINDOW = 20;

  private static suspended = false;
  private static windowStart = 0;
  private static countInWindow = 0;

  /** Stop forwarding: the server is known to be gone, every POST would fail anyway. */
  static suspend(): void {
    RemoteLogging.suspended = true;
  }

  static resume(): void {
    RemoteLogging.suspended = false;
  }

  /** True if this log line may be sent to the server. */
  static allow(): boolean {
    if (RemoteLogging.suspended) {
      return false;
    }
    const now = Date.now();
    if (now - RemoteLogging.windowStart > RemoteLogging.WINDOW_MS) {
      RemoteLogging.windowStart = now;
      RemoteLogging.countInWindow = 0;
    }
    if (RemoteLogging.countInWindow >= RemoteLogging.MAX_PER_WINDOW) {
      return false;
    }
    RemoteLogging.countInWindow++;
    return true;
  }
}
