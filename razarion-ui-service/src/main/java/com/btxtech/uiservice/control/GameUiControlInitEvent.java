package com.btxtech.uiservice.control;

import com.btxtech.shared.dto.ColdGameUiControlConfig;

/**
 * Created by Beat
 * 04.01.2017.
 */
public class GameUiControlInitEvent {
    private ColdGameUiControlConfig coldGameUiControlConfig;

    public GameUiControlInitEvent(ColdGameUiControlConfig coldGameUiControlConfig) {
        this.coldGameUiControlConfig = coldGameUiControlConfig;
    }

    public ColdGameUiControlConfig getColdGameUiControlConfig() {
        return coldGameUiControlConfig;
    }
}
