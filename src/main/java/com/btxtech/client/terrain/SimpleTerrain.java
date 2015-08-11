package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.math3d.Mesh;
import com.btxtech.client.math3d.Triangle;
import com.btxtech.client.math3d.Vertex;
import com.btxtech.client.math3d.VertexListProvider;
import com.btxtech.game.jsre.client.common.Index;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 09.08.2015.
 */
@Singleton
public class SimpleTerrain {
    private Mesh mesh;

    public SimpleTerrain() {
        mesh = new Mesh();
        mesh.fill(400, 400, 100);

        mesh.iterate(new Mesh.Visitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                mesh.setVertex(index, vertex, Mesh.Type.PLANE_BOTTOM);
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
