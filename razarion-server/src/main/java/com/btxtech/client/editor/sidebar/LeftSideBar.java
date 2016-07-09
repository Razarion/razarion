package com.btxtech.client.editor.sidebar;

import com.google.gwt.user.client.ui.RootPanel;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 03.05.2016.
 */
@Singleton
public class LeftSideBar {
    private Logger logger = Logger.getLogger(LeftSideBar.class.getName());
    @Inject
    private Instance<SideBarPanel> sideBarPanelInstance;
    private SideBarPanel sideBarPanel;

    public void show(LeftSideBarContent leftSideBarContent) {
        if (sideBarPanel == null) {
            sideBarPanel = sideBarPanelInstance.get();
            RootPanel.get().add(sideBarPanel);
        } else {
            sideBarPanel.getContent().onClose();
        }
        // sideBarPanel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        sideBarPanel.setContent(leftSideBarContent);
    }

    public void onClose(LeftSideBarContent leftSideBarContent) {
        leftSideBarContent.onClose();
        if (sideBarPanel == null) {
            logger.severe("LeftSideBar already null");
        } else {
            RootPanel.get().remove(sideBarPanel);
            sideBarPanel = null;
        }
    }
}
