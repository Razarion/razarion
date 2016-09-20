package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

/**
 * Created by Beat
 * 16.05.2016.
 */
public class Contact {
    private SyncPhysicalMovable item1;
    private SyncPhysicalArea item2;
    private Obstacle obstacle;
    private DecimalPosition normal;

    public Contact(SyncPhysicalMovable item1, SyncPhysicalArea item2, DecimalPosition normal) {
        this.item1 = item1;
        this.item2 = item2;
        this.normal = normal;
    }

    public Contact(SyncPhysicalMovable unit, Obstacle obstacle, DecimalPosition normal) {
        this.item1 = unit;
        this.obstacle = obstacle;
        this.normal = normal;
    }

    public DecimalPosition getNormal() {
        return normal;
    }

    public SyncPhysicalMovable getItem1() {
        return item1;
    }

    public SyncPhysicalArea getItem2() {
        return item2;
    }

    public Obstacle getObstacle() {
        return obstacle;
    }

    public boolean hasUnit2AndCanMove() {
        return item2 != null && item2.canMove();
    }

    @Override
    public String toString() {
        return "Contact{" +
                "item1=" + item1 +
                (item2 != null ? (", item2=" + item2) : (", obstacle=" + obstacle)) +
                " normal=" + normal +
                '}';
    }
}
