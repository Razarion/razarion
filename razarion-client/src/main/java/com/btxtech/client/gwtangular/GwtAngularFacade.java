package com.btxtech.client.gwtangular;

import com.btxtech.client.editor.EditorFrontendProvider;
import com.btxtech.uiservice.cockpit.item.ItemCockpitPanel;
import elemental2.dom.HTMLCanvasElement;
import jsinterop.annotations.JsType;

/**
 * Instantiated by Angular
 */
@JsType(isNative = true)
public class GwtAngularFacade {
    public HTMLCanvasElement canvasElement; // Initialized by Angular
    public ItemCockpitPanel itemCockpitPanel; // Initialized by Angular
    public EditorFrontendProvider editorFrontendProvider;  // Initialized by GWT
}
