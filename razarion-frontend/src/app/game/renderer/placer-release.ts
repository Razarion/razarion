/**
 * When the base item placer last closed. On a touch screen the tap that builds (the placer's
 * deploy button, drawn by Babylon on the canvas) is followed by the browser's emulated mouse
 * events, dispatched to whatever DOM element is under the finger once the tap is over. The
 * placer has closed by then and the item cockpit is back - and a build button that answers to
 * mousedown opens the placer again, for a building that is already going up (phone test,
 * 2026-09-25, quest 395).
 */
let closedAt = -Infinity;

export function markPlacerClosed(now = performance.now()): void {
  closedAt = now;
}

/** True for a moment after the placer closed: too soon for a press the player meant. */
export function placerJustClosed(now = performance.now()): boolean {
  return now - closedAt < PLACER_RELEASE_MILLIS;
}

export const PLACER_RELEASE_MILLIS = 700;
