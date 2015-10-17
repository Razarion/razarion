package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.client.renderer.model.MeshGroup;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.shared.VertexList;
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
    public static final int MESH_EDGE_LENGTH = 32;
    private Mesh mesh = new Mesh();
    private ImageDescriptor topImageDescriptor = ImageDescriptor.TEX_DEV_2;
    private ImageDescriptor blendImageDescriptor = ImageDescriptor.TEX_DEV_2;
    private ImageDescriptor bottomImageDescriptor = ImageDescriptor.TEX_DEV_2;
    private double edgeDistance = 0.5;
    private double roughnessTop;
    private double roughnessHillside;
    private double roughnessGround;
    private MeshGroup planeMeshGroup;
    private Plateau plateau;

    // private Logger logger = Logger.getLogger(TerrainSurface.class.getName());

    @PostConstruct
    public void init() {
        mesh.fill(1024, 1024, MESH_EDGE_LENGTH);

        planeMeshGroup = mesh.createMeshGroup();

        plateau = new Plateau(mesh);
        plateau.assignVertices(planeMeshGroup);
    }

    private void randomize(Collection<Index> indices, double roughness) {
        for (Index hillsideIndex : indices) {
            mesh.randomNorm(hillsideIndex, roughness);
        }
    }

    public VertexList getPlainVertexList() {
        return planeMeshGroup.provideVertexListPlain(topImageDescriptor, false);
    }

    public VertexList getSlopeVertexList() {
        return plateau.provideVertexListSlope(topImageDescriptor);
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
