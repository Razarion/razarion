package com.btxtech.client;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.control.PlaybackControl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 31.05.2017.
 */
@ApplicationScoped
public class ClientPlaybackControl extends PlaybackControl {
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    protected void enterCanvasPlaybackMode() {
        gameCanvas.enterPlaybackMode();
    }

    @Override
    protected void setCanvasPlaybackDimension(Index browserWindowDimension) {
        try {
            gameCanvas.setPlaybackDimension(browserWindowDimension);
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }
}
