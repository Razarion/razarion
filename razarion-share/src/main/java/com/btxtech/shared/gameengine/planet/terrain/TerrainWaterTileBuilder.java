package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Beat
 * 10.04.2017.
 */
@Dependent
public class TerrainWaterTileBuilder {
    // private static Logger LOGGER = Logger.getLogger(TerrainWaterTileBuilder.class.getName());
    @Inject
    private JsInteropObjectFactory jsInteropObjectFactory;
    private TerrainTileBuilder terrainTileBuilder;
    private MapList<Integer, Vertex> trianglePositions = new MapList<>();
    private MapList<Integer, Vertex> shallowTrianglePositions = new MapList<>();
    private MapList<Integer, DecimalPosition> shallowTriangleUvs = new MapList<>();
    private List<Vertex[]> tmpShallowWaterMesh;
    private List<DecimalPosition[]> tmpUvShallowWaterMesh;
    private List<DecimalPosition[]> tmpUvTerminationShallowWaterMesh;

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
        if (trianglePositions.isEmpty() && shallowTrianglePositions.isEmpty()) {
            return null;
        }

        List<TerrainWaterTile> terrainWaterTiles = new ArrayList<>();
        Set<Integer> slopeIds = new HashSet<>(trianglePositions.getKeys());
        slopeIds.addAll(shallowTrianglePositions.getKeys());
        slopeIds.forEach(slopeId -> {
            TerrainWaterTile terrainWaterTile = new TerrainWaterTile();
            terrainWaterTile.setSlopeConfigId(slopeId);
            List<Vertex> vertices = trianglePositions.get(slopeId);
            if (vertices != null && !vertices.isEmpty()) {
                terrainWaterTile.setPositions(jsInteropObjectFactory.newFloat32Array4Vertices(vertices));
            }
            vertices = shallowTrianglePositions.get(slopeId);
            if (vertices != null && !vertices.isEmpty()) {
                terrainWaterTile.setShallowPositions(jsInteropObjectFactory.newFloat32Array4Vertices(vertices));
                terrainWaterTile.setShallowUvs(jsInteropObjectFactory.newFloat32Array4DecimalPositions(shallowTriangleUvs.get(slopeId)));
            }
            terrainWaterTiles.add(terrainWaterTile);
        });
        return terrainWaterTiles;
    }

    public void startWaterMesh() {
        tmpShallowWaterMesh = new ArrayList<>();
        tmpUvShallowWaterMesh = new ArrayList<>();
        tmpUvTerminationShallowWaterMesh = new ArrayList<>();
    }

    public void addShallowWaterMeshVertices(Matrix4 transformationMatrix, double width, double desiredDistance, double waterLevel, double uvY, Double uvYTermination) {
        int parts = Math.max(1, (int) Math.round(width / desiredDistance));
        double distance = width / (double) parts;
        parts++;

        Vertex[] vertices = new Vertex[parts];
        DecimalPosition[] uvs = new DecimalPosition[parts];
        DecimalPosition[] uvTerminations = new DecimalPosition[parts];
        for (int i = 0; i < parts; i++) {
            double x = distance * i;
            vertices[i] = transformationMatrix.multiply(new Vertex(x, 0, 0), 1.0).add(0, 0, waterLevel);
            uvs[i] = new DecimalPosition(x, uvY);
            if (uvYTermination != null) {
                uvTerminations[i] = new DecimalPosition(x, uvYTermination);
            } else {
                uvTerminations[i] = null;
            }
        }

        tmpShallowWaterMesh.add(vertices);
        tmpUvShallowWaterMesh.add(uvs);
        tmpUvTerminationShallowWaterMesh.add(uvTerminations);
    }

    public void triangulateShallowWaterMesh(int slopeConfigId) {
        for (int x = 0; x < tmpShallowWaterMesh.size() - 3; x++) { // TODO -3 ??? remove fragment for norm... but why -3???
            for (int y = 0; y < tmpShallowWaterMesh.get(x).length - 1; y++) {

                Vertex vertexBL = tmpShallowWaterMesh.get(x)[y];
                Vertex vertexBR = tmpShallowWaterMesh.get(x + 1)[y];
                Vertex vertexTR = tmpShallowWaterMesh.get(x + 1)[y + 1];
                Vertex vertexTL = tmpShallowWaterMesh.get(x)[y + 1];

                if (!terrainTileBuilder.checkPlayGround(vertexBL, vertexBR, vertexTR, vertexTL)) {
                    continue;
                }

                DecimalPosition uvBL = tmpUvShallowWaterMesh.get(x)[y];
                DecimalPosition uvBR = tmpUvTerminationShallowWaterMesh.get(x)[y] != null ? tmpUvTerminationShallowWaterMesh.get(x)[y] : tmpUvShallowWaterMesh.get(x + 1)[y];
                DecimalPosition uvTR = tmpUvTerminationShallowWaterMesh.get(x)[y + 1] != null ? tmpUvTerminationShallowWaterMesh.get(x)[y + 1] : tmpUvShallowWaterMesh.get(x + 1)[y + 1];
                DecimalPosition uvTL = tmpUvShallowWaterMesh.get(x)[y + 1];

                if (!vertexBL.equalsDelta(vertexBR, 0.001)) {
                    shallowTrianglePositions.put(slopeConfigId, vertexBL);
                    shallowTrianglePositions.put(slopeConfigId, vertexBR);
                    shallowTrianglePositions.put(slopeConfigId, vertexTL);
                    shallowTriangleUvs.put(slopeConfigId, uvBL);
                    shallowTriangleUvs.put(slopeConfigId, uvBR);
                    shallowTriangleUvs.put(slopeConfigId, uvTL);
                }

                if (!vertexTL.equalsDelta(vertexTR, 0.001)) {
                    shallowTrianglePositions.put(slopeConfigId, vertexBR);
                    shallowTrianglePositions.put(slopeConfigId, vertexTR);
                    shallowTrianglePositions.put(slopeConfigId, vertexTL);
                    shallowTriangleUvs.put(slopeConfigId, uvBR);
                    shallowTriangleUvs.put(slopeConfigId, uvTR);
                    shallowTriangleUvs.put(slopeConfigId, uvTL);
                }
            }
        }
        tmpShallowWaterMesh = null;
        tmpUvShallowWaterMesh = null;
        tmpUvTerminationShallowWaterMesh = null;
    }
}
