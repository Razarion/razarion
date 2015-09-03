package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;
import com.btxtech.client.renderer.model.VertexListProvider;
import com.btxtech.game.jsre.client.common.Index;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 09.08.2015.
 */
// @Singleton
public class SimpleTerrain {
    private Mesh mesh;

    @Inject
    private Logger logger;

    public SimpleTerrain(int z) {
        mesh = new Mesh();
        mesh.fill(1024, 1024, 64, z);

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
}
