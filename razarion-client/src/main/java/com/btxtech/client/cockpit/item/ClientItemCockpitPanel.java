package com.btxtech.client.cockpit.item;

import com.btxtech.client.MainPanelService;
import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.utils.Elemental2Utils;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.uiservice.cockpit.item.BuildupItemPanel;
import com.btxtech.uiservice.cockpit.item.ItemCockpitPanel;
import com.btxtech.uiservice.cockpit.item.ItemContainerPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 30.09.2016.
 */
@Templated("ClientItemCockpitPanel.html#item-cockpit")
public class ClientItemCockpitPanel implements IsElement, ItemCockpitPanel {
    @Inject
    private MainPanelService mainPanelService;
    @Inject
    @DataField
    private HTMLDivElement itemCockpitDiv;
    @Inject
    @DataField
    private SimplePanel infoPanel;
    @Inject
    @DataField
    private HTMLDivElement buildupItemPanel;
    @Inject
    @DataField
    private HTMLDivElement itemContainerPanel;

    @PostConstruct
    public void postConstruct() {
        GwtUtils.preventContextMenu(itemCockpitDiv);
    }

    @Override
    public HTMLElement getElement() {
        return itemCockpitDiv;
    }

    @Override
    public void cleanPanels() {
        infoPanel.clear();
        Elemental2Utils.removeAllChildren(buildupItemPanel);
        Elemental2Utils.removeAllChildren(itemContainerPanel);
        itemCockpitDiv.style.zIndex = ZIndexConstants.ITEM_COCKPIT;
    }

    @Override
    public void setInfoPanel(Object infoPanel) {
        this.infoPanel.setWidget((IsWidget) infoPanel);
    }

    @Override
    public void setBuildupItemPanel(BuildupItemPanel buildupItemPanel) {
        this.buildupItemPanel.appendChild(((IsElement) buildupItemPanel).getElement());
    }

    @Override
    public void setItemContainerPanel(ItemContainerPanel itemContainerPanel) {
        this.itemContainerPanel.appendChild(((IsElement) itemContainerPanel).getElement());
    }

    @Override
    public void maximizeMinButton() {
        // TODO
    }

    @Override
    public void showPanel(boolean visible) {
        if (visible) {
            mainPanelService.addToGamePanel(itemCockpitDiv);
        } else {
            mainPanelService.removeFromGamePanel(itemCockpitDiv);
        }
    }
}
