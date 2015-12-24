package com.btxtech.client.terrain;

import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.shared.MathHelper2;
import com.btxtech.shared.TerrainMeshVertex;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 21.12.2015.
 */
public class Beach {
    private static final Rectangle INDEX_RECT = new Rectangle(100, 30, 30, 15);
    private static final double BOTTOM = -8.0;
    private static final double WATER_LEVEL = -4.0;
    private final List<Double> SLOP_INDEX = Arrays.asList(-8.0, -7.0, -6.0, -5.0, -4.0, -3.0, -2.0, -1.0, -0.0);
    private Mesh mesh;
    // private Logger logger = Logger.getLogger(Beach.class.getName());

    public Beach(Mesh mesh) {
        this.mesh = mesh;
    }

    public void sculpt() {
        final int slopeSize = SLOP_INDEX.size();

        final List<Double> slopeForm = new ArrayList<>();
        slopeForm.add(BOTTOM);
        slopeForm.addAll(SLOP_INDEX);
        slopeForm.add(0.0);


        mesh.iterate(new Mesh.VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                Mesh.VertexData vertexData = mesh.getVertexDataSafe(index);
                if (isInside(index)) {
                    vertexData.setVertex(new Vertex(vertex.getX(), vertex.getY(), BOTTOM));
                    vertexData.setSlopeFactor(0);
                    vertexData.setType(TerrainMeshVertex.Type.UNDER_WATER);
                } else {
                    double distance = INDEX_RECT.getNearestPointInclusive(new DecimalPosition(index)).getDistance(new DecimalPosition(index));
                    if (distance < slopeSize + 1) {
                        vertexData.addZValue(MathHelper2.interpolate(distance, slopeForm));
                        vertexData.setSlopeFactor(1.0);
                        vertexData.setType(TerrainMeshVertex.Type.BEACH);
                    }
                }
            }
        });
    }

    public boolean isInside(Index index) {
        return INDEX_RECT.contains2(new DecimalPosition(index));
    }

    public VertexList provideWaterVertexList() {
        Index waterStart = INDEX_RECT.getStart().sub(10, 10).scale(TerrainSurface.MESH_NODE_EDGE_LENGTH);
        Index waterEnd = INDEX_RECT.getEnd().add(10, 10).scale(TerrainSurface.MESH_NODE_EDGE_LENGTH);
        Vertex bottomLeft = new Vertex(waterStart.getX(), waterStart.getY(), WATER_LEVEL);
        Vertex bottomRight = new Vertex(waterEnd.getX(), waterStart.getY(), WATER_LEVEL);
        Vertex topLeft = new Vertex(waterStart.getX(), waterEnd.getY(), WATER_LEVEL);
        Vertex topRight = new Vertex(waterEnd.getX(), waterEnd.getY(), WATER_LEVEL);

        VertexList vertexList = new VertexList();
        vertexList.add(new Triangle(bottomLeft, bottomRight, topLeft));
        vertexList.add(new Triangle(bottomRight, topRight, topLeft));

        return vertexList;
    }

    public double getWaterLevel() {
        return WATER_LEVEL;
    }

    public double getWaterGround() {
        return BOTTOM;
    }
}
