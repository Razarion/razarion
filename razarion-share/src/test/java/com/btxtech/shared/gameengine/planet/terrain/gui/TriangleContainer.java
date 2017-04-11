package com.btxtech.shared.gameengine.planet.terrain.gui;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 11.04.2017.
 */
public class TriangleContainer {
    private Collection<TriangleElement> triangleElements = new ArrayList<>();

    public TriangleContainer(Collection<TerrainTile> actual) {
        for (TerrainTile terrainTile : actual) {
            insertTerrainTile(terrainTile);
        }
    }

    public void printTrianglesAt(DecimalPosition position) {
        for (TriangleElement triangleElement : triangleElements) {
            if (triangleElement.isInside(position)) {
                System.out.println("Triangle at '" + String.format("%.2f:%.2f", position.getX(), position.getY())+ "' " + triangleElement.toDisplayString());
            }
        }
    }

    private void insertTerrainTile(TerrainTile terrainTile) {
        addTriangles(terrainTile.getGroundVertices(), terrainTile.getGroundVertexCount(), "Ground");
        if (terrainTile.getTerrainSlopeTiles() != null) {
            for (TerrainSlopeTile terrainSlopeTile : terrainTile.getTerrainSlopeTiles()) {
                addTriangles(terrainSlopeTile.getVertices(), terrainSlopeTile.getSlopeVertexCount(), "Slope");
            }
        }
        if (terrainTile.getTerrainWaterTile() != null) {
            addTriangles(terrainTile.getTerrainWaterTile().getVertices(), terrainTile.getTerrainWaterTile().getVertexCount(), "Water");
        }
    }

    private void addTriangles(double[] vertices, int vertexCount, String type) {
        for (int triangleIndex = 0; triangleIndex < vertexCount / 3; triangleIndex++) {
            triangleElements.add(new TriangleElement(vertices, triangleIndex, type));
        }
    }
}
