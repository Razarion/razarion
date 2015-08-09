package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.math3d.Mesh;
import com.btxtech.client.math3d.Vertex;
import com.btxtech.client.math3d.VertexListProvider;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Rectangle;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 03.08.2015.
 */
@Singleton
public class Terrain2 {
    public List<Index> corners = Arrays.asList(new Index(200, 290), new Index(600, 290), new Index(307, 461));
    private static final int HEIGHT = 100;
    private static final int SLOPE_WIDTH = 60;
    private Rectangle innerRect = new Rectangle(new Index(200, 300), new Index(600, 600));
    private Rectangle outerRect;
    private Mesh mesh;
    private static Terrain2 INSTANCE;
    private double roughnessTop;
    private double roughnessHillside;
    private double roughnessGround;

    public Terrain2() {
        INSTANCE = this;
        mesh = new Mesh();
        mesh.fill(1000, 1000, 20);

        outerRect = innerRect.copy();
        outerRect.growEast(SLOPE_WIDTH);
        outerRect.growNorth(SLOPE_WIDTH);
        outerRect.growSouth(SLOPE_WIDTH);
        outerRect.growWest(SLOPE_WIDTH);

        setupTerrain();
    }

    public void setupTerrain() {
        mesh.fill(1000, 1000, 20);
        final Collection<Index> topIndices = new ArrayList<>();
        final Collection<Index> hillsideIndices = new ArrayList<>();
        final Collection<Index> groundIndices = new ArrayList<>();
        mesh.iterate(new Mesh.Visitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                // Index index = vertex.toXY().getPosition();
                setupVertex(vertex, mesh, index, topIndices, hillsideIndices, groundIndices);
            }
        });

        randomize(topIndices, roughnessTop);
        randomize(hillsideIndices, roughnessHillside);
        randomize(groundIndices, roughnessGround);
    }

    private void randomize(Collection<Index> indices, double roughness) {
        for (Index hillsideIndex : indices) {
            mesh.randomNorm(hillsideIndex, roughness);
        }
    }

    private void setupVertex(Vertex vertex, Mesh mesh, Index index, Collection<Index> topIndices, Collection<Index> hillsideIndices, Collection<Index> groundIndices) {
        DecimalPosition position2d = vertex.toXY();
        if (outerRect.contains(position2d.getPosition()) && !innerRect.contains(position2d.getPosition())) {
            DecimalPosition pointOnInner = innerRect.getNearestPoint(position2d);
            double distance = pointOnInner.getDistance(position2d);
            if (distance > SLOPE_WIDTH) {
                mesh.setVertex(index, vertex, Mesh.Type.PLANE);
                groundIndices.add(index);
            } else {
                double factor = 1.0 - distance / SLOPE_WIDTH;
                hillsideIndices.add(index);
                mesh.setVertex(index, new Vertex(vertex.getX(), vertex.getY(), HEIGHT * factor), Mesh.Type.SLOPE);
            }
        } else if (innerRect.contains(position2d.getPosition())) {
            mesh.setVertex(index, new Vertex(vertex.getX(), vertex.getY(), HEIGHT), Mesh.Type.PLANE);
            topIndices.add(index);
        } else {
            mesh.setVertex(index, vertex, Mesh.Type.PLANE);
            groundIndices.add(index);
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

    public static Terrain2 getInstance() {
        return INSTANCE;
    }

    public double getRoughnessHillside() {
        return roughnessHillside;
    }

    public void setRoughnessHillside(double roughnessHillside) {
        this.roughnessHillside = roughnessHillside;
    }

    public double getRoughnessTop() {
        return roughnessTop;
    }

    public void setRoughnessTop(double roughnessTop) {
        this.roughnessTop = roughnessTop;
    }

    public double getRoughnessGround() {
        return roughnessGround;
    }

    public void setRoughnessGround(double roughnessGround) {
        this.roughnessGround = roughnessGround;
    }
}
