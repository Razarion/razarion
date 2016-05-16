package com.btxtech.shared.gameengine.pathing;

import org.dyn4j.geometry.Vector2;

/**
 * Created by Beat
 * 16.05.2016.
 */
public class DebugHelper {
    private boolean active;
    private StringBuilder stringBuilder = new StringBuilder();

    public DebugHelper(String description, Unit protagonist, boolean active) {
        this.active = active;
        if (Pathing.DEBUG_ALL_FILTER != null && protagonist.getId() == Pathing.DEBUG_ALL_FILTER) {
            this.active = true;
        } else if (Pathing.DEBUG_SELECTIVE_FILTER != null && active) {
            this.active = protagonist.getId() == Pathing.DEBUG_SELECTIVE_FILTER;
        }
        stringBuilder.append(description);
        stringBuilder.append(" id: ");
        stringBuilder.append(protagonist.getId());
        append("cfw", protagonist.getCenter());
        append("v", protagonist.getVelocity());
        append("d", protagonist.getDestination());
        appendAngle("a", protagonist.getAngle());
    }

    public DebugHelper(String description, Obstacle obstacle, boolean active) {
        this.active = active;
        stringBuilder.append(description);
        stringBuilder.append(" obstacle");
    }

    public void append(String description, Vector2 vector) {
        stringBuilder.append(" ");
        stringBuilder.append(description);
        stringBuilder.append(": ");
        stringBuilder.append(Pathing.toVector2String(vector));
    }

    public void append(String description, double value) {
        stringBuilder.append(" ");
        stringBuilder.append(description);
        stringBuilder.append(String.format(": %.2f", value));
    }

    public void appendAngle(String description, double angleInRad) {
        stringBuilder.append(" ");
        stringBuilder.append(description);
        stringBuilder.append(String.format(": %.2f", Math.toDegrees(angleInRad)));
    }

    public void append(String description, Unit other) {
        if (Pathing.DEBUG_ALL_FILTER != null && other.getId() == Pathing.DEBUG_ALL_FILTER) {
            active = true;
        }
        stringBuilder.append(" ");
        stringBuilder.append(description);
        stringBuilder.append(": [id: ");
        stringBuilder.append(other.getId());
        append("cfw", other.getCenter());
        append("v", other.getVelocity());
        stringBuilder.append("]");
    }

    public void append(String s) {
        stringBuilder.append(" ");
        stringBuilder.append(s);
    }

    public void dump() {
        if (active) {
            System.out.println(stringBuilder);
        }
    }
}
