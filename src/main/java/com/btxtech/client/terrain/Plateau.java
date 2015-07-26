package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.math3d.Mesh;
import com.btxtech.client.math3d.Vertex;
import com.btxtech.client.math3d.VertexListProvider;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainPolygon;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainPolygonLine;
import com.google.gwt.core.client.GWT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 11.04.2015.
 */
public class Plateau implements VertexListProvider {
    public static int TRIANGLE_SIDE_LENGTH = 20;
    //public static List<Vertex> shape = Arrays.asList(new Vertex(0, 0, 0), new Vertex(0, 0, 90));
    //public static List<Vertex> shape = Arrays.asList(new Vertex(0, 0, 0), new Vertex(-2, 0, 43), new Vertex(-18, 0, 94), new Vertex(-25, 0, 117), new Vertex(-38, 0, 133), new Vertex(-54, 0, 147), new Vertex(-68, 0, 131));
    // public static List<Vertex> shape = Arrays.asList(new Vertex(0, 0, 0), new Vertex(140, 0, 101));
    // public static List<Vertex> shape = Arrays.asList(new Vertex(50, 0, 2), new Vertex(20, 0, 4), new Vertex(5, 0, 21), new Vertex(4, 0, 171), new Vertex(-5, 0, 199), new Vertex(-22, 0, 206), new Vertex(-37, 0, 197), new Vertex(-58, 0, 197));
    // public static List<Vertex> shape = Arrays.asList(new Vertex(50, 0, 2), new Vertex(20, 0, 4), new Vertex(5, 0, 21), new Vertex(4, 0, 171), new Vertex(-18, 0, 174));
    // public static List<Vertex> shape = Arrays.asList(new Vertex(30, 0, 0), new Vertex(-8, 0, 180));
    // public static List<Vertex> shape = Arrays.asList(new Vertex(89, 0, 1), new Vertex(28, 0, 1), new Vertex(6, 0, 173), new Vertex(23, 0, 198), new Vertex(11, 0, 211));
    // public static List<Vertex> shape = Arrays.asList(new Vertex(89, 0, 1), new Vertex(28, 0, 1));
    // public static List<Vertex> shape = Arrays.asList(new Vertex(0, 0, 0), new Vertex(0, 0, 200));
    //public static List<Vertex> shape = Arrays.asList(new Vertex(118, 0, 1), new Vertex(-134, 0, 156));
    public static List<Vertex> shape = Arrays.asList(new Vertex(50, 0, 0), new Vertex(0, 0, 100));
    private final Ground ground;
    // private Logger logger = Logger.getLogger(Plateau.class.getName());
    private TerrainPolygon<PlateauCorner, TerrainPolygonLine> polygon;
    private Mesh hillSideMesh;
    private double roughness = 0;

    public Plateau(List<Index> points, Ground ground) {
        this.ground = ground;
        updateCorners(points);
    }

    public void updateCorners(List<Index> corners) {
        polygon = new TerrainPolygon<PlateauCorner, TerrainPolygonLine>(corners) {
            @Override
            protected PlateauCorner createTerrainPolygonCorner(TerrainPolygon terrainPolygon, int index, Index point) {
                return new PlateauCorner(terrainPolygon, index, point);
            }
        };
    }

    @Override
    public VertexList provideVertexList(ImageDescriptor imageDescriptor) {
        int horizontalCount = polygon.getCornerCount();
        int verticalCount = shape.size() - 1;

        hillSideMesh = new Mesh();
        List<Integer> verticalTileCounts = new ArrayList<>();
        int lastX = 0;
        for (int horizontalIndex = 0; horizontalIndex < horizontalCount; horizontalIndex++) {
            Integer horizontalTileCount = null;
            int lastZ = 0;
            for (int verticalIndex = 0; verticalIndex < verticalCount; verticalIndex++) {
                PlateauCorner plateauCorner = polygon.getTerrainPolygonCornerSafe(horizontalIndex);
                PlateauCorner plateauCornerSuccessor = polygon.getTerrainPolygonCornerSafe(horizontalIndex + 1);
                double segmentAngle = plateauCorner.getPoint().getAngleToNord(plateauCornerSuccessor.getPoint());
                Vertex shapeVertex = shape.get(verticalIndex);
                Vertex nextShapeVertex = shape.get(verticalIndex + 1);

                double angle = plateauCorner.getOuterAngle() / 2.0 - MathHelper.QUARTER_RADIANT;
                double angleSuccessor = plateauCornerSuccessor.getOuterAngle() / 2.0 - MathHelper.QUARTER_RADIANT;

                Index bL = plateauCorner.getOutsideNormal(calculateNormDistance(angle, shapeVertex.getX()));
                Index tL = plateauCorner.getOutsideNormal(calculateNormDistance(angle, nextShapeVertex.getX()));
                Index bR = plateauCornerSuccessor.getOutsideNormal(calculateNormDistance(angleSuccessor, shapeVertex.getX()));
                Index tR = plateauCornerSuccessor.getOutsideNormal(calculateNormDistance(angleSuccessor, nextShapeVertex.getX()));

                double zBottom = shapeVertex.getZ();
                double zTop = nextShapeVertex.getZ();

                Vertex bottomLeft = new Vertex(bL.getX(), bL.getY(), zBottom);
                Vertex topLeft = new Vertex(tL.getX(), tL.getY(), zTop);
                Vertex bottomRight = new Vertex(bR.getX(), bR.getY(), zBottom);
                Vertex topRight = new Vertex(tR.getX(), tR.getY(), zTop);

                Integer verticalTileCount = null;
                if (horizontalIndex > 0) {
                    verticalTileCount = verticalTileCounts.get(verticalIndex);
                }

                Segment segment = new Segment(bottomLeft, topLeft, bottomRight, topRight, segmentAngle);
                segment.rasterize(hillSideMesh, TRIANGLE_SIDE_LENGTH, lastX, lastZ, horizontalTileCount, verticalTileCount, verticalIndex == verticalCount - 1);
                if (horizontalIndex == 0) {
                    verticalTileCounts.add(segment.getVerticalCount());
                }
                lastZ += segment.getVerticalCount();
                horizontalTileCount = segment.getHorizontalCount();
            }
            lastX += horizontalTileCount != null ? horizontalTileCount : 0;
        }


        for (int x = 0; x < hillSideMesh.getX(); x++) {
            for (int z = 0; z < hillSideMesh.getZ(); z++) {
                hillSideMesh.randomNorm(x, z, roughness);
            }
        }

        VertexList vertexList = new VertexList();
        hillSideMesh.appendVertexList(vertexList, imageDescriptor, ground);
        return vertexList;
    }

    private double calculateNormDistance(double angle, double distance) {
        return distance / Math.cos(angle);
    }

    public Mesh getHillSideMesh() {
        return hillSideMesh;
    }

    public TerrainPolygon<PlateauCorner, TerrainPolygonLine> getPolygon() {
        return polygon;
    }

    public PlateauTop getPlateauTop() {
        return new PlateauTop(this);
    }

    public double getRoughness() {
        return roughness;
    }

    public void setRoughness(double roughness) {
        this.roughness = roughness;
    }

    public int getHeight() {
        return (int) shape.get(shape.size() - 1).getZ();
    }
}
