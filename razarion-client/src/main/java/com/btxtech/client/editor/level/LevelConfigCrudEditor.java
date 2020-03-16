package com.btxtech.client.editor.level;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.config.LevelEditConfig;
import com.btxtech.shared.rest.LevelEditorController;
import org.jboss.errai.common.client.api.Caller;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 22.08.2017.
 */
public class LevelConfigCrudEditor extends AbstractCrudeEditor<LevelEditConfig> {
    // private Logger logger = Logger.getLogger(LevelConfigCrudEditor.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Caller<LevelEditorController> provider;
    private List<ObjectNameId> objectNameIds = new ArrayList<>();

    @Override
    public void init() {
//   TODO     provider.call((RemoteCallback<List<ObjectNameId>>) objectNameIds -> {
//            objectNameIds.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getInternalName())));
//            LevelConfigCrudEditor.this.objectNameIds = objectNameIds;
//            fire();
//        }, exceptionHandler.restErrorHandler("LevelEditorProvider.readObjectNameIds failed: ")).readObjectNameIds();
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return objectNameIds;
    }

    @Override
    public void create() {
//  TODO      provider.call((RemoteCallback<LevelEditConfig>) levelConfig -> {
//            objectNameIds.add(levelConfig.createObjectNameId());
//            objectNameIds.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getInternalName())));
//            fire();
//            fireSelection(levelConfig.createObjectNameId());
//        }, exceptionHandler.restErrorHandler("LevelEditorProvider.create failed: ")).create();
    }

    @Override
    public void delete(LevelEditConfig levelEditConfig) {
//    TODO    provider.call((RemoteCallback<Void>) aVoid -> {
//            objectNameIds.removeIf(objectNameId -> objectNameId.getId() == levelEditConfig.getLevelId());
//            fire();
//        }, exceptionHandler.restErrorHandler("LevelEditorProvider.delete failed: ")).delete(levelEditConfig.getLevelId());
    }

    @Override
    public void save(LevelEditConfig levelEditConfig) {
       // TODO provider.call(ignore -> fire(), exceptionHandler.restErrorHandler("LevelEditorProvider.update failed: ")).update(levelEditConfig);
    }

    @Override
    public void reload() {
        init();
    }

    @Override
    public void getInstance(ObjectNameId id, Consumer<LevelEditConfig> callback) {
        // TODO provider.call((RemoteCallback<LevelEditConfig>) callback::accept, exceptionHandler.restErrorHandler("LevelEditorProvider.read failed: ")).read(id.getId());
    }
}
