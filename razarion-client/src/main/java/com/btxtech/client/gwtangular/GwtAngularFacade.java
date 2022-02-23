package com.btxtech.client.gwtangular;

import com.btxtech.client.editor.EditorFrontendProvider;
import com.btxtech.uiservice.cockpit.MainCockpit;
import com.btxtech.uiservice.cockpit.item.ItemCockpitFrontend;
import com.btxtech.uiservice.renderer.ThreeJsRendererService;
import elemental2.dom.HTMLCanvasElement;
import jsinterop.annotations.JsType;

/**
 * Instantiated by Angular
 */
@JsType(isNative = true)
public abstract class GwtAngularFacade {
    @Deprecated
    public HTMLCanvasElement canvasElement; // Initialized by Angular
    public Callback canvasResizeCallback;  // Initialized by GWT
    public MainCockpit mainCockpit; // Initialized by Angular
    public ItemCockpitFrontend itemCockpitFrontend; // Initialized by Angular
    public EditorFrontendProvider editorFrontendProvider;  // Initialized by GWT
    public StatusProvider statusProvider; // Initialized by GWT
    public ThreeJsRendererService threeJsRendererService; // Initialized by Angular

    public abstract void onCrash();
}
