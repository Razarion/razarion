package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.client.renderer.model.MeshGroup;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Rectangle;
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
    private static final int SLOPE_LEVEL_COUNT = 2;
    private static final int SLOPE_WIDTH = TerrainSurface.MESH_EDGE_LENGTH * SLOPE_LEVEL_COUNT;
    private static final Rectangle INNER_RECT = new Rectangle(new Index(200, 300), new Index(600, 600));
    private Mesh mesh;
    private MeshGroup slopMeshGroup;
    private Logger logger = Logger.getLogger(Plateau.class.getName());

    public Plateau(Mesh mesh) {
        this.mesh = mesh;
    }

    public void sculpt(final MeshGroup planeMeshGroup) {
        slopMeshGroup = mesh.createMeshGroup();
        // Mark top vertices
        mesh.iterate(new Mesh.VertexVisitor() {
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

//        Vertex vertex = mesh.getVertexSafe(new Index(2, 2));
//        mesh.setVertex(new Index(2, 2), vertex.add(0, 0, PLANE_TOP_HEIGHT));
//        mesh.iterate(new Mesh.VertexVisitor() {
//            @Override
//            public void onVisit(Index index, Vertex vertex) {
//                if (index.equals(new Index(2, 2))) {
//                    slopMeshGroup.add(index);
//                } else {
//                    planeMeshGroup.add(index);
//                }
//            }
//        });
    }

    public VertexList provideVertexListSlope(ImageDescriptor imageDescriptor) {
        final VertexList vertexList = new VertexList();

        // Old
//        mesh.iterateOverTriangles(new Mesh.TriangleVisitor() {
//            @Override
//            public void onVisit(Index bottomLeftIndex, Vertex bottomLeftVertex, Triangle triangle1, Triangle triangle2) {
//                if (!slopMeshGroup.isNoneOfTriangleContained(true, bottomLeftIndex)) {
//                    triangle1.setTextureCoordinateA(new TextureCoordinate(0, 0));
//                    triangle1.setTextureCoordinateB(new TextureCoordinate(0.1, 0));
//                    triangle1.setTextureCoordinateC(new TextureCoordinate(0, 0.1));
//                    vertexList.add(triangle1);
//                }
//                if (!slopMeshGroup.isNoneOfTriangleContained(false, bottomLeftIndex)) {
//                    triangle2.setTextureCoordinateA(new TextureCoordinate(0.1, 0));
//                    triangle2.setTextureCoordinateB(new TextureCoordinate(0.1, 0.1));
//                    triangle2.setTextureCoordinateC(new TextureCoordinate(0, 0.1));
//                    vertexList.add(triangle2);
//                }
//            }
//        });

        // Start with specific triangle 11:8
        Index start = new Index(7, 7);
        for (int x = start.getX(); !slopMeshGroup.isNoneOfSquadContained(new Index(x, start.getY())); x++) {
            Triangle under = null;
            for (int y = start.getY(); !slopMeshGroup.isNoneOfSquadContained(new Index(x, y)); y++) {
                Index bottomLeftIndex = new Index(x, y);
                Mesh.VertexData vertexData = mesh.getVertexDataSafe(bottomLeftIndex);

                Triangle triangle1 = null;
                if (!slopMeshGroup.isNoneOfTriangleContained(true, bottomLeftIndex)) {
                    triangle1 = vertexData.getTriangle1();
                    triangle1.setupTextureProjection(new TextureCoordinateCalculator(triangle1));
                    triangle1.adjustTextureCoordinate();
                    if (under != null && under.getTextureCoordinateC() != null) {
                        triangle1.setTextureCoordinateA(triangle1.getTextureCoordinateA().add(under.getTextureCoordinateC()));
                        triangle1.setTextureCoordinateB(triangle1.getTextureCoordinateB().add(under.getTextureCoordinateC()));
                        triangle1.setTextureCoordinateC(triangle1.getTextureCoordinateC().add(under.getTextureCoordinateC()));
                    } else {
                        Mesh.VertexData vertexDataLeft = mesh.getVertexData(new Index(x - 1, y));
                        if (vertexDataLeft != null && vertexDataLeft.getTriangle2() != null && vertexDataLeft.getTriangle2().getTextureCoordinateA() != null) {
                            triangle1.setTextureCoordinateA(triangle1.getTextureCoordinateA().add(vertexDataLeft.getTriangle2().getTextureCoordinateA()));
                            triangle1.setTextureCoordinateB(triangle1.getTextureCoordinateB().add(vertexDataLeft.getTriangle2().getTextureCoordinateA()));
                            triangle1.setTextureCoordinateC(triangle1.getTextureCoordinateC().add(vertexDataLeft.getTriangle2().getTextureCoordinateA()));
                        }
                    }

                    vertexList.add(triangle1);
                }

                if (!slopMeshGroup.isNoneOfTriangleContained(false, bottomLeftIndex)) {
                    Triangle triangle2 = vertexData.getTriangle2();
                    triangle2.setupTextureProjection(new TextureCoordinateCalculator(triangle2));
                    triangle2.adjustTextureCoordinate();
                    if (triangle1 != null) {
                        triangle2.setTextureCoordinateA(triangle2.getTextureCoordinateA().add(triangle1.getTextureCoordinateA()));
                        triangle2.setTextureCoordinateB(triangle2.getTextureCoordinateB().add(triangle1.getTextureCoordinateA()));
                        triangle2.setTextureCoordinateC(triangle2.getTextureCoordinateC().add(triangle1.getTextureCoordinateA()));
                    }
                    under = triangle1;
                    vertexList.add(triangle2);
                }
            }
        }
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
