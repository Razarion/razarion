package com.btxtech.shared.gameengine.pathing;

import com.btxtech.game.jsre.client.common.DecimalPosition;

/**
 * Created by Beat
 * 16.05.2016.
 */
public class Contact {
    private Unit unit1;
    private Unit unit2;
    private Obstacle obstacle;
    private DecimalPosition normal;

    public Contact(Unit unit1, Unit unit2, DecimalPosition normal) {
        this.unit1 = unit1;
        this.unit2 = unit2;
        this.normal = normal;
    }

    public Contact(Unit unit, Obstacle obstacle, DecimalPosition normal) {
        this.unit1 = unit;
        this.obstacle = obstacle;
        this.normal = normal;
    }

    public DecimalPosition getNormal() {
        return normal;
    }

    public Unit getUnit1() {
        return unit1;
    }

    public Unit getUnit2() {
        return unit2;
    }

    public Obstacle getObstacle() {
        return obstacle;
    }

    public boolean hasUnit2AndCanMove() {
        return unit2 != null && unit2.isCanMove();
    }

    @Override
    public String toString() {
        return "Contact{" +
                "unit1=" + unit1 +
                (unit2 != null ? (", unit2=" + unit2):(", obstacle=" + obstacle)) +
                " normal=" + normal +
                '}';
    }
}
