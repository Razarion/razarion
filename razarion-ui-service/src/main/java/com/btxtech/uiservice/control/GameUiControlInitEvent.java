package com.btxtech.uiservice.control;

import com.btxtech.shared.dto.GameUiControlConfig;

/**
 * Created by Beat
 * 04.01.2017.
 */
public class GameUiControlInitEvent {
    private GameUiControlConfig gameUiControlConfig;

    public GameUiControlInitEvent(GameUiControlConfig gameUiControlConfig) {
        this.gameUiControlConfig = gameUiControlConfig;
    }

    public GameUiControlConfig getGameUiControlConfig() {
        return gameUiControlConfig;
    }
}
