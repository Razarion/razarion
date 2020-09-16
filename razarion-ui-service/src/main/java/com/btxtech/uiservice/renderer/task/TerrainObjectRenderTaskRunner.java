package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.btxtech.shared.system.alarm.Alarm.Type.INVALID_TERRAIN_OBJECT;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class TerrainObjectRenderTaskRunner extends AbstractShape3DRenderTaskRunner {
    // private Logger logger = Logger.getLogger(TerrainObjectRenderTask.class.getName());
    @Inject
    private Shape3DUiService shape3DUiService;
    @Inject
    private AlarmService alarmService;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private TerrainUiService terrainUiService;

    @PostConstruct
    public void postConstruct() {
        setupTerrainObject();
    }

    public void reloadEditMode() {
        clear();
        setupTerrainObject();
    }

    private void setupTerrainObject() {
        terrainTypeService.getTerrainObjectConfigs().forEach(terrainObjectConfig -> {
            if (terrainObjectConfig.getShape3DId() != null) {
                createShape3DRenderTasks(shape3DUiService.getShape3D(terrainObjectConfig.getShape3DId())
                        , timeStamp -> terrainUiService.provideTerrainObjectModelMatrices(terrainObjectConfig.getId()));
            } else {
                alarmService.riseAlarm(INVALID_TERRAIN_OBJECT, "No shape3DId", terrainObjectConfig.getId());
            }
        });
    }

}
