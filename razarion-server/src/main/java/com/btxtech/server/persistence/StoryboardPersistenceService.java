package com.btxtech.server.persistence;

import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Singleton
public class StoryboardPersistenceService {
    @Inject
    private ExceptionHandler exceptionHandler;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public StoryboardConfig load() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            // Query for total row count in invitations
            CriteriaQuery<StoryboardEntity> userQuery = criteriaBuilder.createQuery(StoryboardEntity.class);
            Root<StoryboardEntity> from = userQuery.from(StoryboardEntity.class);
            CriteriaQuery<StoryboardEntity> userSelect = userQuery.select(from);
            return entityManager.createQuery(userSelect).getSingleResult().toStoryboardConfig();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

}
