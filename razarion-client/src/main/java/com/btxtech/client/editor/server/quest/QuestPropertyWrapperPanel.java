package com.btxtech.client.editor.server.quest;

import com.btxtech.client.editor.framework.ObjectNamePropertyPanel;
import com.btxtech.client.editor.widgets.quest.QuestPropertyPanel;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.rest.ServerGameEngineEditorProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 16.08.2017.
 */
@Templated("QuestPropertyWrapperPanel.html#questPropertyPanel")
public class QuestPropertyWrapperPanel extends ObjectNamePropertyPanel {
    private Logger logger = Logger.getLogger(QuestPropertyWrapperPanel.class.getName());
    @Inject
    private Caller<ServerGameEngineEditorProvider> provider;
    @Inject
    @DataField
    private QuestPropertyPanel questPropertyPanel;

    @Override
    public void setObjectNameId(ObjectNameId objectNameId) {
        ServerLevelQuestConfig serverLevelQuestConfig = (ServerLevelQuestConfig) getPredecessorConfigObject();
        provider.call(response -> questPropertyPanel.init((QuestConfig) response), (message, throwable) -> {
            logger.log(Level.SEVERE, "ServerGameEngineEditorProvider.readQuestConfig failed: " + message, throwable);
            return false;
        }).readQuestConfig(serverLevelQuestConfig.getId(), objectNameId.getId());
        registerSaveButton(this::save);
        enableSaveButton(true);
    }

    @Override
    public Object getConfigObject() {
        return questPropertyPanel.getQuestConfig();
    }

    private void save() {
        ServerLevelQuestConfig serverLevelQuestConfig = (ServerLevelQuestConfig) getPredecessorConfigObject();
        provider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ServerGameEngineEditorProvider.readQuestConfig failed: " + message, throwable);
            return false;
        }).updateQuestConfig(serverLevelQuestConfig.getId(), questPropertyPanel.getQuestConfig());
    }

}
