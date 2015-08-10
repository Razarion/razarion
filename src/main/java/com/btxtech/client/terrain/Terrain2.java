package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.math3d.Mesh;
import com.btxtech.client.math3d.Triangle;
import com.btxtech.client.math3d.Vertex;
import com.btxtech.client.math3d.VertexListProvider;
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
    // public List<Index> corners = Arrays.asList(new Index(200, 290), new Index(600, 290), new Index(307, 461));
    private static final int EDGE_LENGTH = 20;
    private static final int PLANE_TOP_HEIGHT = 100;
    private static final List<Integer> SLOPE_INDICES = Arrays.asList(PLANE_TOP_HEIGHT, 99, 75, 50, 25, 0);
    private static final int LOWEST_SLOPE_INDEX = SLOPE_INDICES.size() - 1;
    private Rectangle innerRect = new Rectangle(new Index(200, 300), new Index(600, 600));
    private Mesh mesh;
    private static Terrain2 INSTANCE;
    private double roughnessTop;
    private double roughnessHillside;
    private double roughnessGround;
    private boolean changed = true;

    public Terrain2() {
        INSTANCE = this;
        mesh = new Mesh();
        mesh.fill(1000, 1000, 20);

        setupTerrain();
    }

    public void setupTerrain() {
        mesh.fill(1000, 1000, EDGE_LENGTH);
        final Collection<Index> topIndices = new ArrayList<>();
        // Mark top vertices
        mesh.iterate(new Mesh.Visitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                if (innerRect.contains2(vertex.toXY())) {
                    Mesh.VertexData vertexData = mesh.getVertexDataSafe(index);
                    vertexData.setType(Mesh.Type.PLANE_TOP);
                    vertexData.setVertex(new Vertex(vertex.getX(), vertex.getY(), PLANE_TOP_HEIGHT));
                    topIndices.add(index);
                }
            }
        });

        // Calculate distance to top index
        changed = true;
        while (changed) {
            changed = false;
            mesh.iterate(new Mesh.Visitor() {
                @Override
                public void onVisit(Index index, Vertex vertex) {
                    Mesh.VertexData vertexData = mesh.getVertexDataSafe(index);
                    if (vertexData.getType() == Mesh.Type.PLANE_TOP || vertexData.getSlopeIndex() != null) {
                        return;
                    }
                    if (mesh.hasPlaneTopAsNeighbour(index)) {
                        vertexData.setSlopeIndex(1);
                        changed = true;
                        return;
                    }
                    Integer slopeIndex = mesh.getHighestNeighbourSlopeIndex(index);
                    if (slopeIndex != null) {
                        vertexData.setSlopeIndex(slopeIndex + 1);
                        changed = true;
                    }
                }
            });
        }

        // Mark bottom vertices
        final Collection<Index> hillsideIndices = new ArrayList<>();
        final Collection<Index> groundIndices = new ArrayList<>();
        mesh.iterate(new Mesh.Visitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                Mesh.VertexData vertexData = mesh.getVertexDataSafe(index);
                if (vertexData.getSlopeIndex() == null) {
                    return;
                }

                int slopIndex = vertexData.getSlopeIndex();
                if (slopIndex > LOWEST_SLOPE_INDEX) {
                    vertexData.setType(Mesh.Type.PLANE_BOTTOM);
                    vertexData.setSlopeIndex(null);
                    groundIndices.add(index);
                } else {
                    vertexData.setType(Mesh.Type.SLOPE);
                    vertexData.setVertex(new Vertex(vertex.getX(), vertex.getY(), SLOPE_INDICES.get(slopIndex)));
                    hillsideIndices.add(index);
                }
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

    public VertexListProvider getPlainProvider() {
        return new VertexListProvider() {
            @Override
            public VertexList provideVertexList(ImageDescriptor imageDescriptor) {
                return mesh.provideVertexList(imageDescriptor, Triangle.Type.PLAIN);
            }
        };
    }

    public VertexListProvider getSlopeProvider() {
        return new VertexListProvider() {
            @Override
            public VertexList provideVertexList(ImageDescriptor imageDescriptor) {
                return mesh.provideVertexList(imageDescriptor, Triangle.Type.SLOPE);
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
