package com.btxtech.client.editor.server.quest;

import com.btxtech.client.editor.framework.ObjectNamePropertyPanel;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.rest.ServerGameEngineEditorProvider;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.common.client.dom.TextInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 06.08.2017.
 */
@Templated("QuestPropertyPanel.html#propertyPanel")
public class QuestPropertyPanel extends ObjectNamePropertyPanel {
    private Logger logger = Logger.getLogger(QuestPropertyPanel.class.getName());
    @Inject
    private Caller<ServerGameEngineEditorProvider> provider;
    @Inject
    @AutoBound
    private DataBinder<QuestConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private TextInput internalName;
    @Inject
    @Bound
    @DataField
    private TextInput title;
    @Inject
    @Bound
    @DataField
    private TextInput description;
    @Inject
    @Bound
    @DataField
    private NumberInput xp;
    @Inject
    @Bound
    @DataField
    private NumberInput money;
    @Inject
    @Bound
    @DataField
    private NumberInput cristal;
    @Inject
    @Bound
    @DataField
    private TextInput passedMessage;
    @Inject
    @Bound
    @DataField
    private CheckboxInput hidePassedDialog;
    @Inject
    @DataField
    private ConditionConfigPropertyPanel conditionConfig;

    @Override
    public void setObjectNameId(ObjectNameId objectNameId) {
        ServerLevelQuestConfig serverLevelQuestConfig = (ServerLevelQuestConfig) getPredecessorConfigObject();
        provider.call(this::onQuestConfig, (message, throwable) -> {
            logger.log(Level.SEVERE, "ServerGameEngineEditorProvider.readQuestConfig failed: " + message, throwable);
            return false;
        }).readQuestConfig(serverLevelQuestConfig.getId(), objectNameId.getId());
        registerSaveButton(this::save);
        enableSaveButton(true);
    }

    private void onQuestConfig(Object o) {
        QuestConfig questConfig = (QuestConfig) o;
        dataBinder.setModel(questConfig);
        conditionConfig.init(questConfig.getConditionConfig(), questConfig::setConditionConfig);
    }

    @Override
    public Object getConfigObject() {
        return dataBinder.getModel();
    }

    private void save() {
        ServerLevelQuestConfig serverLevelQuestConfig = (ServerLevelQuestConfig) getPredecessorConfigObject();
        provider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "ServerGameEngineEditorProvider.readQuestConfig failed: " + message, throwable);
            return false;
        }).updateQuestConfig(serverLevelQuestConfig.getId(), dataBinder.getModel());
    }
}
