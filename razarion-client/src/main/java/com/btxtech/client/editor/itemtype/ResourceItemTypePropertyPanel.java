package com.btxtech.client.editor.itemtype;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.shape3dwidget.Shape3DReferenceFiled;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Created by Beat
 * 24.08.2016.
 */
@Templated("ResourceItemTypePropertyPanel.html#resource-item-property-panel")
public class ResourceItemTypePropertyPanel extends AbstractPropertyPanel<ResourceItemType> {
    @Inject
    @AutoBound
    private DataBinder<ResourceItemType> resourceItemTypeDataBinder;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Bound
    @DataField
    private Label id;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Bound
    @DataField
    private TextBox name;
    @Inject
    @DataField
    private Shape3DReferenceFiled shape3DReferenceFiled;
    @Inject
    @Bound
    @DataField
    private DoubleBox radius;
    @Inject
    @Bound
    @DataField
    private IntegerBox amount;
    @Inject
    private Event<ResourceItemType> trigger;
    private ResourceItemType resourceItemType;

    @Override
    public void init(ResourceItemType resourceItemType) {
        this.resourceItemType = resourceItemType;
        resourceItemTypeDataBinder.setModel(resourceItemType);
        shape3DReferenceFiled.init(resourceItemType.getShape3DId(), shape3DId -> {
            resourceItemType.setShape3DId(shape3DId);
            trigger.fire(resourceItemTypeDataBinder.getModel()); // Inform Renderer
        });
    }

    @Override
    public ResourceItemType getConfigObject() {
        return resourceItemType;
    }
}
