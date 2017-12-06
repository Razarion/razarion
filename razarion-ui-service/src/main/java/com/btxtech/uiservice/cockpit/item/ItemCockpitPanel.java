package com.btxtech.uiservice.cockpit.item;

/**
 * Created by Beat
 * 30.09.2016.
 */
public interface ItemCockpitPanel {
    void cleanPanels();

    void setInfoPanel(Object infoPanel);

    void setBuildupItemPanel(BuildupItemPanel buildupItemPanel);

    void setItemContainerPanel(ItemContainerPanel itemContainerPanel);

    void maximizeMinButton();

    void showPanel(boolean visible);
}
