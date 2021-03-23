package com.btxtech.client.gwtangular;

import com.btxtech.client.editor.EditorFrontendProvider;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMapOfAny;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class GwtAngularService {
    @Inject
    private EditorFrontendProvider editorFrontendProvider;
    private GwtAngularFacade gwtAngularFacade;

    @PostConstruct
    public void init() {
        gwtAngularFacade = Js.uncheckedCast(Js.<JsPropertyMapOfAny>uncheckedCast(DomGlobal.window).get("gwtAngularFacade"));
        gwtAngularFacade.editorFrontendProvider = editorFrontendProvider;
    }

    public HTMLCanvasElement getCanvasElement() {
        return gwtAngularFacade.canvasElement;
    }
}
