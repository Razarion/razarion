package com.btxtech.worker;

import org.teavm.jso.JSBody;

/**
 * JWT Helper utility for TeaVM worker
 */
public final class JwtHelper {

    private JwtHelper() {
    }

    /**
     * Converts a bearer token to a URL-safe query parameter
     *
     * @param bearerToken The bearer token
     * @return URL query string with the token
     */
    public static String bearerTokenToUrl(String bearerToken) {
        if (bearerToken == null || bearerToken.isEmpty()) {
            return "";
        }
        return "?token=" + encodeURIComponent(bearerToken);
    }

    @JSBody(params = {"str"}, script = "return encodeURIComponent(str);")
    private static native String encodeURIComponent(String str);
}
