package com.btxtech.shared.datatypes.tracking;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 30.05.2017.
 */
public class TrackingContainer {
    private String gameSessionUuid;
    private List<CameraTracking> cameraTrackings = new ArrayList<>();
    private List<BrowserWindowTracking> browserWindowTrackings = new ArrayList<>();
    private List<MouseMoveTracking> mouseMoveTrackings = new ArrayList<>();
    private List<MouseButtonTracking> mouseButtonTrackings = new ArrayList<>();
    private List<SelectionTracking> selectionTrackings = new ArrayList<>();
    private List<DialogTracking> dialogTrackings = new ArrayList<>();
    private List<PlayerBaseTracking> playerBaseTrackings = new ArrayList<>();
    private List<SyncItemDeletedTracking> syncItemDeletedTrackings = new ArrayList<>();
    private List<SyncBaseItemTracking> syncBaseItemTrackings = new ArrayList<>();
    private List<SyncResourceItemTracking> syncResourceItemTrackings = new ArrayList<>();
    private List<SyncBoxItemTracking> syncBoxItemTrackings = new ArrayList<>();

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public TrackingContainer setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
        return this;
    }

    public List<CameraTracking> getCameraTrackings() {
        return cameraTrackings;
    }

    public TrackingContainer setCameraTrackings(List<CameraTracking> cameraTrackings) {
        this.cameraTrackings = cameraTrackings;
        return this;
    }

    public List<BrowserWindowTracking> getBrowserWindowTrackings() {
        return browserWindowTrackings;
    }

    public TrackingContainer setBrowserWindowTrackings(List<BrowserWindowTracking> browserWindowTrackings) {
        this.browserWindowTrackings = browserWindowTrackings;
        return this;
    }

    public List<SelectionTracking> getSelectionTrackings() {
        return selectionTrackings;
    }

    public TrackingContainer setSelectionTrackings(List<SelectionTracking> selectionTrackings) {
        this.selectionTrackings = selectionTrackings;
        return this;
    }

    public List<DialogTracking> getDialogTrackings() {
        return dialogTrackings;
    }

    public TrackingContainer setDialogTrackings(List<DialogTracking> dialogTrackings) {
        this.dialogTrackings = dialogTrackings;
        return this;
    }

    public List<MouseMoveTracking> getMouseMoveTrackings() {
        return mouseMoveTrackings;
    }

    public TrackingContainer setMouseMoveTrackings(List<MouseMoveTracking> mouseMoveTrackings) {
        this.mouseMoveTrackings = mouseMoveTrackings;
        return this;
    }

    public List<MouseButtonTracking> getMouseButtonTrackings() {
        return mouseButtonTrackings;
    }

    public TrackingContainer setMouseButtonTrackings(List<MouseButtonTracking> mouseButtonTrackings) {
        this.mouseButtonTrackings = mouseButtonTrackings;
        return this;
    }

    public List<PlayerBaseTracking> getPlayerBaseTrackings() {
        return playerBaseTrackings;
    }

    public void setPlayerBaseTrackings(List<PlayerBaseTracking> playerBaseTrackings) {
        this.playerBaseTrackings = playerBaseTrackings;
    }

    public List<SyncItemDeletedTracking> getSyncItemDeletedTrackings() {
        return syncItemDeletedTrackings;
    }

    public void setSyncItemDeletedTrackings(List<SyncItemDeletedTracking> syncItemDeletedTrackings) {
        this.syncItemDeletedTrackings = syncItemDeletedTrackings;
    }

    public List<SyncBaseItemTracking> getSyncBaseItemTrackings() {
        return syncBaseItemTrackings;
    }

    public void setSyncBaseItemTrackings(List<SyncBaseItemTracking> syncBaseItemTrackings) {
        this.syncBaseItemTrackings = syncBaseItemTrackings;
    }

    public List<SyncResourceItemTracking> getSyncResourceItemTrackings() {
        return syncResourceItemTrackings;
    }

    public void setSyncResourceItemTrackings(List<SyncResourceItemTracking> syncResourceItemTrackings) {
        this.syncResourceItemTrackings = syncResourceItemTrackings;
    }

    public List<SyncBoxItemTracking> getSyncBoxItemTrackings() {
        return syncBoxItemTrackings;
    }

    public void setSyncBoxItemTrackings(List<SyncBoxItemTracking> syncBoxItemTrackings) {
        this.syncBoxItemTrackings = syncBoxItemTrackings;
    }

    public void addCameraTracking(CameraTracking cameraTracking) {
        if (cameraTrackings == null) {
            cameraTrackings = new ArrayList<>();
        }
        cameraTrackings.add(cameraTracking);
    }

    public void addBrowserWindowTracking(BrowserWindowTracking browserWindowTracking) {
        if (browserWindowTrackings == null) {
            browserWindowTrackings = new ArrayList<>();
        }
        browserWindowTrackings.add(browserWindowTracking);
    }

    public void addSelectionTracking(SelectionTracking selectionTracking) {
        if (selectionTrackings == null) {
            selectionTrackings = new ArrayList<>();
        }
        selectionTrackings.add(selectionTracking);
    }

    public void addDialogTracking(DialogTracking dialogTracking) {
        if (dialogTrackings == null) {
            dialogTrackings = new ArrayList<>();
        }
        dialogTrackings.add(dialogTracking);
    }

    public void addMouseMoveTrackings(MouseMoveTracking mouseMoveTracking) {
        if (mouseMoveTrackings == null) {
            mouseMoveTrackings = new ArrayList<>();
        }
        mouseMoveTrackings.add(mouseMoveTracking);
    }

    public void addMouseButtonTrackings(MouseButtonTracking mouseButtonTracking) {
        if (mouseButtonTrackings == null) {
            mouseButtonTrackings = new ArrayList<>();
        }
        mouseButtonTrackings.add(mouseButtonTracking);
    }

    public void addPlayerBaseTrackings(PlayerBaseTracking playerBaseTracking) {
        if (playerBaseTrackings == null) {
            playerBaseTrackings = new ArrayList<>();
        }
        playerBaseTrackings.add(playerBaseTracking);
    }

    public void addSyncItemDeletedTrackings(SyncItemDeletedTracking syncItemDeletedTracking) {
        if (syncItemDeletedTrackings == null) {
            syncItemDeletedTrackings = new ArrayList<>();
        }
        syncItemDeletedTrackings.add(syncItemDeletedTracking);
    }

    public void addSyncBaseItemTrackings(SyncBaseItemTracking syncBaseItemTracking) {
        if (syncBaseItemTrackings == null) {
            syncBaseItemTrackings = new ArrayList<>();
        }
        syncBaseItemTrackings.add(syncBaseItemTracking);
    }

    public void addSyncResourceItemTrackings(SyncResourceItemTracking syncResourceItemTracking) {
        if (syncResourceItemTrackings == null) {
            syncResourceItemTrackings = new ArrayList<>();
        }
        syncResourceItemTrackings.add(syncResourceItemTracking);
    }

    public void addSyncBoxItemTrackings(SyncBoxItemTracking syncBoxItemTracking) {
        if (syncBoxItemTrackings == null) {
            syncBoxItemTrackings = new ArrayList<>();
        }
        syncBoxItemTrackings.add(syncBoxItemTracking);
    }

    public boolean checkEmpty() {
        return (cameraTrackings == null || cameraTrackings.isEmpty())
                && (browserWindowTrackings == null || browserWindowTrackings.isEmpty())
                && (selectionTrackings == null || selectionTrackings.isEmpty())
                && (dialogTrackings == null || dialogTrackings.isEmpty())
                && (mouseMoveTrackings == null || mouseMoveTrackings.isEmpty())
                && (mouseButtonTrackings == null || mouseButtonTrackings.isEmpty())
                && (playerBaseTrackings == null || playerBaseTrackings.isEmpty())
                && (syncItemDeletedTrackings == null || syncItemDeletedTrackings.isEmpty())
                && (syncBaseItemTrackings == null || syncBaseItemTrackings.isEmpty())
                && (syncResourceItemTrackings == null || syncResourceItemTrackings.isEmpty())
                && (syncBoxItemTrackings == null || syncBoxItemTrackings.isEmpty());
    }
}
