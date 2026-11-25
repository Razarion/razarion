package com.btxtech.client.jso.facade;

import com.btxtech.client.jso.JsObject;
import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.uiservice.cockpit.ChatCockpit;
import org.teavm.jso.JSObject;

public class JsChatCockpit implements ChatCockpit {
    private final JSObject js;

    JsChatCockpit(JSObject js) {
        this.js = js;
    }

    /**
     * A plain JS object with the fields, not the Java object: under WASM-GC a Java object reaches JS
     * as an opaque reference, and calling getUserName() on it there fails - a message sent while
     * the chat was open never appeared, the sender's own included.
     */
    @Override
    public void onMessage(ChatMessage chatMessage) {
        JsObject message = JsObject.create();
        message.set("userName", chatMessage.getUserName());
        message.set("message", chatMessage.getMessage());
        if (chatMessage.getInstagramUrl() != null) {
            message.set("instagramUrl", chatMessage.getInstagramUrl());
        } else {
            message.setNull("instagramUrl");
        }
        if (chatMessage.getTimestamp() != null) {
            message.set("timestamp", (double) chatMessage.getTimestamp());
        } else {
            message.setNull("timestamp");
        }
        callOnMessage(js, message);
    }

    @org.teavm.jso.JSBody(params = {"obj", "message"}, script = "obj.onMessage(message);")
    private static native void callOnMessage(JSObject obj, JSObject message);

    /** Plain fields again, for the same reason as {@link #onMessage}. */
    @Override
    public void onEmote(com.btxtech.shared.datatypes.EmotePacket emotePacket) {
        JsObject emote = JsObject.create();
        emote.set("emote", emotePacket.getEmote());
        emote.set("syncItemId", emotePacket.getSyncItemId());
        emote.set("baseId", emotePacket.getBaseId());
        if (emotePacket.getName() != null) {
            emote.set("name", emotePacket.getName());
        } else {
            emote.setNull("name");
        }
        emote.set("x", emotePacket.getX());
        emote.set("y", emotePacket.getY());
        emote.set("reply", emotePacket.isReply());
        callOnEmote(js, emote);
    }

    @org.teavm.jso.JSBody(params = {"obj", "emote"}, script = "if (obj.onEmote) { obj.onEmote(emote); }")
    private static native void callOnEmote(JSObject obj, JSObject emote);
}
