package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.Webgl;
import com.btxtech.client.math3d.TextureCoordinate;
import com.btxtech.client.math3d.Triangle;
import com.btxtech.client.math3d.Vertex;
import com.btxtech.client.math3d.VertexListProvider;

/**
 * Created by Beat
 * 30.04.2015.
 */
public class Ground implements VertexListProvider {
    private final int x;
    private final int y;
    private final int z;
    private final int width;
    private final int height;

    public Ground(int x, int y, int z, int width, int height) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
    }

    @Override
    public VertexList provideVertexList(ImageDescriptor imageDescriptor) {
        VertexList vertexList = new VertexList();
        Vertex pointA = new Vertex(x, y, z);
        Vertex pointB = new Vertex(x, y + height, z);
        Vertex pointC = new Vertex(x + width, y + height, z);
        Vertex pointD = new Vertex(x + width, y, z);

        double textureWidth = (double) width / (double) Webgl.TEX_IMAGE_WIDTH;
        double textureHeight = (double) height / (double) Webgl.TEX_IMAGE_HEIGHT;

        vertexList.add(Triangle.createTriangleWithNorm(pointA, new TextureCoordinate(0, textureHeight),
                pointB, new TextureCoordinate(0, 0),
                pointC, new TextureCoordinate(textureWidth, 0),
                new Vertex(0, 0, 1)));

        vertexList.add(Triangle.createTriangleWithNorm(pointA, new TextureCoordinate(0, textureHeight),
                pointC, new TextureCoordinate(textureWidth, 0),
                pointD, new TextureCoordinate(textureWidth, textureHeight),
                new Vertex(0, 0, 1)));
        return vertexList;
    }
}
