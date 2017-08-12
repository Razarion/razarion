package com.btxtech.client.editor.server.resource;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.itemtype.resource.ResourceItemTypeWidget;
import com.btxtech.client.editor.widgets.placeconfig.PlaceConfigWidget;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.shared.dto.ResourceRegionConfig;
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
@Templated("ResourceRegionPropertyPanel.html#propertyPanel")
public class ResourceRegionPropertyPanel extends AbstractPropertyPanel<ResourceRegionConfig> {
    @Inject
    @AutoBound
    private DataBinder<ResourceRegionConfig> dataBinder;
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
    private ResourceItemTypeWidget resourceItemTypeWidget;
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
    public void init(ResourceRegionConfig resourceRegionConfig) {
        dataBinder.setModel(resourceRegionConfig);
        resourceItemTypeWidget.init(resourceRegionConfig.getResourceItemTypeId(), resourceRegionConfig::setResourceItemTypeId);
    }

    @Override
    public ResourceRegionConfig getConfigObject() {
        return dataBinder.getModel();
    }
}
