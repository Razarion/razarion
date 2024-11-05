package com.btxtech.server.persistence;

import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.object.TerrainObjectPositionEntity;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 08.07.2016.
 */
@Singleton
public class PlanetCrudPersistence extends AbstractConfigCrudPersistence<PlanetConfig, PlanetEntity> {
    @Inject
    private TerrainObjectCrudPersistence terrainObjectCrudPersistence;
    @Inject
    private BaseItemTypeCrudPersistence baseItemTypeCrudPersistence;
    @Inject
    private GroundCrudPersistence groundCrudPersistence;
    @Inject
    private WaterCrudPersistence waterCrudPersistence;
    @PersistenceContext
    private EntityManager entityManager;

    public PlanetCrudPersistence() {
        super(PlanetEntity.class, PlanetEntity_.id, PlanetEntity_.internalName);
    }

    @Override
    protected PlanetConfig toConfig(PlanetEntity planetEntity) {
        return planetEntity.toPlanetConfig();
    }

    @Override
    protected void fromConfig(PlanetConfig planetConfig, PlanetEntity planetEntity) {
        planetEntity.fromPlanetConfig(planetConfig,
                groundCrudPersistence.getEntity(planetConfig.getGroundConfigId()),
                waterCrudPersistence.getEntity(planetConfig.getWaterConfigId()),
                baseItemTypeCrudPersistence.getEntity(planetConfig.getStartBaseItemTypeId()),
                baseItemTypeLimitation(planetConfig.getItemTypeLimitation()));
    }

    private Map<BaseItemTypeEntity, Integer> baseItemTypeLimitation(Map<Integer, Integer> input) {
        if (input == null) {
            return Collections.emptyMap();
        }
        return input.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> baseItemTypeCrudPersistence.getEntity(entry.getKey()), Map.Entry::getValue));
    }

    @Override
    protected PlanetConfig newConfig() {
        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setSize(new DecimalPosition(640, 640));
        return planetConfig;
    }

    @Transactional
    public List<TerrainObjectPosition> getTerrainObjectPositions(int planetId) {
        PlanetEntity planetEntity = entityManager.find(PlanetEntity.class, planetId);
        if (planetEntity == null) {
            throw new IllegalArgumentException("No planet for id: " + planetId);
        }

        return planetEntity.getTerrainObjectPositionEntities()
                .stream()
                .map(TerrainObjectPositionEntity::toTerrainObjectPosition)
                .collect(Collectors.toList());
    }

    @Transactional
    @SecurityCheck
    public void createTerrainObjectPositions(int planetId, List<TerrainObjectPosition> createdTerrainObjects) {
        List<TerrainObjectPositionEntity> terrainObjectPositionEntities = new ArrayList<>();
        for (TerrainObjectPosition terrainObjectPosition : createdTerrainObjects) {
            TerrainObjectPositionEntity terrainObjectPositionEntity = new TerrainObjectPositionEntity();
            terrainObjectPositionEntity.fromTerrainObjectPosition(terrainObjectPosition, terrainObjectCrudPersistence);
            terrainObjectPositionEntities.add(terrainObjectPositionEntity);
        }

        PlanetEntity planetEntity = getEntity(planetId);
        planetEntity.getTerrainObjectPositionEntities().addAll(terrainObjectPositionEntities);
        entityManager.persist(planetEntity);
    }

    @Transactional
    @SecurityCheck
    public void updateTerrainObjectPositions(int planetId, List<TerrainObjectPosition> updatedTerrainObjects) {
        PlanetEntity planetEntity = getEntity(planetId);
        for (TerrainObjectPosition terrainObjectPosition : updatedTerrainObjects) {
            TerrainObjectPositionEntity terrainObjectPositionEntity = getTerrainObjectPositionEntity(planetEntity, terrainObjectPosition.getId());
            terrainObjectPositionEntity.fromTerrainObjectPosition(terrainObjectPosition, terrainObjectCrudPersistence);
        }
        entityManager.merge(planetEntity);
    }

    @Transactional
    @SecurityCheck
    public void deleteTerrainObjectPositionIds(int planetId, List<Integer> deletedTerrainIds) {
        PlanetEntity planetEntity = getEntity(planetId);
        for (int terrainSlopePositionId : deletedTerrainIds) {
            planetEntity.getTerrainObjectPositionEntities().remove(getTerrainObjectPositionEntity(planetEntity, terrainSlopePositionId));
        }
    }

    private TerrainObjectPositionEntity getTerrainObjectPositionEntity(PlanetEntity planetEntity, int id) {
        for (TerrainObjectPositionEntity terrainObjectPositionEntity : planetEntity.getTerrainObjectPositionEntities()) {
            if (terrainObjectPositionEntity.getId() == id) {
                return terrainObjectPositionEntity;
            }
        }
        throw new IllegalArgumentException("No TerrainObjectPositionEntity on planet for id: " + id);
    }

    @Transactional
    @SecurityCheck
    public void updateMiniMapImage(int planetId, byte[] data) {
        PlanetEntity planetEntity = entityManager.find(PlanetEntity.class, planetId);
        if (planetEntity == null) {
            throw new IllegalArgumentException("No planet for id: " + planetId);
        }
        planetEntity.setMiniMapImage(data);
    }

    @Transactional
    public byte[] getMiniMapImage(int planetId) {
        PlanetEntity planetEntity = entityManager.find(PlanetEntity.class, planetId);
        if (planetEntity == null) {
            throw new IllegalArgumentException("No planet for id: " + planetId);
        }
        return planetEntity.getMiniMapImage();
    }

    @Transactional
    @SecurityCheck
    public void updateCompressedHeightMap(int planetId, byte[] data) {
        PlanetEntity planetEntity = entityManager.find(PlanetEntity.class, planetId);
        if (planetEntity == null) {
            throw new IllegalArgumentException("No planet for id: " + planetId);
        }
        planetEntity.setCompressedHeightMap(data);
    }

    @Transactional
    public byte[] getCompressedHeightMap(int planetId) {
        PlanetEntity planetEntity = entityManager.find(PlanetEntity.class, planetId);
        if (planetEntity == null) {
            throw new IllegalArgumentException("No planet for id: " + planetId);
        }
        return planetEntity.getCompressedHeightMap();
    }
}
