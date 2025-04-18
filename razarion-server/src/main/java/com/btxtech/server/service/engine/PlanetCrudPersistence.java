package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.BaseItemTypeEntity;
import com.btxtech.server.model.engine.PlanetEntity;
import com.btxtech.server.model.engine.TerrainObjectPositionEntity;
import com.btxtech.server.repository.engine.PlanetRepository;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlanetCrudPersistence extends AbstractConfigCrudPersistence<PlanetConfig, PlanetEntity> {
    private final TerrainObjectService terrainObjectCrudPersistence;
    private final BaseItemTypeCrudPersistence baseItemTypeCrudPersistence;
    private final GroundCrudPersistence groundCrudPersistence;

    public PlanetCrudPersistence(PlanetRepository planetRepository,
                                 TerrainObjectService terrainObjectCrudPersistence,
                                 BaseItemTypeCrudPersistence baseItemTypeCrudPersistence,
                                 GroundCrudPersistence groundCrudPersistence) {
        super(PlanetEntity.class, planetRepository);
        this.terrainObjectCrudPersistence = terrainObjectCrudPersistence;
        this.baseItemTypeCrudPersistence = baseItemTypeCrudPersistence;
        this.groundCrudPersistence = groundCrudPersistence;
    }

    @Override
    protected PlanetConfig toConfig(PlanetEntity planetEntity) {
        return planetEntity.toPlanetConfig();
    }

    @Override
    protected void fromConfig(PlanetConfig planetConfig, PlanetEntity planetEntity) {
        planetEntity.fromPlanetConfig(planetConfig,
                groundCrudPersistence.getEntity(planetConfig.getGroundConfigId()),
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
        return getJpaRepository()
                .findById(planetId)
                .orElseThrow()
                .getTerrainObjectPositionEntities()
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
        getJpaRepository().save(planetEntity);
    }

    @Transactional
    @SecurityCheck
    public void updateTerrainObjectPositions(int planetId, List<TerrainObjectPosition> updatedTerrainObjects) {
        PlanetEntity planetEntity = getEntity(planetId);
        for (TerrainObjectPosition terrainObjectPosition : updatedTerrainObjects) {
            TerrainObjectPositionEntity terrainObjectPositionEntity = getTerrainObjectPositionEntity(planetEntity, terrainObjectPosition.getId());
            terrainObjectPositionEntity.fromTerrainObjectPosition(terrainObjectPosition, terrainObjectCrudPersistence);
        }
        getJpaRepository().save(planetEntity);
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
        PlanetEntity planetEntity = getEntity(planetId);
        planetEntity.setMiniMapImage(data);
        getJpaRepository().save(planetEntity);
    }

    @Transactional
    public byte[] getMiniMapImage(int planetId) {
        return getEntity(planetId).getMiniMapImage();
    }

    @Transactional
    @SecurityCheck
    public void updateCompressedHeightMap(int planetId, byte[] data) {
        PlanetEntity planetEntity = getEntity(planetId);
        planetEntity.setCompressedHeightMap(data);
        getJpaRepository().save(planetEntity);
    }

    @Transactional
    public byte[] getCompressedHeightMap(int planetId) {
        return getEntity(planetId).getCompressedHeightMap();
    }
}
