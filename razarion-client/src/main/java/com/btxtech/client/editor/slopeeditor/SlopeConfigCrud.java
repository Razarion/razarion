package com.btxtech.client.editor.slopeeditor;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.rest.TerrainElementEditorProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 22.10.2016.
 */
@ApplicationScoped
public class SlopeConfigCrud extends AbstractCrudeEditor<SlopeConfig> {
    private Logger logger = Logger.getLogger(SlopeConfigCrud.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<TerrainElementEditorProvider> provider;
    @Inject
    private TerrainTypeService terrainTypeService;

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return terrainTypeService.getSlopeSkeletonConfigs().stream().map(SlopeSkeletonConfig::createObjectNameId).collect(Collectors.toList());
    }

    @Override
    public void create() {
        provider.call(new RemoteCallback<SlopeConfig>() {
            @Override
            public void callback(SlopeConfig slopeConfig) {
                fire();
                fireSelection(slopeConfig.createObjectNameId());
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "createSlopeConfig failed: " + message, throwable);
            return false;
        }).createSlopeConfig();
    }

    @Override
    public void delete(SlopeConfig slopeConfig) {
        provider.call(ignore -> {
            terrainTypeService.deleteSlopeSkeletonConfig(slopeConfig.getSlopeSkeletonConfig());
            fire();
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "deleteSlopeConfig failed: " + message, throwable);
            return false;
        }).deleteSlopeConfig(slopeConfig.getId());
    }

    @Override
    public void save(SlopeConfig slopeConfig) {
        provider.call(ignore -> fire(), (message, throwable) -> {
            logger.log(Level.SEVERE, "deleteSlopeConfig failed: " + message, throwable);
            return false;
        }).updateSlopeConfig(slopeConfig);
    }

    @Override
    public void reload() {
        provider.call(new RemoteCallback<List<SlopeConfig>>() {
            @Override
            public void callback(List<SlopeConfig> slopeConfigs) {
                terrainTypeService.setSlopeSkeletonConfigs(slopeConfigs.stream().map(SlopeConfig::getSlopeSkeletonConfig).collect(Collectors.toCollection(ArrayList::new)));
                fire();
                fireChange(slopeConfigs);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "readSlopeConfigs failed: " + message, throwable);
            return false;
        }).readSlopeConfigs();
    }

    @Override
    public void getInstance(ObjectNameId id, Consumer<SlopeConfig> callback) {
        provider.call(new RemoteCallback<SlopeConfig>() {
            @Override
            public void callback(SlopeConfig slopeConfig) {
                callback.accept(slopeConfig);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "readSlopeConfig failed: " + message, throwable);
            return false;
        }).readSlopeConfig(id.getId());
    }
}
