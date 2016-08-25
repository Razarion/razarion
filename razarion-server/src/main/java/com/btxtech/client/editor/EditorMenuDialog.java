package com.btxtech.client.editor;

import com.btxtech.client.dialog.ModalDialogContent;
import com.btxtech.client.dialog.ModalDialogPanel;
import com.btxtech.client.editor.itemtype.BaseItemTypeCrudSidebar;
import com.btxtech.client.editor.shape3dgallery.Shape3DCrudeSidebar;
import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.editor.sidebar.LeftSideBarManager;
import com.btxtech.client.editor.slopeeditor.SlopeConfigCrudSidebar;
import com.btxtech.client.editor.terrainobject.TerrainObjectCrudSidebar;
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
    private LeftSideBarManager leftSideBarManager;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button renderEngineButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button visualConfigButton;
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
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button imageButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button shape3DButton;
    private ModalDialogPanel<Void> modalDialogPanel;

    @EventHandler("renderEngineButton")
    private void onRenderEngineButtonClicked(ClickEvent event) {
        openEditor(RenderEngineEditorPanel.class);
    }

    @EventHandler("visualConfigButton")
    private void onVisualConfigButtonClicked(ClickEvent event) {
        openEditor(VisualConfigPanel.class);
    }

    @EventHandler("terrainButton")
    private void onTerrainButtonClicked(ClickEvent event) {
    }

    @EventHandler("slopeButton")
    private void onSlopeButtonClicked(ClickEvent event) {
        openEditor(SlopeConfigCrudSidebar.class);
    }

    @EventHandler("groundButton")
    private void onGroundButtonClicked(ClickEvent event) {
        openEditor(GroundSidebar.class);
    }

    @EventHandler("itemButton")
    private void onItemButtonClicked(ClickEvent event) {
        openEditor(BaseItemTypeCrudSidebar.class);
    }

    @EventHandler("terrainObjectButton")
    private void onTerrainObjectButtonClicked(ClickEvent event) {
        openEditor(TerrainObjectCrudSidebar.class);
    }

    @EventHandler("imageButton")
    private void onImageButtonClicked(ClickEvent event) {
        // TODO
    }

    @EventHandler("shape3DButton")
    private void onShape3DButtonClicked(ClickEvent event) {
        openEditor(Shape3DCrudeSidebar.class);
    }

    @Override
    public void onClose() {

    }

    @Override
    public void init(Void aVoid) {

    }

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    private void openEditor(Class<? extends LeftSideBarContent> editorPanelClass) {
        modalDialogPanel.close();
        leftSideBarManager.show(editorPanelClass);
    }
}
