package com.btxtech.client.editor.level;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.childtable.ChildTable;
import com.btxtech.client.editor.widgets.itemtype.basecount.BaseItemTypeCountWidget;
import com.btxtech.shared.gameengine.datatypes.config.LevelEditConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 22.08.2017.
 */
@Templated("LevelConfigPropertyPanel.html#levelConfigPropertyPanel")
public class LevelConfigPropertyPanel extends AbstractPropertyPanel<LevelEditConfig> {
    @Inject
    @AutoBound
    private DataBinder<LevelEditConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private Label levelId;
    @Inject
    @Bound
    @DataField
    private NumberInput number;
    @Inject
    @Bound
    @DataField
    private NumberInput xp2LevelUp;
    @Inject
    @DataField
    private BaseItemTypeCountWidget itemTypeLimitation;
    @Inject
    @DataField
    private ChildTable<LevelUnlockConfig> levelUnlockConfigChildTable;

    @Override
    public void init(LevelEditConfig levelEditConfig) {
        dataBinder.setModel(levelEditConfig);
        itemTypeLimitation.init(levelEditConfig.getItemTypeLimitation(), levelEditConfig::setItemTypeLimitation);
        levelUnlockConfigChildTable.init(levelEditConfig.getLevelUnlockConfigs(), levelEditConfig::setLevelUnlockConfigs, LevelUnlockConfig::new, LevelUnlockConfigPanel.class);
    }

    @Override
    public LevelEditConfig getConfigObject() {
        return dataBinder.getModel();
    }
}
