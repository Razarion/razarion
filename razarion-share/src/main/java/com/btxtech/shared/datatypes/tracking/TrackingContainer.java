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
    // TODO private List<EventTrackingItem> eventTrackingItems = new ArrayList<>();
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

    public void setCameraTrackings(List<CameraTracking> cameraTrackings) {
        this.cameraTrackings = cameraTrackings;
    }

    public List<BrowserWindowTracking> getBrowserWindowTrackings() {
        return browserWindowTrackings;
    }

    public void setBrowserWindowTrackings(List<BrowserWindowTracking> browserWindowTrackings) {
        this.browserWindowTrackings = browserWindowTrackings;
    }

    public List<SelectionTracking> getSelectionTrackings() {
        return selectionTrackings;
    }

    public void setSelectionTrackings(List<SelectionTracking> selectionTrackings) {
        this.selectionTrackings = selectionTrackings;
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

    public boolean checkEmpty() {
        return (cameraTrackings == null || cameraTrackings.isEmpty())
                && (browserWindowTrackings == null || browserWindowTrackings.isEmpty())
                && (selectionTrackings == null || selectionTrackings.isEmpty());
    }
}
