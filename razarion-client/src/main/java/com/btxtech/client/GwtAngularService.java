package com.btxtech.client;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMapOfAny;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GwtAngularService {
    private GwtAngularFacade gwtAngularFacade;

    public GwtAngularService() {
        JsPropertyMapOfAny jsPropertyMapOfAny = Js.uncheckedCast(DomGlobal.window);
        gwtAngularFacade = Js.uncheckedCast(jsPropertyMapOfAny.get("gwtAngularFacade"));
    }

    public HTMLCanvasElement getCanvasElement() {
        return gwtAngularFacade.canvasElement;
    }
}
