package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.VertexList;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.Collection;

/**
 * Created by Beat
 * 09.08.2015.
 */
@Singleton
public class TerrainSurface {
    public static final int MESH_EDGE_LENGTH = 8;
    private Mesh mesh = new Mesh();
    private ImageDescriptor topImageDescriptor = ImageDescriptor.GRASS_IMAGE;
    private ImageDescriptor blendImageDescriptor = ImageDescriptor.SAND_2;
    private ImageDescriptor bottomImageDescriptor = ImageDescriptor.SAND_2;
    private double edgeDistance = 0.5;
    private double roughnessTop;
    private double roughnessHillside;
    private double roughnessGround;
    private Plateau plateau;

    // private Logger logger = Logger.getLogger(TerrainSurface.class.getName());

    @PostConstruct
    public void init() {
        mesh.fill(1024, 1024, MESH_EDGE_LENGTH);

        plateau = new Plateau(mesh);
        plateau.sculpt();
        mesh.generateAllTriangle();
        mesh.adjustNorm();
    }

    private void randomize(Collection<Index> indices, double roughness) {
        for (Index hillsideIndex : indices) {
            mesh.randomNorm(hillsideIndex, roughness);
        }
    }

    public VertexList getVertexList() {
        return mesh.provideVertexList(topImageDescriptor);
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
