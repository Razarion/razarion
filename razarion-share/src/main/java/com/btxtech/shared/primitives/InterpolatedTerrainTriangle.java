package com.btxtech.shared.primitives;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.shared.primitives.Vertex;

/**
 * Created by Beat
 * 02.07.2016.
 */
public class InterpolatedTerrainTriangle {
    private Vertex interpolation;
    private TerrainTriangleCorner cornerA;
    private TerrainTriangleCorner cornerB;
    private TerrainTriangleCorner cornerC;

    public void setCornerA(TerrainTriangleCorner cornerA) {
        this.cornerA = cornerA;
    }

    public void setCornerB(TerrainTriangleCorner cornerB) {
        this.cornerB = cornerB;
    }

    public void setCornerC(TerrainTriangleCorner cornerC) {
        this.cornerC = cornerC;
    }

    public void setupInterpolation(DecimalPosition point) {
        double areaA = point.cross(cornerB.getVertex().toXY(), cornerC.getVertex().toXY()) / 2.0;
        double areaB = point.cross(cornerC.getVertex().toXY(), cornerA.getVertex().toXY()) / 2.0;
        double areaC = point.cross(cornerA.getVertex().toXY(), cornerB.getVertex().toXY()) / 2.0;

        if (areaA < 0.0 || areaB < 0.0 || areaC < 0.0) {
            throw new IllegalArgumentException("Triangle is wrong, area becomes negative");
        }

        double totalArea = areaA + areaB + areaC;

        double weightA = areaA / totalArea;
        double weightB = areaB / totalArea;
        double weightC = areaC / totalArea;

        interpolation = new Vertex(weightA, weightB, weightC);
    }

    public double getSplatting() {
        return cornerA.getSplatting() * interpolation.getX() + cornerB.getSplatting() * interpolation.getY() + cornerC.getSplatting() * interpolation.getZ();
    }

    public double getHeight() {
        return cornerA.getVertex().getZ() * interpolation.getX() + cornerB.getVertex().getZ() * interpolation.getY() + cornerC.getVertex().getZ() * interpolation.getZ();
    }

    public Vertex getNorm() {
        return cornerA.getNorm().multiply(interpolation.getX()).add(cornerB.getNorm().multiply(interpolation.getY()).add(cornerC.getNorm().multiply(interpolation.getZ())));
    }

    public Vertex getTangent() {
        return cornerA.getTangent().multiply(interpolation.getX()).add(cornerB.getTangent().multiply(interpolation.getY()).add(cornerC.getTangent().multiply(interpolation.getZ())));
    }
}
