export class SignalGenerator {
  static triangle(millis: number, durationMs: number, offsetMs: number): number {
    let totMillis = millis + offsetMs;
    let mod = totMillis % durationMs;
    if (mod < durationMs / 2) {
      // Raising
      return 2.0 * mod / durationMs;
    } else {
      // Falling
      return 2.0 - 2.0 * mod / durationMs;
    }
  }

  static sawtooth(millis: number, durationMs: number, offsetMs: number): number {
    if (durationMs == 0.0) {
      return 0;
    }
    let totMillis = millis + offsetMs;
    return (totMillis % durationMs) / durationMs; // Saegezahn
  }
}
