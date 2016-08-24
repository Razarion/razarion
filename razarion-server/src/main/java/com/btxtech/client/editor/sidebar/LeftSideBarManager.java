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
    @Inject
    private Instance<LeftSideBarContent> leftSideBarContentInstance;
    private SideBarPanel sideBarPanel;

    public void show(Class<? extends LeftSideBarContent> leftSideBarContentClass) {
        if (sideBarPanel == null) {
            sideBarPanel = sideBarPanelInstance.get();
            RootPanel.get().add(sideBarPanel);
        } else {
            sideBarPanel.getContent().onClose();
        }
        sideBarPanel.setContent(leftSideBarContentInstance.select(leftSideBarContentClass).get());
    }

    public void close(LeftSideBarContent leftSideBarContent) {
        leftSideBarContent.onClose();
        if (sideBarPanel == null) {
            logger.severe("LeftSideBarManager already null");
        } else {
            if (leftSideBarContent != sideBarPanel.getContent()) {
                logger.severe("Closing command comes from a different LeftSideBarContent then shown");
            }
            RootPanel.get().remove(sideBarPanel);
            sideBarPanel = null;
        }
    }
}
