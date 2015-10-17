package com.btxtech.client.renderer.model;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 16.10.2015.
 */
public class MeshGroup {
    private Collection<Index> members = new ArrayList<>();
    private Mesh mesh;

    public MeshGroup(Mesh mesh) {
        this.mesh = mesh;
    }

    public void add(Index index) {
        members.add(index);
    }

    public VertexList provideVertexListPlain(ImageDescriptor topImageDescriptor, final boolean inclusive) {
        final VertexList vertexList = new VertexList();
        final Vertex sAxis = new Vertex(1, 0, 0);
        final Vertex tAxis = new Vertex(0, 1, 0);

        mesh.iterateExclude(new Mesh.Visitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                if (inclusive) {
                    if (!members.contains(index) && !members.contains(index.add(1, 0)) && !members.contains(index.add(0, 1)) && !members.contains(index.add(1, 1))) {
                        return;
                    }
                } else {
                    if (!members.contains(index) || !members.contains(index.add(1, 0)) || !members.contains(index.add(0, 1)) || !members.contains(index.add(1, 1))) {
                        return;
                    }
                }

                Triangle triangle1 = mesh.generateTriangle(true, index);
                Triangle triangle2 = mesh.generateTriangle(false, index);
                triangle1.setupTextureProjection(sAxis, tAxis);
                triangle2.setupTextureProjection(sAxis, tAxis);
                vertexList.add(triangle1);
                vertexList.add(triangle2);
            }
        });
        vertexList.normalize(topImageDescriptor);
        return vertexList;
    }
}
