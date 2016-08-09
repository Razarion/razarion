package com.btxtech.server.persistence.impl;

import com.btxtech.server.persistence.StoryboardEntity;
import com.btxtech.server.persistence.TerrainElementPersistence;
import com.btxtech.servercommon.StoryboardPersistence;
import com.btxtech.servercommon.collada.ColladaException;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by Beat
 * 03.08.2016.
 */
@Singleton
public class StoryboardPersistenceImpl implements StoryboardPersistence {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private TerrainElementPersistence terrainElementPersistence;

    @Override
    @Transactional
    public StoryboardConfig load() throws ParserConfigurationException, ColladaException, SAXException, IOException {
        GameEngineConfig gameEngineConfig = new GameEngineConfig();
        gameEngineConfig.setSlopeSkeletonConfigs(terrainElementPersistence.loadSlopeSkeletons());
        gameEngineConfig.setGroundSkeletonConfig(terrainElementPersistence.loadGroundSkeleton());
        gameEngineConfig.setTerrainObjectConfigs(terrainElementPersistence.loadTerrainObjects());
        // TODO gameEngineConfig.setItemTypes();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<StoryboardEntity> userQuery = criteriaBuilder.createQuery(StoryboardEntity.class);
        Root<StoryboardEntity> from = userQuery.from(StoryboardEntity.class);
        CriteriaQuery<StoryboardEntity> userSelect = userQuery.select(from);
        return entityManager.createQuery(userSelect).getSingleResult().toStoryboardConfig(gameEngineConfig);
    }
}
