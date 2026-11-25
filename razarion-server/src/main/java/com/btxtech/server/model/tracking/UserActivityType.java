package com.btxtech.server.model.tracking;

public enum UserActivityType {
    BASE_CREATED,
    USER_CREATED,
    QUEST_PASSED,
    LEVEL_UP,
    /**
     * The only record that carries a user id and a game session uuid at the same time, and
     * therefore the only way to ask what a startup did after it finished. Startup tracking is
     * keyed on the session, everything the player then does is keyed on the user, and without
     * this row the two halves cannot be joined: "how many of the players who tabbed away while
     * loading never placed their base" has no answer.
     */
    GAME_SESSION_STARTED,
    /** "Save your progress" was put in front of the player; detail says where (e.g. LEVEL_9, MENU). */
    PROGRESS_SAVE_OFFERED,
    /** A save or sign-in link was mailed; detail SAVE, RESAVE or SIGN_IN. */
    PROGRESS_LINK_SENT,
    /** The player went off to Facebook from "Save progress". */
    PROGRESS_FACEBOOK_STARTED,
    /** Progress saved; detail EMAIL (link opened) or FACEBOOK. */
    PROGRESS_SAVED,
    /** A saved player continues in another browser; detail EMAIL or FACEBOOK. */
    PROGRESS_SIGN_IN,
    /** A sign over one of the player's units (EmoteService); detail the emote, ":REPLY" when it answered one. */
    EMOTE_SENT;
}
