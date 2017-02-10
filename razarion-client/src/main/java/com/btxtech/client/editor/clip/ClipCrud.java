package com.btxtech.client.editor.clip;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.shared.dto.ClipConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.rest.ClipProvider;
import com.btxtech.uiservice.clip.EffectService;
import com.btxtech.uiservice.renderer.task.BaseItemRenderTask;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 17.08.2016.
 */
@ApplicationScoped
public class ClipCrud extends AbstractCrudeEditor<ClipConfig> {
    // TODO This is may deprecated and has been replaced by ParticleSystem


    private Logger logger = Logger.getLogger(ClipCrud.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<ClipProvider> caller;
    @Inject
    private EffectService effectService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private BaseItemRenderTask baseItemRenderTask;

    @Override
    public void create() {
        caller.call(new RemoteCallback<ClipConfig>() {
            @Override
            public void callback(ClipConfig clipConfig) {
                effectService.override(clipConfig);
                fire();
                fireSelection(clipConfig.createObjectNameId());
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ClipCrud.create failed: " + message, throwable);
            return false;
        }).create();
    }

    @Override
    public void reload() {
        caller.call(new RemoteCallback<List<ClipConfig>>() {
            @Override
            public void callback(List<ClipConfig> clipConfigs) {
                effectService.setShapes3Ds(clipConfigs);
                fire();
                fireChange(clipConfigs);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ClipCrud.read failed: " + message, throwable);
            return false;
        }).read();
    }

    @Override
    public void getInstance(ObjectNameId objectNameId, Consumer<ClipConfig> callback) {
        callback.accept(effectService.getClipConfig(objectNameId.getId()));
    }

    @Override
    public void save(ClipConfig clipConfig) {
        caller.call(ignore -> fire(), (message, throwable) -> {
            logger.log(Level.SEVERE, "ClipCrud.update failed: " + message, throwable);
            return false;
        }).update(clipConfig);
    }


    @Override
    public void delete(ClipConfig clipConfig) {
        caller.call(response -> {
            effectService.remove(clipConfig);
            fire();
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ClipCrud.delete failed: " + message, throwable);
            return false;
        }).delete(clipConfig.getId());
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return effectService.getClipConfigs().stream().map(ClipConfig::createObjectNameId).collect(Collectors.toList());
    }

    @Override
    public void onChange(ClipConfig shape3D) {
        // Has been replaced by particle
        // Update BaseItemType renderer
//        for (BaseItemType baseItemType : itemTypeService.getBaseItemTypes()) {
//            WeaponType weaponType = baseItemType.getWeaponType();
//            if (weaponType != null) {
//                if (weaponType.getMuzzleFlashClipId() != null && weaponType.getMuzzleFlashClipId() == shape3D.getId()) {
//                    baseItemRenderTask.onBaseItemTypeChanged(baseItemType);
//                }
//                if (weaponType.getDetonationClipId() != null && weaponType.getDetonationClipId() == shape3D.getId()) {
//                    baseItemRenderTask.onBaseItemTypeChanged(baseItemType);
//                }
//            }
//        }
    }
}
