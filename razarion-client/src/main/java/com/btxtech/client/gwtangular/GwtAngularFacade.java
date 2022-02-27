package com.btxtech.client.gwtangular;

import com.btxtech.client.editor.EditorFrontendProvider;
import com.btxtech.uiservice.cockpit.MainCockpit;
import com.btxtech.uiservice.cockpit.item.ItemCockpitFrontend;
import com.btxtech.uiservice.renderer.ThreeJsRendererServiceAccess;
import com.btxtech.uiservice.terrain.InputService;
import elemental2.dom.HTMLCanvasElement;
import jsinterop.annotations.JsType;

/**
 * Instantiated by Angular
 */
@JsType(isNative = true)
public abstract class GwtAngularFacade {
    @Deprecated
    public HTMLCanvasElement canvasElement; // Initialized by Angular
    @Deprecated
    public Callback canvasResizeCallback;  // Initialized by GWT
    public MainCockpit mainCockpit; // Initialized by Angular
    public ItemCockpitFrontend itemCockpitFrontend; // Initialized by Angular
    public EditorFrontendProvider editorFrontendProvider;  // Initialized by GWT
    public StatusProvider statusProvider; // Initialized by GWT
    public ThreeJsRendererServiceAccess threeJsRendererServiceAccess; // Initialized by Angular
    public InputService inputService; // Initialized by GWT

    public abstract void onCrash();
}
