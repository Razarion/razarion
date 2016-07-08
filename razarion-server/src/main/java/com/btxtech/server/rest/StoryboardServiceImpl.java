package com.btxtech.server.rest;

import com.btxtech.server.collada.ColladaException;
import com.btxtech.server.persistence.StoryboardPersistenceService;
import com.btxtech.server.persistence.TerrainElementPersistenceService;
import com.btxtech.server.system.DebugHelper;
import com.btxtech.shared.StoryboardService;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.system.ExceptionHandler;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by Beat
 * 06.07.2016.
 */
public class StoryboardServiceImpl implements StoryboardService {
    @Inject
    private StoryboardPersistenceService storyboardPersistenceService;
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
            StoryboardConfig storyboardConfig = storyboardPersistenceService.load();
            storyboardConfig.getPlanetConfig().setGroundSkeleton(terrainElementPersistenceService.loadGroundSkeleton());
            storyboardConfig.getPlanetConfig().setSlopeSkeletons(terrainElementPersistenceService.loadSlopeSkeletons());
            storyboardConfig.getPlanetConfig().setTerrainObjects(terrainElementPersistenceService.loadTerrainObjects());
            debugHelper.writeToJsonFile("StoryboardConfig.json", storyboardConfig);
            return storyboardConfig;
        } catch (ParserConfigurationException | ColladaException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
