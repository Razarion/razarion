package com.btxtech.client.editor;

import com.btxtech.client.dialog.ModalDialogContent;
import com.btxtech.client.dialog.ModalDialogManager;
import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.editor.sidebar.LeftSideBarManager;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 13.08.2016.
 */
@Templated("EditorMenuDialog.html#editor-menu-dialog")
public class EditorMenuDialog extends Composite implements ModalDialogContent<Void> {
    @Inject
    private ModalDialogManager modalDialogManager;
    @Inject
    private LeftSideBarManager leftSideBarManager;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button renderEngineButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button planetButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button terrainButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button slopeButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button groundButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button itemButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button terrainObjectButton;

    @EventHandler("renderEngineButton")
    private void onRenderEngineButtonClicked(ClickEvent event) {
        openEditor(RenderEngineEditorPanel.class);
    }

    @EventHandler("planetButton")
    private void onPlanetButtonClicked(ClickEvent event) {
        openEditor(PlanetEditorPanel.class);
    }

    @EventHandler("terrainButton")
    private void onTerrainButtonClicked(ClickEvent event) {
    }

    @EventHandler("slopeButton")
    private void onSlopeButtonClicked(ClickEvent event) {
    }

    @EventHandler("groundButton")
    private void onGroundButtonClicked(ClickEvent event) {
    }

    @EventHandler("itemButton")
    private void onItemButtonClicked(ClickEvent event) {
    }

    @EventHandler("terrainObjectButton")
    private void onTerrainObjectButtonClicked(ClickEvent event) {
    }

    @Override
    public void onClose() {

    }

    @Override
    public void init(Void aVoid) {

    }

    @Override
    public void customize(ModalDialogManager modalDialogManager) {

    }

    private void openEditor(Class<? extends LeftSideBarContent> editorPanelClass) {
        modalDialogManager.hide();
        leftSideBarManager.show(editorPanelClass);
    }
}
