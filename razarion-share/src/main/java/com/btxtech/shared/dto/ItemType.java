package com.btxtech.shared.dto;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 15.05.2016.
 */
@Portable
public class ItemType {
    private int id;
    private VertexContainer vertexContainer;

    /**
     * Used by errai
     */
    public ItemType() {
    }

    public ItemType(int id, VertexContainer vertexContainer) {
        this.id = id;
        this.vertexContainer = vertexContainer;
    }

    public int getId() {
        return id;
    }

    public VertexContainer getVertexContainer() {
        return vertexContainer;
    }
}
