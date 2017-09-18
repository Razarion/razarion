package com.btxtech.client.editor.server.box;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.itemtype.box.BoxItemTypeWidget;
import com.btxtech.client.editor.widgets.placeconfig.PlaceConfigWidget;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.common.client.dom.NumberInput;
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
@Templated("BoxRegionPropertyPanel.html#propertyPanel")
public class BoxRegionPropertyPanel extends AbstractPropertyPanel<BoxRegionConfig> {
    @Inject
    @AutoBound
    private DataBinder<BoxRegionConfig> dataBinder;
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
    private BoxItemTypeWidget boxItemTypeWidget;
    @Inject
    @Bound
    @DataField
    private NumberInput minInterval;
    @Inject
    @Bound
    @DataField
    private NumberInput maxInterval;
    @Inject
    @Bound
    @DataField
    private PlaceConfigWidget region;
    @Inject
    @Bound
    @DataField
    private NumberInput count;
    @Inject
    @Bound
    @DataField
    private CommaDoubleBox minDistanceToItems;

    @Override
    public void init(BoxRegionConfig boxRegionConfig) {
        dataBinder.setModel(boxRegionConfig);
        boxItemTypeWidget.init(boxRegionConfig.getBoxItemTypeId(), boxRegionConfig::setBoxItemTypeId);
    }

    @Override
    public BoxRegionConfig getConfigObject() {
        return dataBinder.getModel();
    }
}
