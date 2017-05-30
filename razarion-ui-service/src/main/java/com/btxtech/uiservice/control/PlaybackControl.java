package com.btxtech.uiservice.control;


import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.tracking.CameraTracking;
import com.btxtech.shared.datatypes.tracking.TrackingContainer;
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
    private TrackingContainer trackingContainer;

    protected abstract void enterCanvasPlaybackMode();

    protected abstract void setCanvasPlaybackDimension(Index browserWindowDimension);

    public void start(PlaybackGameUiControlConfig playbackGameUiControlConfig) {
        lastAction = playbackGameUiControlConfig.getTrackingStart().getTimeStamp();
        trackingContainer = playbackGameUiControlConfig.getTrackingContainer();
        enterCanvasPlaybackMode();
        setCanvasPlaybackDimension(playbackGameUiControlConfig.getTrackingStart().getBrowserWindowDimension());
        scheduleNextAction();
    }

    private void scheduleNextAction() {
        if (trackingContainer.getCameraTrackings().isEmpty()) {
            finished();
            return;
        }
        CameraTracking cameraTracking = trackingContainer.getCameraTrackings().get(0);
        long timeToSleep = cameraTracking.getTimeStamp().getTime() - lastAction.getTime();
        if (timeToSleep < 0) {
            timeToSleep = 0;
        }
        simpleExecutorService.schedule(timeToSleep, this::executeAction, SimpleExecutorService.Type.UNSPECIFIED);
    }

    private void executeAction() {
        CameraTracking cameraTracking = trackingContainer.getCameraTrackings().remove(0);
        camera.setTranslateXY(cameraTracking.getPosition().getX(), cameraTracking.getPosition().getY());
        projectionTransformation.setFovY(cameraTracking.getFovY());
        lastAction = cameraTracking.getTimeStamp();
        scheduleNextAction();
    }

    private void finished() {
        logger.warning("******** Playback finished");
    }
}
