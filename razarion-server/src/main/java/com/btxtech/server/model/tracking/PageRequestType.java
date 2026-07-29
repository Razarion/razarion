package com.btxtech.server.model.tracking;

public enum PageRequestType {
    /** The landing page was rendered and its tracking pixel loaded. */
    HOME,
    /**
     * "Play Now" was pressed on the landing page. Reported by the page itself rather than
     * inferred from the {@link #GAME} request that follows: without it, someone who never pressed
     * the button and someone who pressed it but never arrived at the game look exactly the same,
     * and those two call for opposite fixes.
     */
    HOME_PLAY_CLICKED,
    /**
     * The landing page was left, carrying how long it was open. Separates a visitor who was gone
     * within a second from one who read the page and decided against it.
     */
    HOME_EXIT,
    GAME
}
