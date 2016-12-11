package com.btxtech.client.cockpit.item;

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
import java.util.List;

/**
 * Created by Beat
 * 30.09.2016.
 */
@Templated("ClientBuildupItemPanel.html#buildup-item-panel")
public class ClientBuildupItemPanel extends BuildupItemPanel implements IsElement {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField("buildup-item-panel")
    private Div div;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button leftArrowButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button rightArrowButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
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
        throw new UnsupportedOperationException();
    }

    @EventHandler("rightArrowButton")
    private void rightArrowButtonClick(ClickEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void clear() {
        buildItemTypePanel.setValue(null);
    }

    @Override
    protected void setBuildupItem(List<BuildupItem> buildupItems) {
        DOMUtil.removeAllElementChildren(buildItemTypePanel.getElement()); // Remove placeholder table row from template.
        buildItemTypePanel.setValue(buildupItems);
    }
}
