package com.btxtech.client.editor.sidebar;

import com.btxtech.client.MainPanelService;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Stack;

/**
 * Created by Beat
 * 03.05.2016.
 */
@Singleton
public class LeftSideBarManager {
    // private Logger logger = Logger.getLogger(LeftSideBarManager.class.getName());
    @Inject
    private MainPanelService mainPanelService;
    @Inject
    private Instance<SideBarPanel> sideBarPanelInstance;
    private SideBarPanel sideBarPanel;
    private Stack<SideBarPanel> sideBarPanelStack = new Stack<>();

    public void show(Class<? extends LeftSideBarContent> leftSideBarContentClass) {
        if (sideBarPanel == null) {
            sideBarPanel = sideBarPanelInstance.get();
            mainPanelService.addToGamePanel(sideBarPanel);
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
        mainPanelService.addToGamePanel(sideBarPanel);
    }

    public void close() {
        if (sideBarPanel != null) {
            mainPanelService.removeFromGamePanel(sideBarPanel);
            sideBarPanel = null;
        }
    }

}
