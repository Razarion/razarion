package com.btxtech.server.persistence;

import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.server.persistence.object.TerrainObjectEntity_;
import com.btxtech.server.persistence.surface.DrivewayConfigEntity;
import com.btxtech.server.persistence.surface.DrivewayConfigEntity_;
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
public class TerrainObjectCrudPersistence extends AbstractCrudPersistence<TerrainObjectConfig, TerrainObjectEntity> {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private Shape3DCrudPersistence shape3DPersistence;

    public TerrainObjectCrudPersistence() {
        super(TerrainObjectEntity.class, TerrainObjectEntity_.id, TerrainObjectEntity_.internalName);
    }

    @Override
    protected TerrainObjectConfig toConfig(TerrainObjectEntity entity) {
        return entity.toTerrainObjectConfig();
    }

    @Override
    protected void fromConfig(TerrainObjectConfig config, TerrainObjectEntity entity) {
        entity.fromTerrainObjectConfig(config, shape3DPersistence.getEntity(config.getShape3DId()));
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