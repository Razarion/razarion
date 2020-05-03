package com.btxtech.client.editor;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.client.editor.EditorMenuButtonSection.CrudControllerButton;
import com.btxtech.client.editor.audio.AudioGalleryDialog;
import com.btxtech.client.editor.basemgmt.BaseMgmtEditorPanel;
import com.btxtech.client.editor.client.scene.SceneConfigSidebar;
import com.btxtech.client.editor.i18n.I18nPanel;
import com.btxtech.client.editor.imagegallery.ImageGalleryDialog;
import com.btxtech.client.editor.inventory.InventoryItemCrudSidebar;
import com.btxtech.client.editor.itemtype.BaseItemTypeCrudSidebar;
import com.btxtech.client.editor.itemtype.BoxItemTypeCrudSidebar;
import com.btxtech.client.editor.itemtype.ResourceItemTypeCrudSidebar;
import com.btxtech.client.editor.particle.ParticleCrudeSidebar;
import com.btxtech.client.editor.perfmon.PerfmonDialog;
import com.btxtech.client.editor.renderpanel.RenderEngineEditorPanel;
import com.btxtech.client.editor.server.bot.BotSidebar;
import com.btxtech.client.editor.server.botscene.BotSceneSidebar;
import com.btxtech.client.editor.server.box.BoxRegionSidebar;
import com.btxtech.client.editor.server.quest.LevelQuestSidebar;
import com.btxtech.client.editor.server.resource.ResourceRegionSidebar;
import com.btxtech.client.editor.server.startregion.StartRegionSidebar;
import com.btxtech.client.editor.shape3dgallery.Shape3DCrudeSidebar;
import com.btxtech.client.editor.terrain.TerrainEditor;
import com.btxtech.client.editor.terrainobject.TerrainObjectCrudSidebar;
import com.btxtech.client.editor.water.WaterSidebar;
import com.btxtech.shared.rest.GameUiContextEditorController;
import com.btxtech.shared.rest.GroundEditorController;
import com.btxtech.shared.rest.LevelEditorController;
import com.btxtech.shared.rest.PlanetEditorController;
import com.btxtech.shared.rest.SlopeEditorController;
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
    private EditorService editorService;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    @DataField
    private EditorMenuButtonSection globalConfig;
    @Inject
    @DataField
    private Button clientAlarmServiceButton;
    @Inject
    @DataField
    private Button serverAlarmServiceButton;
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
    private Button baseMgmtButton;
    @Inject
    @DataField
    private Button planetVisualConfigButton;
    @Inject
    @DataField
    private Button terrainButton;
    @Inject
    @DataField
    private Button resourceRegionButton;
    @Inject
    @DataField
    private Button boxRegionButton;
    @Inject
    @DataField
    private Button startRegionsButton;
    @Inject
    @DataField
    private Button levelQuestButton;
    @Inject
    @DataField
    private Button botButton;
    @Inject
    @DataField
    private Button botSceneButton;
    @Inject
    @DataField
    private Button sceneConfigButton;
    @Inject
    @DataField
    private Button waterButton;
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
    private Button particleButton;
    @Inject
    @DataField
    private Button inventoryItemButton;
    @Inject
    @DataField
    private Button i18nPanelButton;
    private ModalDialogPanel<Void> modalDialogPanel;

    @Override
    public void init(Void aVoid) {
        globalConfig.showSection(() -> modalDialogPanel.close(),
                new CrudControllerButton(LevelEditorController.class, "Levels"),
                new CrudControllerButton(PlanetEditorController.class, "Planets"),
                new CrudControllerButton(GroundEditorController.class, "Grounds"),
                new CrudControllerButton(SlopeEditorController.class, "Slope"),
                new CrudControllerButton(GameUiContextEditorController.class, "Game Ui Context")
        );
    }

    @EventHandler("clientAlarmServiceButton")
    private void onClientAlarmServiceButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openClientAlarmView();
    }

    @EventHandler("serverAlarmServiceButton")
    private void onServerAlarmServiceButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openServerAlarmView();
    }

    @EventHandler("perfmonButton")
    private void onPerfmonButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        modalDialogManager.show("Perfmon", ClientModalDialogManagerImpl.Type.STACK_ABLE, PerfmonDialog.class, null, null, null, DialogButton.Button.CLOSE);
    }

    @EventHandler("renderEngineButton")
    private void onRenderEngineButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(RenderEngineEditorPanel.class, "???Unknown");
    }

    @EventHandler("gameEngineButton")
    private void onGameEngineButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(GameEngineEditorPanel.class, "???Unknown");
    }

    @EventHandler("baseMgmtButton")
    private void onBaseMgmtButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(BaseMgmtEditorPanel.class, "???Unknown");
    }

    @EventHandler("planetVisualConfigButton")
    private void onPanetVisualConfigButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(PlanetVisualConfigPanel.class, "???Unknown");
    }

    @EventHandler("terrainButton")
    private void onTerrainButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(TerrainEditor.class, "???Unknown");
    }

    @EventHandler("resourceRegionButton")
    private void onResourceRegionsButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(ResourceRegionSidebar.class, "???Unknown");
    }

    @EventHandler("boxRegionButton")
    private void onBoxRegionsButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(BoxRegionSidebar.class, "???Unknown");
    }

    @EventHandler("startRegionsButton")
    private void startRegionsButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(StartRegionSidebar.class, "???Unknown");
    }

    @EventHandler("levelQuestButton")
    private void levelQuestButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(LevelQuestSidebar.class, "???Unknown");
    }

    @EventHandler("inventoryItemButton")
    private void inventoryItemButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(InventoryItemCrudSidebar.class, "???Unknown");
    }

    @EventHandler("botButton")
    private void botButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(BotSidebar.class, "???Unknown");
    }

    @EventHandler("botSceneButton")
    private void botSceneButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(BotSceneSidebar.class, "???Unknown");
    }

    @EventHandler("sceneConfigButton")
    private void sceneConfigButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(SceneConfigSidebar.class, "???Unknown");
    }

    @EventHandler("waterButton")
    private void onWaterButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(WaterSidebar.class, "???Unknown");
    }

    @EventHandler("baseItemButton")
    private void onBaseItemButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(BaseItemTypeCrudSidebar.class, "???Unknown");
    }

    @EventHandler("resourceItemButton")
    private void onResourceItemButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(ResourceItemTypeCrudSidebar.class, "???Unknown");
    }

    @EventHandler("boxItemButton")
    private void onBoxItemButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(BoxItemTypeCrudSidebar.class, "???Unknown");
    }

    @EventHandler("terrainObjectButton")
    private void onTerrainObjectButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(TerrainObjectCrudSidebar.class, "???Unknown");
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
        modalDialogPanel.close();
        editorService.openEditor(Shape3DCrudeSidebar.class, "???Unknown");
    }

    @EventHandler("particleButton")
    private void particleButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(ParticleCrudeSidebar.class, "???Unknown");
    }

    @EventHandler("i18nPanelButton")
    private void i18nPanelButtonClicked(ClickEvent event) {
        modalDialogPanel.close();
        editorService.openEditor(I18nPanel.class, "???Unknown");
    }

    @Override
    public void onClose() {

    }

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }
}
