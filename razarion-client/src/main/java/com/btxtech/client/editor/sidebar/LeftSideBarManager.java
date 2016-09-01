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
public class LeftSideBarManager {
    private Logger logger = Logger.getLogger(LeftSideBarManager.class.getName());
    @Inject
    private Instance<SideBarPanel> sideBarPanelInstance;
    private SideBarPanel sideBarPanel;

    public void show(Class<? extends LeftSideBarContent> leftSideBarContentClass) {
        if (sideBarPanel == null) {
            sideBarPanel = sideBarPanelInstance.get();
            RootPanel.get().add(sideBarPanel);
        }
        sideBarPanel.setContent(leftSideBarContentClass);
    }

    // Is only called from SideBarPanel
    void close() {
        if (sideBarPanel == null) {
            logger.severe("LeftSideBarManager already null");
        } else {
            RootPanel.get().remove(sideBarPanel);
            sideBarPanel = null;
        }
    }
}
