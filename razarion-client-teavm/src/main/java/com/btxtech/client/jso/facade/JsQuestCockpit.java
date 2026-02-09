package com.btxtech.client.jso.facade;

import com.btxtech.client.bridge.DtoConverter;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.uiservice.cockpit.QuestCockpit;
import org.teavm.jso.JSObject;

import static com.btxtech.client.jso.facade.JsGwtAngularFacade.*;

public class JsQuestCockpit implements QuestCockpit {
    private final JSObject js;

    JsQuestCockpit(JSObject js) {
        this.js = js;
    }

    @Override
    public void showQuestSideBar(QuestDescriptionConfig<?> descriptionConfig, boolean showQuestSelectionButton) {
        JSObject convertedConfig = DtoConverter.convertQuestDescriptionConfig(descriptionConfig);
        callShowQuestSideBar(js, convertedConfig, showQuestSelectionButton);
    }

    @Override
    public void setShowQuestInGameVisualisation() {
        callMethod0(js, "setShowQuestInGameVisualisation");
    }

    @Override
    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        JSObject converted = DtoConverter.convertQuestProgressInfo(questProgressInfo);
        callOnQuestProgress(js, converted);
    }

    @org.teavm.jso.JSBody(params = {"obj", "config", "showButton"}, script = "obj.showQuestSideBar(config, showButton);")
    private static native void callShowQuestSideBar(JSObject obj, JSObject config, boolean showButton);

    @org.teavm.jso.JSBody(params = {"obj", "progressInfo"}, script = "obj.onQuestProgress(progressInfo);")
    private static native void callOnQuestProgress(JSObject obj, JSObject progressInfo);
}
