package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.tracking.DetailedTracking;
import com.btxtech.shared.datatypes.tracking.TrackingContainer;

import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 31.05.2017.
 */
public class TrackingContainerAccess {
    private TrackingContainer trackingContainer;

    public TrackingContainerAccess(TrackingContainer trackingContainer) {
        this.trackingContainer = trackingContainer;
    }

    public boolean isEmpty() {
        return trackingContainer.checkEmpty();
    }

    public DetailedTracking removeNextDetailedTracking() {
        BestFit bestFit = new BestFit(true);

        bestFit.analyse(trackingContainer.getCameraTrackings());
        bestFit.analyse(trackingContainer.getSelectionTrackings());
        bestFit.analyse(trackingContainer.getDialogTrackings());
        bestFit.analyse(trackingContainer.getBrowserWindowTrackings());
        bestFit.analyse(trackingContainer.getMouseMoveTrackings());
        bestFit.analyse(trackingContainer.getMouseButtonTrackings());
        bestFit.analyse(trackingContainer.getPlayerBaseTrackings());
        bestFit.analyse(trackingContainer.getSyncItemDeletedTrackings());
        bestFit.analyse(trackingContainer.getSyncBaseItemTrackings());
        bestFit.analyse(trackingContainer.getSyncResourceItemTrackings());
        bestFit.analyse(trackingContainer.getSyncBoxItemTrackings());

        return bestFit.removeBest();
    }

    public DetailedTracking readLast() {
        BestFit bestFit = new BestFit(false);

        bestFit.analyse(trackingContainer.getCameraTrackings());
        bestFit.analyse(trackingContainer.getSelectionTrackings());
        bestFit.analyse(trackingContainer.getDialogTrackings());
        bestFit.analyse(trackingContainer.getBrowserWindowTrackings());
        bestFit.analyse(trackingContainer.getMouseMoveTrackings());
        bestFit.analyse(trackingContainer.getMouseButtonTrackings());
        bestFit.analyse(trackingContainer.getPlayerBaseTrackings());
        bestFit.analyse(trackingContainer.getSyncItemDeletedTrackings());
        bestFit.analyse(trackingContainer.getSyncBaseItemTrackings());
        bestFit.analyse(trackingContainer.getSyncResourceItemTrackings());
        bestFit.analyse(trackingContainer.getSyncBoxItemTrackings());

        return bestFit.readBest();
    }

    private class BestFit {
        private Date timeStamp = null;
        private List<? extends DetailedTracking> list;
        private boolean earliest;

        public BestFit(boolean earliest) {
            this.earliest = earliest;
        }

        public void analyse(List<? extends DetailedTracking> detailedTrackings) {
            if (detailedTrackings != null && !detailedTrackings.isEmpty()) {
                if (timeStamp == null) {
                    fill(detailedTrackings);
                } else {
                    if (earliest) {
                        if (detailedTrackings.get(0).getTimeStamp().getTime() < timeStamp.getTime()) {
                            fill(detailedTrackings);
                        }
                    } else {
                        if (detailedTrackings.get(detailedTrackings.size() - 1).getTimeStamp().getTime() > timeStamp.getTime()) {
                            fill(detailedTrackings);
                        }
                    }
                }
            }
        }

        private void fill(List<? extends DetailedTracking> detailedTrackings) {
            if (earliest) {
                timeStamp = detailedTrackings.get(0).getTimeStamp();
            } else {
                timeStamp = detailedTrackings.get(detailedTrackings.size() - 1).getTimeStamp();
            }
            list = detailedTrackings;
        }

        public DetailedTracking removeBest() {
            if (timeStamp == null || list == null) {
                throw new IllegalStateException("TrackingContainerAccess.removeBest(): timeStamp == null || list == null");
            }
            if (earliest) {
                return list.remove(0);
            } else {
                return list.remove(list.size() - 1);
            }
        }

        public DetailedTracking readBest() {
            if (timeStamp == null || list == null) {
                throw new IllegalStateException("TrackingContainerAccess.removeBest(): timeStamp == null || list == null");
            }
            if (earliest) {
                return list.get(0);
            } else {
                return list.get(list.size() - 1);
            }
        }
    }
}
