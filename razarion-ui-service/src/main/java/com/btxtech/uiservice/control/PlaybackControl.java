package com.btxtech.uiservice.control;


import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.tracking.BrowserWindowTracking;
import com.btxtech.shared.datatypes.tracking.CameraTracking;
import com.btxtech.shared.datatypes.tracking.DetailedTracking;
import com.btxtech.shared.datatypes.tracking.MouseButtonTracking;
import com.btxtech.shared.datatypes.tracking.MouseMoveTracking;
import com.btxtech.shared.dto.PlaybackGameUiControlConfig;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;

import javax.inject.Inject;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 30.05.2017.
 */
public abstract class PlaybackControl {
    private Logger logger = Logger.getLogger(PlaybackControl.class.getName());
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    private Date lastAction;
    private TrackingContainerAccess trackingContainerAccess;
    private DetailedTracking nextDetailedTracking;

    protected abstract void activatePlaybackMode();

    protected abstract void setCanvasPlaybackDimension(Index browserWindowDimension);

    protected abstract void displayMouseMove(Index position);

    protected abstract void displayMouseButton(int button, boolean down);

    public void start(PlaybackGameUiControlConfig playbackGameUiControlConfig) {
        lastAction = playbackGameUiControlConfig.getTrackingStart().getTimeStamp();
        trackingContainerAccess = new TrackingContainerAccess(playbackGameUiControlConfig.getTrackingContainer());
        activatePlaybackMode();
        setCanvasPlaybackDimension(playbackGameUiControlConfig.getTrackingStart().getBrowserWindowDimension());
        scheduleNextAction();
    }

    private void scheduleNextAction() {
        if (trackingContainerAccess.isEmpty()) {
            finished();
            return;
        }
        nextDetailedTracking = trackingContainerAccess.removeNextDetailedTracking();
        long timeToSleep = nextDetailedTracking.getTimeStamp().getTime() - lastAction.getTime();
        if (timeToSleep < 0) {
            timeToSleep = 0;
        }
        simpleExecutorService.schedule(timeToSleep, this::executeAction, SimpleExecutorService.Type.UNSPECIFIED);
    }

    private void executeAction() {
        if (nextDetailedTracking instanceof CameraTracking) {
            CameraTracking cameraTracking = (CameraTracking) nextDetailedTracking;
            camera.setTranslateXY(cameraTracking.getPosition().getX(), cameraTracking.getPosition().getY());
            projectionTransformation.setFovY(cameraTracking.getFovY());
        } else if (nextDetailedTracking instanceof BrowserWindowTracking) {
            BrowserWindowTracking browserWindowTracking = (BrowserWindowTracking) nextDetailedTracking;
            setCanvasPlaybackDimension(browserWindowTracking.getDimension());
        } else if (nextDetailedTracking instanceof MouseMoveTracking) {
            MouseMoveTracking mouseMoveTracking = (MouseMoveTracking) nextDetailedTracking;
            displayMouseMove(mouseMoveTracking.getPosition());
        } else if (nextDetailedTracking instanceof MouseButtonTracking) {
            MouseButtonTracking mouseButtonTracking = (MouseButtonTracking) nextDetailedTracking;
            displayMouseButton(mouseButtonTracking.getButton(), mouseButtonTracking.isDown());
        } else {
            logger.severe("PlaybackControl.executeAction() can not handle: " + nextDetailedTracking + " class: " + nextDetailedTracking.getClass());
        }
        lastAction = nextDetailedTracking.getTimeStamp();
        scheduleNextAction();
    }

    private void finished() {
        logger.warning("******** Playback finished");
    }
}
