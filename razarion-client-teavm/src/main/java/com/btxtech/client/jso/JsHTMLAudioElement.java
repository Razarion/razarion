package com.btxtech.client.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface JsHTMLAudioElement extends JSObject {

    @JSBody(script = "return document.createElement('audio');")
    static JsHTMLAudioElement create() {
        return null;
    }

    @JSProperty
    void setSrc(String src);

    @JSProperty
    String getSrc();

    @JSProperty
    void setVolume(double volume);

    @JSProperty
    double getVolume();

    @JSProperty
    void setLoop(boolean loop);

    @JSProperty
    boolean isLoop();

    @JSProperty
    void setCurrentTime(double currentTime);

    @JSProperty
    double getCurrentTime();

    @JSProperty
    boolean isEnded();

    @JSProperty
    boolean isPaused();

    @JSProperty
    int getNetworkState();

    void play();

    // networkState constants
    int NETWORK_EMPTY = 0;
    int NETWORK_IDLE = 1;
    int NETWORK_LOADING = 2;
    int NETWORK_NO_SOURCE = 3;
}
