package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.tracking.DetailedTracking;
import com.btxtech.uiservice.control.PlaybackControl;

import java.util.Date;

/**
 * Created by Beat
 * on 04.06.2017.
 */
public class DevToolPlaybackControl extends PlaybackControl {
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
}
