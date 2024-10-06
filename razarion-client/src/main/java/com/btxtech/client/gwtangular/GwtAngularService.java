package com.btxtech.client.gwtangular;

import com.btxtech.client.editor.EditorFrontendProvider;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.QuestCockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.ThreeJsModelPackService;
import com.btxtech.uiservice.system.boot.GwtAngularBoot;
import com.btxtech.uiservice.terrain.InputService;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GwtAngularService {

    private GameUiControl gameUiControl;

    private EditorFrontendProvider editorFrontendProvider;

    private MainCockpitService cockpitService;

    private ItemCockpitService itemCockpitService;

    private QuestCockpitService questCockpitService;

    private BaseItemPlacerService baseItemPlacerService;

    private StatusProvider statusProvider;

    private InputService inputService;

    private SelectionHandler selectionHandler;

    private TerrainTypeService terrainTypeService;

    private ItemTypeService itemTypeService;

    private ThreeJsModelPackService threeJsModelPackService;

    private BaseItemUiService baseItemUiService;

    private ModalDialogManager modalDialogManager;

    private InventoryTypeService inventoryTypeService;

    private InventoryUiService inventoryUiService;

    private InGameQuestVisualizationService inGameQuestVisualizationService;
    private GwtAngularFacade gwtAngularFacade;

    public static GwtAngularFacade getGwtAngularFacade() {
        return Js.uncheckedCast(Js.<JsPropertyMap<Object>>uncheckedCast(DomGlobal.window).get("gwtAngularFacade"));
    }

    @Inject
    public GwtAngularService(InGameQuestVisualizationService inGameQuestVisualizationService, InventoryUiService inventoryUiService, InventoryTypeService inventoryTypeService, ModalDialogManager modalDialogManager, BaseItemUiService baseItemUiService, ThreeJsModelPackService threeJsModelPackService, ItemTypeService itemTypeService, TerrainTypeService terrainTypeService, SelectionHandler selectionHandler, InputService inputService, StatusProvider statusProvider, BaseItemPlacerService baseItemPlacerService, QuestCockpitService questCockpitService, ItemCockpitService itemCockpitService, MainCockpitService cockpitService, EditorFrontendProvider editorFrontendProvider, GameUiControl gameUiControl) {
        this.inGameQuestVisualizationService = inGameQuestVisualizationService;
        this.inventoryUiService = inventoryUiService;
        this.inventoryTypeService = inventoryTypeService;
        this.modalDialogManager = modalDialogManager;
        this.baseItemUiService = baseItemUiService;
        this.threeJsModelPackService = threeJsModelPackService;
        this.itemTypeService = itemTypeService;
        this.terrainTypeService = terrainTypeService;
        this.selectionHandler = selectionHandler;
        this.inputService = inputService;
        this.statusProvider = statusProvider;
        this.baseItemPlacerService = baseItemPlacerService;
        this.questCockpitService = questCockpitService;
        this.itemCockpitService = itemCockpitService;
        this.cockpitService = cockpitService;
        this.editorFrontendProvider = editorFrontendProvider;
        this.gameUiControl = gameUiControl;
    }

    public void init() {
        gwtAngularFacade = getGwtAngularFacade();
        gwtAngularFacade.gameUiControl = gameUiControl;
        gwtAngularFacade.editorFrontendProvider = editorFrontendProvider;
        gwtAngularFacade.inGameQuestVisualizationService = inGameQuestVisualizationService;
        gwtAngularFacade.statusProvider = statusProvider;
        gwtAngularFacade.inputService = inputService;
        gwtAngularFacade.selectionHandler = selectionHandler;
        gwtAngularFacade.terrainTypeService = terrainTypeService;
        gwtAngularFacade.itemTypeService = itemTypeService;
        gwtAngularFacade.threeJsModelPackService = threeJsModelPackService;
        gwtAngularFacade.baseItemUiService = baseItemUiService;
        gwtAngularFacade.inventoryTypeService = inventoryTypeService;
        gwtAngularFacade.inventoryUiService = inventoryUiService;
        cockpitService.init(gwtAngularFacade.mainCockpit);
        itemCockpitService.init(gwtAngularFacade.itemCockpitFrontend);
        questCockpitService.init(gwtAngularFacade.questCockpit);
        selectionHandler.setActionServiceListener(gwtAngularFacade.actionServiceListener);
        baseItemPlacerService.init(gwtAngularFacade.baseItemPlacerPresenter);
        modalDialogManager.init(gwtAngularFacade.modelDialogPresenter);
    }

    public void onCrash() {
        gwtAngularFacade.onCrash();
    }

    public GwtAngularBoot getGwtAngularBoot() {
        return gwtAngularFacade.gwtAngularBoot;
    }
}
