package com.btxtech.client.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

public final class JsDocument {

    private JsDocument() {
    }

    @JSBody(params = {"tagName"}, script = "return document.createElement(tagName);")
    public static native JSObject createElement(String tagName);

    @JSBody(params = {"callback"}, script = "document.addEventListener('beforeunload', callback);")
    public static native void addBeforeUnloadListener(VoidCallback callback);

    @JSFunctor
    public interface VoidCallback extends JSObject {
        void call();
    }
}
