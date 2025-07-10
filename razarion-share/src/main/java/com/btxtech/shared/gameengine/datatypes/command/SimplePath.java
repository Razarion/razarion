package com.btxtech.shared.gameengine.datatypes.command;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.List;

public class SimplePath {
    private List<DecimalPosition> wayPositions;
    private boolean destinationReachable;
    private DecimalPosition alternativeDestination;

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
}
