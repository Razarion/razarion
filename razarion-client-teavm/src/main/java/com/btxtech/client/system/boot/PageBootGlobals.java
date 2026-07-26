package com.btxtech.client.system.boot;

/**
 * Names of the window globals the page and the WebAssembly client share while booting.
 * <p>
 * Startup tracking begins in index.html, long before this module exists: the page opens the
 * session, reports that it was loaded and installs the beacon that fires when the player leaves.
 * These globals are how the two halves stay one session - the page writes {@link #SESSION_UUID},
 * the client adopts it and keeps {@link #CURRENT_TASK} and {@link #TERMINATED} up to date so the
 * beacon can report where a startup was abandoned.
 * <p>
 * Keep in sync with the inline script in razarion-frontend/src/index.html and with
 * client-bootstrap.js.
 */
public final class PageBootGlobals {
    /**
     * Session id created by the page. Adopted by the boot on its first startup so the tasks
     * reported from the page and from the engine share one gameSessionUuid.
     */
    public static final String SESSION_UUID = "RAZ_gameSessionUuid";
    /**
     * Set once the boot has taken over {@link #SESSION_UUID}, so a warm restart knows it has to
     * make a fresh id instead of adopting the page's one again. The id itself must stay readable
     * for the whole life of the page: the beacon reads it when it fires, and an abort that
     * reports no session cannot be matched with anything.
     */
    public static final String SESSION_UUID_ADOPTED = "RAZ_gameSessionUuidAdopted";
    /**
     * Name of the startup task currently running. Read by the abort beacon to report where the
     * player left.
     */
    public static final String CURRENT_TASK = "RAZ_currentTask";
    /**
     * Set as soon as the startup terminated on its own, successfully or not. Silences the abort
     * beacon: leaving a running game is not an abandoned startup.
     */
    public static final String TERMINATED = "RAZ_startupTerminated";

    private PageBootGlobals() {
    }
}
