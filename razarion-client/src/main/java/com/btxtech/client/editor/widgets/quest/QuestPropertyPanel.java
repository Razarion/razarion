package com.btxtech.client.editor.widgets.quest;

import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.common.client.dom.TextInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 06.08.2017.
 */
@Templated("QuestPropertyPanel.html#propertyPanel")
public class QuestPropertyPanel extends Composite {
    private Logger logger = Logger.getLogger(QuestPropertyPanel.class.getName());
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
    private NumberInput razarion;
    @Inject
    @Bound
    @DataField
    private NumberInput crystal;
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

    public void init(QuestConfig questConfig) {
        dataBinder.setModel(questConfig);
        conditionConfig.init(questConfig.getConditionConfig(), questConfig::setConditionConfig);
    }

    public QuestConfig getQuestConfig() {
        return dataBinder.getModel();
    }
}
