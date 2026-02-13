package com.btxtech.client;

import com.btxtech.client.bridge.AngularProxyFactory;
import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.facade.JsGwtAngularFacade;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.SelectionService;
import com.btxtech.uiservice.cockpit.ChatCockpitService;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.QuestCockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.system.boot.GwtAngularBoot;
import com.btxtech.uiservice.terrain.InputService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TeaVMGwtAngularService {
    private final GameUiControl gameUiControl;
    private final MainCockpitService cockpitService;
    private final ChatCockpitService chatCockpitService;
    private final ItemCockpitService itemCockpitService;
    private final QuestCockpitService questCockpitService;
    private final BaseItemPlacerService baseItemPlacerService;
    private final TeaVMStatusProvider statusProvider;
    private final InputService inputService;
    private final SelectionService selectionService;
    private final TerrainTypeService terrainTypeService;
    private final ItemTypeService itemTypeService;
    private final BaseItemUiService baseItemUiService;
    private final ResourceUiService resourceUiService;
    private final ModalDialogManager modalDialogManager;
    private final InventoryTypeService inventoryTypeService;
    private final InventoryUiService inventoryUiService;
    private final InGameQuestVisualizationService inGameQuestVisualizationService;
    private final TerrainUiService terrainUiService;
    private JsGwtAngularFacade facade;

    @Inject
    public TeaVMGwtAngularService(ChatCockpitService chatCockpitService,
                                  InGameQuestVisualizationService inGameQuestVisualizationService,
                                  InventoryUiService inventoryUiService,
                                  InventoryTypeService inventoryTypeService,
                                  ModalDialogManager modalDialogManager,
                                  BaseItemUiService baseItemUiService,
                                  ResourceUiService resourceUiService,
                                  ItemTypeService itemTypeService,
                                  TerrainTypeService terrainTypeService,
                                  SelectionService selectionService,
                                  InputService inputService,
                                  TeaVMStatusProvider statusProvider,
                                  BaseItemPlacerService baseItemPlacerService,
                                  QuestCockpitService questCockpitService,
                                  ItemCockpitService itemCockpitService,
                                  MainCockpitService cockpitService,
                                  GameUiControl gameUiControl,
                                  TerrainUiService terrainUiService) {
        this.inGameQuestVisualizationService = inGameQuestVisualizationService;
        this.inventoryUiService = inventoryUiService;
        this.inventoryTypeService = inventoryTypeService;
        this.modalDialogManager = modalDialogManager;
        this.baseItemUiService = baseItemUiService;
        this.resourceUiService = resourceUiService;
        this.itemTypeService = itemTypeService;
        this.terrainTypeService = terrainTypeService;
        this.selectionService = selectionService;
        this.inputService = inputService;
        this.statusProvider = statusProvider;
        this.baseItemPlacerService = baseItemPlacerService;
        this.questCockpitService = questCockpitService;
        this.itemCockpitService = itemCockpitService;
        this.cockpitService = cockpitService;
        this.gameUiControl = gameUiControl;
        this.terrainUiService = terrainUiService;
        this.chatCockpitService = chatCockpitService;
    }

    public void init() {
        JsConsole.log("TeaVMGwtAngularService.init()");
        facade = JsGwtAngularFacade.get();

        // Java -> Angular: Set Java service proxies on the facade object
        facade.setJavaService("gameUiControl", AngularProxyFactory.createGameUiControlProxy(gameUiControl));
        facade.setJavaService("inputService", AngularProxyFactory.createInputServiceProxy(inputService));
        facade.setJavaService("selectionService", AngularProxyFactory.createSelectionServiceProxy(selectionService));
        facade.setJavaService("statusProvider", AngularProxyFactory.createStatusProviderProxy(statusProvider));
        facade.setJavaService("inGameQuestVisualizationService", AngularProxyFactory.createInGameQuestVisualizationServiceProxy(inGameQuestVisualizationService));
        facade.setJavaService("terrainTypeService", AngularProxyFactory.createTerrainTypeServiceProxy(terrainTypeService));
        facade.setJavaService("itemTypeService", AngularProxyFactory.createItemTypeServiceProxy(itemTypeService));
        facade.setJavaService("baseItemUiService", AngularProxyFactory.createBaseItemUiServiceProxy(baseItemUiService));
        facade.setJavaService("resourceUiService", AngularProxyFactory.createResourceUiServiceProxy(resourceUiService));
        facade.setJavaService("inventoryTypeService", AngularProxyFactory.createInventoryTypeServiceProxy(inventoryTypeService));
        facade.setJavaService("inventoryUiService", AngularProxyFactory.createInventoryUiServiceProxy(inventoryUiService));
        facade.setJavaService("terrainUiService", AngularProxyFactory.createTerrainUiServiceProxy(terrainUiService));

        // Angular -> Java: Initialize services with Angular-provided implementations
        cockpitService.init(facade.getMainCockpitAdapter());
        itemCockpitService.init(facade.getItemCockpitFrontendAdapter());
        questCockpitService.init(facade.getQuestCockpitAdapter());
        chatCockpitService.init(facade.getChatCockpitAdapter());
        selectionService.setActionServiceListener(facade.getActionServiceListenerAdapter());
        baseItemPlacerService.init(facade.getBaseItemPlacerPresenterAdapter());
        modalDialogManager.init(facade.getModelDialogPresenterAdapter());

        JsConsole.log("TeaVMGwtAngularService initialized");
    }

    public void onCrash() {
        facade.onCrash();
    }

    public GwtAngularBoot getGwtAngularBoot() {
        return facade.getGwtAngularBootAdapter();
    }
}
