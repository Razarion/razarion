package com.btxtech.server.persistence;

import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.server.persistence.object.TerrainObjectEntity_;
import com.btxtech.server.persistence.surface.DrivewayConfigEntity;
import com.btxtech.server.persistence.surface.DrivewayConfigEntity_;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Singleton
public class TerrainElementPersistence {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private Shape3DCrudPersistence shape3DPersistence;

    @Transactional
    public List<ObjectNameId> getTerrainObjectNameIds() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<TerrainObjectEntity> root = cq.from(TerrainObjectEntity.class);
        cq.multiselect(root.get(TerrainObjectEntity_.id), root.get(TerrainObjectEntity_.internalName));
        List<Tuple> tupleResult = entityManager.createQuery(cq).getResultList();
        return tupleResult.stream().map(t -> new ObjectNameId((int) t.get(0), (String) t.get(1))).collect(Collectors.toList());
    }

    @Transactional
    public TerrainObjectConfig readTerrainObjectConfig(int id) {
        return entityManager.find(TerrainObjectEntity.class, id).toTerrainObjectConfig();
    }

    @Transactional
    public List<TerrainObjectConfig> readTerrainObjects() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TerrainObjectEntity> userQuery = criteriaBuilder.createQuery(TerrainObjectEntity.class);
        Root<TerrainObjectEntity> from = userQuery.from(TerrainObjectEntity.class);
        CriteriaQuery<TerrainObjectEntity> userSelect = userQuery.select(from);
        List<TerrainObjectEntity> terrainObjectEntities = entityManager.createQuery(userSelect).getResultList();

        return terrainObjectEntities.stream().map(TerrainObjectEntity::toTerrainObjectConfig).collect(Collectors.toList());
    }

    @Transactional
    @SecurityCheck
    public void saveTerrainObject(TerrainObjectConfig terrainObjectConfig) {
        TerrainObjectEntity terrainObjectEntity = entityManager.find(TerrainObjectEntity.class, terrainObjectConfig.getId());
        terrainObjectEntity.fromTerrainObjectConfig(terrainObjectConfig, shape3DPersistence.getEntity(terrainObjectConfig.getShape3DId()));
    }

    @Transactional
    @SecurityCheck
    public void deleteTerrainObjectConfig(TerrainObjectConfig terrainObjectConfig) {
        TerrainObjectEntity terrainObjectEntity = entityManager.find(TerrainObjectEntity.class, terrainObjectConfig.getId());
        entityManager.remove(terrainObjectEntity);
    }

    @Transactional
    @SecurityCheck
    public TerrainObjectConfig createTerrainObjectConfig() {
        TerrainObjectEntity terrainObjectEntity = new TerrainObjectEntity();
        entityManager.persist(terrainObjectEntity);
        return terrainObjectEntity.toTerrainObjectConfig();
    }

    @Transactional
    public TerrainObjectEntity getTerrainObjectEntity(int terrainObjectId) {
        return entityManager.find(TerrainObjectEntity.class, terrainObjectId);
    }

    @Transactional
    public List<ObjectNameId> readDrivewayObjectNameIds() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<DrivewayConfigEntity> root = cq.from(DrivewayConfigEntity.class);
        cq.multiselect(root.get(DrivewayConfigEntity_.id), root.get(DrivewayConfigEntity_.internalName));
        List<Tuple> tupleResult = entityManager.createQuery(cq).getResultList();
        return tupleResult.stream().map(t -> new ObjectNameId((int) t.get(0), (String) t.get(1))).collect(Collectors.toList());
    }

    @Transactional
    public List<DrivewayConfig> loadDrivewayConfigs() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DrivewayConfigEntity> userQuery = criteriaBuilder.createQuery(DrivewayConfigEntity.class);
        Root<DrivewayConfigEntity> root = userQuery.from(DrivewayConfigEntity.class);
        CriteriaQuery<DrivewayConfigEntity> userSelect = userQuery.select(root);
        Collection<DrivewayConfigEntity> drivewayConfigEntities = entityManager.createQuery(userSelect).getResultList();

        return drivewayConfigEntities.stream().map(DrivewayConfigEntity::toDrivewayConfig).collect(Collectors.toList());
    }

    @Transactional
    public DrivewayConfigEntity getDrivewayConfigEntity(Integer id) {
        if (id == null) {
            return null;
        }
        DrivewayConfigEntity drivewayConfigEntity = entityManager.find(DrivewayConfigEntity.class, id);
        if (drivewayConfigEntity == null) {
            throw new IllegalArgumentException("No DrivewayConfigEntity for id: " + id);
        }
        return drivewayConfigEntity;
    }
}