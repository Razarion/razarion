package com.btxtech.shared.gameengine.pathing;

import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

/**
 * Created by Beat
 * 16.05.2016.
 */
public class Obstacle {
    private Rectangle rectangle;

    public Obstacle(double x, double y, double width, double height) {
        rectangle = new Rectangle(width, height);
        rectangle.translate(x + width / 2.0, y + height / 2.0);
    }

    public Vector2 getCenter() {
        return rectangle.getCenter();
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
        if (rectangle.contains(unit.getCenter())) {
            throw new IllegalStateException();
        }
        Vector2 projection = Pathing.projectOnRectangle(rectangle, unit.getCenter());
        double distance = projection.distance(unit.getCenter());
        if (distance >= unit.getRadius()) {
            return null;
        }
        Vector2 normal = unit.getCenter().difference(projection);
        normal.normalize();
        return new Contact(unit, this, normal);
    }

    public double getDistance(Unit unit) {
        if (rectangle.contains(unit.getCenter())) {
            throw new IllegalStateException();
        }
        Vector2 projection = Pathing.projectOnRectangle(rectangle, unit.getCenter());
        return projection.distance(unit.getCenter()) - unit.getRadius();
    }

    public String testString() {
        return String.valueOf(getCenter().x - getWidth() / 2.0) + ", " + (getCenter().y - getHeight() / 2.0) + ", " + getWidth() + ", " + getHeight();
    }
}
