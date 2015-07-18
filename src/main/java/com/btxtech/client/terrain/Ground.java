package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
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
        Vertex pointB = new Vertex(x + width, y, z);
        Vertex pointC = new Vertex(x, y + height, z);
        Vertex pointD = new Vertex(x + width, y + height, z);

        double textureWidth = (double) width / imageDescriptor.getWidth();
        double textureHeight = (double) height / imageDescriptor.getHeight();

        vertexList.add(new Triangle(pointA, new TextureCoordinate(0, 0),
                pointB, new TextureCoordinate(textureWidth, 0),
                pointC, new TextureCoordinate(0, textureHeight)));

        vertexList.add(new Triangle(pointD, new TextureCoordinate(textureWidth, textureHeight),
                pointC, new TextureCoordinate(0, textureHeight),
                pointB, new TextureCoordinate(textureWidth, 0)));

        return vertexList;
    }
}
