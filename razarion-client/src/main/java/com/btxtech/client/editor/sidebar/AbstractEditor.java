package com.btxtech.client.editor.sidebar;

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
        editorPanel.getSaveButton().style.visibility = "visible";
        editorPanel.getSaveButton().addEventListener("click", event -> callback.run());
    }

    protected void enableSaveButton(boolean enabled) {
        editorPanel.getSaveButton().disabled = !enabled;
    }

    protected void registerDeleteButton(Runnable callback) {
        editorPanel.getDeleteButton().style.visibility = "visible";
        editorPanel.getDeleteButton().addEventListener("click", event -> callback.run());
    }

    protected void enableDeleteButton(boolean enabled) {
        editorPanel.getDeleteButton().disabled = !enabled;
    }

}
