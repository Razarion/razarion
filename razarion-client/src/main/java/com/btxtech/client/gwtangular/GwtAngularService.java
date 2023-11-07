package com.btxtech.client.gwtangular;

import com.btxtech.client.ClientCursorService;
import com.btxtech.client.editor.EditorFrontendProvider;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.QuestCockpitService;
import com.btxtech.uiservice.cockpit.ScreenCover;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
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
    private ClientCursorService clientCursorService;
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private StatusProvider statusProvider;
    @Inject
    private InputService inputService;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private ThreeJsModelPackService threeJsModelPackService;
    private GwtAngularFacade gwtAngularFacade;

    public void init() {
        gwtAngularFacade = Js.uncheckedCast(Js.<JsPropertyMapOfAny>uncheckedCast(DomGlobal.window).get("gwtAngularFacade"));
        gwtAngularFacade.gameUiControl = gameUiControl;
        gwtAngularFacade.language = I18nHelper.getLanguage();
        gwtAngularFacade.editorFrontendProvider = editorFrontendProvider;
        gwtAngularFacade.statusProvider = statusProvider;
        gwtAngularFacade.inputService = inputService;
        gwtAngularFacade.terrainTypeService = terrainTypeService;
        gwtAngularFacade.itemTypeService = itemTypeService;
        gwtAngularFacade.threeJsModelPackService = threeJsModelPackService;
        cockpitService.init(gwtAngularFacade.mainCockpit);
        itemCockpitService.init(gwtAngularFacade.itemCockpitFrontend);
        questCockpitService.init(gwtAngularFacade.questCockpit);
        clientCursorService.init(gwtAngularFacade.angularCursorService);
        baseItemPlacerService.init(gwtAngularFacade.baseItemPlacerPresenter);
    }

    public void onCrash() {
        gwtAngularFacade.onCrash();
    }

    public GwtAngularBoot getGwtAngularBoot() {
        return gwtAngularFacade.gwtAngularBoot;
    }

    @Produces
    public BabylonRenderServiceAccess threeJsRendererServiceAccess() {
        return gwtAngularFacade.threeJsRendererServiceAccess;
    }

    @Produces
    public ScreenCover screenCover() {
        return gwtAngularFacade.screenCover;
    }

}
