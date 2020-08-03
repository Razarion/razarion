package com.btxtech.server.persistence;

import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.object.TerrainObjectPositionEntity;
import com.btxtech.server.persistence.surface.TerrainSlopeCornerEntity;
import com.btxtech.server.persistence.surface.TerrainSlopePositionEntity;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 08.07.2016.
 */
@Singleton
public class PlanetCrudPersistence extends AbstractCrudPersistence<PlanetConfig, PlanetEntity> {
    @Inject
    private TerrainObjectCrudPersistence terrainObjectCrudPersistence;
    @Inject
    private ItemTypePersistence itemTypePersistence;
    @Inject
    private GroundCrudPersistence groundCrudPersistence;
    @Inject
    private SlopeCrudPersistence slopeCrudPersistence;
    @Inject
    private DrivewayCrudPersistence drivewayCrudPersistence;
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
                itemTypePersistence.readBaseItemTypeEntity(planetConfig.getStartBaseItemTypeId()),
                itemTypePersistence.baseItemTypeLimitation(planetConfig.getItemTypeLimitation()));
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

        List<TerrainObjectPosition> terrainObjectPositions = new ArrayList<>();
        for (TerrainObjectPositionEntity terrainObjectPositionEntity : planetEntity.getTerrainObjectPositionEntities()) {
            terrainObjectPositions.add(terrainObjectPositionEntity.toTerrainObjectPosition());
        }
        return terrainObjectPositions;
    }

    @Transactional
    @SecurityCheck
    public void createTerrainObjectPositions(int planetId, List<TerrainObjectPosition> createdTerrainObjects) {
        List<TerrainObjectPositionEntity> terrainObjectPositionEntities = new ArrayList<>();
        for (TerrainObjectPosition terrainObjectPosition : createdTerrainObjects) {
            TerrainObjectPositionEntity terrainObjectPositionEntity = new TerrainObjectPositionEntity();
            terrainObjectPositionEntity.setTerrainObjectEntity(terrainObjectCrudPersistence.getEntity(terrainObjectPosition.getTerrainObjectId()));
            terrainObjectPositionEntity.setPosition(terrainObjectPosition.getPosition());
            // TODO terrainObjectPositionEntity.setScale(terrainObjectPosition.get_Scale());
            // TODO terrainObjectPositionEntity.setRotationZ(terrainObjectPosition.getRotationZ());
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
            terrainObjectPositionEntity.setTerrainObjectEntity(terrainObjectCrudPersistence.getEntity(terrainObjectPosition.getTerrainObjectId()));
            terrainObjectPositionEntity.setPosition(terrainObjectPosition.getPosition());
            // TODO terrainObjectPositionEntity.setScale(terrainObjectPosition.getScale());
            // TODO terrainObjectPositionEntity.setRotationZ(terrainObjectPosition.getRotationZ());
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

    @Transactional
    @SecurityCheck
    public void updateTerrainSlopePositions(int planetId, List<TerrainSlopePosition> updatedSlopes) {
        PlanetEntity planetEntity = getEntity(planetId);
        for (TerrainSlopePosition terrainSlopePosition : updatedSlopes) {
            TerrainSlopePositionEntityChain chain = getSlopePositionEntityFromPlanet(planetEntity, terrainSlopePosition.getId());
            chain.getChild().setSlopeConfigEntity(slopeCrudPersistence.getEntity(terrainSlopePosition.getSlopeConfigId()));
            chain.getChild().getPolygon().clear();
            chain.getChild().setInverted(terrainSlopePosition.isInverted());
            chain.getChild().getPolygon().addAll(terrainSlopePosition.getPolygon().stream()
                    .map(terrainSlopeCorner -> new TerrainSlopeCornerEntity()
                            .position(terrainSlopeCorner.getPosition())
                            .drivewayConfigEntity(drivewayCrudPersistence.getEntity(terrainSlopeCorner.getSlopeDrivewayId()))).collect(Collectors.toList()));
            if (chain.getParent() != null) {
                entityManager.persist(chain.getParent());
            }
        }
        entityManager.merge(planetEntity);
    }

    @Transactional
    @SecurityCheck
    public void createTerrainSlopePositions(int planetId, Collection<TerrainSlopePosition> terrainSlopePositions) {
        List<TerrainSlopePositionEntity> terrainSlopePositionEntities = new ArrayList<>();
        for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
            TerrainSlopePositionEntity terrainSlopePositionEntity = new TerrainSlopePositionEntity();
            terrainSlopePositionEntity.setSlopeConfigEntity(slopeCrudPersistence.getEntity(terrainSlopePosition.getSlopeConfigId()));
            terrainSlopePositionEntity.setInverted(terrainSlopePosition.isInverted());
            terrainSlopePositionEntity.setPolygon(terrainSlopePosition.getPolygon().stream()
                    .map(terrainSlopeCorner -> new TerrainSlopeCornerEntity()
                            .position(terrainSlopeCorner.getPosition())
                            .drivewayConfigEntity(drivewayCrudPersistence.getEntity(terrainSlopeCorner.getSlopeDrivewayId()))).collect(Collectors.toList()));
            if (terrainSlopePosition.getEditorParentId() != null) {
                TerrainSlopePositionEntity parent = entityManager.find(TerrainSlopePositionEntity.class, terrainSlopePosition.getEditorParentId());
                parent.addChild(terrainSlopePositionEntity);
                entityManager.persist(parent);
            } else {
                terrainSlopePositionEntities.add(terrainSlopePositionEntity);
            }
        }
        PlanetEntity planetEntity = getEntity(planetId);
        planetEntity.getTerrainSlopePositionEntities().addAll(terrainSlopePositionEntities);
        entityManager.persist(planetEntity);
    }

    @Transactional
    @SecurityCheck
    public void deleteTerrainSlopePositions(int planetId, Collection<Integer> terrainSlopePositionIds) {
        PlanetEntity planetEntity = getEntity(planetId);
        for (int terrainSlopePositionId : terrainSlopePositionIds) {
            TerrainSlopePositionEntityChain chain = getSlopePositionEntityFromPlanet(planetEntity, terrainSlopePositionId);
            if (chain.getParent() != null) {
                chain.getParent().removeChild(chain.getChild());
                entityManager.persist(chain.getParent());
            } else {
                planetEntity.getTerrainSlopePositionEntities().remove(chain.getChild());
            }
        }
        entityManager.persist(planetEntity);
    }

    private TerrainSlopePositionEntityChain getSlopePositionEntityFromPlanet(PlanetEntity planetEntity, int id) {
        for (TerrainSlopePositionEntity terrainSlopePositionEntity : planetEntity.getTerrainSlopePositionEntities()) {
            if (terrainSlopePositionEntity.getId() == id) {
                return new TerrainSlopePositionEntityChain(null, terrainSlopePositionEntity);
            }
            TerrainSlopePositionEntityChain child = terrainSlopePositionEntity.deepFirstSearchSlope(id);
            if (child != null) {
                return child;
            }
        }
        throw new IllegalArgumentException("No TerrainSlopePositionEntity on planet for id: " + id);
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
    public void updatePlanetVisualConfig(int planetId, PlanetVisualConfig planetVisualConfig) {
        PlanetEntity planetEntity = getEntity(planetId);
        planetEntity.fromPlanetVisualConfig(planetVisualConfig);
        entityManager.merge(planetEntity);
    }

    @Transactional
    public List<TerrainSlopePosition> getTerrainSlopePositions(int planetId) {
        PlanetEntity planetEntity = entityManager.find(PlanetEntity.class, planetId);
        if (planetEntity == null) {
            throw new IllegalArgumentException("No planet for id: " + planetId);
        }

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        for (TerrainSlopePositionEntity terrainSlopePositionEntity : planetEntity.getTerrainSlopePositionEntities()) {
            terrainSlopePositions.add(terrainSlopePositionEntity.toTerrainSlopePosition());
        }
        return terrainSlopePositions;
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

    public static class TerrainSlopePositionEntityChain {
        private TerrainSlopePositionEntity parent;
        private TerrainSlopePositionEntity child;

        public TerrainSlopePositionEntityChain(TerrainSlopePositionEntity parent, TerrainSlopePositionEntity child) {
            this.parent = parent;
            this.child = child;
        }

        public TerrainSlopePositionEntity getParent() {
            return parent;
        }

        public TerrainSlopePositionEntity getChild() {
            return child;
        }
    }
}
