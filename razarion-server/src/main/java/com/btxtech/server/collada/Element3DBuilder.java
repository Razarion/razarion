package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        return new Element3D()
                .id(id)
                .vertexContainers(vertexContainerBuilders.stream()
                        .map(vertexContainerBuilder -> vertexContainerBuilder.getVertexContainer(shape3DId, id))
                        .collect(Collectors.toList()));
    }

    public List<VertexContainerBuffer> createVertexContainerBuffers(int shape3DId) {
        List<VertexContainerBuffer> vertexContainerBuffers = new ArrayList<>();
        for (VertexContainerBuilder vertexContainerBuilder : vertexContainerBuilders) {
            vertexContainerBuffers.add(vertexContainerBuilder.getVertexContainerBuffer(shape3DId, id));
        }
        return vertexContainerBuffers;
    }
}
