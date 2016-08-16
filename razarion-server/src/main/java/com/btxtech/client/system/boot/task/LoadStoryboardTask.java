package com.btxtech.client.system.boot.task;

import com.btxtech.client.editor.terrain.TerrainObjectEditor;
import com.btxtech.client.editor.terrain.TerrainEditor;
import com.btxtech.shared.StoryboardProvider;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.uiservice.storyboard.StoryboardService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.02.2016.
 */
@Dependent
public class LoadStoryboardTask extends AbstractStartupTask {
    @Inject
    private StoryboardService storyboardService;
    @Inject
    private TerrainObjectEditor terrainObjectEditor;
    @Inject
    private TerrainEditor terrainEditor;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<StoryboardProvider> serviceCaller;
    private Logger logger = Logger.getLogger(LoadStoryboardTask.class.getName());

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        serviceCaller.call(new RemoteCallback<StoryboardConfig>() {
            @Override
            public void callback(StoryboardConfig storyboardConfig) {
                storyboardService.init(storyboardConfig);
                // TODO terrainObjectEditor.setTerrainObjectConfigs(storyboardConfig.getPlanetConfig().getTerrainObjectPositions());
                // TODO terrainEditor.setTerrainSlopePositions(storyboardConfig.getPlanetConfig().getTerrainSlopePositions());
                deferredStartup.finished();
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "loadSlopeSkeletons failed: " + message, throwable);
                deferredStartup.failed(throwable);
                return false;
            }
        }).loadStoryboard();
    }
}
