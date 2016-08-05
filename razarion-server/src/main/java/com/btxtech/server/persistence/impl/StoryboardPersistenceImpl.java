package com.btxtech.server.persistence.impl;

import com.btxtech.server.persistence.StoryboardEntity;
import com.btxtech.servercommon.StoryboardPersistence;
import com.btxtech.shared.dto.StoryboardConfig;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

/**
 * Created by Beat
 * 03.08.2016.
 */
@Singleton
public class StoryboardPersistenceImpl implements StoryboardPersistence {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public StoryboardConfig load() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<StoryboardEntity> userQuery = criteriaBuilder.createQuery(StoryboardEntity.class);
        Root<StoryboardEntity> from = userQuery.from(StoryboardEntity.class);
        CriteriaQuery<StoryboardEntity> userSelect = userQuery.select(from);
        return entityManager.createQuery(userSelect).getSingleResult().toStoryboardConfig();
    }
}
