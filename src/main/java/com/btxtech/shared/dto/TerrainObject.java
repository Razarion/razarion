package com.btxtech.shared.dto;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Beat
 * 10.05.2016.
 */
@Portable
public class TerrainObject {
    public enum Type {
        OPAQUE,
        TRANSPARENT_NO_SHADOW_CAST,
        TRANSPARENT_SHADOW_CAST_ONLY
    }
    private int id;
    private Map<Type, VertexContainer> vertexContainers;

    /**
     * Used by errai
     */
    public TerrainObject() {
    }

    public TerrainObject(int id, Map<Type, VertexContainer> vertexContainers) {
        this.id = id;
        this.vertexContainers = vertexContainers;
    }

    public int getId() {
        return id;
    }

    public Map<Type, VertexContainer> getVertexContainers() {
        return vertexContainers;
    }
}
