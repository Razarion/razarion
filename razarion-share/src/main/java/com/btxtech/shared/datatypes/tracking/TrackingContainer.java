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
    // TODO private List<SyncItemInfo> syncItemInfos = new ArrayList<>();
    // TODO private List<DialogTracking> dialogTrackings = new ArrayList<>();

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

    public boolean checkEmpty() {
        return (cameraTrackings == null || cameraTrackings.isEmpty())
                && (browserWindowTrackings == null || browserWindowTrackings.isEmpty())
                && (selectionTrackings == null || selectionTrackings.isEmpty())
                && (mouseMoveTrackings == null || mouseMoveTrackings.isEmpty())
                && (mouseButtonTrackings == null || mouseButtonTrackings.isEmpty());
    }
}
