package com.btxtech.shared.gameengine.datatypes.command;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.List;

public class SimplePath {
    private List<DecimalPosition> wayPositions;
    private boolean destinationReachable;
    private DecimalPosition alternativeDestination;
    /**
     * A* ran out of nodes before it reached the destination: the way ends at the reachable node closest
     * to it. Fine for a move order - the unit gets as near as it can - but a unit attacking from there
     * may never get in range.
     */
    private boolean shortOfDestination;

    public List<DecimalPosition> getWayPositions() {
        return wayPositions;
    }

    public void setWayPositions(List<DecimalPosition> wayPositions) {
        this.wayPositions = wayPositions;
        destinationReachable = true;
    }

    public SimplePath destinationUnreachable(DecimalPosition alternativeDestination) {
        destinationReachable = false;
        setAlternativeDestination(alternativeDestination);
        return this;
    }

    public boolean isDestinationReachable() {
        return destinationReachable;
    }

    public void setDestinationReachable(boolean destinationReachable) {
        this.destinationReachable = destinationReachable;
    }

    public DecimalPosition getAlternativeDestination() {
        return alternativeDestination;
    }

    public void setAlternativeDestination(DecimalPosition alternativeDestination) {
        this.alternativeDestination = alternativeDestination;
    }

    public boolean isShortOfDestination() {
        return shortOfDestination;
    }

    public void setShortOfDestination(boolean shortOfDestination) {
        this.shortOfDestination = shortOfDestination;
    }
}
