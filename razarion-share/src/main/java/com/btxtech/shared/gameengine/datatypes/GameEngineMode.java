package com.btxtech.shared.gameengine.datatypes;

/**
 * Who runs the game engine: SLAVE is the browser, MASTER is the server.
 * <p>
 * That split is fixed today - there is exactly one GameUiContext and it is SLAVE. The browser used
 * to run as MASTER for the standalone tutorial, and a third mode PLAYBACK replayed a recorded
 * session to study how tutorial players moved. Neither is needed while players cannot attack each
 * other, so both are gone.
 * <p>
 * User: beat
 * Date: 29.10.2011
 * Time: 00:47:33
 */
public enum GameEngineMode {
    SLAVE,
    MASTER
}
