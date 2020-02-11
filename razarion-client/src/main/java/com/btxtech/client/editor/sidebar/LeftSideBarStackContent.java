package com.btxtech.client.editor.sidebar;

/**
 * Created by Beat
 * on 05.08.2017.
 */
// Not needed anymore... may be
@Deprecated
public class LeftSideBarStackContent extends LeftSideBarContent {
    private SideBarPanel predecessor;

    @Override
    public void init(SideBarPanel sideBarPanel) {
        super.init(sideBarPanel);
        // TODO sideBarPanel.getCloseButton().setVisible(false);
        // TODO sideBarPanel.getBackButton().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
    }

    public SideBarPanel getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(SideBarPanel predecessor) {
        this.predecessor = predecessor;
    }
}
