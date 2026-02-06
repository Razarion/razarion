package com.btxtech.client.jso.facade;

import com.btxtech.client.bridge.DtoConverter;
import com.btxtech.client.jso.JsConsole;
import com.btxtech.uiservice.itemplacer.BaseItemPlacer;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerPresenter;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

import static com.btxtech.client.jso.facade.JsGwtAngularFacade.*;

public class JsBaseItemPlacerPresenter implements BaseItemPlacerPresenter {
    private final JSObject js;

    JsBaseItemPlacerPresenter(JSObject js) {
        this.js = js;
    }

    @Override
    public void activate(BaseItemPlacer baseItemPlacer) {
        JSObject placerJs = DtoConverter.convertBaseItemPlacer(baseItemPlacer);
        callActivate(js, placerJs);
    }

    @Override
    public void deactivate() {
        callMethod0(js, "deactivate");
    }

    @JSBody(params = {"obj", "placer"}, script = "obj.activate(placer);")
    private static native void callActivate(JSObject obj, JSObject placer);
}
