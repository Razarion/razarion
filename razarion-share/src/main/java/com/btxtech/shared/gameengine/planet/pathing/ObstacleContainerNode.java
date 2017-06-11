package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.terrain.slope.Driveway;
import com.btxtech.shared.gameengine.planet.terrain.slope.VerticalSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 21.01.2017.
 */
public class ObstacleContainerNode {
    private Collection<Obstacle> obstacles;
    private List<VerticalSegment> slopeSegments;
    private Double groundHeight;
    private boolean belongsToSlope;
    private boolean fullWater;
    private boolean fractionWater;
    private Driveway fractionDriveway;
    private Driveway fullDriveway;
    private Collection<List<DecimalPosition>> outerSlopeGroundPiercingLine;
    private Collection<List<DecimalPosition>> innerSlopeGroundPiercingLine;
    private List<DecimalPosition> drivewayGroundPiercingLine;
    private List<DecimalPosition> drivewaySlopePiercingLine;

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

    public void setGroundHeight(Double groundHeight) {
        this.groundHeight = groundHeight;
    }

    public void setFullWater() {
        fullWater = true;
    }

    public void setFractionWater() {
        fractionWater = true;
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

    public boolean isFullWater() {
        return fullWater;
    }

    public boolean isFractionWater() {
        return fractionWater;
    }

    public Double getGroundHeight() {
        return groundHeight;
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

    public boolean exitsInSlopeGroundPiercing(DecimalPosition absolutePosition, boolean isOuter) {
        if (isOuter) {
            return exitsInOuterSlopeGroundPiercing(absolutePosition);
        } else {
            return exitsInInnerSlopeGroundPiercing(absolutePosition);
        }
    }

    private boolean exitsInInnerSlopeGroundPiercing(DecimalPosition absolutePosition) {
        if (innerSlopeGroundPiercingLine == null) {
            return false;
        }
        for (List<DecimalPosition> piercings : innerSlopeGroundPiercingLine) {
            for (DecimalPosition piercing : piercings) {
                if (piercing.equals(absolutePosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean exitsInOuterSlopeGroundPiercing(DecimalPosition absolutePosition) {
        if (outerSlopeGroundPiercingLine == null) {
            return false;
        }
        for (List<DecimalPosition> piercings : outerSlopeGroundPiercingLine) {
            for (DecimalPosition piercing : piercings) {
                if (piercing.equals(absolutePosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addSlopeGroundPiercing(List<DecimalPosition> piercingLine, boolean isOuter) {
        if (isOuter) {
            if (outerSlopeGroundPiercingLine == null) {
                outerSlopeGroundPiercingLine = new ArrayList<>();
            }
            outerSlopeGroundPiercingLine.add(piercingLine);
        } else {
            if (innerSlopeGroundPiercingLine == null) {
                innerSlopeGroundPiercingLine = new ArrayList<>();
            }
            Collections.reverse(piercingLine);
            innerSlopeGroundPiercingLine.add(piercingLine);
        }
    }

    public Collection<List<DecimalPosition>> getInnerSlopeGroundPiercingLine() {
        return innerSlopeGroundPiercingLine;
    }

    public Collection<List<DecimalPosition>> getOuterSlopeGroundPiercingLine() {
        return outerSlopeGroundPiercingLine;
    }

    public boolean isFree() {
        return groundHeight == null && !belongsToSlope && !fullWater && !fractionWater && obstacles == null;
    }

    public Driveway getFractionDriveway() {
        return fractionDriveway;
    }

    public void setFractionDriveway(Driveway fractionDriveway) {
        this.fractionDriveway = fractionDriveway;
    }

    public Driveway getFullDriveway() {
        return fullDriveway;
    }

    public void setFullDriveway(Driveway fullDriveway) {
        this.fullDriveway = fullDriveway;
    }

    public List<DecimalPosition> getDrivewayGroundPiercingLine() {
        return drivewayGroundPiercingLine;
    }

    public void setDrivewayGroundPiercingLine(List<DecimalPosition> drivewayGroundPiercingLine) {
        this.drivewayGroundPiercingLine = drivewayGroundPiercingLine;
    }

    public List<DecimalPosition> getDrivewaySlopePiercingLine() {
        return drivewaySlopePiercingLine;
    }

    public void setDrivewaySlopePiercingLine(List<DecimalPosition> drivewaySlopePiercingLine) {
        this.drivewaySlopePiercingLine = drivewaySlopePiercingLine;
    }
}
