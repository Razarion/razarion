package com.btxtech.client.editor.sidebar;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;

/**
 * Created by Beat
 * 05.05.2016.
 */
public abstract class AbstractEditor extends Composite {
    private EditorPanel editorPanel;

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

    public void init(EditorPanel editorPanel) {
        this.editorPanel = editorPanel;
        onConfigureDialog();
    }

    protected EditorPanel getEditorPanel() {
        return editorPanel;
    }

    protected void registerSaveButton(Runnable callback) {
        editorPanel.getSaveButton().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
        editorPanel.getSaveButton().addClickHandler(event -> callback.run());
    }

    protected void enableSaveButton(boolean enabled) {
        editorPanel.getSaveButton().setEnabled(enabled);
    }

    protected void registerDeleteButton(Runnable callback) {
        editorPanel.getDeleteButton().getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
        editorPanel.getDeleteButton().addClickHandler(event -> callback.run());
    }

    protected void enableDeleteButton(boolean enabled) {
        editorPanel.getDeleteButton().setEnabled(enabled);
    }

}
