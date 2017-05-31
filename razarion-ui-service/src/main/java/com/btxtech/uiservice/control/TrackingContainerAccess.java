package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.tracking.CameraTracking;
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
        BestFit bestFit = new BestFit();

        bestFit.analyse(trackingContainer.getCameraTrackings());
        bestFit.analyse(trackingContainer.getSelectionTrackings());
        bestFit.analyse(trackingContainer.getBrowserWindowTrackings());

        return bestFit.removeBest();
    }

    private class BestFit {
        private Date timeStamp = null;
        private List<? extends DetailedTracking> list;

        public void analyse(List<? extends DetailedTracking> detailedTrackings) {
            if (detailedTrackings != null && !detailedTrackings.isEmpty()) {
                if(timeStamp == null) {
                    fill(detailedTrackings);
                } else {
                    if(detailedTrackings.get(0).getTimeStamp().getTime() < timeStamp.getTime()) {
                        fill(detailedTrackings);
                    }
                }
            }
        }

        private void fill(List<? extends DetailedTracking> detailedTrackings) {
            timeStamp = detailedTrackings.get(0).getTimeStamp();
            list = detailedTrackings;
        }

        public DetailedTracking removeBest() {
            if(timeStamp == null || list == null) {
                throw new IllegalStateException("TrackingContainerAccess.removeBest(): timeStamp == null || list == null");
            }
            return list.remove(0);
        }
    }
}
