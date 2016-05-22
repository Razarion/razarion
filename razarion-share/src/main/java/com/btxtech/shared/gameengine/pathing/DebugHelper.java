package com.btxtech.shared.gameengine.pathing;

import com.btxtech.game.jsre.client.common.DecimalPosition;

/**
 * Created by Beat
 * 16.05.2016.
 */
public class DebugHelper {
    private static Integer debugAllFilter = null;
    private static Integer debugSelectiveFilter = null;
    private boolean active;
    private StringBuilder stringBuilder = new StringBuilder();

    public DebugHelper(String description, Unit protagonist, boolean active) {
        this.active = active;
        if (debugAllFilter != null && protagonist.getId() == debugAllFilter) {
            this.active = true;
        } else if (debugSelectiveFilter != null && active) {
            this.active = protagonist.getId() == debugSelectiveFilter;
        }
        stringBuilder.append(description);
        stringBuilder.append(" [id: ");
        stringBuilder.append(protagonist.getId());
        append("position", protagonist.getPosition());
        append("velocity", protagonist.getVelocity());
        append("destination", protagonist.getDestination());
        appendAngle("angle", protagonist.getAngle());
        stringBuilder.append("]");
    }

    public void append(String description, DecimalPosition vector) {
        stringBuilder.append(" ");
        stringBuilder.append(description);
        stringBuilder.append(": ");
        if (vector != null) {
            // stringBuilder.append(String.format("(%.2f:%.2f)", vector.getX(), vector.getY()));
            stringBuilder.append(vector.getX() + ":" + vector.getY());
        } else {
            stringBuilder.append("(-:-)");
        }
    }

    public void append(String description, double value) {
        stringBuilder.append(" ");
        stringBuilder.append(description);
        // stringBuilder.append(String.format(": %.2f", value));
        stringBuilder.append(value);
    }

    public void appendAngle(String description, double angleInRad) {
        stringBuilder.append(" ");
        stringBuilder.append(description);
        // stringBuilder.append(String.format(": %.2f", Math.toDegrees(angleInRad)));
        stringBuilder.append(Math.toDegrees(angleInRad));
    }

    public void append(String description, Unit other) {
        if (debugAllFilter != null && other.getId() == debugAllFilter) {
            active = true;
        }
        stringBuilder.append(" ");
        stringBuilder.append(description);
        stringBuilder.append(": [id: ");
        stringBuilder.append(other.getId());
        append("position", other.getPosition());
        append("velocity", other.getVelocity());
        stringBuilder.append("]");
    }

    public void append(String description, Obstacle obstacle) {
        stringBuilder.append(" ");
        stringBuilder.append(description);
        stringBuilder.append(": ");
        stringBuilder.append(obstacle);
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

    public static void setDebugAllFilter(Integer debugAllFilter) {
        DebugHelper.debugAllFilter = debugAllFilter;
    }

    public static void setDebugSelectiveFilter(Integer debugSelectiveFilter) {
        DebugHelper.debugSelectiveFilter = debugSelectiveFilter;
    }
}
