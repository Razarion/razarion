package com.btxtech.server.terrain;

import com.btxtech.server.ExceptionHandler;
import com.btxtech.server.terrain.surface.GroundConfigEntity;
import com.btxtech.server.terrain.surface.SlopeConfigEntity;
import com.btxtech.server.terrain.surface.SlopeConfigEntity_;
import com.btxtech.server.terrain.surface.TerrainSlopePositionEntity;
import com.btxtech.shared.TerrainEditorService;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.SlopeConfig;
import com.btxtech.shared.dto.SlopeNameId;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.google.gson.Gson;
import org.jboss.errai.bus.server.annotations.Service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.11.2015.
 */
@Service
@ApplicationScoped
public class TerrainEditorServiceImpl implements TerrainEditorService {
    @Inject
    private Logger logger;
    @Inject
    private ExceptionHandler exceptionHandler;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Collection<SlopeNameId> getSlopeNameIds() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
            Root<SlopeConfigEntity> root = cq.from(SlopeConfigEntity.class);
            cq.multiselect(root.get(SlopeConfigEntity_.id), root.get(SlopeConfigEntity_.internalName));
            List<Tuple> tupleResult = entityManager.createQuery(cq).getResultList();
            Collection<SlopeNameId> slopeNameIds = new ArrayList<>();
            for (Tuple t : tupleResult) {
                slopeNameIds.add(new SlopeNameId(((Long) t.get(0)).intValue(), (String) t.get(1)));
            }
            return slopeNameIds;
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public SlopeConfig loadSlopeConfig(int id) {
        try {
            return entityManager.find(SlopeConfigEntity.class, (long) id).toSlopeConfig();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public SlopeConfig saveSlopeConfig(SlopeConfig slopeConfig) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(slopeConfig);
            System.out.println("--------------------------------------------------------");
            System.out.println(json);
            System.out.println("--------------------------------------------------------");

            SlopeConfigEntity slopeConfigEntity;
            if (slopeConfig.hasId()) {
                slopeConfigEntity = entityManager.find(SlopeConfigEntity.class, (long) slopeConfig.getId());
            } else {
                slopeConfigEntity = new SlopeConfigEntity();
            }
            slopeConfigEntity.fromSlopeConfig(slopeConfig);

            return entityManager.merge(slopeConfigEntity).toSlopeConfig();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteSlopeConfig(SlopeConfig slopeConfig) {
        try {
            SlopeConfigEntity slopeConfigEntity = entityManager.find(SlopeConfigEntity.class, (long) slopeConfig.getId());
            entityManager.remove(slopeConfigEntity);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public GroundConfig loadGroundConfig() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
            Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
            CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
            return entityManager.createQuery(userSelect).getSingleResult().toGroundConfig();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public GroundConfig saveGroundConfig(GroundConfig groundConfig) {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
            Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
            CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
            GroundConfigEntity groundConfigEntity = entityManager.createQuery(userSelect).getSingleResult();
            groundConfigEntity.fromGroundConfig(groundConfig);
            return entityManager.merge(groundConfigEntity).toGroundConfig();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void saveTerrainSlopePositions(Collection<TerrainSlopePosition> terrainSlopePositions) {
        try {
            for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
                TerrainSlopePositionEntity terrainSlopePositionEntity;
                if(terrainSlopePosition.hasId()) {
                    terrainSlopePositionEntity = entityManager.find(TerrainSlopePositionEntity.class, (long) terrainSlopePosition.getId());
                } else {
                    terrainSlopePositionEntity = new TerrainSlopePositionEntity();
                }
                SlopeConfigEntity slopeConfigEntity = entityManager.find(SlopeConfigEntity.class, (long) terrainSlopePosition.getSlopeId());
                terrainSlopePositionEntity.setSlopeConfigEntity(slopeConfigEntity);
                terrainSlopePositionEntity.setPolygon(terrainSlopePosition.getPolygon());
                entityManager.merge(terrainSlopePositionEntity);
            }
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
