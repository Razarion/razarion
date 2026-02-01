package com.btxtech.worker.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.core.JSPromise;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.Uint16Array;

/**
 * TeaVM JSO interface for Fetch API
 */
public final class JsFetch {

    private JsFetch() {
    }

    @JSBody(params = {"url"}, script = "return fetch(url);")
    public static native JSPromise<JsResponse> fetch(String url);

    /**
     * Response interface for fetch
     */
    public interface JsResponse extends JSObject {

        @JSProperty
        boolean isOk();

        @JSProperty
        int getStatus();

        @JSProperty
        String getStatusText();

        JSPromise<String> text();

        JSPromise<JSObject> json();

        JSPromise<ArrayBuffer> arrayBuffer();
    }

    /**
     * Helper to create Uint16Array from ArrayBuffer
     */
    @JSBody(params = {"buffer"}, script = "return new Uint16Array(buffer);")
    public static native Uint16Array createUint16Array(ArrayBuffer buffer);
}
