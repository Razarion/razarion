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
    GAME_SESSION_STARTED;
}
