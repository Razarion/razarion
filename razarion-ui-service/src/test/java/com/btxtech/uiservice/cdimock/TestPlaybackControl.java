package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.tracking.DetailedTracking;
import com.btxtech.uiservice.control.PlaybackControl;

import javax.enterprise.context.ApplicationScoped;
import java.util.Date;

/**
 * Created by Beat
 * on 31.05.2017.
 */
@ApplicationScoped
public class TestPlaybackControl extends PlaybackControl {
    @Override
    protected void activatePlaybackMode(Date startTimeStamp, DetailedTracking endDetailedTracking) {
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

    @Override
    protected void onNextAction(DetailedTracking nextDetailedTracking) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void onFinished() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void onSleeping(long timeToSleep) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void showPlaybackDialog(int identityHashCode, String title, int left, int top, int width, int height, int zIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void hidePlaybackDialog(int identityHashCode) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void onOnPause() {
        throw new UnsupportedOperationException();
    }
}
