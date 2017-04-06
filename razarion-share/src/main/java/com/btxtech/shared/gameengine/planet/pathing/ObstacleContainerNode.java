package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.slope.VerticalSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 21.01.2017.
 */
public class ObstacleContainerNode {
    private Collection<Obstacle> obstacles;
    private List<VerticalSegment> slopeSegments;
    private Double slopHeight;
    private boolean belongsToSlope;
    private Collection<List<Vertex>> outerSlopeGroundPiercingLine;

    public void addObstacle(Obstacle obstacle) {
        if (obstacles == null) {
            obstacles = new ArrayList<>();
        }
        obstacles.add(obstacle);
    }

    public void addSlopeSegment(VerticalSegment verticalSegment) {
        if (slopeSegments == null) {
            slopeSegments = new ArrayList<>();
        }
        slopeSegments.add(verticalSegment);
    }

    public void setSlopHeight(Double slopHeight) {
        this.slopHeight = slopHeight;
    }

    public void setBelongsToSlope() {
        belongsToSlope = true;
    }

    public Collection<Obstacle> getObstacles() {
        return obstacles;
    }

    public List<VerticalSegment> getSlopeSegments() {
        return slopeSegments;
    }

    public Double getSlopHeight() {
        return slopHeight;
    }

    public boolean isInSlope() {
        if (belongsToSlope) {
            return true;
        }
        if (obstacles == null) {
            return false;
        }

        for (Obstacle obstacle : obstacles) {
            if (obstacle instanceof ObstacleSlope) {
                return true;
            }
        }
        return false;
    }

    public boolean exitsInSlopeGroundPiercing(DecimalPosition absolutePosition) {
        if (outerSlopeGroundPiercingLine == null) {
            return false;
        }
        for (List<Vertex> piercings : outerSlopeGroundPiercingLine) {
            for (Vertex piercing : piercings) {
                if (piercing.toXY().equals(absolutePosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addSlopeGroundPiercing(List<Vertex> piercingLine) {
        if (outerSlopeGroundPiercingLine == null) {
            outerSlopeGroundPiercingLine = new ArrayList<>();
        }
        outerSlopeGroundPiercingLine.add(piercingLine);
    }

    public Collection<List<Vertex>> getOuterSlopeGroundPiercingLine() {
        return outerSlopeGroundPiercingLine;
    }
}
