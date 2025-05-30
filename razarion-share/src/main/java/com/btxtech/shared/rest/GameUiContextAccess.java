package com.btxtech.shared.rest;

import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.WarmGameUiContext;

public interface GameUiContextAccess {
    String PATH = "/game-ui-context-control";

    @Deprecated
    ColdGameUiContext loadColdGameUiContext();

    @Deprecated
    WarmGameUiContext loadWarmGameUiContext();
}
