package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.primitives.Matrix4;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.09.2015.
 */
@Singleton
public class TerrainObjectService {
    private Logger logger = Logger.getLogger(TerrainObjectService.class.getName());
    private ImageDescriptor opaqueDescriptor = ImageDescriptor.SAND_2;
    private ImageDescriptor transparentDescriptor = ImageDescriptor.BRANCH_01;
    private Map<Integer, TerrainObject> terrainObjects;
    private Collection<TerrainObjectPosition> terrainObjectPositions;
    private Map<Integer, VertexContainer> opaqueIds;
    private Map<Integer, VertexContainer> transparentNoShadowIds;
    private Map<Integer, VertexContainer> transparentOnlyShadowIds;
    private Map<Integer, Collection<Matrix4>> objectIdMatrices;

    public void init() {
        objectIdMatrices = new HashMap<>();
        for (TerrainObjectPosition terrainObjectPosition : terrainObjectPositions) {
            Collection<Matrix4> matrices = objectIdMatrices.get(terrainObjectPosition.getTerrainObjectId());
            if (matrices == null) {
                matrices = new ArrayList<>();
                objectIdMatrices.put(terrainObjectPosition.getTerrainObjectId(), matrices);
            }
            matrices.add(terrainObjectPosition.createModelMatrix());
        }
        opaqueIds = new HashMap<>();
        transparentNoShadowIds = new HashMap<>();
        transparentOnlyShadowIds = new HashMap<>();
        for (Map.Entry<Integer, TerrainObject> entry : terrainObjects.entrySet()) {
            for (VertexContainer vertexContainer : entry.getValue().getVertexContainers()) {
                switch (vertexContainer.getType()) {
                    case OPAQUE:
                        opaqueIds.put(entry.getKey(), vertexContainer);
                        break;
                    case TRANSPARENT_NO_SHADOW_CAST:
                        transparentNoShadowIds.put(entry.getKey(), vertexContainer);
                        break;
                    case TRANSPARENT_SHADOW_CAST_ONLY:
                        transparentOnlyShadowIds.put(entry.getKey(), vertexContainer);
                        break;
                    default:
                        logger.severe("Can not handle: " + vertexContainer.getType());
                }
            }
        }
    }

    public ImageDescriptor getOpaqueDescriptor() {
        return opaqueDescriptor;
    }

    public ImageDescriptor getTransparentDescriptor() {
        return transparentDescriptor;
    }

    public Collection<Integer> getOpaqueIds() {
        return opaqueIds.keySet();
    }

    public VertexContainer getOpaqueVertexContainer(int id) {
        return opaqueIds.get(id);
    }

    public Collection<Integer> getTransparentNoShadowIds() {
        return transparentNoShadowIds.keySet();
    }

    public VertexContainer getTransparentNoShadow(int id) {
        return transparentNoShadowIds.get(id);
    }

    public VertexContainer getTransparentOnlyShadow(int id) {
        return transparentOnlyShadowIds.get(id);
    }


    public void setTerrainObjectPositions(Collection<TerrainObjectPosition> terrainObjectPositions) {
        this.terrainObjectPositions = terrainObjectPositions;
    }

    public void setTerrainObjects(Collection<TerrainObject> terrainObjects) {
        this.terrainObjects = new HashMap<>();
        for (TerrainObject terrainObject : terrainObjects) {
            this.terrainObjects.put(terrainObject.getId(), terrainObject);
        }
    }

    public Collection<Matrix4> getObjectIdMatrices(int terrainObjectId) {
        return objectIdMatrices.get(terrainObjectId);
    }
}
