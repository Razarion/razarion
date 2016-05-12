package com.btxtech.shared.dto;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.Collection;

/**
 * Created by Beat
 * 10.05.2016.
 */
@Portable
public class TerrainObject {
    private int id;
    private Collection<VertexContainer> vertexContainers;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Collection<VertexContainer> getVertexContainers() {
        return vertexContainers;
    }

    public void setVertexContainers(Collection<VertexContainer> vertexContainers) {
        this.vertexContainers = vertexContainers;
    }
}
