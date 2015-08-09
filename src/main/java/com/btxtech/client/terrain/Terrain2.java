package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.math3d.Mesh;
import com.btxtech.client.math3d.Triangle2d;
import com.btxtech.client.math3d.Vertex;
import com.btxtech.client.math3d.VertexListProvider;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainPolygon;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainPolygonCorner;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainPolygonLine;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 03.08.2015.
 */
@Singleton
public class Terrain2 {
    public List<Index> corners = Arrays.asList(new Index(200, 290), new Index(600, 290), new Index(307, 461));
    private static final int HEIGHT = 100;
    private static final int SLOPE_WIDTH = 100;
    private Rectangle innerRect = new Rectangle(new Index(200, 300), new Index(600, 600));
    private Rectangle outerRect;
    private Mesh mesh;

    public Terrain2() {
        mesh = new Mesh();
        mesh.fill(1000, 1000, 20);

        outerRect = innerRect.copy();
        outerRect.growEast(SLOPE_WIDTH);
        outerRect.growNorth(SLOPE_WIDTH);
        outerRect.growSouth(SLOPE_WIDTH);
        outerRect.growWest(SLOPE_WIDTH);

        TerrainPolygon<TerrainPolygonCorner, TerrainPolygonLine> terrainPolygon = new TerrainPolygon<>(corners);
        for (TerrainPolygonCorner terrainPolygonCorner : terrainPolygon.getTerrainPolygonCorners()) {
            terrainPolygonCorner.getOutsideNormal(SLOPE_WIDTH);
        }


        final Triangle2d triangle2d = new Triangle2d(corners.get(0), corners.get(1), corners.get(2));
        mesh.iterate(new Mesh.Visitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                // Index index = vertex.toXY().getPosition();
                setupVertex(vertex, mesh, index);
            }
        });
    }

    private void setupVertex(Vertex vertex, Mesh mesh, Index index) {
        DecimalPosition position2d = vertex.toXY();
        if (outerRect.contains(position2d.getPosition()) && !innerRect.contains(position2d.getPosition())) {
            DecimalPosition pointOnInner = innerRect.getNearestPoint(position2d);
            double distance = pointOnInner.getDistance(position2d);
            if (distance > SLOPE_WIDTH) {
                mesh.setVertex(index, vertex, Mesh.Type.PLANE);
            } else {
                double factor = 1.0 - distance / SLOPE_WIDTH;
                mesh.setVertex(index, new Vertex(vertex.getX(), vertex.getY(), HEIGHT * factor), Mesh.Type.SLOPE);
            }
        } else if (innerRect.contains(position2d.getPosition())) {
            mesh.setVertex(index, new Vertex(vertex.getX(), vertex.getY(), HEIGHT), Mesh.Type.PLANE);
        } else {
            mesh.setVertex(index, vertex, Mesh.Type.PLANE);
        }
    }

    public VertexListProvider getPlainProvider() {
        return new VertexListProvider() {
            @Override
            public VertexList provideVertexList(ImageDescriptor imageDescriptor) {
                return mesh.provideVertexList(imageDescriptor, Mesh.Type.PLANE);
            }
        };
    }

    public VertexListProvider getSlopeProvider() {
        return new VertexListProvider() {
            @Override
            public VertexList provideVertexList(ImageDescriptor imageDescriptor) {
                return mesh.provideVertexList(imageDescriptor, Mesh.Type.SLOPE);
            }
        };
    }
}
