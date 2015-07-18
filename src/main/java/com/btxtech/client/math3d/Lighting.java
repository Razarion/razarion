package com.btxtech.client.math3d;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 23.06.2015.
 */
@Singleton
public class Lighting {
    private Color ambientColor;
    private Color color;
    private double altitude;
    private double azimuth;

    public Lighting() {
        setGame();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getAmbientColor() {
        return ambientColor;
    }

    public void setAmbientColor(Color ambientColor) {
        this.ambientColor = ambientColor;
    }

    public double getAltitude() {
        return altitude;
    }

    /**
     * Altitude or height. 0 means the sun is at the horizon
     * PI / 2 (90dec) means the sun is at the zenith
     *
     * @param altitude altitude in radians
     */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAzimuth() {
        return azimuth;
    }

    /**
     * Azimuth or directions
     * 0 means the sun is coming from the east
     * 90 means sun is comming from the north
     * 180 means sun is comming from the west
     * 270 means sun is comming from the south
     * counter clock
     *
     * @param azimuth azimuth in radians
     */
    public void setAzimuth(double azimuth) {
        this.azimuth = azimuth;
    }

    /**
     * Return the point on the surface pointing to the sun
     *
     * @return direction normalized
     */
    public Vertex getLightDirection() {
        double z = Math.sin(altitude);
        double adjacentSide = Math.cos(altitude);
        double y = adjacentSide * Math.sin(azimuth);
        double x = adjacentSide * Math.cos(azimuth);
        return new Vertex(x, y, z);
    }

    public void setGame() {
        azimuth = Math.toRadians(270);
        altitude = Math.toRadians(60);
        color = new Color(0.6, 0.6, 0.6);
        ambientColor = new Color(0.5, 0.5, 0.5);
    }
}
