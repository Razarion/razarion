package com.btxtech.client;

import com.btxtech.client.jso.JsLocalStorage;
import org.teavm.jso.JSBody;

public final class JwtHelper {

    private static final String BEARER_TOKEN_KEY = "app.token";

    private JwtHelper() {
    }

    public static String bearerTokenToUrl(String bearerToken) {
        if (bearerToken == null || bearerToken.isEmpty()) {
            return "";
        }
        return "?token=" + encodeURIComponent(bearerToken);
    }

    public static String getBearerTokenFromLocalStorage() {
        try {
            return JsLocalStorage.getItem(BEARER_TOKEN_KEY);
        } catch (Throwable t) {
            return null;
        }
    }

    @JSBody(params = {"str"}, script = "return encodeURIComponent(str);")
    private static native String encodeURIComponent(String str);
}
