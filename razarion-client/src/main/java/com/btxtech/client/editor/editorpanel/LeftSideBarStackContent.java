package com.btxtech.client.editor.editorpanel;

/**
 * Created by Beat
 * on 05.08.2017.
 */
// Not needed anymore... may be
@Deprecated
public class LeftSideBarStackContent extends AbstractEditor {
    private EditorPanel predecessor;

    @Override
    public void init(EditorPanel sideBarPanel) {
        super.init(sideBarPanel);
        // TODO sideBarPanel.getCloseButton().setVisible(false);
        // TODO sideBarPanel.getBackButton().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
    }

    public EditorPanel getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(EditorPanel predecessor) {
        this.predecessor = predecessor;
    }
}
