package com.btxtech.client.renderer.model;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.TextureCoordinateCalculator;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 16.10.2015.
 */
public class MeshGroup {
    private Collection<Index> members = new ArrayList<>();
    private Mesh mesh;
    private Logger logger = Logger.getLogger(MeshGroup.class.getName());

    public MeshGroup(Mesh mesh) {
        this.mesh = mesh;
    }

    public void add(Index index) {
        members.add(index);
    }

    public VertexList provideVertexListPlain(ImageDescriptor topImageDescriptor) {
        final VertexList vertexList = new VertexList();
        final TextureCoordinateCalculator textureCoordinateCalculator = new TextureCoordinateCalculator(new Vertex(1, 0, 0), new Vertex(0, 1, 0));

        mesh.iterateOverTriangles(new Mesh.TriangleVisitor() {
            @Override
            public void onVisit(Index bottomLeftIndex, Vertex bottomLeftVertex, Triangle triangle1, Triangle triangle2) {
                if (isAllOfTriangleContained(true, bottomLeftIndex)) {
                    // logger.severe("MeshGroup 1: " + bottomLeftIndex);
                    triangle1.setupTextureProjection(textureCoordinateCalculator);
                    vertexList.add(triangle1);
                }

                if (isAllOfTriangleContained(false, bottomLeftIndex)) {
                    // logger.severe("MeshGroup 2: " + bottomLeftIndex);
                    triangle2.setupTextureProjection(textureCoordinateCalculator);
                    vertexList.add(triangle2);
                }
            }
        });

        vertexList.normalize(topImageDescriptor);
        return vertexList;
    }

    public boolean isNoneOfSquadContained(Index bottomLeft) {
        return !members.contains(bottomLeft) && !members.contains(bottomLeft.add(1, 0)) && !members.contains(bottomLeft.add(0, 1)) && !members.contains(bottomLeft.add(1, 1));
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
