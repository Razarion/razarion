package com.btxtech.client;

import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.datatypes.tracking.DetailedTracking;
import com.btxtech.shared.datatypes.tracking.MouseButtonTracking;
import com.btxtech.shared.datatypes.tracking.MouseMoveTracking;
import com.btxtech.shared.datatypes.tracking.SelectionTracking;
import com.btxtech.shared.datatypes.tracking.TrackingContainer;
import com.btxtech.shared.datatypes.tracking.TrackingStart;
import com.btxtech.shared.dto.GameUiControlTrackerInfo;
import com.btxtech.shared.dto.SceneTrackerInfo;
import com.btxtech.shared.dto.StartupTaskJson;
import com.btxtech.shared.dto.StartupTerminatedJson;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.rest.TrackerControllerFactory;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.SelectionEventService;
import com.btxtech.uiservice.TrackerService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.StartupProgressListener;
import com.btxtech.uiservice.system.boot.StartupTaskInfo;
import com.google.gwt.user.client.Window;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.MouseEvent;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 03.03.2017.
 */
@Singleton
public class ClientTrackerService implements TrackerService, StartupProgressListener {
    private static final String WINDOW_CLOSE = "Window closed -> move to DB";
    private static final String START_UUID = "uuid";
    private static final int DETAILED_TRACKING_DELAY = 1000 * 10;
    private final Logger logger = Logger.getLogger(ClientTrackerService.class.getName());
    private final Provider<Boot> boot;
    private final SimpleExecutorService simpleExecutorService;
    private TrackingContainer trackingContainer;
    private boolean detailedTracking = false;
    private SimpleScheduledFuture detailedTrackingFuture;

    @Inject
    public ClientTrackerService(SimpleExecutorService simpleExecutorService,
                                Provider<Boot> boot,
                                SelectionEventService selectionEventService) {
        this.simpleExecutorService = simpleExecutorService;
        this.boot = boot;
        selectionEventService.receiveSelectionEvent(this::onSelectionEvent);
    }

    @Override
    public void trackGameUiControl(Date startTimeStamp) {
        GameUiControlTrackerInfo gameUiControlTrackerInfo = new GameUiControlTrackerInfo();
        gameUiControlTrackerInfo.setStartTime(startTimeStamp);
        gameUiControlTrackerInfo.setGameSessionUuid(boot.get().getGameSessionUuid());
        gameUiControlTrackerInfo.setDuration((int) (startTimeStamp.getTime() - System.currentTimeMillis()));
        TrackerControllerFactory.INSTANCE
                .gameUiControlTrackerInfo(gameUiControlTrackerInfo)
                .onFailed(fail -> logger.log(Level.SEVERE, "TrackerProvider.gameUiControlTrackerInfo() onFailed: " + fail.getStatusText(), fail.getThrowable()))
                .send();
    }

    @Override
    public void trackScene(Date startTimeStamp, String sceneInternalName) {
        SceneTrackerInfo sceneTrackerInfo = new SceneTrackerInfo();
        sceneTrackerInfo.setStartTime(startTimeStamp);
        sceneTrackerInfo.setInternalName(sceneInternalName);
        sceneTrackerInfo.setGameSessionUuid(boot.get().getGameSessionUuid());
        sceneTrackerInfo.setDuration((int) (System.currentTimeMillis() - startTimeStamp.getTime()));
        TrackerControllerFactory.INSTANCE
                .sceneTrackerInfo(sceneTrackerInfo)
                .onFailed(fail -> logger.log(Level.SEVERE, "TrackerProvider.sceneTrackerInfo() trackScene: " + fail.getStatusText(), fail.getThrowable()))
                .send();
    }

    @Override
    public void onTaskFinished(AbstractStartupTask task) {
        TrackerControllerFactory.INSTANCE
                .startupTask(createStartupTaskJson(task, null))
                .onFailed(fail -> logger.log(Level.SEVERE, "TrackerProvider.startupTask() onTaskFinished: " + fail.getStatusText(), fail.getThrowable()))
                .send();
    }

    @Override
    public void onTaskFailed(AbstractStartupTask task, String error, Throwable t) {
        logger.log(Level.SEVERE, "onTaskFailed: " + task + " error:" + error, t);
        TrackerControllerFactory.INSTANCE
                .startupTask(createStartupTaskJson(task, error))
                .onFailed(fail -> logger.log(Level.SEVERE, "TrackerProvider.startupTask() onTaskFailed: " + fail.getStatusText(), fail.getThrowable()))
                .send();
    }

    @Override
    public void onStartupFinished(List<StartupTaskInfo> taskInfo, long totalTime) {
        TrackerControllerFactory.INSTANCE
                .startupTerminated(createStartupTerminatedJson(totalTime, true))
                .onFailed(fail -> logger.log(Level.SEVERE, "TrackerProvider.startupTerminated() onStartupFinished: " + fail.getStatusText(), fail.getThrowable()))
                .send();
    }

    @Override
    public void onStartupFailed(List<StartupTaskInfo> taskInfo, long totalTime) {
        TrackerControllerFactory.INSTANCE
                .startupTerminated(createStartupTerminatedJson(totalTime, false))
                .onFailed(fail -> logger.log(Level.SEVERE, "TrackerProvider.startupTerminated() onStartupFailed: " + fail.getStatusText(), fail.getThrowable()))
                .send();
        logger.severe("onStartupFailed: " + taskInfo + " totalTime:" + totalTime);
    }

    private StartupTaskJson createStartupTaskJson(AbstractStartupTask task, String error) {
        StartupTaskJson startupTaskJson = new StartupTaskJson();
        startupTaskJson.setGameSessionUuid(boot.get().getGameSessionUuid());
        startupTaskJson.setStartTime(new Date(task.getStartTime())).setDuration((int) task.getDuration());
        startupTaskJson.setTaskEnum(task.getTaskEnum().name()).setError(error);
        return startupTaskJson;
    }

    private StartupTerminatedJson createStartupTerminatedJson(long totalTime, boolean success) {
        StartupTerminatedJson startupTerminatedJson = new StartupTerminatedJson();
        startupTerminatedJson.setGameSessionUuid(boot.get().getGameSessionUuid());
        startupTerminatedJson.successful(success).totalTime((int) totalTime);
        return startupTerminatedJson;
    }

    @Override
    public void startDetailedTracking(int planetId) {
        Window.addCloseHandler(windowCloseEvent -> sendEventTrackerItems());
        stopDetailedTracking();
        detailedTracking = true;
        createTrackingContainer();
        detailedTrackingFuture = simpleExecutorService.scheduleAtFixedRate(DETAILED_TRACKING_DELAY, true, this::sendEventTrackerItems, SimpleExecutorService.Type.DETAILED_TRACKING);

        // TODO viewService.addViewFieldListeners(this);
        DomGlobal.document.addEventListener("mousemove", this::onMouseMove, true);
        DomGlobal.document.addEventListener("mousedown", this::onMouseButtonDown, true);
        DomGlobal.document.addEventListener("mouseup", this::onMouseButtonUp, true);
        // TODO clientModalDialogManager.setTrackerCallback(this::trackDialog);

        TrackingStart trackingStart = new TrackingStart().setPlanetId(planetId).setGameSessionUuid(boot.get().getGameSessionUuid());
        // TODO trackingStart.setBrowserWindowDimension(gameCanvas.getWindowDimenionForPlayback());
        initDetailedTracking(trackingStart);
        TrackerControllerFactory.INSTANCE
                .trackingStart(trackingStart)
                .onFailed(fail -> logger.log(Level.SEVERE, "TrackerProvider.trackingStart(): " + fail.getStatusText(), fail.getThrowable()))
                .send();
    }

    @Override
    public void stopDetailedTracking() {
        detailedTracking = false;
        // TODO viewService.removeViewFieldListeners(this);
        // TODO clientModalDialogManager.setTrackerCallback(null);
        if (detailedTrackingFuture != null) {
            detailedTrackingFuture.cancel();
            detailedTrackingFuture = null;
        }
        sendEventTrackerItems();
    }

    private void onSelectionEvent(SelectionEvent selectionEvent) {
        if (!detailedTracking) {
            return;
        }
        SelectionTracking selectionTracking = new SelectionTracking();
        initDetailedTracking(selectionTracking);
        List<Integer> selectedIds = new ArrayList<>();
        if (selectionEvent.getSelectedGroup() != null && selectionEvent.getSelectedGroup()._getItems() != null) {
            for (SyncBaseItemSimpleDto syncBaseItemSimpleDto : selectionEvent.getSelectedGroup()._getItems()) {
                selectedIds.add(syncBaseItemSimpleDto.getId());
            }
        }
        if (selectionEvent.getSelectedOther() != null) {
            selectedIds.add(selectionEvent.getSelectedOther().getId());
        }
        selectionTracking.setSelectedIds(selectedIds);
        trackingContainer.addSelectionTracking(selectionTracking);
    }

    private void onMouseMove(Event event) {
        try {
            MouseEvent mouseEvent = (MouseEvent) event;
            MouseMoveTracking mouseMoveTracking = new MouseMoveTracking().setPosition(GwtUtils.correctIndex((int) mouseEvent.clientX, (int) mouseEvent.clientY));
            initDetailedTracking(mouseMoveTracking);
            trackingContainer.addMouseMoveTrackings(mouseMoveTracking);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "ClientTrackerService.onMouseMove()", e);
        }
    }

    private void onMouseButtonDown(Event event) {
        try {
            MouseEvent mouseEvent = (MouseEvent) event;
            MouseButtonTracking mouseButtonTracking = new MouseButtonTracking().setButton(mouseEvent.button).setDown(true);
            initDetailedTracking(mouseButtonTracking);
            trackingContainer.addMouseButtonTrackings(mouseButtonTracking);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "ClientTrackerService.onMouseButton()", e);
        }
    }

    private void onMouseButtonUp(Event event) {
        try {
            MouseEvent mouseEvent = (MouseEvent) event;
            MouseButtonTracking mouseButtonTracking = new MouseButtonTracking().setButton(mouseEvent.button).setDown(false);
            initDetailedTracking(mouseButtonTracking);
            trackingContainer.addMouseButtonTrackings(mouseButtonTracking);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "ClientTrackerService.onMouseButton()", e);
        }
    }

    private void initDetailedTracking(DetailedTracking detailedTracking) {
        detailedTracking.setTimeStamp(new Date());
    }

    private void sendEventTrackerItems() {
        if (trackingContainer == null || trackingContainer.checkEmpty()) {
            return;
        }
        TrackingContainer tmpTrackingContainer = trackingContainer;
        createTrackingContainer();
        TrackerControllerFactory.INSTANCE
                .detailedTracking(tmpTrackingContainer)
                .onFailed(fail -> logger.log(Level.SEVERE, "TrackerProvider.detailedTracking(): " + fail.getStatusText(), fail.getThrowable()))
                .send();
    }

    private void createTrackingContainer() {
        trackingContainer = new TrackingContainer();
        trackingContainer.setGameSessionUuid(boot.get().getGameSessionUuid());
    }
}
