/**
 * Names and reasons reported when a quest tip stops making progress.
 *
 * Both are plain strings, not class names or enum members: they end up in the tracking database
 * and are compared across releases, and the production build minifies class names away.
 */

/** One per tip task class. Must stay stable - the tracking data is read by this name. */
export const TipTaskName = {
  SELECT: 'SELECT',
  START_BUILD_PLACER: 'START_BUILD_PLACER',
  SEND_BUILD_COMMAND: 'SEND_BUILD_COMMAND',
  SEND_FABRICATE_COMMAND: 'SEND_FABRICATE_COMMAND',
  SEND_HARVEST_COMMAND: 'SEND_HARVEST_COMMAND',
  SEND_ATTACK_COMMAND: 'SEND_ATTACK_COMMAND',
  IDLE_ITEM: 'IDLE_ITEM'
} as const;

/**
 * What a task is waiting for. A task sets its reason whenever it enters a waiting state; the
 * watchdog reads the current one when it fires.
 *
 * The distinction that matters when reading the data: reasons of the AWAIT_ family mean the tip
 * is up and doing its job and the player did not act, everything else means the tip itself could
 * not point at anything.
 */
export const TipStallReason = {
  /** Default before a task names anything more specific. */
  WAITING: 'WAITING',
  /**
   * Not a waiting state at all: the chain keeps failing and restarting. Reported by the tracker
   * itself, because a restart loop resets the watchdog and would otherwise report nothing ever.
   */
  CHAIN_THRASHING: 'CHAIN_THRASHING',
  /**
   * The chain threw. Everything in it runs on timers and renderer callbacks, so the exception
   * only reached the console while the player sat in front of a tip that was never coming back.
   */
  CHAIN_ERROR: 'CHAIN_ERROR',
  /**
   * The tip was waiting for a click while its actor was off screen. Beats every AWAIT_ reason:
   * a prompt nobody can see says nothing about whether the player wanted to act on it.
   */
  ACTOR_OUT_OF_VIEW: 'ACTOR_OUT_OF_VIEW',
  /** The item the tip wants selected does not exist in the renderer (yet). */
  ACTOR_NOT_FOUND: 'ACTOR_NOT_FOUND',
  AWAIT_SELECTION: 'AWAIT_SELECTION',
  /** Item cockpit not rendered yet - normal for a moment after selecting, a defect if it lasts. */
  COCKPIT_NOT_READY: 'COCKPIT_NOT_READY',
  /** The quest asks for something this actor cannot build at all. */
  NOT_BUILDABLE: 'NOT_BUILDABLE',
  /** Button is there but greyed out: item limit, house space or too little Razarion. */
  BUTTON_DISABLED: 'BUTTON_DISABLED',
  FACTORY_QUEUE_FULL: 'FACTORY_QUEUE_FULL',
  /** In the cockpit model but no DOM element carries it - carousel page not materialised. */
  BUTTON_NOT_RENDERED: 'BUTTON_NOT_RENDERED',
  /**
   * Compact layout only: the button is there and clickable, but the item panel is collapsed, so
   * nobody can see it. The tip points at the icon that opens the panel instead of at the button.
   */
  ITEM_PANEL_CLOSED: 'ITEM_PANEL_CLOSED',
  AWAIT_PLACER: 'AWAIT_PLACER',
  AWAIT_PLACEMENT: 'AWAIT_PLACEMENT',
  /** Placed, waiting for the server to put the construction site into the world. */
  AWAIT_BUILD_SITE: 'AWAIT_BUILD_SITE',
  AWAIT_BUILD_FINALIZE: 'AWAIT_BUILD_FINALIZE',
  /** The construction site the tip points at is off screen. */
  TARGET_OUT_OF_VIEW: 'TARGET_OUT_OF_VIEW',
  /** Site stands, buildup frozen: the builder left and the player has not sent it back. */
  BUILD_NOT_PROGRESSING: 'BUILD_NOT_PROGRESSING',
  AWAIT_FABRICATE_CLICK: 'AWAIT_FABRICATE_CLICK',
  /** No resource anywhere - neither on screen nor reported by the engine. */
  NO_RESOURCE: 'NO_RESOURCE',
  RESOURCE_OUT_OF_VIEW: 'RESOURCE_OUT_OF_VIEW',
  AWAIT_HARVEST_CLICK: 'AWAIT_HARVEST_CLICK',
  NO_ENEMY: 'NO_ENEMY',
  ENEMY_OUT_OF_VIEW: 'ENEMY_OUT_OF_VIEW',
  AWAIT_ATTACK_CLICK: 'AWAIT_ATTACK_CLICK',
  AWAIT_IDLE: 'AWAIT_IDLE'
} as const;

/**
 * What the stall watchdog needs from a tip task. Kept separate from AbstractTipTask so the
 * tracker does not import the task hierarchy that imports the tip service that owns the tracker.
 */
export interface TipStallSource {
  getTaskName(): string;

  getStallReason(): string;
}
