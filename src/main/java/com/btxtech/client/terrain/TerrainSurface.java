package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 09.08.2015.
 */
@Singleton
public class TerrainSurface {
    private static final int MESH_EDGE_LENGTH = 20;
    private static final int PLANE_TOP_HEIGHT = 100;
    private static final Rectangle INNER_RECT = new Rectangle(new Index(200, 300), new Index(600, 600));
    private static final List<Integer> SLOPE_INDICES = Arrays.asList(PLANE_TOP_HEIGHT, 99, 60, 30, 0);
    private static final int LOWEST_SLOPE_INDEX = SLOPE_INDICES.size() - 1;
    private Mesh mesh = new Mesh();
    private ImageDescriptor topImageDescriptor = ImageDescriptor.GRASS_IMAGE;
    private ImageDescriptor blendImageDescriptor = ImageDescriptor.BLEND_2;
    private ImageDescriptor bottomImageDescriptor = ImageDescriptor.SAND_2;
    private double edgeDistance = 0.5;
    private boolean changed = true;
    private double roughnessTop;
    private double roughnessHillside;
    private double roughnessGround;
    // private Logger logger = Logger.getLogger(TerrainSurface.class.getName());

    @PostConstruct
    public void init() {
        mesh.fill(4000, 4000, MESH_EDGE_LENGTH);
        final Collection<Index> topIndices = new ArrayList<>();
        // Mark top vertices
        mesh.iterate(new Mesh.Visitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                if (INNER_RECT.contains2(vertex.toXY())) {
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

    public VertexList getPlainVertexList() {
        return mesh.provideVertexList(topImageDescriptor, Triangle.Type.PLAIN);
    }

    public VertexList getSlopeVertexList() {
        return mesh.provideVertexList(topImageDescriptor, Triangle.Type.SLOPE);
    }

    public ImageDescriptor getTopImageDescriptor() {
        return topImageDescriptor;
    }

    public ImageDescriptor getBlendImageDescriptor() {
        return blendImageDescriptor;
    }

    public ImageDescriptor getBottomImageDescriptor() {
        return bottomImageDescriptor;
    }

    public double getEdgeDistance() {
        return edgeDistance;
    }

    public void setEdgeDistance(double edgeDistance) {
        this.edgeDistance = edgeDistance;
    }

    public double getRoughnessTop() {
        return roughnessTop;
    }

    public void setRoughnessTop(double roughnessTop) {
        this.roughnessTop = roughnessTop;
    }

    public double getRoughnessHillside() {
        return roughnessHillside;
    }

    public void setRoughnessHillside(double roughnessHillside) {
        this.roughnessHillside = roughnessHillside;
    }

    public double getRoughnessGround() {
        return roughnessGround;
    }

    public void setRoughnessGround(double roughnessGround) {
        this.roughnessGround = roughnessGround;
    }
}
