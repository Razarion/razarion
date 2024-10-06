package com.btxtech.client.gwtangular;

import com.btxtech.client.editor.EditorFrontendProvider;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.ActionServiceListener;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.cockpit.MainCockpit;
import com.btxtech.uiservice.cockpit.QuestCockpit;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.cockpit.item.ItemCockpitFrontend;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.ModelDialogPresenter;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerPresenter;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.BabylonRenderServiceAccess;
import com.btxtech.uiservice.renderer.ThreeJsModelPackService;
import com.btxtech.uiservice.system.boot.GwtAngularBoot;
import com.btxtech.uiservice.terrain.InputService;
import jsinterop.annotations.JsType;

/**
 * Instantiated by Angular
 */
@JsType(isNative = true)
public abstract class GwtAngularFacade {
    public abstract void onCrash();
    // Initialized by Angular called by GWT (GameEngine & UI-Engine event)
    public GwtAngularBoot gwtAngularBoot;  // Initialized by Angular
    public ScreenCover screenCover; // Initialized by Angular
    public ActionServiceListener actionServiceListener; // Initialized by Angular
    public MainCockpit mainCockpit; // Initialized by Angular
    public ItemCockpitFrontend itemCockpitFrontend; // Initialized by Angular
    public QuestCockpit questCockpit; // Initialized by Angular
    public BaseItemPlacerPresenter baseItemPlacerPresenter; // Initialized by Angular
    public BabylonRenderServiceAccess babylonRenderServiceAccess; // Initialized by Angular
    public ModelDialogPresenter modelDialogPresenter; // Initialized by Angular
    // Initialized by GWT called by Angular (user input)
    public GameUiControl gameUiControl; // Initialized by GWT
    public InGameQuestVisualizationService inGameQuestVisualizationService;  // Initialized by GWT
    public EditorFrontendProvider editorFrontendProvider;  // Initialized by GWT
    public StatusProvider statusProvider; // Initialized by GWT
    public InputService inputService; // Initialized by GWT
    public SelectionHandler selectionHandler; // Initialized by GWT
    public TerrainTypeService terrainTypeService; // Initialized by GWT
    public ItemTypeService itemTypeService; // Initialized by GWT
    public ThreeJsModelPackService threeJsModelPackService; // Initialized by GWT
    public BaseItemUiService baseItemUiService ; // Initialized by GWT
    public InventoryTypeService inventoryTypeService; // Initialized by GWT
    public InventoryUiService inventoryUiService; // Initialized by GWT
}
