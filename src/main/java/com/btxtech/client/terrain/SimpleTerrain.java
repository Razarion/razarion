package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.math3d.Mesh;
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

//        mesh.setVertex(new Index(0,0), new Vertex(0, 0, 0), Mesh.Type.PLANE);
//        mesh.setVertex(new Index(0,1), new Vertex(0, 100, 0), Mesh.Type.PLANE);
//        mesh.setVertex(new Index(1,0), new Vertex(100, 0, 0), Mesh.Type.PLANE);
//        mesh.setVertex(new Index(1,1), new Vertex(100, 100, 0), Mesh.Type.SLOPE);


        mesh.iterate(new Mesh.Visitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                if(index.equals(new Index(2,2))) {
                    //mesh.setVertex(index, vertex, Mesh.Type.SLOPE);
                    mesh.setVertex(index, new Vertex(vertex.getX(), vertex.getX(), 100), Mesh.Type.SLOPE);
                } else {
                    mesh.setVertex(index, vertex, Mesh.Type.PLANE);
                }
            }
        });

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
}
