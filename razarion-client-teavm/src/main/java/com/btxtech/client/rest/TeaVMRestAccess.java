package com.btxtech.client.rest;

import com.btxtech.client.JwtHelper;
import com.btxtech.client.jso.JsConsole;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

import java.util.function.Consumer;

public class TeaVMRestAccess {

    @JSFunctor
    public interface StringCallback extends JSObject {
        void call(String value);
    }

    public static void get(String url, Consumer<String> onSuccess, Consumer<String> onError) {
        String bearerToken = JwtHelper.getBearerTokenFromLocalStorage();

        StringCallback successCb = text -> {
            if (onSuccess != null) {
                onSuccess.accept(text);
            }
        };
        StringCallback errorCb = errorMsg -> {
            JsConsole.error("REST GET failed: " + url);
            if (onError != null) {
                onError.accept("GET " + url + " failed: " + errorMsg);
            }
        };

        if (bearerToken != null) {
            fetchWithAuth("GET", url, null, bearerToken, successCb, errorCb);
        } else {
            fetchNoAuth("GET", url, null, successCb, errorCb);
        }
    }

    public static void post(String url, String jsonBody, Consumer<String> onSuccess, Consumer<String> onError) {
        String bearerToken = JwtHelper.getBearerTokenFromLocalStorage();

        StringCallback successCb = text -> {
            if (onSuccess != null) {
                onSuccess.accept(text);
            }
        };
        StringCallback errorCb = errorMsg -> {
            JsConsole.error("REST POST failed: " + url);
            if (onError != null) {
                onError.accept("POST " + url + " failed: " + errorMsg);
            }
        };

        if (bearerToken != null) {
            fetchWithAuth("POST", url, jsonBody, bearerToken, successCb, errorCb);
        } else {
            fetchNoAuth("POST", url, jsonBody, successCb, errorCb);
        }
    }

    public static void postNoBody(String url, Consumer<String> onSuccess, Consumer<String> onError) {
        post(url, null, onSuccess, onError);
    }

    @JSBody(params = {"method", "url", "body", "token", "onSuccess", "onError"}, script =
            "fetch(url, { method: method, headers: { 'Content-Type': 'application/json', 'Accept': 'application/json', 'Authorization': 'Bearer ' + token }, body: body })" +
            ".then(function(response) { if (response.ok) { return response.text(); } else { throw new Error('HTTP ' + response.status); } })" +
            ".then(function(text) { onSuccess(text); })" +
            ".catch(function(error) { onError('' + error); });")
    private static native void fetchWithAuth(String method, String url, String body, String token, StringCallback onSuccess, StringCallback onError);

    @JSBody(params = {"method", "url", "body", "onSuccess", "onError"}, script =
            "fetch(url, { method: method, headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }, body: body })" +
            ".then(function(response) { if (response.ok) { return response.text(); } else { throw new Error('HTTP ' + response.status); } })" +
            ".then(function(text) { onSuccess(text); })" +
            ".catch(function(error) { onError('' + error); });")
    private static native void fetchNoAuth(String method, String url, String body, StringCallback onSuccess, StringCallback onError);
}
