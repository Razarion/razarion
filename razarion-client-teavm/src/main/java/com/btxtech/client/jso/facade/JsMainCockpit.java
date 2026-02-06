package com.btxtech.client.jso.facade;

import com.btxtech.uiservice.cockpit.MainCockpit;
import com.btxtech.uiservice.control.GameUiControl;
import org.teavm.jso.JSObject;

import static com.btxtech.client.jso.facade.JsGwtAngularFacade.*;

public class JsMainCockpit implements MainCockpit {
    private final JSObject js;

    JsMainCockpit(JSObject js) {
        this.js = js;
    }

    @Override public void show() { callMethod0(js, "show"); }
    @Override public void hide() { callMethod0(js, "hide"); }
    @Override public void displayResources(int resources) { callMethod1I(js, "displayResources", resources); }
    @Override public void displayXps(int xp, int xp2LevelUp) { callMethod2D(js, "displayXps", xp, xp2LevelUp); }
    @Override public void displayLevel(int levelNumber) { callMethod1I(js, "displayLevel", levelNumber); }
    @Override public void displayItemCount(int itemCount, int usedHouseSpace, int houseSpace) {
        callMethod3I(js, "displayItemCount", itemCount, usedHouseSpace, houseSpace);
    }
    @Override public void displayEnergy(int consuming, int generating) { callMethod2D(js, "displayEnergy", consuming, generating); }
    @Override public void showRadar(GameUiControl.RadarState radarState) { callMethod1S(js, "showRadar", radarState.name()); }
    @Override public void blinkAvailableUnlock(boolean show) { callMethod1B(js, "blinkAvailableUnlock", show); }
    @Override public void clean() { callMethod0(js, "clean"); }

    @org.teavm.jso.JSBody(params = {"obj", "method", "a1", "a2", "a3"}, script = "obj[method](a1, a2, a3);")
    static native void callMethod3I(JSObject obj, String method, int a1, int a2, int a3);
}
