package com.btxtech.shared.gameengine.pathing;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Rectangle;

/**
 * Created by Beat
 * 16.05.2016.
 */
public class Obstacle {
    private Rectangle rectangle;

    public Obstacle(int x, int y, int width, int height) {
        rectangle = new Rectangle(x, y, width, height);
    }

    public double getWidth() {
        return rectangle.getWidth();
    }

    public double getHeight() {
        return rectangle.getHeight();
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public Contact hasContact(Unit unit) {
        if (rectangle.contains2(unit.getPosition())) {
            throw new IllegalStateException();
        }
        DecimalPosition projection = rectangle.getNearestPoint(unit.getPosition());
        double distance = projection.getDistance(unit.getPosition());
        if (distance >= unit.getRadius()) {
            return null;
        }
        DecimalPosition normal = unit.getPosition().sub(projection).normalize();
        return new Contact(unit, this, normal);
    }

    public double getDistance(Unit unit) {
        if (rectangle.contains2(unit.getPosition())) {
            throw new IllegalStateException();
        }
        DecimalPosition projection = rectangle.getNearestPoint(unit.getPosition());
        return projection.getDistance(unit.getPosition()) - unit.getRadius();
    }
}
