package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 10.04.2017.
 */
@Dependent
public class TerrainWaterTileBuilder {
    @Inject
    private JsInteropObjectFactory jsInteropObjectFactory;
    private MapList<Integer, Vertex> trianglePositions = new MapList<>();
    private MapList<Integer, Double> offsetToOuterCorner = new MapList<>();
    private TerrainTileBuilder terrainTileBuilder;

    public void init(TerrainTileBuilder terrainTileBuilder) {
        this.terrainTileBuilder = terrainTileBuilder;
    }

    public void insertNode(Index nodeIndex, double waterLevel, double[] offsetToOuter, int slopeId) {
        Rectangle2D rect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);

        Vertex bottomLeft = new Vertex(rect.cornerBottomLeft(), waterLevel);
        Vertex bottomRight = new Vertex(rect.cornerBottomRight(), waterLevel);
        Vertex topRight = new Vertex(rect.cornerTopRight(), waterLevel);
        Vertex topLeft = new Vertex(rect.cornerTopLeft(), waterLevel);
        Double offsetToOuterBottomLeft = offsetToOuter != null ? offsetToOuter[0] : null;
        Double offsetToOuterBottomRight = offsetToOuter != null ? offsetToOuter[1] : null;
        Double offsetToOuterTopRight = offsetToOuter != null ? offsetToOuter[2] : null;
        Double offsetToOuterTopLeft = offsetToOuter != null ? offsetToOuter[3] : null;

        if (!terrainTileBuilder.checkPlayGround(bottomLeft, bottomRight, topRight, topLeft)) {
            return;
        }

        // Triangle 1
        trianglePositions.put(slopeId, bottomLeft);
        trianglePositions.put(slopeId, bottomRight);
        trianglePositions.put(slopeId, topLeft);
        offsetToOuterCorner.put(slopeId, offsetToOuterBottomLeft);
        offsetToOuterCorner.put(slopeId, offsetToOuterBottomRight);
        offsetToOuterCorner.put(slopeId, offsetToOuterTopLeft);
        // Triangle 2
        trianglePositions.put(slopeId, bottomRight);
        trianglePositions.put(slopeId, topRight);
        trianglePositions.put(slopeId, topLeft);
        offsetToOuterCorner.put(slopeId, offsetToOuterBottomRight);
        offsetToOuterCorner.put(slopeId, offsetToOuterTopRight);
        offsetToOuterCorner.put(slopeId, offsetToOuterTopLeft);
    }

    public void insertWaterRim(Vertex vertexA, double offsetToOuterA, Vertex vertexB, double offsetToOuterB, Vertex vertexC, double offsetToOuterC, int slopeId) {
        if (!terrainTileBuilder.checkPlayGround(vertexA, vertexB, vertexC)) {
            return;
        }
        trianglePositions.put(slopeId, vertexA);
        trianglePositions.put(slopeId, vertexB);
        trianglePositions.put(slopeId, vertexC);
        offsetToOuterCorner.put(slopeId, offsetToOuterA);
        offsetToOuterCorner.put(slopeId, offsetToOuterB);
        offsetToOuterCorner.put(slopeId, offsetToOuterC);
    }

    public List<TerrainWaterTile> generate() {
        if (trianglePositions.isEmpty()) {
            return Collections.emptyList();
        }

        List<TerrainWaterTile> terrainWaterTiles = new ArrayList<>();
        trianglePositions.getMap().forEach((slopeId, vertices) -> {
            TerrainWaterTile terrainWaterTile = jsInteropObjectFactory.generateTerrainWaterTile();
            terrainWaterTile.setSlopeId(slopeId);
            terrainWaterTile.setVertices(Vertex.toArray(vertices));
            terrainWaterTile.setOffsetToOuters(offsetToOuterCorner.get(slopeId).stream().mapToDouble(value -> value).toArray());
            terrainWaterTiles.add(terrainWaterTile);

        });

        return terrainWaterTiles;
    }
}
