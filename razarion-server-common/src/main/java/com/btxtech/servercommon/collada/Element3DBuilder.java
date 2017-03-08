package com.btxtech.servercommon.collada;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 08.03.2017.
 */
public class Element3DBuilder {
    private List<VertexContainerBuilder> vertexContainerBuilders = new ArrayList<>();
    private String id;

    public Element3DBuilder(String id) {
        this.id = id;
    }

    public void addVertexContainer(VertexContainer vertexContainer, VertexContainerBuffer vertexContainerBuffer) {
        vertexContainerBuilders.add(new VertexContainerBuilder(vertexContainer, vertexContainerBuffer));
    }

    public Element3D createElement3D(int shape3DId) {
        Element3D element3D = new Element3D();
        element3D.setId(id);

        List<VertexContainer> vertexContainers = new ArrayList<>();
        for (VertexContainerBuilder vertexContainerBuilder : vertexContainerBuilders) {
            vertexContainers.add(vertexContainerBuilder.getVertexContainer(shape3DId, id));
        }
        element3D.setVertexContainers(vertexContainers);
        return element3D;
    }

    public List<VertexContainerBuffer> createVertexContainerBuffers(int shape3DId) {
        List<VertexContainerBuffer> vertexContainerBuffers = new ArrayList<>();
        for (VertexContainerBuilder vertexContainerBuilder : vertexContainerBuilders) {
            vertexContainerBuffers.add(vertexContainerBuilder.getVertexContainerBuffer(shape3DId, id));
        }
        return vertexContainerBuffers;
    }
}
