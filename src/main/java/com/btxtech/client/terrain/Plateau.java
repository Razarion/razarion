package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.client.renderer.model.MeshGroup;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.TextureCoordinate;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 17.10.2015.
 */
public class Plateau {
    private static final int PLANE_TOP_HEIGHT = 100;
    // private static final int SLOPE_LEVEL_COUNT = 2;
    // private static final int SLOPE_WIDTH = TerrainSurface.MESH_EDGE_LENGTH * SLOPE_LEVEL_COUNT;
    // private static final Rectangle INNER_RECT = new Rectangle(new Index(200, 300), new Index(600, 600));
    private Mesh mesh;
    private MeshGroup slopMeshGroup;
    private Logger logger = Logger.getLogger(Plateau.class.getName());

    public Plateau(Mesh mesh) {
        this.mesh = mesh;
    }

    public void sculpt(final MeshGroup planeMeshGroup) {
        slopMeshGroup = mesh.createMeshGroup();
        // Mark top vertices
//        mesh.iterate(new Mesh.Visitor() {
//            @Override
//            public void onVisit(Index index, Vertex vertex) {
//                if (INNER_RECT.contains2(vertex.toXY())) {
//                    planeMeshGroup.add(index);
//                    mesh.setVertex(index, vertex.add(0, 0, PLANE_TOP_HEIGHT));
//                } else {
//                    double distance = INNER_RECT.getNearestPointInclusive(vertex.toXY()).getDistance(vertex.toXY());
//                    if (distance < SLOPE_WIDTH) {
//                        slopMeshGroup.add(index);
//                        mesh.setVertex(index, vertex.add(0, 0, PLANE_TOP_HEIGHT - PLANE_TOP_HEIGHT * distance / SLOPE_WIDTH));
//                    } else {
//                        planeMeshGroup.add(index);
//                    }
//                }
//            }
//        });

        Vertex vertex = mesh.getVertexSafe(new Index(2, 2));
        mesh.setVertex(new Index(2, 2), vertex.add(0, 0, PLANE_TOP_HEIGHT));
        mesh.iterate(new Mesh.VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                if (index.equals(new Index(2, 2))) {
                    slopMeshGroup.add(index);
                } else {
                    planeMeshGroup.add(index);
                }
            }
        });
    }

    public VertexList provideVertexListSlope(ImageDescriptor imageDescriptor) {
        final VertexList vertexList = new VertexList();

        mesh.iterateOverTriangles(new Mesh.TriangleVisitor() {
            @Override
            public void onVisit(Index bottomLeftIndex, Vertex bottomLeftVertex, Triangle triangle1, Triangle triangle2) {
                if (!slopMeshGroup.isNoneOfTriangleContained(true, bottomLeftIndex)) {
                    // logger.severe("Plateau 1: " + bottomLeftIndex);
                    triangle1.setupTextureProjection(new TextureCoordinateCalculator(triangle1));
                    triangle1.adjustTextureCoordinate(calculatePredecessorTextureCoordinate(true, bottomLeftIndex));
                    System.out.println("triangle1: " + bottomLeftIndex + "---" + triangle1);
                    vertexList.add(triangle1);
                }
                if (!slopMeshGroup.isNoneOfTriangleContained(false, bottomLeftIndex)) {
                    // logger.severe("Plateau 2: " + bottomLeftIndex);
                    triangle2.setupTextureProjection(new TextureCoordinateCalculator(triangle2));
                    triangle2.adjustTextureCoordinate(calculatePredecessorTextureCoordinate(false, bottomLeftIndex));
                    System.out.println("triangle2: " + bottomLeftIndex + "---" + triangle2);
                    vertexList.add(triangle2);
                }
            }
        });
        vertexList.normalize(imageDescriptor);
        return vertexList;
    }

    private TextureCoordinate calculatePredecessorTextureCoordinate(boolean triangle1, Index bottomLeftIndex) {
        Index predecessorIndex = mesh.getPredecessorIndex(triangle1, bottomLeftIndex);
        if (slopMeshGroup.isNoneOfTriangleContained(!triangle1, predecessorIndex)) {
            return null;
        }
        Mesh.VertexData vertexData = mesh.getVertexDataSafe(predecessorIndex);
        if (triangle1) {
            return vertexData.getTriangle2().getTextureCoordinateA();
        } else {
            return vertexData.getTriangle1().getTextureCoordinateB();
        }
    }
}
