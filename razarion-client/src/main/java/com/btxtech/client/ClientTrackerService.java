package com.btxtech.client;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.tracking.BrowserWindowTracking;
import com.btxtech.shared.datatypes.tracking.CameraTracking;
import com.btxtech.shared.datatypes.tracking.DetailedTracking;
import com.btxtech.shared.datatypes.tracking.DialogTracking;
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
import com.btxtech.shared.rest.TrackerProvider;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.TrackerService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.ClientRunner;
import com.btxtech.uiservice.system.boot.StartupProgressListener;
import com.btxtech.uiservice.system.boot.StartupSeq;
import com.btxtech.uiservice.system.boot.StartupTaskEnum;
import com.btxtech.uiservice.system.boot.StartupTaskInfo;
import com.google.gwt.user.client.Window;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.MouseEvent;
import org.jboss.errai.common.client.api.Caller;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 03.03.2017.
 */
@ApplicationScoped
public class ClientTrackerService implements TrackerService, StartupProgressListener, ViewService.ViewFieldListener {
    private static final String WINDOW_CLOSE = "Window closed -> move to DB";
    private static final String START_UUID = "uuid";
    private static final int DETAILED_TRACKING_DELAY = 1000 * 10;
    private Logger logger = Logger.getLogger(ClientTrackerService.class.getName());
    @Inject
    private Caller<TrackerProvider> trackingProvider;
    @Inject
    private ClientRunner clientRunner;
    @Inject
    private SimpleExecutorService detailedExecutionService;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private ViewService viewService;
    @Inject
    private ClientModalDialogManagerImpl clientModalDialogManager;
    private TrackingContainer trackingContainer;
    private boolean detailedTracking = false;
    private SimpleScheduledFuture detailedTrackingFuture;

    @Override
    public void trackGameUiControl(Date startTimeStamp) {
        GameUiControlTrackerInfo gameUiControlTrackerInfo = new GameUiControlTrackerInfo();
        gameUiControlTrackerInfo.setStartTime(startTimeStamp);
        gameUiControlTrackerInfo.setGameSessionUuid(clientRunner.getGameSessionUuid());
        gameUiControlTrackerInfo.setDuration((int) (startTimeStamp.getTime() - System.currentTimeMillis()));
        trackingProvider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "TrackerProvider.gameUiControlTrackerInfo() trackGameUiControl: " + message, throwable);
            return false;
        }).gameUiControlTrackerInfo(gameUiControlTrackerInfo);
    }

    @Override
    public void trackScene(Date startTimeStamp, String sceneInternalName) {
        SceneTrackerInfo sceneTrackerInfo = new SceneTrackerInfo();
        sceneTrackerInfo.setStartTime(startTimeStamp);
        sceneTrackerInfo.setInternalName(sceneInternalName);
        sceneTrackerInfo.setGameSessionUuid(clientRunner.getGameSessionUuid());
        sceneTrackerInfo.setDuration((int) (System.currentTimeMillis() - startTimeStamp.getTime()));
        trackingProvider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "TrackerProvider.sceneTrackerInfo() trackScene: " + message, throwable);
            return false;
        }).sceneTrackerInfo(sceneTrackerInfo);
    }

    @Override
    public void onStart(StartupSeq startupSeq) {
        // Ignore
    }

    @Override
    public void onNextTask(StartupTaskEnum taskEnum) {
        // Ignore
    }

    @Override
    public void onTaskFinished(AbstractStartupTask task) {
        trackingProvider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "TrackerProvider.startupTask() onTaskFinished: " + message, throwable);
            return false;
        }).startupTask(createStartupTaskJson(task, null));
    }

    @Override
    public void onTaskFailed(AbstractStartupTask task, String error, Throwable t) {
        logger.log(Level.SEVERE, "onTaskFailed: " + task + " error:" + error, t);
        trackingProvider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "TrackerProvider.startupTask() onTaskFailed: " + message, throwable);
            return false;
        }).startupTask(createStartupTaskJson(task, error));
    }

    @Override
    public void onStartupFinished(List<StartupTaskInfo> taskInfo, long totalTime) {
        trackingProvider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "TrackerProvider.startupTerminated() onStartupFinished: " + message, throwable);
            return false;
        }).startupTerminated(createStartupTerminatedJson(totalTime, true));
    }

    @Override
    public void onStartupFailed(List<StartupTaskInfo> taskInfo, long totalTime) {
        logger.severe("onStartupFailed: " + taskInfo + " totalTime:" + totalTime);
        trackingProvider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "TrackerProvider.startupTerminated() onStartupFailed: " + message, throwable);
            return false;
        }).startupTerminated(createStartupTerminatedJson(totalTime, false));
    }

    private StartupTaskJson createStartupTaskJson(AbstractStartupTask task, String error) {
        StartupTaskJson startupTaskJson = new StartupTaskJson();
        startupTaskJson.setGameSessionUuid(clientRunner.getGameSessionUuid());
        startupTaskJson.setStartTime(new Date(task.getStartTime())).setDuration((int) task.getDuration());
        startupTaskJson.setTaskEnum(task.getTaskEnum().name()).setError(error);
        return startupTaskJson;
    }

    private StartupTerminatedJson createStartupTerminatedJson(long totalTime, boolean success) {
        StartupTerminatedJson startupTerminatedJson = new StartupTerminatedJson();
        startupTerminatedJson.setGameSessionUuid(clientRunner.getGameSessionUuid());
        startupTerminatedJson.setSuccessful(success).setTotalTime((int) totalTime);
        return startupTerminatedJson;
    }

    @Override
    public void startDetailedTracking(int planetId) {
        Window.addCloseHandler(windowCloseEvent -> sendEventTrackerItems());
        stopDetailedTracking();
        detailedTracking = true;
        createTrackingContainer();
        detailedTrackingFuture = detailedExecutionService.scheduleAtFixedRate(DETAILED_TRACKING_DELAY, true, this::sendEventTrackerItems, SimpleExecutorService.Type.DETAILED_TRACKING);

        viewService.addViewFieldListeners(this);
        Browser.getDocument().addEventListener(Event.MOUSEMOVE, this::onMouseMove, true);
        Browser.getDocument().addEventListener(Event.MOUSEDOWN, this::onMouseButtonDown, true);
        Browser.getDocument().addEventListener(Event.MOUSEUP, this::onMouseButtonUp, true);
        clientModalDialogManager.setTrackerCallback(this::trackDialog);

        TrackingStart trackingStart = new TrackingStart().setPlanetId(planetId).setGameSessionUuid(clientRunner.getGameSessionUuid());
        trackingStart.setBrowserWindowDimension(gameCanvas.getWindowDimenionForPlayback());
        initDetailedTracking(trackingStart);
        trackingProvider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "trackingStart failed: " + message, throwable);
            return false;
        }).trackingStart(trackingStart);
    }

    @Override
    public void stopDetailedTracking() {
        detailedTracking = false;
        viewService.removeViewFieldListeners(this);
        clientModalDialogManager.setTrackerCallback(null);
        if (detailedTrackingFuture != null) {
            detailedTrackingFuture.cancel();
            detailedTrackingFuture = null;
        }
        sendEventTrackerItems();
    }

    public void onSelectionEvent(@Observes SelectionEvent selectionEvent) {
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


    @Override
    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
        if (!detailedTracking) {
            return;
        }
        if (viewField.hasNullPosition()) {
            return;
        }
        CameraTracking cameraTracking = new CameraTracking();
        initDetailedTracking(cameraTracking);
        cameraTracking.setPosition(camera.getPosition().toXY()).setFovY(projectionTransformation.getFovY());
        trackingContainer.addCameraTracking(cameraTracking);
    }

    public void onResizeCanvas(Index dimension) {
        if (!detailedTracking) {
            return;
        }
        BrowserWindowTracking browserWindowTracking = new BrowserWindowTracking();
        initDetailedTracking(browserWindowTracking);
        browserWindowTracking.setDimension(dimension);
        trackingContainer.addBrowserWindowTracking(browserWindowTracking);
    }

    private void onMouseMove(Event event) {
        try {
            MouseEvent mouseEvent = (MouseEvent) event;
            MouseMoveTracking mouseMoveTracking = new MouseMoveTracking().setPosition(GwtUtils.correctIndex(mouseEvent.getClientX(), mouseEvent.getClientY()));
            initDetailedTracking(mouseMoveTracking);
            trackingContainer.addMouseMoveTrackings(mouseMoveTracking);
        } catch (Exception e) {
            exceptionHandler.handleException("ClientTrackerService.onMouseMove()", e);
        }
    }

    private void onMouseButtonDown(Event event) {
        try {
            MouseEvent mouseEvent = (MouseEvent) event;
            MouseButtonTracking mouseButtonTracking = new MouseButtonTracking().setButton(mouseEvent.getButton()).setDown(true);
            initDetailedTracking(mouseButtonTracking);
            trackingContainer.addMouseButtonTrackings(mouseButtonTracking);
        } catch (Exception e) {
            exceptionHandler.handleException("ClientTrackerService.onMouseButton()", e);
        }
    }

    private void onMouseButtonUp(Event event) {
        try {
            MouseEvent mouseEvent = (MouseEvent) event;
            MouseButtonTracking mouseButtonTracking = new MouseButtonTracking().setButton(mouseEvent.getButton()).setDown(false);
            initDetailedTracking(mouseButtonTracking);
            trackingContainer.addMouseButtonTrackings(mouseButtonTracking);
        } catch (Exception e) {
            exceptionHandler.handleException("ClientTrackerService.onMouseButton()", e);
        }
    }

    private void trackDialog(ModalDialogPanel modalDialogPanel, boolean appearing) {
        if (!detailedTracking) {
            return;
        }
        try {
            DialogTracking dialogTracking = new DialogTracking();
            initDetailedTracking(dialogTracking);
            dialogTracking.setAppearing(appearing);
            dialogTracking.setIdentityHashCode(System.identityHashCode(modalDialogPanel));
            if (appearing) {
                dialogTracking.setTitle(modalDialogPanel.getTitle());
                Rectangle dialogRectangle = modalDialogPanel.getDialogRectangle();
                dialogTracking.setLeft(dialogRectangle.startX());
                dialogTracking.setTop(dialogRectangle.startY());
                dialogTracking.setWidth(dialogRectangle.width());
                dialogTracking.setHeight(dialogRectangle.height());
                dialogTracking.setIndexZ(Integer.parseInt(modalDialogPanel.getElement().getStyle().getZIndex()));
            }
            trackingContainer.addDialogTracking(dialogTracking);
        } catch (Throwable t) {
            exceptionHandler.handleException("ClientTrackerService.trackDialog()", t);
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
        trackingProvider.call(response -> {
        }, exceptionHandler.restErrorHandler("TrackerProvider.detailedTracking()")).detailedTracking(tmpTrackingContainer);
    }

    private void createTrackingContainer() {
        trackingContainer = new TrackingContainer();
        trackingContainer.setGameSessionUuid(clientRunner.getGameSessionUuid());
    }
}
