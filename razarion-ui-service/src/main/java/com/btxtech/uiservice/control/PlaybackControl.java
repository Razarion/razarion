package com.btxtech.uiservice.control;


import com.btxtech.shared.datatypes.tracking.CameraTracking;
import com.btxtech.shared.dto.PlaybackGameUiControlConfig;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 30.05.2017.
 */
@Singleton
public class PlaybackControl {
    private Logger logger = Logger.getLogger(PlaybackControl.class.getName());
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    private Date lastAction;
    private List<CameraTracking> cameraTrackings;

    public void start(PlaybackGameUiControlConfig playbackGameUiControlConfig) {
        lastAction = playbackGameUiControlConfig.getOriginTime();
        cameraTrackings = playbackGameUiControlConfig.getCameraTrackings();

        scheduleNextAction();
    }

    private void scheduleNextAction() {
        if (cameraTrackings.isEmpty()) {
            finished();
            return;
        }
        CameraTracking cameraTracking = cameraTrackings.get(0);
        long timeToSleep = cameraTracking.getTimeStamp().getTime() - lastAction.getTime();
        if (timeToSleep < 0) {
            timeToSleep = 0;
        }
        simpleExecutorService.schedule(timeToSleep, this::executeAction, SimpleExecutorService.Type.UNSPECIFIED);
    }

    private void executeAction() {
        CameraTracking cameraTracking = cameraTrackings.remove(0);
        camera.setTranslateXY(cameraTracking.getPosition().getX(), cameraTracking.getPosition().getY());
        projectionTransformation.setFovY(cameraTracking.getFovY());
        lastAction = cameraTracking.getTimeStamp();
        scheduleNextAction();
    }

    private void finished() {
        logger.warning("******** Playback finished");
    }
}
