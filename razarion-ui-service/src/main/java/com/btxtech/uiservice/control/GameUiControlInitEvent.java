package com.btxtech.uiservice.control;

import com.btxtech.shared.dto.ColdGameUiContext;

/**
 * Created by Beat
 * 04.01.2017.
 */
public class GameUiControlInitEvent {
    private ColdGameUiContext coldGameUiContext;

    public GameUiControlInitEvent(ColdGameUiContext coldGameUiContext) {
        this.coldGameUiContext = coldGameUiContext;
    }

    public ColdGameUiContext getColdGameUiContext() {
        return coldGameUiContext;
    }
}
