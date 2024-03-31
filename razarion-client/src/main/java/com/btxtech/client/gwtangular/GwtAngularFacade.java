package com.btxtech.client.gwtangular;

import com.btxtech.client.editor.EditorFrontendProvider;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
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
    public GwtAngularBoot gwtAngularBoot;  // Initialized by Angular
    public GameUiControl gameUiControl; // Initialized by GWT
    public String language; // Initialized by GWT
    public ScreenCover screenCover; // Initialized by GWT
    public AngularCursorService angularCursorService; // Initialized by Angular
    public MainCockpit mainCockpit; // Initialized by Angular
    public ItemCockpitFrontend itemCockpitFrontend; // Initialized by Angular
    public QuestCockpit questCockpit; // Initialized by Angular
    public InGameQuestVisualizationService inGameQuestVisualizationService;  // Initialized by GWT
    public BaseItemPlacerPresenter baseItemPlacerPresenter; // Initialized by Angular
    public EditorFrontendProvider editorFrontendProvider;  // Initialized by GWT
    public StatusProvider statusProvider; // Initialized by GWT
    public BabylonRenderServiceAccess babylonRenderServiceAccess; // Initialized by Angular
    public InputService inputService; // Initialized by GWT
    public SelectionHandler selectionHandler; // Initialized by GWT
    public TerrainTypeService terrainTypeService; // Initialized by GWT
    public ItemTypeService itemTypeService; // Initialized by GWT
    public ThreeJsModelPackService threeJsModelPackService; // Initialized by GWT
    public BaseItemUiService baseItemUiService ; // Initialized by GWT
    public ModelDialogPresenter modelDialogPresenter; // Initialized by Angular
    public InventoryTypeService inventoryTypeService; // Initialized by GWT
    public InventoryUiService inventoryUiService; // Initialized by GWT
    public abstract void onCrash();
}
