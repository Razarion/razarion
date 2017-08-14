package com.btxtech.client.editor.client.scene;

import com.btxtech.client.editor.widgets.marker.DecimalPositionWidget;
import com.btxtech.client.editor.widgets.itemtype.resource.ResourceItemTypeWidget;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 13.08.2017.
 */
@Templated("SceneConfigPropertyPanel.html#resourceItemTypePositionRow")
public class ResourceItemPositionRow implements TakesValue<ResourceItemPosition>, IsElement {
    @Inject
    @DataField
    private TableRow resourceItemTypePositionRow;
    @Inject
    @AutoBound
    private DataBinder<ResourceItemPosition> dataBinder;
    @Inject
    @Bound
    @DataField
    private DecimalPositionWidget position;
    @Inject
    @Bound(converter = GradToRadConverter.class)
    @DataField
    private CommaDoubleBox rotationZ;
    @Inject
    @DataField
    private ResourceItemTypeWidget resourceItemTypeWidget;
    @Inject
    @DataField
    private Button resourcePositionDeleteButton;
    private SceneConfigPropertyPanel sceneConfigPropertyPanel;

    @Override
    public void setValue(ResourceItemPosition resourceItemPosition) {
        dataBinder.setModel(resourceItemPosition);
        resourceItemTypeWidget.init(resourceItemPosition.getResourceItemTypeId(), resourceItemPosition::setResourceItemTypeId);
    }

    @Override
    public ResourceItemPosition getValue() {
        return dataBinder.getModel();
    }

    @Override
    public HTMLElement getElement() {
        return resourceItemTypePositionRow;
    }

    @EventHandler("resourcePositionDeleteButton")
    private void resourcePositionDeleteButtonClicked(ClickEvent event) {
        sceneConfigPropertyPanel.removeResourceItemPosition(dataBinder.getModel());
    }

    public void setSceneConfigPropertyPanel(SceneConfigPropertyPanel sceneConfigPropertyPanel) {
        this.sceneConfigPropertyPanel = sceneConfigPropertyPanel;
    }
}
