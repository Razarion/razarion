package com.btxtech.client.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.core.JSPromise;

public final class JsFetch {

    private JsFetch() {
    }

    @JSBody(params = {"url"}, script = "return fetch(url);")
    public static native JSPromise<JsResponse> fetch(String url);

    @JSBody(params = {"url", "options"}, script = "return fetch(url, options);")
    public static native JSPromise<JsResponse> fetch(String url, JSObject options);

    public interface JsResponse extends JSObject {

        @JSProperty
        boolean isOk();

        @JSProperty
        int getStatus();

        @JSProperty
        String getStatusText();

        JSPromise<String> text();

        JSPromise<JSObject> json();
    }

    public static JSObject createPostOptions(String body) {
        return createFetchOptions("POST", body);
    }

    @JSBody(params = {"method", "body"}, script =
            "return { method: method, headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }, body: body };")
    public static native JSObject createFetchOptions(String method, String body);

    @JSBody(params = {"method", "body", "token"}, script =
            "return { method: method, headers: { 'Content-Type': 'application/json', 'Accept': 'application/json', 'Authorization': 'Bearer ' + token }, body: body };")
    public static native JSObject createFetchOptionsWithAuth(String method, String body, String token);
}
