package com.btxtech.server.persistence.impl;

import com.btxtech.server.persistence.Shape3DPersistence;
import com.btxtech.server.persistence.StoryboardEntity;
import com.btxtech.server.persistence.TerrainElementPersistence;
import com.btxtech.servercommon.StoryboardPersistence;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.dto.VisualConfig;
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
    @Inject
    private Shape3DPersistence shape3DPersistence;

    @Override
    @Transactional
    public StoryboardConfig load() throws ParserConfigurationException, SAXException, IOException {
        GameEngineConfig gameEngineConfig = new GameEngineConfig();
        gameEngineConfig.setSlopeSkeletonConfigs(terrainElementPersistence.loadSlopeSkeletons());
        gameEngineConfig.setGroundSkeletonConfig(terrainElementPersistence.loadGroundSkeleton());
        gameEngineConfig.setTerrainObjectConfigs(terrainElementPersistence.readTerrainObjects());
        // TODO gameEngineConfig.setItemTypes();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<StoryboardEntity> userQuery = criteriaBuilder.createQuery(StoryboardEntity.class);
        Root<StoryboardEntity> from = userQuery.from(StoryboardEntity.class);
        CriteriaQuery<StoryboardEntity> userSelect = userQuery.select(from);
        StoryboardConfig storyboardConfig = entityManager.createQuery(userSelect).getSingleResult().toStoryboardConfig(gameEngineConfig);
        storyboardConfig.setVisualConfig(defaultVisualConfig());
        return storyboardConfig;
    }

    private VisualConfig defaultVisualConfig() throws IOException, SAXException, ParserConfigurationException {
        // TODO remove this method. Make method which creates a new default Storyboard
        VisualConfig visualConfig = new VisualConfig();
        visualConfig.setShadowAlpha(0.2).setShadowRotationX(Math.toRadians(25)).setShadowRotationZ(Math.toRadians(250));
        visualConfig.setShape3DLightRotateX(Math.toRadians(25)).setShape3DLightRotateZ(Math.toRadians(290));
        visualConfig.setWaterGroundLevel(-20).setWaterBmDepth(10).setWaterTransparency(0.65);
        LightConfig lightConfig = new LightConfig();
        lightConfig.setDiffuse(new Color(1, 1, 1)).setAmbient(new Color(1, 1, 1)).setXRotation(Math.toRadians(-20));
        lightConfig.setYRotation(Math.toRadians(-20)).setSpecularIntensity(1.0).setSpecularHardness(0.5);
        visualConfig.setWaterLightConfig(lightConfig);
        visualConfig.setShape3DGeneralScale(10).setShape3Ds(shape3DPersistence.getShape3Ds());
        return visualConfig;
    }
}
