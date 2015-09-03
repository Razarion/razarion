package com.btxtech.shared.primitives;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainPolygon;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainPolygonCorner;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainPolygonLine;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 16.07.2015.
 *
 * http://www.geometrictools.com/Documentation/TriangulationByEarClipping.pdf
 */
public class Triangulator {
    private Logger logger = Logger.getLogger(Triangulator.class.getName());

    private List<Triangle2d> triangles = new ArrayList<>();
    private TerrainPolygon<TerrainPolygonCorner, TerrainPolygonLine> lastKnownGoodPolygon;

    public List<Triangle2d> calculate(List<Index> positions) {
        try {
            triangles.clear();
            List<Index> positionCopy = new ArrayList<>(positions);
            extractTriangle(positionCopy);
            return triangles;
        } catch(RuntimeException re) {
            logger.severe(Index.testString(positions));
            throw re;
        }
    }

    public List<Triangle2d> getTriangles() {
        return triangles;
    }

    private void extractTriangle(List<Index> positions) {
        System.out.println(Index.testString(positions));


        if(positions.size() < 3) {
            throw new IllegalStateException("Polygon with less then  3 vertices: " + positions);
        }

        if(positions.size() == 3) {
            triangles.add(new Triangle2d(positions.get(0), positions.get(1), positions.get(2)));
            return;
        }

        TerrainPolygon<TerrainPolygonCorner, TerrainPolygonLine> terrainPolygon = new TerrainPolygon<>(positions);
        lastKnownGoodPolygon = terrainPolygon;

        // Setup convex & reflex vertices list
        // Interior angle is smaller than 180 degrees.
        LinkedList<Integer> convexVertices = new LinkedList<>();
        // Interior angle is larger than 180 degrees.
        LinkedList<Integer> reflexVertices = new LinkedList<>();
        List<TerrainPolygonCorner> terrainPolygonCorners = terrainPolygon.getTerrainPolygonCorners();
        for (int i = 0; i < terrainPolygonCorners.size(); i++) {
            TerrainPolygonCorner corner = terrainPolygonCorners.get(i);
            if (corner.getInnerAngle() > MathHelper.HALF_RADIANT) {
                reflexVertices.add(i);
            } else {
                convexVertices.add(i);
            }
        }
        // Setup ear list
        LinkedList<Integer> ears = new LinkedList<>();
        for (int convexVertex : convexVertices) {
            TerrainPolygonCorner corner = terrainPolygonCorners.get(convexVertex);
            TerrainPolygonCorner previousCorner = terrainPolygon.getTerrainPolygonCornerSafe(convexVertex - 1);
            TerrainPolygonCorner nextCorner = terrainPolygon.getTerrainPolygonCornerSafe(convexVertex + 1);

            Triangle2d triangle = new Triangle2d(corner.getPoint(), previousCorner.getPoint(), nextCorner.getPoint());

            boolean isEar = true;
            for (int reflexVertex : reflexVertices) {
                if (reflexVertex == convexVertex || reflexVertex == convexVertex + 1 || reflexVertex == convexVertex - 1) {
                    continue;
                }

                if(triangle.isInside(terrainPolygonCorners.get(reflexVertex).getPoint())) {
                    isEar = false;
                    break;
                }
            }

            if(isEar) {
                ears.add(convexVertex);
            }

        }

        // Add the triangle
        if(ears.isEmpty()) {
            throw new IllegalStateException("No ears found");
        }

        int earIndex = ears.get(0);
        positions.remove(earIndex);
        TerrainPolygonCorner corner = terrainPolygonCorners.get(earIndex);
        TerrainPolygonCorner previousCorner = terrainPolygon.getTerrainPolygonCornerSafe(earIndex - 1);
        TerrainPolygonCorner nextCorner = terrainPolygon.getTerrainPolygonCornerSafe(earIndex + 1);
        triangles.add(new Triangle2d(corner.getPoint(), previousCorner.getPoint(), nextCorner.getPoint()));

        extractTriangle(positions);
    }

    public TerrainPolygon<TerrainPolygonCorner, TerrainPolygonLine> getLastKnownGoodPolygon() {
        return lastKnownGoodPolygon;
    }
}
