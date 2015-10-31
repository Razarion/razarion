package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.client.renderer.model.MeshGroup;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 17.10.2015.
 */
public class Plateau {
    private static final int PLANE_TOP_HEIGHT = 100;
    private static final int SLOPE_WIDTH = 3;
    private static final Rectangle INDEX_RECT = new Rectangle(7, 9, 11, 8);
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
                if (INDEX_RECT.contains2(new DecimalPosition(index))) {
                    planeMeshGroup.add(index);
                    mesh.setVertex(index, vertex.add(0, 0, PLANE_TOP_HEIGHT));
                } else {
                    double distance = INDEX_RECT.getNearestPointInclusive(new DecimalPosition(index)).getDistance(new DecimalPosition(index));
                    if (distance < SLOPE_WIDTH) {
                        slopMeshGroup.add(index);
                        mesh.setVertex(index, vertex.add(0, 0, PLANE_TOP_HEIGHT - PLANE_TOP_HEIGHT * distance / (double) SLOPE_WIDTH));
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

        // Iterator over outer rectangle
        // OUTER_RECT.

        // Old
        // Start with specific triangle 11:8
        Index start = new Index(7, 6);
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

    public VertexList provideVertexListSlope2(ImageDescriptor imageDescriptor) {
        final Rectangle rect = new Rectangle(mesh.getVertexData(INDEX_RECT.getStart()).getVertex().toXY().getPosition(), mesh.getVertexData(INDEX_RECT.getEnd()).getVertex().toXY().getPosition());
        final VertexList vertexList = new VertexList();
        final double slopeAngle = Math.tan((double) PLANE_TOP_HEIGHT / (double) (SLOPE_WIDTH * TerrainSurface.MESH_EDGE_LENGTH));

        mesh.iterateOverTriangles(new Mesh.TriangleVisitor() {
            @Override
            public void onVisit(Index bottomLeftIndex, Vertex bottomLeftVertex, Triangle triangle1, Triangle triangle2) {
                Mesh.VertexData vertexData = mesh.getVertexDataSafe(bottomLeftIndex);
                if (!slopMeshGroup.isNoneOfTriangleContained(true, bottomLeftIndex)) {
                    Triangle triangle = vertexData.getTriangle1();
                    Vertex startTriangle = getStartVertex(triangle);
                    DecimalPosition positionOnRect = rect.getNearestPoint(startTriangle.toXY());
                    Vertex topVertex = new Vertex(positionOnRect.getX(), positionOnRect.getY(), PLANE_TOP_HEIGHT);
                    logger.severe("bottomLeftIndex: " + bottomLeftIndex + " topVertex: " + topVertex);
                    Matrix4 rotationMatrix = Matrix4.createYRotation(-slopeAngle);
                    rotationMatrix = rotationMatrix.multiply(Matrix4.createZRotation(topVertex.toXY().getAngleToNorth(startTriangle.toXY())));
                    Vertex tDirection = rotationMatrix.multiply(new Vertex(1, 0, 0), 1.0);
                    Vertex groundIntersection = calculateGroundIntersection(topVertex, tDirection);


                    triangle.setTextureCoordinate(groundIntersection, groundIntersection.add(new Vertex(1, 0, 0)), groundIntersection.add(tDirection));

                    vertexList.add(triangle);
                }
            }
        });

        vertexList.normalize(imageDescriptor);
        return vertexList;
    }

    private Vertex calculateGroundIntersection(Vertex start, Vertex direction) {
        // http://www.rither.de/a/mathematik/lineare-algebra-und-analytische-geometrie/schnittprobleme/gerade-schneidet-ebene/
        // Koordinatenform
        // Plane Cartesian equation
        // a x + b y + c z = d
        // Ground plane
        // a = 0, b = 0, c = 1, d = 0
        // Line
        // start + d * direction
        // resolve to d (distance)

        double d = -start.getZ() / direction.getZ();
        return start.add(direction.multiply(d));
    }

    private Vertex getStartVertex(Triangle triangle) {
        Vertex normal = triangle.calculateNorm().normalize(1.0);
        Vertex sAxis = new Vertex(0, 0, 1).cross(normal);
        if (sAxis.magnitude() == 0.0) {
            logger.severe("sAxis.magnitude() == 0.0");
            logger.severe("triangle: " + triangle);
        }

        if (sAxis.projection(triangle.getVertexA()) < sAxis.projection(triangle.getVertexB())) {
            if (sAxis.projection(triangle.getVertexA()) < sAxis.projection(triangle.getVertexC())) {
                return triangle.getVertexA();
            } else {
                return triangle.getVertexC();
            }
        } else {
            if (sAxis.projection(triangle.getVertexB()) < sAxis.projection(triangle.getVertexC())) {
                return triangle.getVertexB();
            } else {
                return triangle.getVertexC();
            }
        }
    }
}
