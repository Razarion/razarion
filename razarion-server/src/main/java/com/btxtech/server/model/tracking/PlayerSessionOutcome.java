package com.btxtech.server.model.tracking;

/**
 * What became of one attempt to start the game.
 */
public enum PlayerSessionOutcome {
    /** The startup reported its own successful end. */
    SUCCESSFUL,
    /** It ran to its end but reported a failure. */
    FAILED,
    /**
     * Nobody ever reported an end: the player left, or a task waited forever for a callback. Where
     * it stopped is in lastTaskEnum.
     */
    ABORTED,
    /**
     * Still booting. No end reported yet, but the last task is too young to call it abandoned -
     * these rows change on the next reload.
     */
    RUNNING,
    /**
     * The game page was opened and not a single startup task ever arrived. The startup tracking
     * only begins to report once the first task runs, so a browser that dies before that - too old
     * for WebAssembly GC, for instance - is invisible in every other view.
     */
    NO_STARTUP
}
