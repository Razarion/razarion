package com.btxtech.client;

import com.btxtech.shared.datatypes.tracking.DetailedTracking;
import com.btxtech.shared.datatypes.tracking.DialogTracking;
import com.btxtech.shared.datatypes.tracking.EventTrackingItem;
import com.btxtech.shared.datatypes.tracking.SelectionTracking;
import com.btxtech.shared.datatypes.tracking.ViewFieldTracking;
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
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.ClientRunner;
import com.btxtech.uiservice.system.boot.StartupProgressListener;
import com.btxtech.uiservice.system.boot.StartupSeq;
import com.btxtech.uiservice.system.boot.StartupTaskEnum;
import com.btxtech.uiservice.system.boot.StartupTaskInfo;
import com.google.gwt.user.client.Window;
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
public class ClientTrackerService implements TrackerService, StartupProgressListener {
    private static final String WINDOW_CLOSE = "Window closed -> move to DB";
    private static final String START_UUID = "uuid";
    private static final int DETAILED_TRACKING_DELAY = 1000 * 5;
    private Logger logger = Logger.getLogger(ClientTrackerService.class.getName());
    @Inject
    private Caller<TrackerProvider> trackingProvider;
    @Inject
    private ClientRunner clientRunner;
    @Inject
    private SimpleExecutorService detailedExecutionService;
    private List<EventTrackingItem> eventTrackingItems = new ArrayList<>();
    private List<ViewFieldTracking> viewFieldTrackings = new ArrayList<>();
    private List<SelectionTracking> selectionTrackings = new ArrayList<>();
    // TODO private List<SyncItemInfo> syncItemInfos = new ArrayList<>();
    private List<DialogTracking> dialogTrackings = new ArrayList<>();
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
            logger.log(Level.SEVERE, "startupTask failed: " + message, throwable);
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
            logger.log(Level.SEVERE, "startupTask failed: " + message, throwable);
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
            logger.log(Level.SEVERE, "startupTask failed: " + message, throwable);
            return false;
        }).startupTask(createStartupTaskJson(task, null));
    }

    @Override
    public void onTaskFailed(AbstractStartupTask task, String error, Throwable t) {
        logger.log(Level.SEVERE, "onTaskFailed: " + task + " error:" + error, t);
        trackingProvider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "startupTask failed: " + message, throwable);
            return false;
        }).startupTask(createStartupTaskJson(task, error));
    }

    @Override
    public void onStartupFinished(List<StartupTaskInfo> taskInfo, long totalTime) {
        trackingProvider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "startupTask failed: " + message, throwable);
            return false;
        }).startupTerminated(createStartupTerminatedJson(totalTime, true));
    }

    @Override
    public void onStartupFailed(List<StartupTaskInfo> taskInfo, long totalTime) {
        logger.severe("onStartupFailed: " + taskInfo + " totalTime:" + totalTime);
        trackingProvider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "startupTask failed: " + message, throwable);
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
    public void startDetailedTracking() {
        Window.addCloseHandler(windowCloseEvent -> sendEventTrackerItems());
        stopDetailedTracking();
        detailedTracking = true;
        detailedTrackingFuture = detailedExecutionService.scheduleAtFixedRate(DETAILED_TRACKING_DELAY, true, this::sendEventTrackerItems, SimpleExecutorService.Type.DETAILED_TRACKING);
        // MapWindow.getInstance().setTrackingEvents(true);
        // TerrainView.getInstance().addTerrainScrollListener(this);
        // DialogManager.getInstance().addDialogListener(this);
    }

    @Override
    public void stopDetailedTracking() {
        detailedTracking = false;
        // TODO DialogManager.getInstance().removeDialogListener(this);
        if (detailedTrackingFuture != null) {
            detailedTrackingFuture.cancel();
            detailedTrackingFuture = null;
        }
        // TODO SelectionHandler.getInstance().removeSelectionListener(this);
        // TODO MapWindow.getInstance().setTrackingEvents(false);
        // TODO TerrainView.getInstance().removeTerrainScrollListener(this);
        sendEventTrackerItems();
    }

    public void onSelectionEvent(@Observes SelectionEvent selectionEvent) {
        if (!detailedTracking) {
            return;
        }
        SelectionTracking selectionTracking = new SelectionTracking();
        initDetailedTracking(selectionTracking);
        List<Integer> selectedIds = new ArrayList<>();
        if (selectionEvent.getSelectedGroup() != null && selectionEvent.getSelectedGroup().getItems() != null) {
            for (SyncBaseItemSimpleDto syncBaseItemSimpleDto : selectionEvent.getSelectedGroup().getItems()) {
                selectedIds.add(syncBaseItemSimpleDto.getId());
            }
        }
        if (selectionEvent.getSelectedOther() != null) {
            selectedIds.add(selectionEvent.getSelectedOther().getId());
        }
        selectionTracking.setSelectedIds(selectedIds);
        selectionTrackings.add(selectionTracking);
    }

    @Override
    public void onViewChanged(ViewField currentViewField) {
        if (!detailedTracking) {
            return;
        }
        if (currentViewField.hasNullPosition()) {
            return;
        }
        ViewFieldTracking viewFieldTracking = new ViewFieldTracking();
        initDetailedTracking(viewFieldTracking);
        viewFieldTracking.setBottomLeft(currentViewField.getBottomLeft()).setBottomRight(currentViewField.getBottomRight());
        viewFieldTracking.setBottomRight(currentViewField.getTopRight()).setBottomLeft(currentViewField.getTopRight()).setZ(currentViewField.getZ());
        viewFieldTrackings.add(viewFieldTracking);
    }

    private void initDetailedTracking(DetailedTracking detailedTracking) {
        detailedTracking.setTimeStamp(new Date()).setStartUuid(clientRunner.getGameSessionUuid());
    }

//  TODO  public void addEventTrackingItem(int xPos, int yPos, int eventType) {
//        if (detailedTracking) {
//            eventTrackingItems.add(new EventTrackingItem(ClientGlobalServices.getInstance().getClientRunner().getStartUuid(),
//                    GwtCommon.correctInt(xPos),
//                    GwtCommon.correctInt(yPos),
//                    GwtCommon.correctInt(eventType)));
//        }
//    }
//   todo public void trackSyncInfo(SyncItem syncItem) {
//        if (detailedTracking) {
//            SyncItemInfo syncItemInfo = syncItem.getSyncInfo();
//            syncItemInfo.setStartUuid(ClientGlobalServices.getInstance().getClientRunner().getStartUuid());
//            syncItemInfo.setClientTimeStamp();
//            syncItemInfos.add(syncItemInfo);
//        }
//    }

    private void sendEventTrackerItems() {
        if (eventTrackingItems.isEmpty()
                // TODO  && syncItemInfos.isEmpty()
                && selectionTrackings.isEmpty()
                && viewFieldTrackings.isEmpty()
                && dialogTrackings.isEmpty()) {
            return;
        }
        trackingProvider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "detailedTracking failed: " + message, throwable);
            return false;
        }).detailedTracking(viewFieldTrackings);

        clearTracking();
    }

    private void clearTracking() {
        eventTrackingItems = new ArrayList<>();
        // TODO syncItemInfos = new ArrayList<>();
        selectionTrackings = new ArrayList<>();
        viewFieldTrackings = new ArrayList<>();
        dialogTrackings = new ArrayList<>();
    }

//  TODO  public void onDialogAppears(Widget widget, String description) {
//        if (detailedTracking) {
//            Integer zIndex = null;
//            try {
//                zIndex = Integer.parseInt(widget.getElement().getStyle().getZIndex());
//            } catch (NumberFormatException e) {
//                // Ignore
//            }
//
//            dialogTrackings.add(new DialogTracking(
//                    ClientGlobalServices.getInstance().getClientRunner().getStartUuid(),
//                    GwtCommon.correctInt(widget.getAbsoluteLeft()),
//                    GwtCommon.correctInt(widget.getAbsoluteTop()),
//                    GwtCommon.correctInt(widget.getOffsetWidth()),
//                    GwtCommon.correctInt(widget.getOffsetHeight()),
//                    GwtCommon.correctInt(zIndex),
//                    description,
//                    GwtCommon.correctInt(System.identityHashCode(widget))
//            ));
//        }
//    }
//
//  TODO  public void onDialogDisappears(Widget widget) {
//        if (detailedTracking) {
//            dialogTrackings.add(new DialogTracking(ClientGlobalServices.getInstance().getClientRunner().getStartUuid(),
//                    GwtCommon.checkInt(System.identityHashCode(widget), "onDialogDisappears System.identityHashCode(widget)")));
//        }
//    }
//
//    @Override
//  TODO  public void onDialogShown(Dialog dialog) {
//        onDialogAppears(dialog, "Dialog: " + dialog.getTitle());
//    }
//
//    @Override
// TODO   public void onDialogHidden(Dialog dialog) {
//        onDialogDisappears(dialog);
//    }

}
