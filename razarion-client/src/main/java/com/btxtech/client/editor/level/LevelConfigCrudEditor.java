package com.btxtech.client.editor.level;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.rest.LevelEditorProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 22.08.2017.
 */
public class LevelConfigCrudEditor extends AbstractCrudeEditor<LevelConfig> {
    private Logger logger = Logger.getLogger(LevelConfigCrudEditor.class.getName());
    @Inject
    private Caller<LevelEditorProvider> provider;
    private List<ObjectNameId> objectNameIds = new ArrayList<>();

    @Override
    public void init() {
        provider.call(new RemoteCallback<List<ObjectNameId>>() {
            @Override
            public void callback(List<ObjectNameId> objectNameIds) {
                objectNameIds.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getInternalName())));
                LevelConfigCrudEditor.this.objectNameIds = objectNameIds;
                fire();
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "LevelEditorProvider.readObjectNameIds failed: " + message, throwable);
            return false;
        }).readObjectNameIds();
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return objectNameIds;
    }

    @Override
    public void create() {
        provider.call(new RemoteCallback<LevelConfig>() {
            @Override
            public void callback(LevelConfig levelConfig) {
                objectNameIds.add(levelConfig.createObjectNameId());
                objectNameIds.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getInternalName())));
                fire();
                fireSelection(levelConfig.createObjectNameId());
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "LevelEditorProvider.create failed: " + message, throwable);
            return false;
        }).create();
    }

    @Override
    public void delete(LevelConfig levelConfig) {
        provider.call(new RemoteCallback<Void>() {
            @Override
            public void callback(Void aVoid) {
                objectNameIds.removeIf(objectNameId -> objectNameId.getId() == levelConfig.getLevelId());
                fire();
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "LevelEditorProvider.delete failed: " + message, throwable);
            return false;
        }).delete(levelConfig.getLevelId());
    }

    @Override
    public void save(LevelConfig levelConfig) {
        provider.call(ignore -> fire(), (message, throwable) -> {
            logger.log(Level.SEVERE, "LevelEditorProvider.update failed: " + message, throwable);
            return false;
        }).update(levelConfig);
    }

    @Override
    public void reload() {
        init();
    }

    @Override
    public void getInstance(ObjectNameId id, Consumer<LevelConfig> callback) {
        provider.call(new RemoteCallback<LevelConfig>() {
            @Override
            public void callback(LevelConfig levelConfig) {
                callback.accept(levelConfig);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "LevelEditorProvider.reads failed: " + message, throwable);
            return false;
        }).reads(id.getId());
    }
}
