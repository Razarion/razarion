package com.btxtech.client.renderer.model;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.TextureCoordinateCalculator;
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

    public VertexList provideVertexListPlain(ImageDescriptor topImageDescriptor) {
        final VertexList vertexList = new VertexList();
        final TextureCoordinateCalculator textureCoordinateCalculator = new TextureCoordinateCalculator(new Vertex(1, 0, 0), new Vertex(0, 1, 0));

        mesh.iterateExclude(new Mesh.Visitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                if (isAllOfTriangleContained(true, index)) {
                    Triangle triangle1 = mesh.generateTriangle(true, index);
                    triangle1.setupTextureProjection(textureCoordinateCalculator);
                    vertexList.add(triangle1);
                }

                if (isAllOfTriangleContained(false, index)) {
                    Triangle triangle2 = mesh.generateTriangle(false, index);
                    triangle2.setupTextureProjection(textureCoordinateCalculator);
                    vertexList.add(triangle2);
                }
            }
        });
        vertexList.normalize(topImageDescriptor);
        return vertexList;
    }

    public boolean isNoneOfTriangleContained(boolean isTriangle1, Index bottomLeft) {
        if (isTriangle1) {
            return !members.contains(bottomLeft) && !members.contains(bottomLeft.add(1, 0)) && !members.contains(bottomLeft.add(0, 1));
        } else {
            return !members.contains(bottomLeft.add(1, 0)) && !members.contains(bottomLeft.add(0, 1)) && !members.contains(bottomLeft.add(1, 1));
        }
    }

    public boolean isAllOfTriangleContained(boolean isTriangle1, Index bottomLeft) {
        if (isTriangle1) {
            return members.contains(bottomLeft) && members.contains(bottomLeft.add(1, 0)) && members.contains(bottomLeft.add(0, 1));
        } else {
            return members.contains(bottomLeft.add(1, 0)) && members.contains(bottomLeft.add(0, 1)) && members.contains(bottomLeft.add(1, 1));
        }
    }
}
