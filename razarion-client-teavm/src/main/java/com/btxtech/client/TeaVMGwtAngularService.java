package com.btxtech.client;

import com.btxtech.client.bridge.AngularProxyFactory;
import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.facade.JsGameCommandService;
import com.btxtech.client.jso.facade.JsGwtAngularFacade;
import com.btxtech.client.jso.facade.JsItemCockpitBridge;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.cockpit.ChatCockpitService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.QuestCockpitService;
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
import com.btxtech.uiservice.user.UserUiService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class TeaVMGwtAngularService {
    private final GameUiControl gameUiControl;
    private final MainCockpitService cockpitService;
    private final ChatCockpitService chatCockpitService;
    private final QuestCockpitService questCockpitService;
    private final BaseItemPlacerService baseItemPlacerService;
    private final TeaVMStatusProvider statusProvider;
    private final InputService inputService;
    private final TerrainTypeService terrainTypeService;
    private final ItemTypeService itemTypeService;
    private final BaseItemUiService baseItemUiService;
    private final ResourceUiService resourceUiService;
    private final ModalDialogManager modalDialogManager;
    private final InventoryTypeService inventoryTypeService;
    private final InventoryUiService inventoryUiService;
    private final InGameQuestVisualizationService inGameQuestVisualizationService;
    private final TerrainUiService terrainUiService;
    private final Provider<GameEngineControl> gameEngineControl;
    private final UserUiService userUiService;
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
                                  InputService inputService,
                                  TeaVMStatusProvider statusProvider,
                                  BaseItemPlacerService baseItemPlacerService,
                                  QuestCockpitService questCockpitService,
                                  MainCockpitService cockpitService,
                                  GameUiControl gameUiControl,
                                  TerrainUiService terrainUiService,
                                  UserUiService userUiService,
                                  Provider<GameEngineControl> gameEngineControl) {
        this.inGameQuestVisualizationService = inGameQuestVisualizationService;
        this.inventoryUiService = inventoryUiService;
        this.inventoryTypeService = inventoryTypeService;
        this.modalDialogManager = modalDialogManager;
        this.baseItemUiService = baseItemUiService;
        this.resourceUiService = resourceUiService;
        this.itemTypeService = itemTypeService;
        this.terrainTypeService = terrainTypeService;
        this.inputService = inputService;
        this.statusProvider = statusProvider;
        this.baseItemPlacerService = baseItemPlacerService;
        this.questCockpitService = questCockpitService;
        this.cockpitService = cockpitService;
        this.gameUiControl = gameUiControl;
        this.terrainUiService = terrainUiService;
        this.chatCockpitService = chatCockpitService;
        this.userUiService = userUiService;
        this.gameEngineControl = gameEngineControl;
    }

    public void init() {
        JsConsole.log("TeaVMGwtAngularService.init()");
        facade = JsGwtAngularFacade.get();

        // Java -> Angular: Set Java service proxies on the facade object
        facade.setJavaService("gameUiControl", AngularProxyFactory.createGameUiControlProxy(gameUiControl));
        facade.setJavaService("inputService", AngularProxyFactory.createInputServiceProxy(inputService));
        facade.setJavaService("statusProvider", AngularProxyFactory.createStatusProviderProxy(statusProvider));
        facade.setJavaService("inGameQuestVisualizationService", AngularProxyFactory.createInGameQuestVisualizationServiceProxy(inGameQuestVisualizationService));
        facade.setJavaService("terrainTypeService", AngularProxyFactory.createTerrainTypeServiceProxy(terrainTypeService));
        facade.setJavaService("itemTypeService", AngularProxyFactory.createItemTypeServiceProxy(itemTypeService));
        facade.setJavaService("baseItemUiService", AngularProxyFactory.createBaseItemUiServiceProxy(baseItemUiService));
        facade.setJavaService("resourceUiService", AngularProxyFactory.createResourceUiServiceProxy(resourceUiService));
        facade.setJavaService("inventoryTypeService", AngularProxyFactory.createInventoryTypeServiceProxy(inventoryTypeService));
        facade.setJavaService("inventoryUiService", AngularProxyFactory.createInventoryUiServiceProxy(inventoryUiService));
        facade.setJavaService("terrainUiService", AngularProxyFactory.createTerrainUiServiceProxy(terrainUiService));
        facade.setJavaService("gameCommandService", JsGameCommandService.createProxy(gameEngineControl.get(), inputService));
        facade.setJavaService("itemCockpitBridge", JsItemCockpitBridge.createProxy(gameEngineControl.get(), baseItemPlacerService, itemTypeService, baseItemUiService));

        // Register cockpit state changed callback
        baseItemUiService.setCockpitStateChangedCallback(JsItemCockpitBridge::notifyCockpitStateChanged);
        userUiService.setCockpitStateChangedCallback(JsItemCockpitBridge::notifyCockpitStateChanged);

        // Angular -> Java: Initialize services with Angular-provided implementations
        cockpitService.init(facade.getMainCockpitAdapter());
        questCockpitService.init(facade.getQuestCockpitAdapter());
        chatCockpitService.init(facade.getChatCockpitAdapter());
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
