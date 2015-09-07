package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 09.08.2015.
 */
@Singleton
public class TerrainSurface {
    private Mesh mesh;
    private ImageDescriptor topImageDescriptor = Terrain.GRASS_IMAGE;
    private ImageDescriptor blendImageDescriptor = Terrain.BLEND_2;
    private ImageDescriptor bottomImageDescriptor = Terrain.SAND_2;
    private double edgeDistance = 0.5;

    @Inject
    private Logger logger;

    public TerrainSurface() {
        mesh = new Mesh();
        mesh.fill(4000, 4000, 100);

        mesh.iterate(new Mesh.Visitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                mesh.setVertex(index, vertex, Mesh.Type.PLANE_BOTTOM);
            }
        });


        final FractalField fractalField = new FractalField(FractalField.nearestPossibleNumber(mesh.getX(), mesh.getY()), 1);
        fractalField.normalize();
        mesh.iterate(new Mesh.Visitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                Mesh.VertexData vertexData = mesh.getVertexDataSafe(index);
                vertexData.setEdge(fractalField.get(index));
            }
        });

    }

    public VertexList getVertexList() {
        return mesh.provideVertexList(topImageDescriptor, Triangle.Type.PLAIN);
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
}
