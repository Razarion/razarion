package com.btxtech.client.gwtangular;

import com.btxtech.client.editor.EditorFrontendProvider;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.SelectionService;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.QuestCockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.ModalDialogManager;
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
    private final GameUiControl gameUiControl;
    private final EditorFrontendProvider editorFrontendProvider;
    private final MainCockpitService cockpitService;
    private final ItemCockpitService itemCockpitService;
    private final QuestCockpitService questCockpitService;
    private final BaseItemPlacerService baseItemPlacerService;
    private final StatusProvider statusProvider;
    private final InputService inputService;
    private final SelectionService selectionService;
    private final TerrainTypeService terrainTypeService;
    private final ItemTypeService itemTypeService;
    private final ThreeJsModelPackService threeJsModelPackService;
    private final BaseItemUiService baseItemUiService;
    private final ModalDialogManager modalDialogManager;
    private final InventoryTypeService inventoryTypeService;
    private final InventoryUiService inventoryUiService;
    private final InGameQuestVisualizationService inGameQuestVisualizationService;
    private GwtAngularFacade gwtAngularFacade;

    @Inject
    public GwtAngularService(InGameQuestVisualizationService inGameQuestVisualizationService,
                             InventoryUiService inventoryUiService,
                             InventoryTypeService inventoryTypeService,
                             ModalDialogManager modalDialogManager,
                             BaseItemUiService baseItemUiService,
                             ThreeJsModelPackService threeJsModelPackService,
                             ItemTypeService itemTypeService,
                             TerrainTypeService terrainTypeService,
                             SelectionService selectionService,
                             InputService inputService,
                             StatusProvider statusProvider,
                             BaseItemPlacerService baseItemPlacerService,
                             QuestCockpitService questCockpitService,
                             ItemCockpitService itemCockpitService,
                             MainCockpitService cockpitService,
                             EditorFrontendProvider editorFrontendProvider,
                             GameUiControl gameUiControl) {
        this.inGameQuestVisualizationService = inGameQuestVisualizationService;
        this.inventoryUiService = inventoryUiService;
        this.inventoryTypeService = inventoryTypeService;
        this.modalDialogManager = modalDialogManager;
        this.baseItemUiService = baseItemUiService;
        this.threeJsModelPackService = threeJsModelPackService;
        this.itemTypeService = itemTypeService;
        this.terrainTypeService = terrainTypeService;
        this.selectionService = selectionService;
        this.inputService = inputService;
        this.statusProvider = statusProvider;
        this.baseItemPlacerService = baseItemPlacerService;
        this.questCockpitService = questCockpitService;
        this.itemCockpitService = itemCockpitService;
        this.cockpitService = cockpitService;
        this.editorFrontendProvider = editorFrontendProvider;
        this.gameUiControl = gameUiControl;
    }

    public static GwtAngularFacade getGwtAngularFacade() {
        return Js.uncheckedCast(Js.<JsPropertyMap<Object>>uncheckedCast(DomGlobal.window).get("gwtAngularFacade"));
    }

    public void init() {
        gwtAngularFacade = getGwtAngularFacade();
        gwtAngularFacade.gameUiControl = gameUiControl;
        gwtAngularFacade.editorFrontendProvider = editorFrontendProvider;
        gwtAngularFacade.inGameQuestVisualizationService = inGameQuestVisualizationService;
        gwtAngularFacade.statusProvider = statusProvider;
        gwtAngularFacade.inputService = inputService;
        gwtAngularFacade.selectionService = selectionService;
        gwtAngularFacade.terrainTypeService = terrainTypeService;
        gwtAngularFacade.itemTypeService = itemTypeService;
        gwtAngularFacade.threeJsModelPackService = threeJsModelPackService;
        gwtAngularFacade.baseItemUiService = baseItemUiService;
        gwtAngularFacade.inventoryTypeService = inventoryTypeService;
        gwtAngularFacade.inventoryUiService = inventoryUiService;
        cockpitService.init(gwtAngularFacade.mainCockpit);
        itemCockpitService.init(gwtAngularFacade.itemCockpitFrontend);
        questCockpitService.init(gwtAngularFacade.questCockpit);
        selectionService.setActionServiceListener(gwtAngularFacade.actionServiceListener);
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
