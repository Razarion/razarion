package com.btxtech.server.terrain;

import com.btxtech.shared.SlopeSkeletonEntity;
import com.btxtech.client.terrain.slope.skeleton.SlopeSkeletonFactory;
import com.btxtech.server.ExceptionHandler;
import com.btxtech.shared.SlopeConfigEntity;
import com.btxtech.shared.SlopeConfigEntity_;
import com.btxtech.shared.SlopeNameId;
import com.btxtech.shared.TerrainEditorService;
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
    public SlopeConfigEntity load(int id) {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<SlopeConfigEntity> userQuery = criteriaBuilder.createQuery(SlopeConfigEntity.class);
            Root<SlopeConfigEntity> from = userQuery.from(SlopeConfigEntity.class);
            userQuery.where(criteriaBuilder.equal(from.get(SlopeConfigEntity_.id), id));
            CriteriaQuery<SlopeConfigEntity> userSelect = userQuery.select(from);
            return entityManager.createQuery(userSelect).getSingleResult();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public SlopeConfigEntity save(SlopeConfigEntity slopeConfigEntity) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(slopeConfigEntity);
            System.out.println(json);
            return entityManager.merge(slopeConfigEntity);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void delete(SlopeConfigEntity slopeConfigEntity) {
        try {
            entityManager.remove(entityManager.contains(slopeConfigEntity) ? slopeConfigEntity : entityManager.merge(slopeConfigEntity));
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
