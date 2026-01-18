package com.btxtech.client.gwtangular;

import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.ActionServiceListener;
import com.btxtech.uiservice.SelectionService;
import com.btxtech.uiservice.cockpit.ChatCockpit;
import com.btxtech.uiservice.cockpit.MainCockpit;
import com.btxtech.uiservice.cockpit.QuestCockpit;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.cockpit.item.ItemCockpitFrontend;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.ModelDialogPresenter;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerPresenter;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.BabylonRenderServiceAccess;
import com.btxtech.uiservice.system.boot.GwtAngularBoot;
import com.btxtech.uiservice.terrain.InputService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import jsinterop.annotations.JsType;

/**
 * Instantiated by Angular
 */
@JsType(isNative = true)
public abstract class GwtAngularFacade {
    // Initialized by Angular called by GWT (GameEngine & UI-Engine event)
    public GwtAngularBoot gwtAngularBoot;  // Initialized by Angular
    public ScreenCover screenCover; // Initialized by Angular
    public ActionServiceListener actionServiceListener; // Initialized by Angular
    public MainCockpit mainCockpit; // Initialized by Angular
    public ItemCockpitFrontend itemCockpitFrontend; // Initialized by Angular
    public QuestCockpit questCockpit; // Initialized by Angular
    public ChatCockpit chatCockpit; // Initialized by Angular
    public BaseItemPlacerPresenter baseItemPlacerPresenter; // Initialized by Angular
    public BabylonRenderServiceAccess babylonRenderServiceAccess; // Initialized by Angular
    public ModelDialogPresenter modelDialogPresenter; // Initialized by Angular
    // Initialized by GWT called by Angular (user input)
    public GameUiControl gameUiControl; // Initialized by GWT
    public InGameQuestVisualizationService inGameQuestVisualizationService;  // Initialized by GWT
    public StatusProvider statusProvider; // Initialized by GWT
    public InputService inputService; // Initialized by GWT
    public SelectionService selectionService; // Initialized by GWT
    public TerrainTypeService terrainTypeService; // Initialized by GWT
    public ItemTypeService itemTypeService; // Initialized by GWT
    public BaseItemUiService baseItemUiService; // Initialized by GWT
    public ResourceUiService resourceUiService; // Initialized by GWT
    public InventoryTypeService inventoryTypeService; // Initialized by GWT
    public InventoryUiService inventoryUiService; // Initialized by GWT
    public TerrainUiService terrainUiService; // Initialized by GWT

    public abstract void onCrash();
}
