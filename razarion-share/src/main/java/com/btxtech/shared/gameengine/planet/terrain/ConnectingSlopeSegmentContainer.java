package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.gameengine.planet.terrain.slope.VerticalSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 15.06.2017.
 */
public class ConnectingSlopeSegmentContainer {
    private List<ConnectingSlopeSegment> segments = new ArrayList<>();

    public boolean contains(VerticalSegment examinee) {
        for (ConnectingSlopeSegment connectingSlopeSegment : segments) {
            if (connectingSlopeSegment.contains(examinee)) {
                return true;
            }
        }
        return false;
    }

    public void add(List<VerticalSegment> verticalSegments, double groundHeight) {
        segments.add(new ConnectingSlopeSegment(verticalSegments, groundHeight));
    }


    public List<ConnectingSlopeSegment> getSegments() {
        return segments;
    }

    public static class ConnectingSlopeSegment {
        private List<VerticalSegment> segment = new ArrayList<>();
        private double groundHeight;

        public ConnectingSlopeSegment(List<VerticalSegment> segment, double groundHeight) {
            this.segment = segment;
            this.groundHeight = groundHeight;
        }

        public boolean contains(VerticalSegment examinee) {
            for (VerticalSegment verticalSegment : segment) {
                if (examinee == verticalSegment) {
                    return true;
                }
            }
            return false;
        }

        public List<VerticalSegment> getSegment() {
            return segment;
        }

        public double getGroundHeight() {
            return groundHeight;
        }
    }
}
