package com.btxtech.uiservice.storyboard;

import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;

/**
 * Created by Beat
 * 02.01.2017.
 */
public interface GameEngineControl {
    public void initialise(GameEngineConfig gameEngineConfig);

    public void start();
}
