package com.btxtech.client.jso.facade;

import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.uiservice.cockpit.ChatCockpit;
import org.teavm.jso.JSObject;

import static com.btxtech.client.jso.facade.JsGwtAngularFacade.*;

public class JsChatCockpit implements ChatCockpit {
    private final JSObject js;

    JsChatCockpit(JSObject js) {
        this.js = js;
    }

    @Override
    public void onMessage(ChatMessage chatMessage) {
        // TODO: Convert ChatMessage to JSObject via proxy factory
        callOnMessage(js, chatMessage);
    }

    @org.teavm.jso.JSBody(params = {"obj", "message"}, script = "obj.onMessage(message);")
    private static native void callOnMessage(JSObject obj, Object message);
}
