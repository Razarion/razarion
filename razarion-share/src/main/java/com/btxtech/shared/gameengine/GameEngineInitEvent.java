package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;

/**
 * Created by Beat
 * 18.07.2016.
 */
public class GameEngineInitEvent {
    private GameEngineConfig gameEngineConfig;

    public GameEngineInitEvent(GameEngineConfig gameEngineConfig) {
        this.gameEngineConfig = gameEngineConfig;
    }

    public GameEngineConfig getGameEngineConfig() {
        return gameEngineConfig;
    }
}
