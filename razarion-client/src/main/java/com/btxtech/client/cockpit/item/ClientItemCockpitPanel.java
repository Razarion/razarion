package com.btxtech.client.cockpit.item;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.uiservice.cockpit.item.BuildupItemPanel;
import com.btxtech.uiservice.cockpit.item.ItemCockpitPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 30.09.2016.
 */
@Templated("ClientItemCockpitPanel.html#item-cockpit")
public class ClientItemCockpitPanel extends Composite implements ItemCockpitPanel {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private SimplePanel infoPanel;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private SimplePanel buildupItemPanel;

    @Override
    public void cleanPanels() {
        infoPanel.clear();
        buildupItemPanel.clear();
        getElement().getStyle().setZIndex(ZIndexConstants.ITEM_COCKPIT);
    }

    @Override
    public void setInfoPanel(Object infoPanel) {
        this.infoPanel.setWidget((IsWidget) infoPanel);
    }

    @Override
    public void setBuildupItemPanel(BuildupItemPanel buildupItemPanel) {
        this.buildupItemPanel.setWidget((IsWidget) buildupItemPanel);
    }

    @Override
    public void maximizeMinButton() {
        // TODO
    }

    @Override
    public void showPanel(boolean visible) {
        if (visible) {
            RootPanel.get().add(this);
        } else {
            RootPanel.get().remove(this);
        }
    }
}
