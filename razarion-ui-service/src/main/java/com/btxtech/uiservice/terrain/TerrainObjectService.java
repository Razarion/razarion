package com.btxtech.uiservice.terrain;

import com.btxtech.uiservice.ColladaUiService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 05.09.2015.
 */
@Singleton
@Deprecated // Is an UI service
public class TerrainObjectService {
    // private Logger logger = Logger.getLogger(TerrainObjectService.class.getName());
    @Inject
    private ColladaUiService colladaUiService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private Collection<TerrainObjectPosition> terrainObjectPositions;
    private Map<Integer, TerrainObject> terrainObjects;
    private Map<Integer, Collection<ModelMatrices>> objectIdMatrices;
    private Map<Integer, VertexContainer> vertexContainers;
    private Map<Integer, Integer> vertexContainers2TerrainObject;

    public void setTerrainObjects(Collection<TerrainObject> terrainObjects) {
        this.terrainObjects = new HashMap<>();
        if(terrainObjects == null) {
            return;
        }
        for (TerrainObject terrainObject : terrainObjects) {
            this.terrainObjects.put(terrainObject.getId(), terrainObject);
        }
    }

    public void setTerrainObjectPositions(Collection<TerrainObjectPosition> terrainObjectPositions) {
        this.terrainObjectPositions = terrainObjectPositions;
    }

    public void setup() {
        setupModelMatrices(terrainObjectPositions);
        vertexContainers = new HashMap<>();
        vertexContainers2TerrainObject = new HashMap<>();
        for (TerrainObject terrainObject : terrainObjects.values()) {
            if (!objectIdMatrices.containsKey(terrainObject.getId())) {
                continue;
            }
            for (VertexContainer vertexContainer : terrainObject.getVertexContainers()) {
                int artificialVertexContainerId = vertexContainers.size() + 1;
                vertexContainers.put(artificialVertexContainerId, vertexContainer);
                vertexContainers2TerrainObject.put(artificialVertexContainerId, terrainObject.getId());
            }
        }
    }

    public void setupModelMatrices(Collection<TerrainObjectPosition> terrainObjectPositions) {
        objectIdMatrices = new HashMap<>();
        if(terrainObjectPositions == null) {
            return;
        }
        for (TerrainObjectPosition terrainObjectPosition : terrainObjectPositions) {
            try {
                Collection<ModelMatrices> modelMatrices = objectIdMatrices.get(terrainObjectPosition.getTerrainObjectId());
                if (modelMatrices == null) {
                    modelMatrices = new ArrayList<>();
                    objectIdMatrices.put(terrainObjectPosition.getTerrainObjectId(), modelMatrices);
                }
                int z = (int) terrainUiService.getInterpolatedTerrainTriangle(new DecimalPosition(terrainObjectPosition.getPosition())).getHeight();
                modelMatrices.add(new ModelMatrices().setModel(terrainObjectPosition.createModelMatrix(colladaUiService.getGeneralScale(), z)).setNorm(terrainObjectPosition.createRotationModelMatrix()));
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }
    }

    public void setupModelMatrices() {
        setupModelMatrices(terrainObjectPositions);
    }

    public void overrideTerrainObject(TerrainObject terrainObject) {
        terrainObjects.put(terrainObject.getId(), terrainObject);
        setup();
    }

    public TerrainObject getTerrainObject(int id) {
        return terrainObjects.get(id);
    }

    public Collection<Integer> getVertexContainerIds() {
        return vertexContainers.keySet();
    }

    public VertexContainer getVertexContainer(int vertexContainerId) {
        return vertexContainers.get(vertexContainerId);
    }

    public int getTerrainObjectId4VertexContainer(int vertexContainerId) {
        return vertexContainers2TerrainObject.get(vertexContainerId);
    }

    public Collection<ModelMatrices> getModelMatrices(int terrainObjectId) {
        Collection<ModelMatrices> matrices = objectIdMatrices.get(terrainObjectId);
        if (matrices != null) {
            return matrices;
        } else {
            return Collections.emptyList();
        }
    }
}
