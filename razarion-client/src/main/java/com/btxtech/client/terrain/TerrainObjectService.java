package com.btxtech.client.terrain;

import com.btxtech.client.ColladaUiService;
import com.btxtech.client.ImageDescriptor;
import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.gameengine.pathing.ModelMatrices;

import javax.inject.Inject;
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
    @Inject
    private ColladaUiService colladaUiService;
    private Logger logger = Logger.getLogger(TerrainObjectService.class.getName());
    private ImageDescriptor opaqueDescriptor = ImageDescriptor.SAND_2;
    private ImageDescriptor transparentDescriptor = ImageDescriptor.TREE_TEXTURE_02;
    private Map<Integer, TerrainObject> terrainObjects;
    private Collection<TerrainObjectPosition> terrainObjectPositions;
    private Map<Integer, VertexContainer> opaqueIds;
    private Map<Integer, VertexContainer> transparentNoShadowIds;
    private Map<Integer, VertexContainer> transparentOnlyShadowIds;
    private Map<Integer, Collection<ModelMatrices>> objectIdMatrices;

    public void init() {
        setupModelMatrices(terrainObjectPositions);
        opaqueIds = new HashMap<>();
        transparentNoShadowIds = new HashMap<>();
        transparentOnlyShadowIds = new HashMap<>();
        for (TerrainObject terrainObject : terrainObjects.values()) {
            if (!objectIdMatrices.containsKey(terrainObject.getId())) {
                continue;
            }
            putVertexContainer(terrainObject);
        }
    }

    public void putVertexContainer(TerrainObject terrainObject) {
        for (Map.Entry<TerrainObject.Type, VertexContainer> vertexContainerEntry : terrainObject.getVertexContainers().entrySet()) {
            switch (vertexContainerEntry.getKey()) {
                case OPAQUE:
                    opaqueIds.put(terrainObject.getId(), vertexContainerEntry.getValue());
                    break;
                case TRANSPARENT_NO_SHADOW_CAST:
                    transparentNoShadowIds.put(terrainObject.getId(), vertexContainerEntry.getValue());
                    break;
                case TRANSPARENT_SHADOW_CAST_ONLY:
                    transparentOnlyShadowIds.put(terrainObject.getId(), vertexContainerEntry.getValue());
                    break;
                default:
                    logger.severe("Can not handle: " + vertexContainerEntry.getKey());
            }
        }
    }

    public void setupModelMatrices(Collection<TerrainObjectPosition> terrainObjectPositions) {
        objectIdMatrices = new HashMap<>();
        for (TerrainObjectPosition terrainObjectPosition : terrainObjectPositions) {
            Collection<ModelMatrices> modelMatrices = objectIdMatrices.get(terrainObjectPosition.getTerrainObjectId());
            if (modelMatrices == null) {
                modelMatrices = new ArrayList<>();
                objectIdMatrices.put(terrainObjectPosition.getTerrainObjectId(), modelMatrices);
            }
            modelMatrices.add(new ModelMatrices(terrainObjectPosition.createModelMatrix(colladaUiService.getGeneralScale()), terrainObjectPosition.createRotationModelMatrix()));
        }
    }

    public void setupModelMatrices() {
        setupModelMatrices(terrainObjectPositions);
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

    public Collection<ModelMatrices> getObjectIdMatrices(int terrainObjectId) {
        return objectIdMatrices.get(terrainObjectId);
    }
}
