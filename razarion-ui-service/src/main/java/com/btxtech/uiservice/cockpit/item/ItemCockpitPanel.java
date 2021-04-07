package com.btxtech.uiservice.cockpit.item;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 30.09.2016.
 */
@JsType(isNative = true)
public interface ItemCockpitPanel {
    void cleanPanels();

    void setInfoPanel(Object infoPanel);

    void setBuildupItemPanel(BuildupItemPanel buildupItemPanel);

    void setItemContainerPanel(ItemContainerPanel itemContainerPanel);

    void maximizeMinButton();

    void showPanel(boolean visible);
}
