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

/**
 * Once, when the next placer is confirmed - not when it is cancelled. The build button sets it to
 * put the builder down after placing (phone test, 2026-10-02): the builder drives to the site for
 * half a minute, still selected, and the next tap on the ground while the player waits was a move
 * order that threw the build away.
 */
let placedOnce: (() => void) | null = null;

export function onNextPlacement(callback: (() => void) | null): void {
  placedOnce = callback;
}

/** From the placer: confirmed (true) or closed without a placement (false). Either way it is spent. */
export function notifyPlacement(placed: boolean): void {
  const callback = placedOnce;
  placedOnce = null;
  if (placed && callback) {
    callback();
  }
}

/**
 * How to close the building placer that is open now, or null when none is - the start placer
 * cannot be closed and never sets it. A phone has no Escape key, and until 2026-10-05 a building
 * placer that was opened could only be built with or left open for good: the player in the phone
 * test selected something else, the builder went out of the selection and the placer stayed.
 */
let cancelOpen: (() => void) | null = null;

export function setOpenPlacerCancel(cancel: (() => void) | null): void {
  cancelOpen = cancel;
}

/** Closes the open building placer without building. False when there was none. */
export function cancelOpenPlacer(): boolean {
  const cancel = cancelOpen;
  cancelOpen = null;
  if (!cancel) {
    return false;
  }
  cancel();
  return true;
}
