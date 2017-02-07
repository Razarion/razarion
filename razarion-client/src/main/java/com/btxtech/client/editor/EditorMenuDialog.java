package com.btxtech.client.editor;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.client.editor.audio.AudioGalleryDialog;
import com.btxtech.client.editor.clip.ClipCrudeSidebar;
import com.btxtech.client.editor.ground.GroundSidebar;
import com.btxtech.client.editor.helper.HelperSideBar;
import com.btxtech.client.editor.imagegallery.ImageGalleryDialog;
import com.btxtech.client.editor.itemtype.BaseItemTypeCrudSidebar;
import com.btxtech.client.editor.itemtype.BoxItemTypeCrudSidebar;
import com.btxtech.client.editor.itemtype.ResourceItemTypeCrudSidebar;
import com.btxtech.client.editor.particle.ParticleCrudeSidebar;
import com.btxtech.client.editor.perfmon.PerfmonDialog;
import com.btxtech.client.editor.shape3dgallery.Shape3DCrudeSidebar;
import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.editor.sidebar.LeftSideBarManager;
import com.btxtech.client.editor.slopeeditor.SlopeConfigCrudSidebar;
import com.btxtech.client.editor.terrain.TerrainEditorSidebar;
import com.btxtech.client.editor.terrainobject.TerrainObjectCrudSidebar;
import com.btxtech.uiservice.dialog.DialogButton;
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
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    @DataField
    private Button perfmonButton;
    @Inject
    @DataField
    private Button renderEngineButton;
    @Inject
    @DataField
    private Button gameEngineButton;
    @Inject
    @DataField
    private Button visualConfigButton;
    @Inject
    @DataField
    private Button terrainButton;
    @Inject
    @DataField
    private Button helperButton;
    @Inject
    @DataField
    private Button slopeButton;
    @Inject
    @DataField
    private Button groundButton;
    @Inject
    @DataField
    private Button baseItemButton;
    @Inject
    @DataField
    private Button resourceItemButton;
    @Inject
    @DataField
    private Button boxItemButton;
    @Inject
    @DataField
    private Button terrainObjectButton;
    @Inject
    @DataField
    private Button imageButton;
    @Inject
    @DataField
    private Button audioButton;
    @Inject
    @DataField
    private Button shape3DButton;
    @Inject
    @DataField
    private Button clipButton;
    @Inject
    @DataField
    private Button particleButton;
    private ModalDialogPanel<Void> modalDialogPanel;

    @EventHandler("perfmonButton")
    private void onPerfmonButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        modalDialogManager.show("Perfmon", ClientModalDialogManagerImpl.Type.STACK_ABLE, PerfmonDialog.class, null, null, null, DialogButton.Button.CLOSE);
    }

    @EventHandler("renderEngineButton")
    private void onRenderEngineButtonClicked(ClickEvent event) {
        openEditor(RenderEngineEditorPanel.class);
    }

    @EventHandler("gameEngineButton")
    private void onGameEngineButtonClicked(ClickEvent event) {
        openEditor(GameEngineEditorPanel.class);
    }

    @EventHandler("visualConfigButton")
    private void onVisualConfigButtonClicked(ClickEvent event) {
        openEditor(VisualConfigPanel.class);
    }

    @EventHandler("terrainButton")
    private void onTerrainButtonClicked(ClickEvent event) {
        openEditor(TerrainEditorSidebar.class);
    }

    @EventHandler("helperButton")
    private void helperButtonClicked(ClickEvent event) {
        openEditor(HelperSideBar.class);
    }

    @EventHandler("slopeButton")
    private void onSlopeButtonClicked(ClickEvent event) {
        openEditor(SlopeConfigCrudSidebar.class);
    }

    @EventHandler("groundButton")
    private void onGroundButtonClicked(ClickEvent event) {
        openEditor(GroundSidebar.class);
    }

    @EventHandler("baseItemButton")
    private void onBaseItemButtonClicked(ClickEvent event) {
        openEditor(BaseItemTypeCrudSidebar.class);
    }

    @EventHandler("resourceItemButton")
    private void onResourceItemButtonClicked(ClickEvent event) {
        openEditor(ResourceItemTypeCrudSidebar.class);
    }

    @EventHandler("boxItemButton")
    private void onBoxItemButtonClicked(ClickEvent event) {
        openEditor(BoxItemTypeCrudSidebar.class);
    }

    @EventHandler("terrainObjectButton")
    private void onTerrainObjectButtonClicked(ClickEvent event) {
        openEditor(TerrainObjectCrudSidebar.class);
    }

    @EventHandler("imageButton")
    private void onImageButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        modalDialogManager.show("Image Gallery", ClientModalDialogManagerImpl.Type.STACK_ABLE, ImageGalleryDialog.class, null, null, null, DialogButton.Button.CLOSE);
    }

    @EventHandler("audioButton")
    private void onAudioButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        modalDialogManager.show("Audio Gallery", ClientModalDialogManagerImpl.Type.STACK_ABLE, AudioGalleryDialog.class, null, null, null, DialogButton.Button.CLOSE);
    }

    @EventHandler("shape3DButton")
    private void onShape3DButtonClicked(ClickEvent event) {
        openEditor(Shape3DCrudeSidebar.class);
    }

    @EventHandler("clipButton")
    private void clipButtonClicked(ClickEvent event) {
        openEditor(ClipCrudeSidebar.class);
    }

    @EventHandler("particleButton")
    private void particleButtonClicked(ClickEvent event) {
        openEditor(ParticleCrudeSidebar.class);
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
