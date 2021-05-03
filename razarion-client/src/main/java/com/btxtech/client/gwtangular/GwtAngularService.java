package com.btxtech.client.gwtangular;

import com.btxtech.client.editor.EditorFrontendProvider;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMapOfAny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class GwtAngularService {
    @Inject
    private EditorFrontendProvider editorFrontendProvider;
    @Inject
    private MainCockpitService cockpitService;
    @Inject
    private ItemCockpitService itemCockpitService;
    private GwtAngularFacade gwtAngularFacade;

    public void init() {
        gwtAngularFacade = Js.uncheckedCast(Js.<JsPropertyMapOfAny>uncheckedCast(DomGlobal.window).get("gwtAngularFacade"));
        gwtAngularFacade.editorFrontendProvider = editorFrontendProvider;
        cockpitService.init(gwtAngularFacade.mainCockpit);
        itemCockpitService.init(gwtAngularFacade.itemCockpitFrontend);
    }

    public HTMLCanvasElement getCanvasElement() {
        return gwtAngularFacade.canvasElement;
    }

    public void setCanvasResizeListener(Callback callback) {
        gwtAngularFacade.canvasResizeCallback = callback;
    }
}
