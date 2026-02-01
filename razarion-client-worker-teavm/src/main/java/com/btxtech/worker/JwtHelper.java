package com.btxtech.worker;

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
        return "?access_token=" + bearerToken;
    }
}
