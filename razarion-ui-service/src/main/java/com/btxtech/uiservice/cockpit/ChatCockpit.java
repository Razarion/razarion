package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.ChatMessage;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface ChatCockpit {
    void onMessage(ChatMessage chatMessage);
}
