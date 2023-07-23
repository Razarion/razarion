package com.btxtech.client.gwtangular;

import com.btxtech.client.editor.EditorFrontendProvider;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.AssetService;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.renderer.ThreeJsModelPackService;
import com.btxtech.uiservice.renderer.ThreeJsRendererServiceAccess;
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
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private StatusProvider statusProvider;
    @Inject
    private InputService inputService;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ThreeJsModelPackService threeJsModelPackService;
    @Inject
    private AssetService assetService;
    private GwtAngularFacade gwtAngularFacade;

    public void init() {
        gwtAngularFacade = Js.uncheckedCast(Js.<JsPropertyMapOfAny>uncheckedCast(DomGlobal.window).get("gwtAngularFacade"));
        gwtAngularFacade.gameUiControl = gameUiControl;
        gwtAngularFacade.editorFrontendProvider = editorFrontendProvider;
        gwtAngularFacade.statusProvider = statusProvider;
        gwtAngularFacade.inputService = inputService;
        gwtAngularFacade.terrainTypeService = terrainTypeService;
        gwtAngularFacade.threeJsModelPackService = threeJsModelPackService;
        gwtAngularFacade.assetService = assetService;
        cockpitService.init(gwtAngularFacade.mainCockpit);
        itemCockpitService.init(gwtAngularFacade.itemCockpitFrontend);
        baseItemPlacerService.init(gwtAngularFacade.baseItemPlacerPresenter);
    }

    public void onCrash() {
        gwtAngularFacade.onCrash();
    }

    public GwtAngularBoot getGwtAngularBoot() {
        return gwtAngularFacade.gwtAngularBoot;
    }

    @Produces
    public ThreeJsRendererServiceAccess threeJsRendererServiceAccess() {
        return gwtAngularFacade.threeJsRendererServiceAccess;
    }

}
