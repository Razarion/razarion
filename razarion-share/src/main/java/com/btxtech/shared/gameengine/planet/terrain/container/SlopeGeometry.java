package com.btxtech.shared.gameengine.planet.terrain.container;

public class SlopeGeometry {
    private double[] positions;
    private double[] norms;
    private double[] uvs;
    private double[] slopeFactors;

    public double[] getPositions() {
        return positions;
    }

    public void setPositions(double[] positions) {
        this.positions = positions;
    }

    public double[] getNorms() {
        return norms;
    }

    public void setNorms(double[] norms) {
        this.norms = norms;
    }

    public double[] getUvs() {
        return uvs;
    }

    public void setUvs(double[] uvs) {
        this.uvs = uvs;
    }

    public double[] getSlopeFactors() {
        return slopeFactors;
    }

    public void setSlopeFactors(double[] slopeFactors) {
        this.slopeFactors = slopeFactors;
    }
}
