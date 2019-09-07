package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
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
    private TerrainTileBuilder terrainTileBuilder;
    private MapList<Integer, Vertex> trianglePositions = new MapList<>();
    private MapList<Integer, Vertex> slopeTrianglePositions = new MapList<>();
    private MapList<Integer, DecimalPosition> slopeTriangleUvs = new MapList<>();
    private List<Vertex[]> tmpMesh;
    private List<DecimalPosition[]> tmpUvMesh;
    private List<DecimalPosition[]> tmpUvTerminationMesh;
    private Integer tmpSlopeId;

    public void init(TerrainTileBuilder terrainTileBuilder) {
        this.terrainTileBuilder = terrainTileBuilder;
    }

    public void insertNode(Index nodeIndex, double waterLevel, int slopeId) {
        Rectangle2D rect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);

        Vertex bottomLeft = new Vertex(rect.cornerBottomLeft(), waterLevel);
        Vertex bottomRight = new Vertex(rect.cornerBottomRight(), waterLevel);
        Vertex topRight = new Vertex(rect.cornerTopRight(), waterLevel);
        Vertex topLeft = new Vertex(rect.cornerTopLeft(), waterLevel);

        if (!terrainTileBuilder.checkPlayGround(bottomLeft, bottomRight, topRight, topLeft)) {
            return;
        }

        // Triangle 1
        trianglePositions.put(slopeId, bottomLeft);
        trianglePositions.put(slopeId, bottomRight);
        trianglePositions.put(slopeId, topLeft);
        // Triangle 2
        trianglePositions.put(slopeId, bottomRight);
        trianglePositions.put(slopeId, topRight);
        trianglePositions.put(slopeId, topLeft);
    }

    public void insertWaterRim(Vertex vertexA, Vertex vertexB, Vertex vertexC, int slopeId) {
        if (!terrainTileBuilder.checkPlayGround(vertexA, vertexB, vertexC)) {
            return;
        }
        trianglePositions.put(slopeId, vertexA);
        trianglePositions.put(slopeId, vertexB);
        trianglePositions.put(slopeId, vertexC);
    }

    public List<TerrainWaterTile> generate() {
        if (trianglePositions.isEmpty()) {
            return Collections.emptyList();
        }

        List<TerrainWaterTile> terrainWaterTiles = new ArrayList<>();
//  TODO      trianglePositions.getMap().forEach((slopeId, vertices) -> {
//            TerrainWaterTile terrainWaterTile = jsInteropObjectFactory.generateTerrainWaterTile();
//            terrainWaterTile.setSlopeId(slopeId);
//            terrainWaterTile.setVertices(Vertex.toArray(vertices));
//            terrainWaterTile.setSlopeUvs(offsetToOuterCorner.get(slopeId).stream().mapToDouble(value -> value).toArray());
//            terrainWaterTiles.add(terrainWaterTile);
//
//        });
        slopeTrianglePositions.getMap().forEach((slopeId, vertices) -> {
            TerrainWaterTile terrainWaterTile = jsInteropObjectFactory.generateTerrainWaterTile();
            terrainWaterTile.setSlopeId(slopeId);
            terrainWaterTile.setSlopeVertices(Vertex.toArray(vertices));
            terrainWaterTile.setSlopeUvs(DecimalPosition.toArray(slopeTriangleUvs.get(slopeId)));
            terrainWaterTiles.add(terrainWaterTile);

        });

        return terrainWaterTiles;
    }

    public void startWaterMesh(int slopeSkeletonConfigId) {
        tmpSlopeId = slopeSkeletonConfigId;
        tmpMesh = new ArrayList<>();
        tmpUvMesh = new ArrayList<>();
        tmpUvTerminationMesh = new ArrayList<>();
    }

    public void addWaterMeshVertex(Vertex waterOuter, DecimalPosition uvOuter, DecimalPosition uvTerminationOuter,
                                   Vertex waterInner, DecimalPosition uvInner, DecimalPosition uvTerminationInner) {
        tmpMesh.add(new Vertex[]{waterOuter, waterInner});
        tmpUvMesh.add(new DecimalPosition[]{uvOuter, uvInner});
        tmpUvTerminationMesh.add(new DecimalPosition[]{uvTerminationOuter, uvTerminationInner});
    }

    public void triangulateWaterMesh() {
        for (int i = 0; i < tmpMesh.size() - 1; i++) {
            Vertex vertexBL = tmpMesh.get(i)[0];
            Vertex vertexBR = tmpMesh.get(i + 1)[0];
            Vertex vertexTR = tmpMesh.get(i + 1)[1];
            Vertex vertexTL = tmpMesh.get(i)[1];

            if (!terrainTileBuilder.checkPlayGround(vertexBL, vertexBR, vertexTR, vertexTL)) {
                continue;
            }

            DecimalPosition uvBL = tmpUvMesh.get(i)[0];
            DecimalPosition uvBR = tmpUvTerminationMesh.get(i)[0] != null ? tmpUvTerminationMesh.get(i)[0] : tmpUvMesh.get(i + 1)[0];
            DecimalPosition uvTR = tmpUvTerminationMesh.get(i)[1] != null ? tmpUvTerminationMesh.get(i)[1] : tmpUvMesh.get(i + 1)[1];
            DecimalPosition uvTL = tmpUvMesh.get(i)[1];

            if (!vertexBL.equalsDelta(vertexBR, 0.001)) {
                slopeTrianglePositions.put(tmpSlopeId, vertexBL);
                slopeTrianglePositions.put(tmpSlopeId, vertexBR);
                slopeTrianglePositions.put(tmpSlopeId, vertexTL);
                slopeTriangleUvs.put(tmpSlopeId, uvBL);
                slopeTriangleUvs.put(tmpSlopeId, uvBR);
                slopeTriangleUvs.put(tmpSlopeId, uvTL);
            }

            if (!vertexTL.equalsDelta(vertexTR, 0.001)) {
                slopeTrianglePositions.put(tmpSlopeId, vertexBR);
                slopeTrianglePositions.put(tmpSlopeId, vertexTR);
                slopeTrianglePositions.put(tmpSlopeId, vertexTL);
                slopeTriangleUvs.put(tmpSlopeId, uvBR);
                slopeTriangleUvs.put(tmpSlopeId, uvTR);
                slopeTriangleUvs.put(tmpSlopeId, uvTL);
            }
        }
        tmpMesh = null;
        tmpUvMesh = null;
        tmpUvTerminationMesh = null;
        tmpSlopeId = null;
    }
}
