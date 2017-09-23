package com.btxtech.client.editor.level;

import com.btxtech.client.editor.widgets.I18nStringWidget;
import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import com.btxtech.client.editor.widgets.itemtype.base.BaseItemTypeWidget;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.common.client.dom.TextInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 23.09.2017.
 */
@Templated("LevelUnlockConfigPanel.html#levelUnlockConfigPanel")
public class LevelUnlockConfigPanel extends Composite implements TakesValue<LevelUnlockConfig> {
    @Inject
    @AutoBound
    private DataBinder<LevelUnlockConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private TextInput internalName;
    @Inject
    @DataField
    private ImageItemWidget thumbnail;
    @Inject
    @DataField
    private I18nStringWidget i18nName;
    @Inject
    @DataField
    private I18nStringWidget i18nDescription;
    @Inject
    @DataField
    private BaseItemTypeWidget baseItemType;
    @Inject
    @Bound
    @DataField
    private NumberInput baseItemTypeCount;
    @Inject
    @Bound
    @DataField
    private NumberInput crystalCost;

    @Override
    public void setValue(LevelUnlockConfig levelUnlockConfig) {
        dataBinder.setModel(levelUnlockConfig);
        thumbnail.setImageId(levelUnlockConfig.getThumbnail(), levelUnlockConfig::setThumbnail);
        i18nName.init(levelUnlockConfig.getI18nName(), levelUnlockConfig::setI18nName);
        i18nDescription.init(levelUnlockConfig.getI18nDescription(), levelUnlockConfig::setI18nDescription);
        baseItemType.init(levelUnlockConfig.getBaseItemType(), levelUnlockConfig::setBaseItemType);
    }

    @Override
    public LevelUnlockConfig getValue() {
        return dataBinder.getModel();
    }
}
