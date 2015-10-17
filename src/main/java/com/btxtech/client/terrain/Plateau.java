package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.client.renderer.model.MeshGroup;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Vertex;

/**
 * Created by Beat
 * 17.10.2015.
 */
public class Plateau {
    private static final int PLANE_TOP_HEIGHT = 100;
    private static final int SLOPE_LEVEL_COUNT = 4;
    private static final int SLOPE_WIDTH = TerrainSurface.MESH_EDGE_LENGTH * SLOPE_LEVEL_COUNT;
    private static final Rectangle INNER_RECT = new Rectangle(new Index(200, 300), new Index(600, 600));
    private Mesh mesh;
    private MeshGroup slopMeshGroup;

    public Plateau(Mesh mesh) {
        this.mesh = mesh;
    }

    public void assignVertices(final MeshGroup planeMeshGroup) {
        slopMeshGroup = mesh.createMeshGroup();
        // Mark top vertices
        mesh.iterate(new Mesh.Visitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                if (INNER_RECT.contains2(vertex.toXY())) {
                    planeMeshGroup.add(index);
                    mesh.setVertex(index, vertex.add(0, 0, PLANE_TOP_HEIGHT));
                } else {
                    double distance = INNER_RECT.getNearestPointInclusive(vertex.toXY()).getDistance(vertex.toXY());
                    if (distance < SLOPE_WIDTH) {
                        slopMeshGroup.add(index);
                        mesh.setVertex(index, vertex.add(0, 0, PLANE_TOP_HEIGHT - PLANE_TOP_HEIGHT * distance / SLOPE_WIDTH));
                    } else {
                        planeMeshGroup.add(index);
                    }
                }
            }
        });
    }

    public VertexList provideVertexListSlope(ImageDescriptor imageDescriptor) {
        return slopMeshGroup.provideVertexListPlain(imageDescriptor, true);
    }
}
