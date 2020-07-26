package com.btxtech.client.editor.generic.updater;

import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.uiservice.renderer.ViewService;
import com.btxtech.uiservice.renderer.task.TerrainObjectRenderTask;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

@Singleton
public class EngineUpdater {
    private Logger logger = Logger.getLogger(EngineUpdater.class.getName());
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ViewService viewService;
    @Inject
    private TerrainObjectRenderTask terrainObjectRenderTask;

    public void connect(Object config) {
        if (config instanceof GroundConfig) {
            terrainUiService.enableEditMode((GroundConfig) config);
            viewService.onViewChanged();
        } else if (config instanceof SlopeConfig) {
            terrainUiService.enableEditMode((SlopeConfig) config);
            viewService.onViewChanged();
        } else if (config instanceof WaterConfig) {
            terrainUiService.enableEditMode((WaterConfig) config);
            viewService.onViewChanged();
        } else if (config instanceof Shape3DConfig) {
            terrainUiService.enableEditMode((Shape3DConfig)config);
            terrainObjectRenderTask.reloadEditMode();
            viewService.onViewChanged();
        } else {
            logger.warning("EngineUpdater can not connect editor to render engine: " + config.getClass());
        }

    }
}
