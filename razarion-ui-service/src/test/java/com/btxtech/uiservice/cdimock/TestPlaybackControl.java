package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.uiservice.control.PlaybackControl;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * on 31.05.2017.
 */
@ApplicationScoped
public class TestPlaybackControl extends PlaybackControl {
    @Override
    protected void activatePlaybackMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void setCanvasPlaybackDimension(Index browserWindowDimension) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void displayMouseMove(Index position) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void displayMouseButton(int button, boolean down) {
        throw new UnsupportedOperationException();
    }
}
