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
    private Collection<TriangleElement> expectedTriangleElements = new ArrayList<>();
    private Collection<TriangleElement> actualTriangleElements = new ArrayList<>();
    private Collection<TriangleElement> missingInExpected = new ArrayList<>();
    private Collection<TriangleElement> nonexistentInExpected = new ArrayList<>();

    public TriangleContainer(Collection<TerrainTile> expected, Collection<TerrainTile> actual) {
        for (TerrainTile terrainTile : expected) {
            insertTerrainTile(expectedTriangleElements, terrainTile);
        }
        for (TerrainTile terrainTile : actual) {
            insertTerrainTile(actualTriangleElements, terrainTile);
        }
        setupMissingInExpected();
        setupNonexistentInExpected();
    }

    public void printTrianglesAt(DecimalPosition position) {
        for (TriangleElement triangleElement : actualTriangleElements) {
            if (triangleElement.isInside(position)) {
                System.out.println("Triangle at '" + String.format("%.2f:%.2f", position.getX(), position.getY()) + "' " + triangleElement.toDisplayString());
            }
        }
    }

    public Collection<TriangleElement> getMissingInExpected() {
        return missingInExpected;
    }

    public Collection<TriangleElement> getNonexistentInExpected() {
        return nonexistentInExpected;
    }

    private void insertTerrainTile(Collection<TriangleElement> triangleElements, TerrainTile terrainTile) {
        addTriangles(triangleElements, terrainTile.getGroundVertices(), terrainTile.getGroundVertexCount(), "Ground");
        if (terrainTile.getTerrainSlopeTiles() != null) {
            for (TerrainSlopeTile terrainSlopeTile : terrainTile.getTerrainSlopeTiles()) {
                addTriangles(triangleElements, terrainSlopeTile.getVertices(), terrainSlopeTile.getSlopeVertexCount(), "Slope");
            }
        }
        if (terrainTile.getTerrainWaterTile() != null) {
            addTriangles(triangleElements, terrainTile.getTerrainWaterTile().getVertices(), terrainTile.getTerrainWaterTile().getVertexCount(), "Water");
        }
    }

    private void addTriangles(Collection<TriangleElement> triangleElements, double[] vertices, int vertexCount, String type) {
        for (int triangleIndex = 0; triangleIndex < vertexCount / 3; triangleIndex++) {
            triangleElements.add(new TriangleElement(vertices, triangleIndex, type));
        }
    }

    private void setupMissingInExpected() {
        missingInExpected = new ArrayList<>();
        for (TriangleElement expectedTriangleElement : expectedTriangleElements) {
            boolean found = false;
            for (TriangleElement actualTriangleElement : actualTriangleElements) {
                if (expectedTriangleElement.compare(actualTriangleElement)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missingInExpected.add(expectedTriangleElement);
            }
        }
        System.out.println("**** setupMissingInExpected: " + missingInExpected.size());
    }

    private void setupNonexistentInExpected() {
        nonexistentInExpected = new ArrayList<>();
        for (TriangleElement actualTriangleElement : actualTriangleElements) {
            boolean found = false;
            for (TriangleElement expectedTriangleElement : expectedTriangleElements) {
                if (actualTriangleElement.compare(expectedTriangleElement)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                nonexistentInExpected.add(actualTriangleElement);
            }
        }
        System.out.println("**** setupNonexistentInExpected: " + nonexistentInExpected.size());
    }

}
