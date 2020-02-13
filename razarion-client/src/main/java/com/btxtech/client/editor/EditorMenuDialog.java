package com.btxtech.client.editor;

import com.btxtech.client.MainPanelService;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.client.editor.audio.AudioGalleryDialog;
import com.btxtech.client.editor.basemgmt.BaseMgmtEditorPanel;
import com.btxtech.client.editor.client.scene.SceneConfigSidebar;
import com.btxtech.client.editor.i18n.I18nPanel;
import com.btxtech.client.editor.imagegallery.ImageGalleryDialog;
import com.btxtech.client.editor.inventory.InventoryItemCrudSidebar;
import com.btxtech.client.editor.itemtype.BaseItemTypeCrudSidebar;
import com.btxtech.client.editor.itemtype.BoxItemTypeCrudSidebar;
import com.btxtech.client.editor.itemtype.ResourceItemTypeCrudSidebar;
import com.btxtech.client.editor.level.LevelConfigSidebar;
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
import com.btxtech.client.editor.sidebar.AbstractEditor;
import com.btxtech.client.editor.sidebar.EditorPanel;
import com.btxtech.client.editor.slopeeditor.SlopeConfigCrudSidebar;
import com.btxtech.client.editor.terrain.TerrainEditorSidebar;
import com.btxtech.client.editor.terrainobject.TerrainObjectCrudSidebar;
import com.btxtech.client.editor.water.WaterSidebar;
import com.btxtech.shared.rest.CrudController;
import com.btxtech.shared.rest.GroundEditorController;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.dialog.DialogButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 13.08.2016.
 */
@Templated("EditorMenuDialog.html#editor-menu-dialog")
public class EditorMenuDialog extends Composite implements ModalDialogContent<Void> {
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private MainPanelService mainPanelService;
    @Inject
    private Instance<EditorPanel> editorPanelInstance;
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
    private Button planetButton;
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
    private Button slopeButton;
    @Inject
    @DataField
    private Button groundButton;
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
    private Button levelConfigButton;
    @Inject
    @DataField
    private Button inventoryItemButton;
    @Inject
    @DataField
    private Button i18nPanelButton;
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

    @EventHandler("baseMgmtButton")
    private void onBaseMgmtButtonClicked(ClickEvent event) {
        openEditor(BaseMgmtEditorPanel.class);
    }

    @EventHandler("planetButton")
    private void onPlanetButtonClicked(ClickEvent event) {
        openEditor(PlanetConfigPanel.class);
    }

    @EventHandler("planetVisualConfigButton")
    private void onPanetVisualConfigButtonClicked(ClickEvent event) {
        openEditor(PlanetVisualConfigPanel.class);
    }

    @EventHandler("terrainButton")
    private void onTerrainButtonClicked(ClickEvent event) {
        openEditor(TerrainEditorSidebar.class);
    }

    @EventHandler("resourceRegionButton")
    private void onResourceRegionsButtonClicked(ClickEvent event) {
        openEditor(ResourceRegionSidebar.class);
    }

    @EventHandler("boxRegionButton")
    private void onBoxRegionsButtonClicked(ClickEvent event) {
        openEditor(BoxRegionSidebar.class);
    }

    @EventHandler("startRegionsButton")
    private void startRegionsButtonClicked(ClickEvent event) {
        openEditor(StartRegionSidebar.class);
    }

    @EventHandler("levelQuestButton")
    private void levelQuestButtonClicked(ClickEvent event) {
        openEditor(LevelQuestSidebar.class);
    }

    @EventHandler("inventoryItemButton")
    private void inventoryItemButtonClicked(ClickEvent event) {
        openEditor(InventoryItemCrudSidebar.class);
    }

    @EventHandler("botButton")
    private void botButtonClicked(ClickEvent event) {
        openEditor(BotSidebar.class);
    }

    @EventHandler("botSceneButton")
    private void botSceneButtonClicked(ClickEvent event) {
        openEditor(BotSceneSidebar.class);
    }

    @EventHandler("sceneConfigButton")
    private void sceneConfigButtonClicked(ClickEvent event) {
        openEditor(SceneConfigSidebar.class);
    }

    @EventHandler("slopeButton")
    private void onSlopeButtonClicked(ClickEvent event) {
        openEditor(SlopeConfigCrudSidebar.class);
    }

    @EventHandler("groundButton")
    private void onGroundButtonClicked(ClickEvent event) {
        openGenericCrudEditor(GroundEditorController.class);
    }

    @EventHandler("waterButton")
    private void onWaterButtonClicked(ClickEvent event) {
        openEditor(WaterSidebar.class);
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

    @EventHandler("particleButton")
    private void particleButtonClicked(ClickEvent event) {
        openEditor(ParticleCrudeSidebar.class);
    }

    @EventHandler("levelConfigButton")
    private void levelConfigButtonClicked(ClickEvent event) {
        openEditor(LevelConfigSidebar.class);
    }

    @EventHandler("i18nPanelButton")
    private void i18nPanelButtonClicked(ClickEvent event) {
        openEditor(I18nPanel.class);
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

    private void openEditor(Class<? extends AbstractEditor> editorPanelClass) {
        try {
            modalDialogPanel.close();
            EditorPanel editorPanel = editorPanelInstance.get();
            editorPanel.setContent(editorPanelClass);
            mainPanelService.addEditorPanel(editorPanel);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            modalDialogManager.showMessageDialog("Error open Editor", t.getMessage());
        }
    }

    private void openGenericCrudEditor(Class<? extends CrudController> crudControllerClass) {
        try {
            modalDialogPanel.close();
            EditorPanel editorPanel = editorPanelInstance.get();
            editorPanel.setGenericCrud(crudControllerClass);
            mainPanelService.addEditorPanel(editorPanel);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            modalDialogManager.showMessageDialog("Error open Editor", t.getMessage());
        }
    }
}
