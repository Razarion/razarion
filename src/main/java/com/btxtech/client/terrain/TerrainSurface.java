package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.client.renderer.model.MeshGroup;
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
    private static final int MESH_EDGE_LENGTH = 32;
    private static final int PLANE_TOP_HEIGHT = 100;
    private static final int SLOPE_LEVEL_COUNT = 4;
    private static final int SLOPE_DISTANCE = MESH_EDGE_LENGTH * SLOPE_LEVEL_COUNT;
    private static final Rectangle INNER_RECT = new Rectangle(new Index(200, 300), new Index(600, 600));
    private static final Rectangle OUTER_RECT = INNER_RECT.grow(SLOPE_DISTANCE);
    private static final List<Integer> SLOPE_INDICES = Arrays.asList(PLANE_TOP_HEIGHT, 99, 60, 30, 0);
    private static final int LOWEST_SLOPE_INDEX = SLOPE_INDICES.size() - 1;
    private Mesh mesh = new Mesh();
    private ImageDescriptor topImageDescriptor = ImageDescriptor.TEX_DEV_2;
    private ImageDescriptor blendImageDescriptor = ImageDescriptor.TEX_DEV_2;
    private ImageDescriptor bottomImageDescriptor = ImageDescriptor.TEX_DEV_2;
    private double edgeDistance = 0.5;
    private double roughnessTop;
    private double roughnessHillside;
    private double roughnessGround;
    private MeshGroup planeMeshGroup;
    private MeshGroup slopMeshGroup;

    // private Logger logger = Logger.getLogger(TerrainSurface.class.getName());

    @PostConstruct
    public void init() {
        mesh.fill(1024, 1024, MESH_EDGE_LENGTH);
        final Collection<Index> topIndices = new ArrayList<>();
        // Mark top vertices
        planeMeshGroup = mesh.createMeshGroup();
        slopMeshGroup = mesh.createMeshGroup();
        mesh.iterate(new Mesh.Visitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                if (INNER_RECT.contains2(vertex.toXY())) {
                    planeMeshGroup.add(index);
                    mesh.setVertex(index, vertex.add(0, 0, PLANE_TOP_HEIGHT));
                } else if (OUTER_RECT.contains2(vertex.toXY())) {
                    double distance = INNER_RECT.getNearestPointInclusive(vertex.toXY()).getDistance(vertex.toXY());
                    int slopeLevel = SLOPE_LEVEL_COUNT - (int) (distance / MESH_EDGE_LENGTH);
                    slopMeshGroup.add(index);
                    mesh.setVertex(index, vertex.add(0, 0, PLANE_TOP_HEIGHT * slopeLevel / SLOPE_LEVEL_COUNT));
                } else {
                    planeMeshGroup.add(index);
                }
            }
        });
    }

    private void randomize(Collection<Index> indices, double roughness) {
        for (Index hillsideIndex : indices) {
            mesh.randomNorm(hillsideIndex, roughness);
        }
    }

    public VertexList getPlainVertexList() {
        return planeMeshGroup.provideVertexList(topImageDescriptor);
    }

    public VertexList getSlopeVertexList() {
        return slopMeshGroup.provideVertexList(topImageDescriptor);
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
