package com.btxtech.client.editor.sidebar;

import com.google.gwt.user.client.ui.RootPanel;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Stack;
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
    private Stack<SideBarPanel> sideBarPanelStack = new Stack<>();

    public void show(Class<? extends LeftSideBarContent> leftSideBarContentClass) {
        if (sideBarPanel == null) {
            sideBarPanel = sideBarPanelInstance.get();
            RootPanel.get().add(sideBarPanel);
        }
        sideBarPanel.setContent(leftSideBarContentClass);
    }

    public <T extends LeftSideBarStackContent> T stack(Class<T> leftSideBarStackContentClass) {
        if (sideBarPanel == null) {
            throw new IllegalStateException("Can not stack. No panel is active");
        }
        sideBarPanelStack.push(sideBarPanel);
        SideBarPanel predecessor = sideBarPanel;
        close();
        show(leftSideBarStackContentClass);
        T t = (T) sideBarPanel.getContent();
        t.setPredecessor(predecessor);
        return t;
    }

    public void pop() {
        if (sideBarPanel == null) {
            throw new IllegalStateException("Can not pop. No panel is active");
        }
        if (sideBarPanelStack.isEmpty()) {
            throw new IllegalStateException("Can not pop. No panel in in the stack");
        }
        close();
        sideBarPanel = sideBarPanelStack.pop();
        RootPanel.get().add(sideBarPanel);
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
