package com.btxtech.client.editor.slopeeditor;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig_OLD;
import com.btxtech.shared.rest.TerrainElementEditorProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 22.10.2016.
 */
@ApplicationScoped
public class SlopeConfigCrud extends AbstractCrudeEditor<SlopeConfig_OLD> {
    // private Logger logger = Logger.getLogger(SlopeConfigCrud.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Caller<TerrainElementEditorProvider> provider;
    @Inject
    private TerrainTypeService terrainTypeService;

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return terrainTypeService.getSlopeSkeletonConfigs().stream().map(SlopeConfig::createObjectNameId).collect(Collectors.toList());
    }

    @Override
    public void create() {
        provider.call(new RemoteCallback<SlopeConfig_OLD>() {
            @Override
            public void callback(SlopeConfig_OLD slopeConfigOLD) {
                fire();
                fireSelection(slopeConfigOLD.createObjectNameId());
            }
        }, exceptionHandler.restErrorHandler("createSlopeConfig failed: ")).createSlopeConfig();
    }

    @Override
    public void delete(SlopeConfig_OLD slopeConfigOLD) {
        provider.call(ignore -> {
            terrainTypeService.deleteSlopeSkeletonConfig(slopeConfigOLD.getSlopeConfig());
            fire();
        }, exceptionHandler.restErrorHandler("deleteSlopeConfig failed: ")).deleteSlopeConfig(slopeConfigOLD.getId());
    }

    @Override
    public void save(SlopeConfig_OLD slopeConfigOLD) {
        provider.call(ignore -> fire(), exceptionHandler.restErrorHandler("deleteSlopeConfig failed: ")).updateSlopeConfig(slopeConfigOLD);
    }

    @Override
    public void reload() {
        provider.call(new RemoteCallback<List<SlopeConfig_OLD>>() {
            @Override
            public void callback(List<SlopeConfig_OLD> slopeConfigOLDS) {
                terrainTypeService.setSlopeSkeletonConfigs(slopeConfigOLDS.stream().map(SlopeConfig_OLD::getSlopeConfig).collect(Collectors.toCollection(ArrayList::new)));
                fire();
                fireChange(slopeConfigOLDS);
            }
        }, exceptionHandler.restErrorHandler("readSlopeConfigs failed: ")).readSlopeConfigs();
    }

    @Override
    public void getInstance(ObjectNameId id, Consumer<SlopeConfig_OLD> callback) {
        provider.call(new RemoteCallback<SlopeConfig_OLD>() {
            @Override
            public void callback(SlopeConfig_OLD slopeConfigOLD) {
                callback.accept(slopeConfigOLD);
            }
        }, exceptionHandler.restErrorHandler("readSlopeConfig failed: ")).readSlopeConfig(id.getId());
    }
}
