package com.btxtech.client.editor.client.scene;

import com.btxtech.client.editor.widgets.itemtype.base.BaseItemTypeWidget;
import com.btxtech.client.editor.widgets.itemtype.resource.ResourceItemTypeWidget;
import com.btxtech.client.editor.widgets.placeconfig.PlaceConfigWidget;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 17.08.2017.
 */
@Templated("BotHarvestCommandConfigPropertyPanel.html#botHarvestCommandConfigPropertyPanel")
public class BotHarvestCommandConfigPropertyPanel extends Composite implements TakesValue<BotHarvestCommandConfig> {
    @Inject
    @AutoBound
    private DataBinder<BotHarvestCommandConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private NumberInput botAuxiliaryId;
    @Inject
    @DataField
    private BaseItemTypeWidget harvesterItemTypeId;
    @Inject
    @DataField
    private ResourceItemTypeWidget resourceItemTypeId;
    @Inject
    @Bound
    @DataField
    private PlaceConfigWidget resourceSelection;

    @Override
    public void setValue(BotHarvestCommandConfig botHarvestCommandConfig) {
        dataBinder.setModel(botHarvestCommandConfig);
        harvesterItemTypeId.init(botHarvestCommandConfig.getHarvesterItemTypeId(), botHarvestCommandConfig::setHarvesterItemTypeId);
        resourceItemTypeId.init(botHarvestCommandConfig.getResourceItemTypeId(), botHarvestCommandConfig::setResourceItemTypeId);
    }

    @Override
    public BotHarvestCommandConfig getValue() {
        return dataBinder.getModel();
    }
}
