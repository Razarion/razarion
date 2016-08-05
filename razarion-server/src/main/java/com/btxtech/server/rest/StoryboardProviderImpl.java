package com.btxtech.server.rest;

import com.btxtech.servercommon.StoryboardPersistence;
import com.btxtech.server.persistence.TerrainElementPersistenceService;
import com.btxtech.server.system.DebugHelper;
import com.btxtech.servercommon.collada.Emulation;
import com.btxtech.shared.StoryboardProvider;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Created by Beat
 * 06.07.2016.
 */
public class StoryboardProviderImpl implements StoryboardProvider {
    @Inject
    @Emulation
    private StoryboardPersistence storyboardPersistence;
    @Inject
    private TerrainElementPersistenceService terrainElementPersistenceService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private DebugHelper debugHelper;

    @Override
    @Transactional
    public StoryboardConfig loadStoryboard() {
        try {
            StoryboardConfig storyboardConfig = storyboardPersistence.load();
            // TODO storyboardConfig.getPlanetConfig().setGroundSkeletonConfig(terrainElementPersistenceService.loadGroundSkeleton());
            // TODO storyboardConfig.getPlanetConfig().setSlopeSkeletonConfigs(terrainElementPersistenceService.loadSlopeSkeletons());
            // TODO storyboardConfig.getPlanetConfig().setTerrainObjects(terrainElementPersistenceService.loadTerrainObjects());
            debugHelper.writeToJsonFile("StoryboardConfig.json", storyboardConfig);
            return storyboardConfig;
//  TODO      } catch (ParserConfigurationException | ColladaException | SAXException | IOException e) {
//  TODO          exceptionHandler.handleException(e);
//  TODO          throw new RuntimeException(e);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
