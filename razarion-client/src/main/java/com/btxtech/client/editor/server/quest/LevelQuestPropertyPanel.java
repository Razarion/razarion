package com.btxtech.client.editor.server.quest;

import com.btxtech.client.editor.framework.AbstractObjectNameIdEditor;
import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.framework.ObjectNameIdTable;
import com.btxtech.client.editor.widgets.level.LevelField;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.rest.ServerGameEngineEditorProvider;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("LevelQuestPropertyPanel.html#propertyPanel")
public class LevelQuestPropertyPanel extends AbstractPropertyPanel<ServerLevelQuestConfig> {
    private Logger logger = Logger.getLogger(LevelQuestPropertyPanel.class.getName());
    @Inject
    private Caller<ServerGameEngineEditorProvider> provider;
    @Inject
    @AutoBound
    private DataBinder<ServerLevelQuestConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private TextBox internalName;
    @Inject
    @DataField
    private LevelField minimalLevelId;
    @Inject
    @DataField
    private ObjectNameIdTable objectNameIdTable;

    @Override
    public void init(ServerLevelQuestConfig serverLevelQuestConfig) {
        dataBinder.setModel(serverLevelQuestConfig);
        minimalLevelId.init(serverLevelQuestConfig.getMinimalLevelId(), serverLevelQuestConfig::setMinimalLevelId);
        objectNameIdTable.init(new AbstractObjectNameIdEditor() {

            @Override
            protected void read(RemoteCallback<List<ObjectNameId>> callback) {
                provider.call(callback, (message, throwable) -> {
                    logger.log(Level.SEVERE, "ServerGameEngineEditorProvider.readQuestConfigObjectNameIds failed: " + message, throwable);
                    return false;
                }).readQuestConfigObjectNameIds(serverLevelQuestConfig.getId());
            }

            @Override
            protected void create(RemoteCallback<?> callback) {
                provider.call(callback, (message, throwable) -> {
                    logger.log(Level.SEVERE, "ServerGameEngineEditorProvider.createQuestConfig failed: " + message, throwable);
                    return false;
                }).createQuestConfig(serverLevelQuestConfig.getId());
            }

            @Override
            protected void swap(int index1, int index2) {
                //TODO
            }

            @Override
            protected void delete(ObjectNameId objectNameId) {
                //TODO
            }
        });
    }

    @Override
    public ServerLevelQuestConfig getConfigObject() {
        return dataBinder.getModel();
    }
}
