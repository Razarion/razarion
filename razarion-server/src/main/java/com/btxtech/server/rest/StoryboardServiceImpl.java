package com.btxtech.server.rest;

import com.btxtech.server.persistence.StoryboardPersistenceService;
import com.btxtech.server.persistence.SurfacePersistenceService;
import com.btxtech.server.system.DebugHelper;
import com.btxtech.shared.StoryboardService;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Created by Beat
 * 06.07.2016.
 */
public class StoryboardServiceImpl implements StoryboardService {
    @Inject
    private StoryboardPersistenceService storyboardPersistenceService;
    @Inject
    private SurfacePersistenceService surfacePersistenceService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private DebugHelper debugHelper;

    @Override
    @Transactional
    public StoryboardConfig loadStoryboard() {
        try {
            StoryboardConfig storyboardConfig = storyboardPersistenceService.load();
            storyboardConfig.getPlanetConfig().setGroundSkeleton(surfacePersistenceService.loadGroundSkeleton());
            storyboardConfig.getPlanetConfig().setSlopeSkeletons(surfacePersistenceService.loadSlopeSkeletons());
            storyboardConfig.getPlanetConfig().setTerrainObjects(surfacePersistenceService.loadTerrainObjects());
            debugHelper.writeToJsonFile("StoryboardConfig.json", storyboardConfig);
            return storyboardConfig;
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
