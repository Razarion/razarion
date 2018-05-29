package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * on 23.05.2018.
 */
public class ReciprocalVelocityObstacle {
    private static final double FLANK_LENGTH = 20;
    private SyncPhysicalMovable syncPhysicalMovable;
    private DecimalPosition position;
    private DecimalPosition reciprocalVelocity;
    private double radius;
    private DecimalPosition coneStart;
    private double angle1;
    private double angle2;
    private Line flank1;
    private Line flank2;
    private Line middle;

    public ReciprocalVelocityObstacle(SyncPhysicalMovable syncPhysicalMovable, SyncPhysicalMovable other) {
        this.syncPhysicalMovable = syncPhysicalMovable;
        position = other.getPosition2d().sub(syncPhysicalMovable.getPosition2d());
        radius = syncPhysicalMovable.getRadius() + other.getRadius();
        double distance = position.magnitude();
        double baseAngle = position.angle();
        double halfAngle = Math.asin(radius / distance);
        angle1 = MathHelper.normaliseAngle(baseAngle - halfAngle);
        angle2 = MathHelper.normaliseAngle(baseAngle + halfAngle);
        DecimalPosition v1 = syncPhysicalMovable.getVelocity() != null ? syncPhysicalMovable.getVelocity().multiply(PlanetService.TICK_FACTOR) : DecimalPosition.NULL;
        DecimalPosition v2 = other.getVelocity() != null ? other.getVelocity().multiply(PlanetService.TICK_FACTOR) : DecimalPosition.NULL;
        coneStart = v1.add(v2).divide(2);
        flank1 = new Line(coneStart, angle1, FLANK_LENGTH);
        flank2 = new Line(coneStart, angle2, FLANK_LENGTH);
        middle = new Line(coneStart, baseAngle, FLANK_LENGTH);
    }

    public Collection<DecimalPosition> getPossibleIntersections() {
        Collection<DecimalPosition> possibleIntersections = new ArrayList<>();
        if(coneStart.equalsDelta(DecimalPosition.NULL)) {
            fillInPossibleFlankIntersections(flank1, angle1, null, possibleIntersections);
            fillInPossibleFlankIntersections(flank2, angle2, null, possibleIntersections);
        } else {
            fillInPossibleFlankIntersections(flank1, angle1, flank2, possibleIntersections);
            fillInPossibleFlankIntersections(flank2, angle2, flank1, possibleIntersections);
            fillInPossibleConeStart(possibleIntersections);
        }
        return possibleIntersections;
    }

    private void fillInPossibleFlankIntersections(Line flank, double angle, Line otherFlank, Collection<DecimalPosition> filteredIntersections) {
        flank.circleLineIntersection(syncPhysicalMovable.getVelocity().magnitude() * PlanetService.TICK_FACTOR).forEach(intersection -> {
            if (MathHelper.compareWithPrecision(MathHelper.normaliseAngle(coneStart.getAngle(intersection)), angle)) {
                if (otherFlank == null || otherFlank.getCrossInclusive(new Line(DecimalPosition.NULL, intersection)) == null) {
                    filteredIntersections.add(intersection);
                }
            }
        });
    }

    private void fillInPossibleConeStart(Collection<DecimalPosition> filteredIntersections) {
        DecimalPosition coneIntersection = DecimalPosition.NULL.getPointWithDistance(syncPhysicalMovable.getVelocity().magnitude() * PlanetService.TICK_FACTOR, coneStart, true);
        filteredIntersections.add(coneIntersection);
    }

    public Line getFlank1() {
        return flank1;
    }

    public Line getFlank2() {
        return flank2;
    }

    public Line getMiddle() {
        return middle;
    }
}
