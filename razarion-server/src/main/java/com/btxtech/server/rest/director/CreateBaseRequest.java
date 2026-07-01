package com.btxtech.server.rest.director;

/**
 * Create (reset) the authenticated operator's green (OWN/human) base with its
 * start building at (x, y). Game-plane coordinates. Dev-only.
 */
public class CreateBaseRequest {
    private double x;
    private double y;

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
}
