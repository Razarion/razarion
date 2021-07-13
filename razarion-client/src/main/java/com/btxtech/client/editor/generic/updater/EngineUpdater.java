package com.btxtech.client.editor.generic.updater;

import com.btxtech.shared.datatypes.particle.ParticleEmitterSequenceConfig;
import com.btxtech.shared.datatypes.particle.ParticleShapeConfig;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.Shape3DComposite;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.Shape3DUtils;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.particle.ParticleService;
import com.btxtech.uiservice.renderer.ViewService;
import com.btxtech.uiservice.renderer.task.BaseItemRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.ParticleRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.TerrainObjectRenderTaskRunner;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

@Singleton
public class EngineUpdater {
    private Logger logger = Logger.getLogger(EngineUpdater.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ViewService viewService;
    @Inject
    private ParticleService particleService;
    @Inject
    private ParticleRenderTaskRunner particleRenderTaskRunner;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private Shape3DUiService shape3DUiService;
    @Inject
    private Instance<TerrainObjectRenderTaskRunner> terrainObjectRenderTaskRunnerInstance; // Use Instance -> called to early
    @Inject
    private Instance<BaseItemRenderTaskRunner> baseItemRenderTaskRunnerInstance; // Use Instance -> called to early

    public void connect(Object config) {
        try {
            innerConnect(config);
        } catch (Throwable t) {
            exceptionHandler.handleException("Can not connect to engines " + config, t);
        }
    }

    public void innerConnect(Object config) {
        if (config instanceof GroundConfig) {
            terrainUiService.enableEditMode((GroundConfig) config);
            viewService.onViewChanged();
        } else if (config instanceof SlopeConfig) {
            terrainUiService.enableEditMode((SlopeConfig) config);
            viewService.onViewChanged();
        } else if (config instanceof WaterConfig) {
            terrainUiService.enableEditMode((WaterConfig) config);
            viewService.onViewChanged();
        } else if (config instanceof ParticleEmitterSequenceConfig) {
            particleService.editorUpdate((ParticleEmitterSequenceConfig) config);
        } else if (config instanceof ParticleShapeConfig) {
            particleService.editorUpdate((ParticleShapeConfig) config);
            particleRenderTaskRunner.editorReload();
        } else {
            logger.warning("EngineUpdater can not connect editor to render engine: " + config.getClass());
        }
    }

    public void onShape3D(Shape3DComposite shape3DComposite) {
        try {
            innerOnShape3D(shape3DComposite);
        } catch (Throwable t) {
            exceptionHandler.handleException("Can not connect to engines " + shape3DComposite, t);
        }
    }

    private void innerOnShape3D(Shape3DComposite shape3DComposite) {
        Shape3D shape3D = shape3DComposite.getShape3D();
        shape3DUiService.editorOverrideShape3D(shape3D);
        // Update BaseItemType renderer
        BaseItemRenderTaskRunner baseItemRenderTaskRunner = baseItemRenderTaskRunnerInstance.get();
        for (BaseItemType baseItemType : itemTypeService.getBaseItemTypes()) {
            if (baseItemType.getShape3DId() != null && shape3D.getId() == baseItemType.getShape3DId()) {
                baseItemRenderTaskRunner.onEditorBaseItemTypeChanged();
            }
            if (baseItemType.getSpawnShape3DId() != null && shape3D.getId() == baseItemType.getSpawnShape3DId()) {
                baseItemRenderTaskRunner.onEditorBaseItemTypeChanged();
            }
            if (baseItemType.getWeaponType() != null) {
                WeaponType weaponType = baseItemType.getWeaponType();
                if (weaponType.getProjectileShape3DId() != null && shape3D.getId() == weaponType.getProjectileShape3DId()) {
                    // TODO projectileRenderTask.onBaseItemTypeChanged();
                }
            }
            if (baseItemType.getHarvesterType() != null && baseItemType.getHarvesterType().getAnimationShape3dId() != null && baseItemType.getHarvesterType().getAnimationShape3dId() == shape3D.getId()) {
                baseItemRenderTaskRunner.onEditorBaseItemTypeChanged();
            }
            if (baseItemType.getBuilderType() != null && baseItemType.getBuilderType().getAnimationShape3dId() != null && baseItemType.getBuilderType().getAnimationShape3dId() == shape3D.getId()) {
                baseItemRenderTaskRunner.onEditorBaseItemTypeChanged();
            }
            if (baseItemType.getWreckageShape3DId() != null && shape3D.getId() == baseItemType.getWreckageShape3DId()) {
                // TODO trailRenderTask.onWreckageChanged(baseItemType);
            }
        }
        // Update ResourceItemType renderer
        // TODO itemTypeService.getResourceItemTypes().stream().filter(resourceItemType -> resourceItemType.getShape3DId() != null && shape3D.getDbId() == resourceItemType.getShape3DId()).forEach(resourceItemType -> resourceItemRenderTask.onResourceItemTypeChanged(resourceItemType));
        // Update BoxItemType renderer
        // TODO itemTypeService.getBoxItemTypes().stream().filter(boxItemType -> boxItemType.getShape3DId() != null && shape3D.getDbId() == boxItemType.getShape3DId()).forEach(boxItemType -> boxItemRenderTask.onBoxItemTypeChanged(boxItemType));
        // Update TerrainObject renderer
        // TODO terrainTypeService.getTerrainObjectConfigs().stream().filter(terrainObjectConfig -> terrainObjectConfig.getShape3DId() != null && shape3D.getDbId() == terrainObjectConfig.getShape3DId()).forEach(terrainObjectConfig -> terrainObjectRenderTask.onTerrainObjectChanged(terrainObjectConfig));
    }

    public void handleSetValue(Object object) {
        try {
            innerHandleSetValue(object);
        } catch (Throwable t) {
            exceptionHandler.handleException("handleSetValue failed " + object, t);
        }
    }

    public void innerHandleSetValue(Object object) {
        if (object instanceof Shape3DConfig) {
            Shape3DConfig shape3DConfig = (Shape3DConfig) object;
            Shape3D shape3D = shape3DUiService.getShape3D(shape3DConfig.getId());
            Shape3DUtils.fillMaterialFromSource(shape3D.getElement3Ds(), shape3DConfig);
        }
    }
}
