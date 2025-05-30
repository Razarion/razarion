package com.btxtech.common;

import com.google.gwt.storage.client.Storage;

import static elemental2.core.Global.encodeURIComponent;

public class JwtHelper {

    public static String getBearerTokenFromLocalStorage() {
        // Do not call this methode from the worker LocalStorage is not available
        Storage localStorage = Storage.getLocalStorageIfSupported();
        return localStorage.getItem("app.token");
    }

    public static String bearerTokenToUrl(String bearerToken) {
        if (bearerToken != null && !bearerToken.trim().isEmpty()) {
            return "?token=" + encodeURIComponent(bearerToken);
        } else {
            return "";
        }
    }
}
