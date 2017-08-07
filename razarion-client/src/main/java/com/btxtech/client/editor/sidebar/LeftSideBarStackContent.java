package com.btxtech.client.editor.sidebar;

import com.google.gwt.dom.client.Style;

/**
 * Created by Beat
 * on 05.08.2017.
 */
public class LeftSideBarStackContent extends LeftSideBarContent {
    private SideBarPanel predecessor;

    @Override
    public void init(SideBarPanel sideBarPanel) {
        super.init(sideBarPanel);
        sideBarPanel.getCloseButton().setVisible(false);
        sideBarPanel.getBackButton().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
    }

    public SideBarPanel getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(SideBarPanel predecessor) {
        this.predecessor = predecessor;
    }
}
