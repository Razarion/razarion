package com.btxtech.client.editor.server.quest;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.level.LevelField;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("LevelQuestPropertyPanel.html#propertyPanel")
public class LevelQuestPropertyPanel extends AbstractPropertyPanel<ServerLevelQuestConfig> {
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

    @Override
    public void init(ServerLevelQuestConfig serverLevelQuestConfig) {
        dataBinder.setModel(serverLevelQuestConfig);
        minimalLevelId.init(serverLevelQuestConfig.getMinimalLevelId(), serverLevelQuestConfig::setMinimalLevelId);
    }

    @Override
    public ServerLevelQuestConfig getConfigObject() {
        return dataBinder.getModel();
    }
}
