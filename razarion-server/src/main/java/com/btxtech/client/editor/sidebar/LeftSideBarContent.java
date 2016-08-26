package com.btxtech.client.editor.sidebar;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;

/**
 * Created by Beat
 * 05.05.2016.
 */
public abstract class LeftSideBarContent extends Composite {
    private SideBarPanel sideBarPanel;

    /**
     * Override if interested in configuring dialog
     */
    protected void onConfigureDialog() {

    }

    /**
     * Override if interested in close notification
     */
    protected void onClose() {

    }

    public void init(SideBarPanel sideBarPanel) {
        this.sideBarPanel = sideBarPanel;
        onConfigureDialog();
    }

    protected SideBarPanel getSideBarPanel() {
        return sideBarPanel;
    }

    protected void registerSaveButton(Runnable callback) {
        sideBarPanel.getSaveButton().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
        sideBarPanel.getSaveButton().addHandler(event -> callback.run(), ClickEvent.getType());
    }

    protected void enableSaveButton(boolean enabled) {
        sideBarPanel.getSaveButton().setEnabled(enabled);
    }

    protected void registerDeleteButton(Runnable callback) {
        sideBarPanel.getDeleteButton().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
        sideBarPanel.getDeleteButton().addHandler(event -> callback.run(), ClickEvent.getType());
    }

    protected void enableDeleteButton(boolean enabled) {
        sideBarPanel.getDeleteButton().setEnabled(enabled);
    }

}
