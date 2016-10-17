package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.rest.Shape3DProvider;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemState;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.utils.Shape3DUtils;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.renderer.task.BaseItemRenderTask;
import com.btxtech.uiservice.renderer.task.ProjectileRenderTask;
import com.btxtech.uiservice.renderer.task.ResourceItemRenderTask;
import com.btxtech.uiservice.renderer.task.TerrainObjectRenderTask;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 17.08.2016.
 */
@ApplicationScoped
public class Shape3DCrud extends AbstractCrudeEditor<Shape3D> {
    private Logger logger = Logger.getLogger(Shape3DCrud.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<Shape3DProvider> caller;
    @Inject
    private Shape3DUiService shape3DUiService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private BaseItemRenderTask baseItemRenderTask;
    @Inject
    private ProjectileRenderTask projectileRenderTask;
    @Inject
    private ResourceItemRenderTask resourceItemRenderTask;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private TerrainObjectRenderTask terrainObjectRenderTask;
    private Map<Integer, Shape3DConfig> changes = new HashMap<>();

    @Override
    public void create() {
        caller.call(new RemoteCallback<Shape3D>() {
            @Override
            public void callback(Shape3D shape3D) {
                shape3DUiService.override(shape3D);
                fire();
                fireSelection(shape3D.createObjectNameId());
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.create failed: " + message, throwable);
            return false;
        }).create();
    }

    @Override
    public void reload() {
        caller.call(new RemoteCallback<List<Shape3D>>() {
            @Override
            public void callback(List<Shape3D> shape3Ds) {
                changes.clear();
                shape3DUiService.setShapes3Ds(shape3Ds);
                fire();
                fireChange(shape3Ds);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.getShape3Ds failed: " + message, throwable);
            return false;
        }).getShape3Ds();
    }

    @Override
    public Shape3D getInstance(ObjectNameId objectNameId) {
        return shape3DUiService.getShape3D(objectNameId.getId());
    }

    public void updateCollada(Shape3D originalShape3D, String colladaText) {
        caller.call(new RemoteCallback<Shape3D>() {
            @Override
            public void callback(Shape3D shape3D) {
                shape3D.setDbId(originalShape3D.getDbId());
                Shape3DUtils.replaceTextureIds(originalShape3D, shape3D);
                addChangesCollada(originalShape3D.getDbId(), colladaText);
                shape3DUiService.override(shape3D);
                fireChange(shape3D);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.getShape3Ds failed: " + message, throwable);
            return false;
        }).colladaConvert(colladaText);
    }

    public void updateTexture(Shape3D shape3D, String materialId, int imageId) {
        Shape3DUtils.replaceTextureId(shape3D, materialId, imageId);
        // Update changes set
        Shape3DConfig shape3DConfig = getChangedShape3DConfig(shape3D.getDbId());
        Map<String, Integer> textureMap = new HashMap<>();
        Shape3DUtils.getAllVertexContainers(shape3D).stream().filter(vertexContainer -> vertexContainer.getTextureId() != null).forEach(vertexContainer -> textureMap.put(vertexContainer.getMaterialId(), vertexContainer.getTextureId()));
        shape3DConfig.setTextures(textureMap);
        shape3DUiService.override(shape3D);
        fireChange(shape3D);
    }

    public void updateAnimation(Shape3D shape3D, String animationId, AnimationTrigger animationTrigger) {
        Shape3DUtils.replaceAnimation(shape3D, animationId, animationTrigger);
        // Update changes set
        Shape3DConfig shape3DConfig = getChangedShape3DConfig(shape3D.getDbId());
        Map<String, AnimationTrigger> animationMap = new HashMap<>();
        shape3D.getModelMatrixAnimations().stream().filter(modelMatrixAnimation -> modelMatrixAnimation.getAnimationTrigger() != null).forEach(modelMatrixAnimation -> animationMap.put(modelMatrixAnimation.getId(), modelMatrixAnimation.getAnimationTrigger()));
        shape3DConfig.setAnimations(animationMap);
        shape3DUiService.override(shape3D);
        fireChange(shape3D);
    }

    @Override
    public void save(Shape3D shape3D) {
        Shape3DConfig shape3DConfig = changes.get(shape3D.getDbId());
        if (shape3DConfig == null) {
            return;
        }

        caller.call(response -> changes.remove(shape3D.getDbId()), (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.save failed: " + message, throwable);
            return false;
        }).save(shape3DConfig);
    }


    @Override
    public void delete(Shape3D shape3D) {
        caller.call(response -> {
            shape3DUiService.remove(shape3D);
            fire();
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "Shape3DProvider.delete failed: " + message, throwable);
            return false;
        }).delete(shape3D.getDbId());
    }

    private void addChangesCollada(int dbId, String colladaText) {
        Shape3DConfig shape3DConfig = getChangedShape3DConfig(dbId);
        shape3DConfig.setColladaString(colladaText);
    }

    private Shape3DConfig getChangedShape3DConfig(int dbId) {
        Shape3DConfig shape3DConfig = changes.get(dbId);
        if (shape3DConfig == null) {
            shape3DConfig = new Shape3DConfig();
            shape3DConfig.setDbId(dbId);
            changes.put(dbId, shape3DConfig);
        }
        return shape3DConfig;
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return shape3DUiService.getShape3Ds().stream().map(Shape3D::createObjectNameId).collect(Collectors.toList());
    }

    @Override
    public void onChange(Shape3D shape3D) {
        // Update BaseItemType renderer
        for (BaseItemType baseItemType : itemTypeService.getBaseItemTypes()) {
            if(baseItemType.getShape3DId() != null && shape3D.getDbId() == baseItemType.getShape3DId()) {
                baseItemRenderTask.onBaseItemTypeChanged(baseItemType);
            }
            if(baseItemType.getSpawnShape3DId() != null && shape3D.getDbId() == baseItemType.getSpawnShape3DId()) {
                baseItemRenderTask.onBaseItemTypeChanged(baseItemType);
            }
            if(baseItemType.getWeaponType() != null && baseItemType.getWeaponType().getProjectileShape3DId() != null && shape3D.getDbId() ==  baseItemType.getWeaponType().getProjectileShape3DId()) {
                projectileRenderTask.onBaseItemTypeChanged(baseItemType);
            }
        }
        // Update ResourceItemType renderer
        for (ResourceItemType resourceItemType : itemTypeService.getResourceItemTypes()) {
            if(resourceItemType.getShape3DId() != null && shape3D.getDbId() == resourceItemType.getShape3DId()) {
                resourceItemRenderTask.onResourceItemTypeChanged(resourceItemType);
            }
        }
        // Update TerrainObject renderer
        for (TerrainObjectConfig terrainObjectConfig : terrainTypeService.getTerrainObjectConfigs()) {
            if(terrainObjectConfig.getShape3DId() != null && shape3D.getDbId() == terrainObjectConfig.getShape3DId()) {
                terrainObjectRenderTask.onTerrainObjectChanged(terrainObjectConfig);
            }
        }
    }
}
