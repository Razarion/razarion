package com.btxtech.client.gwtangular;

import com.btxtech.client.editor.EditorFrontendProvider;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.QuestCockpitService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.inventory.InventoryUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.BabylonRenderServiceAccess;
import com.btxtech.uiservice.renderer.ThreeJsModelPackService;
import com.btxtech.uiservice.system.boot.GwtAngularBoot;
import com.btxtech.uiservice.terrain.InputService;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMapOfAny;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class GwtAngularService {
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private EditorFrontendProvider editorFrontendProvider;
    @Inject
    private MainCockpitService cockpitService;
    @Inject
    private ItemCockpitService itemCockpitService;
    @Inject
    private QuestCockpitService questCockpitService;
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private StatusProvider statusProvider;
    @Inject
    private InputService inputService;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private ThreeJsModelPackService threeJsModelPackService;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private ModalDialogManager modalDialogManager;
    @Inject
    private InventoryTypeService inventoryTypeService;
    @Inject
    private InventoryUiService inventoryUiService;
    @Inject
    private InGameQuestVisualizationService inGameQuestVisualizationService;
    private GwtAngularFacade gwtAngularFacade;

    public void init() {
        gwtAngularFacade = Js.uncheckedCast(Js.<JsPropertyMapOfAny>uncheckedCast(DomGlobal.window).get("gwtAngularFacade"));
        gwtAngularFacade.gameUiControl = gameUiControl;
        gwtAngularFacade.language = I18nHelper.getLanguage();
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

    @Produces
    public BabylonRenderServiceAccess babylonRenderServiceAccess() {
        return gwtAngularFacade.babylonRenderServiceAccess;
    }

    @Produces
    public ScreenCover screenCover() {
        return gwtAngularFacade.screenCover;
    }

}
