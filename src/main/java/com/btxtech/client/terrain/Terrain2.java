package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.math3d.Mesh;
import com.btxtech.client.math3d.Triangle2d;
import com.btxtech.client.math3d.Vertex;
import com.btxtech.client.math3d.VertexListProvider;
import com.btxtech.game.jsre.client.common.Index;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 03.08.2015.
 */
@Singleton
public class Terrain2 implements VertexListProvider {
    public List<Index> corners = Arrays.asList(new Index(200, 290), new Index(600, 290), new Index(307, 461));
    private Mesh mesh;

    public Terrain2() {
        mesh = new Mesh();
        mesh.fill(1000, 1000, 20);
        final Triangle2d triangle2d = new Triangle2d(corners.get(0), corners.get(1), corners.get(2));
        mesh.iterate(new Mesh.Visitor() {
            @Override
            public void onVisit(int x, int z, Vertex vertex) {
                Index index = vertex.toXY().getPosition();
                if (triangle2d.isInside(index)) {
                    mesh.setVertex(x, z, new Vertex(vertex.getX(), vertex.getY(), 100));
                }
            }
        });
    }

    @Override
    public VertexList provideVertexList(ImageDescriptor imageDescriptor) {
        return mesh.provideVertexList(imageDescriptor);
    }
}
