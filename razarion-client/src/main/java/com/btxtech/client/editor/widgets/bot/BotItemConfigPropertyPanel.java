package com.btxtech.client.editor.widgets.bot;


import com.btxtech.client.editor.widgets.itemtype.base.BaseItemTypeWidget;
import com.btxtech.client.editor.widgets.placeconfig.PlaceConfigWidget;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 16.08.2017.
 */
@Templated("BotItemConfigPropertyPanel.html#botitemconfigpanel")
public class BotItemConfigPropertyPanel extends Composite implements TakesValue<BotItemConfig> {
    @Inject
    @AutoBound
    private DataBinder<BotItemConfig> dataBinder;
    @Inject
    @DataField
    private BaseItemTypeWidget baseItemTypeId;
    @Inject
    @Bound
    @DataField
    private NumberInput count;
    @Inject
    @Bound
    @DataField
    private CheckboxInput createDirectly;
    @Inject
    @Bound
    @DataField
    private CheckboxInput noSpawn;
    @Inject
    @Bound
    @DataField
    private PlaceConfigWidget place;
    @Inject
    @Bound(converter = GradToRadConverter.class)
    @DataField
    private CommaDoubleBox angle;
    @Inject
    @Bound
    @DataField
    private CheckboxInput moveRealmIfIdle;
    @Inject
    @Bound
    @DataField
    private NumberInput idleTtl;
    @Inject
    @Bound
    @DataField
    private CheckboxInput noRebuild;
    @Inject
    @Bound
    @DataField
    private NumberInput rePopTime;

    @Override
    public void setValue(BotItemConfig botItemConfig) {
        dataBinder.setModel(botItemConfig);
        baseItemTypeId.init(botItemConfig.getBaseItemTypeId(), botItemConfig::baseItemTypeId);
    }

    @Override
    public BotItemConfig getValue() {
        return dataBinder.getModel();
    }
}
