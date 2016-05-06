package com.btxtech.client.sidebar;

import com.btxtech.client.SideBarPanel;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Widget;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 03.05.2016.
 */
@Singleton
public class LeftSideBar {
    private SideBarPanel sideBarPanel;

    public void setSideBarPanel(SideBarPanel sideBarPanel) {
        this.sideBarPanel = sideBarPanel;
    }

    public void show(LeftSideBarContent leftSideBarContent) {
        sideBarPanel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        sideBarPanel.setContent(leftSideBarContent);
    }

    public void onClose(LeftSideBarContent leftSideBarContent) {
        leftSideBarContent.onClose();
    }
}
