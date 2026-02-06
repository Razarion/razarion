package com.btxtech.client.jso.facade;

import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.dialog.ModelDialogPresenter;
import org.teavm.jso.JSObject;

import static com.btxtech.client.jso.facade.JsGwtAngularFacade.*;

public class JsModelDialogPresenter implements ModelDialogPresenter {
    private final JSObject js;

    JsModelDialogPresenter(JSObject js) {
        this.js = js;
    }

    @Override
    public void showLevelUp() {
        callMethod0(js, "showLevelUp");
    }

    @Override
    public void showQuestPassed() {
        callMethod0(js, "showQuestPassed");
    }

    @Override
    public void showAllQuestsCompleted() {
        callMethod0(js, "showAllQuestsCompleted");
    }

    @Override
    public void showBaseLost() {
        callMethod0(js, "showBaseLost");
    }

    @Override
    public void showBoxPicked(BoxContent boxContent) {
        // TODO: Convert BoxContent to JSObject via proxy factory
        callShowBoxPicked(js, boxContent);
    }

    @Override
    public void showUseInventoryItemLimitExceeded(BaseItemType baseItemType) {
        // TODO: Convert BaseItemType to JSObject via proxy factory
        callShowUseInventoryItemLimitExceeded(js, baseItemType);
    }

    @Override
    public void showUseInventoryHouseSpaceExceeded() {
        callMethod0(js, "showUseInventoryHouseSpaceExceeded");
    }

    @Override
    public void showLeaveStartTutorial(Runnable closeListener) {
        JsGwtAngularFacade.VoidCallback cb = closeListener::run;
        callShowLeaveStartTutorial(js, cb);
    }

    @Override
    public void showMessageDialog(String title, String message) {
        callShowMessageDialog(js, title, message);
    }

    @Override
    public void showRegisterDialog() {
        callMethod0(js, "showRegisterDialog");
    }

    @Override
    public void showSetUserNameDialog() {
        callMethod0(js, "showSetUserNameDialog");
    }

    @org.teavm.jso.JSBody(params = {"obj", "boxContent"}, script = "obj.showBoxPicked(boxContent);")
    private static native void callShowBoxPicked(JSObject obj, Object boxContent);

    @org.teavm.jso.JSBody(params = {"obj", "baseItemType"}, script = "obj.showUseInventoryItemLimitExceeded(baseItemType);")
    private static native void callShowUseInventoryItemLimitExceeded(JSObject obj, Object baseItemType);

    @org.teavm.jso.JSBody(params = {"obj", "closeListener"}, script = "obj.showLeaveStartTutorial(function() { closeListener.call(); });")
    private static native void callShowLeaveStartTutorial(JSObject obj, JsGwtAngularFacade.VoidCallback closeListener);

    @org.teavm.jso.JSBody(params = {"obj", "title", "message"}, script = "obj.showMessageDialog(title, message);")
    private static native void callShowMessageDialog(JSObject obj, String title, String message);
}
