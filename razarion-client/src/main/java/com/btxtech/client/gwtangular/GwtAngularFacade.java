package com.btxtech.client.gwtangular;

import com.btxtech.client.editor.EditorFrontendProvider;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.AssetService;
import com.btxtech.uiservice.cockpit.MainCockpit;
import com.btxtech.uiservice.cockpit.item.ItemCockpitFrontend;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerPresenter;
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
    public MainCockpit mainCockpit; // Initialized by Angular
    public ItemCockpitFrontend itemCockpitFrontend; // Initialized by Angular
    public BaseItemPlacerPresenter baseItemPlacerPresenter; // Initialized by Angular
    public EditorFrontendProvider editorFrontendProvider;  // Initialized by GWT
    public StatusProvider statusProvider; // Initialized by GWT
    public BabylonRenderServiceAccess threeJsRendererServiceAccess; // Initialized by Angular
    public InputService inputService; // Initialized by GWT
    public TerrainTypeService terrainTypeService; // Initialized by GWT
    public ThreeJsModelPackService threeJsModelPackService; // Initialized by GWT
    public AssetService assetService; // Initialized by GWT

    public abstract void onCrash();
}
