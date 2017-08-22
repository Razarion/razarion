package com.btxtech.client.editor.level;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.itemtype.basecount.BaseItemTypeCountWidget;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
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
public class LevelConfigPropertyPanel extends AbstractPropertyPanel<LevelConfig> {
    @Inject
    @AutoBound
    private DataBinder<LevelConfig> dataBinder;
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

    @Override
    public void init(LevelConfig levelConfig) {
        dataBinder.setModel(levelConfig);
        itemTypeLimitation.init(levelConfig.getItemTypeLimitation(), levelConfig::setItemTypeLimitation);
    }

    @Override
    public LevelConfig getConfigObject() {
        return dataBinder.getModel();
    }
}
