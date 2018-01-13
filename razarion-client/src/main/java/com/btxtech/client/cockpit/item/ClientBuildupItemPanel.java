package com.btxtech.client.cockpit.item;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.uiservice.cockpit.item.BuildupItem;
import com.btxtech.uiservice.cockpit.item.BuildupItemPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 30.09.2016.
 */
@Templated("ClientBuildupItemPanel.html#buildup-item-panel")
public class ClientBuildupItemPanel extends BuildupItemPanel implements IsElement {
    @Inject
    @DataField("buildup-item-panel")
    private Div div;
    @Inject
    @DataField
    private Button leftArrowButton;
    @Inject
    @DataField
    private Button rightArrowButton;
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<BuildupItem, ClientBuildupItem> buildItemTypePanel;

    @Override
    public HTMLElement getElement() {
        return div;
    }

    @EventHandler("leftArrowButton")
    private void leftArrowButtonClick(ClickEvent event) {
        // TODO throw new UnsupportedOperationException();
    }

    @EventHandler("rightArrowButton")
    private void rightArrowButtonClick(ClickEvent event) {
        // TODO throw new UnsupportedOperationException();
    }

    @Override
    protected void clear() {
        buildItemTypePanel.setValue(Collections.emptyList());
    }

    @Override
    protected void setBuildupItem(List<BuildupItem> buildupItems) {
        DOMUtil.removeAllElementChildren(buildItemTypePanel.getElement()); // Remove placeholder table row from template.
        buildItemTypePanel.setValue(buildupItems);
    }

    @Override
    protected Rectangle getBuildButtonLocation(BuildupItem buildupItem) {
        return buildItemTypePanel.getComponent(buildupItem).orElseThrow(IllegalStateException::new).getBuildButtonLocation();
    }

    @Override
    public void onResourcesChanged(int resources) {
        for (BuildupItem buildupItem : buildItemTypePanel.getValue()) {
            buildItemTypePanel.getComponent(buildupItem).orElseThrow(IllegalStateException::new).onResourcesChanged(resources);
        }
    }
}
