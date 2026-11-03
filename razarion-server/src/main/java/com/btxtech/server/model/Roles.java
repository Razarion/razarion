package com.btxtech.server.model;

public final class Roles {

    public static final String ADMIN = "ADMIN";
    /**
     * The game recorder (razarion-recorder): watches the planet over the game connection and reads
     * the history feed, nothing else. One account, named by razarion.recorder.email.
     */
    public static final String RECORDER = "RECORDER";

    private Roles() {
    }
}
