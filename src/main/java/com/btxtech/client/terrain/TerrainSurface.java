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
    private ImageDescriptor imageDescriptor = Terrain.SAND_1;

    @Inject
    private Logger logger;

    public TerrainSurface() {
        mesh = new Mesh();
        mesh.fill(4000, 4000, 4000);

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
        return mesh.provideVertexList(imageDescriptor, Triangle.Type.PLAIN);
    }

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }
}
