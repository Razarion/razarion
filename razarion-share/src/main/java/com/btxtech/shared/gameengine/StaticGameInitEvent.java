package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;

/**
 * Created by Beat
 * 18.07.2016.
 */
public class StaticGameInitEvent {
    private StaticGameConfig staticGameConfig;

    public StaticGameInitEvent(StaticGameConfig staticGameConfig) {
        this.staticGameConfig = staticGameConfig;
    }

    public StaticGameConfig getStaticGameConfig() {
        return staticGameConfig;
    }
}
