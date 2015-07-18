package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.math3d.TextureCoordinate;
import com.btxtech.client.math3d.Triangle;
import com.btxtech.client.math3d.Triangle2d;
import com.btxtech.client.math3d.Triangulator;
import com.btxtech.client.math3d.Vertex;
import com.btxtech.client.math3d.VertexListProvider;
import com.btxtech.game.jsre.client.common.Index;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 01.05.2015.
 */
public class PlateauTop implements VertexListProvider {
    private Plateau plateau;

    public PlateauTop(Plateau plateau) {
        this.plateau = plateau;
    }

    @Override
    public VertexList provideVertexList(ImageDescriptor imageDescriptor) {

        List<Vertex> vertices = plateau.getHillSideMesh().getTopVertices();
        List<Index> corners2d = new ArrayList<>();
        Index smallestPoint = new Index(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (Vertex vertex : vertices) {
            Index index = new Index((int) vertex.getX(), (int) vertex.getY());
            if (corners2d.isEmpty() || !index.equals(corners2d.get(corners2d.size() - 1))) {
                corners2d.add(index);
                smallestPoint = smallestPoint.getSmallestPoint(index);
            }
        }

//        List<Index> corners2d = new ArrayList<>();
//        for (PlateauCorner plateauCorner : plateau.getPolygon().getTerrainPolygonCorners()) {
//            corners2d.add(plateauCorner.getPoint());
//        }

        Triangulator triangulator = new Triangulator();
        List<Triangle2d> triangles2d = triangulator.calculate(corners2d);

        int height = plateau.getHeight();
        VertexList vertexList = new VertexList();
        for (Triangle2d triangle2d : triangles2d) {
            Vertex vertexA = new Vertex(triangle2d.getPointA().getX(), triangle2d.getPointA().getY(), height);
            Vertex vertexB = new Vertex(triangle2d.getPointB().getX(), triangle2d.getPointB().getY(), height);
            Vertex vertexC = new Vertex(triangle2d.getPointC().getX(), triangle2d.getPointC().getY(), height);
            vertexList.add(Triangle.createTriangleWithNorm(vertexA, createTextureCoordinate(triangle2d.getPointA(), smallestPoint, imageDescriptor),
                    vertexB, createTextureCoordinate(triangle2d.getPointB(), smallestPoint, imageDescriptor),
                    vertexC, createTextureCoordinate(triangle2d.getPointC(), smallestPoint, imageDescriptor),
                    new Vertex(0, 0, 1.0)));
        }

        return vertexList;
    }

    private TextureCoordinate createTextureCoordinate(Index position, Index smallestPoint, ImageDescriptor imageDescriptor) {
        return new TextureCoordinate((double)(position.getX() - smallestPoint.getX()) / (double)imageDescriptor.getWidth(),
                (double)(position.getY() - smallestPoint.getY()) / (double)imageDescriptor.getHeight());
    }
}
