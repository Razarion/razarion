package com.btxtech.client.jso.facade;

import com.btxtech.client.bridge.DtoConverter;
import com.btxtech.uiservice.cockpit.item.ItemCockpitFrontend;
import com.btxtech.uiservice.cockpit.item.OtherItemCockpit;
import com.btxtech.uiservice.cockpit.item.OwnItemCockpit;
import com.btxtech.uiservice.cockpit.item.OwnMultipleIteCockpit;
import org.teavm.jso.JSObject;

import static com.btxtech.client.jso.facade.JsGwtAngularFacade.*;

public class JsItemCockpitFrontend implements ItemCockpitFrontend {
    private final JSObject js;

    JsItemCockpitFrontend(JSObject js) {
        this.js = js;
    }

    @Override
    public void displayOwnSingleType(int count, OwnItemCockpit ownItemCockpit) {
        callDisplayOwnSingleType(js, count, DtoConverter.convertOwnItemCockpit(ownItemCockpit));
    }

    @Override
    public void displayOwnMultipleItemTypes(OwnMultipleIteCockpit[] ownMultipleIteCockpits) {
        callDisplayOwnMultipleItemTypes(js, DtoConverter.convertOwnMultipleIteCockpits(ownMultipleIteCockpits));
    }

    @Override
    public void displayOtherItemType(OtherItemCockpit otherItemCockpit) {
        callDisplayOtherItemType(js, DtoConverter.convertOtherItemCockpit(otherItemCockpit));
    }

    @Override
    public void dispose() {
        callMethod0(js, "dispose");
    }

    @org.teavm.jso.JSBody(params = {"obj", "count", "cockpit"}, script = "obj.displayOwnSingleType(count, cockpit);")
    private static native void callDisplayOwnSingleType(JSObject obj, int count, JSObject cockpit);

    @org.teavm.jso.JSBody(params = {"obj", "cockpits"}, script = "obj.displayOwnMultipleItemTypes(cockpits);")
    private static native void callDisplayOwnMultipleItemTypes(JSObject obj, JSObject cockpits);

    @org.teavm.jso.JSBody(params = {"obj", "cockpit"}, script = "obj.displayOtherItemType(cockpit);")
    private static native void callDisplayOtherItemType(JSObject obj, JSObject cockpit);
}
