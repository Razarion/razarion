package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.dto.GameUiControlConfig;

import javax.ws.rs.core.NewCookie;

/**
 * Created by Beat
 * 21.04.2017.
 */
public class HttpConnectionEmu {
    public static final String SESSION_KEY = "JSESSIONID";
    private static HttpConnectionEmu INSTANCE;
    private GameUiControlConfig gameUiControlConfig;
    private NewCookie sessionCookie;

    public HttpConnectionEmu() {
        INSTANCE = this;
    }

    public GameUiControlConfig getGameUiControlConfig() {
        return gameUiControlConfig;
    }

    public void setGameUiControlConfig(GameUiControlConfig gameUiControlConfig) {
        this.gameUiControlConfig = gameUiControlConfig;
    }

    public void setSessionCookie(NewCookie sessionCookie) {
        this.sessionCookie = sessionCookie;
    }

    public NewCookie getSessionCookie() {
        return sessionCookie;
    }

    public static HttpConnectionEmu getInstance() {
        return INSTANCE;
    }
}
