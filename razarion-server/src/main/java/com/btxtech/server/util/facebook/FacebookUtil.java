package com.btxtech.server.util.facebook;

import com.btxtech.server.util.SocialUtil;
// import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Objects;

/**
 * User: beat
 * Date: 22.07.12
 * Time: 15:57
 */
public interface FacebookUtil {
    public static final String PAYMENT_STATE_COMPLETED = "completed";

    static String makeUrlSafe(String input) {
        String correctedInput = input.replace('-', '+');
        correctedInput = correctedInput.replace('_', '/');
        String padding = "";
        switch (correctedInput.length() % 4) {
            case 0:
                break;
            case 1:
                padding = "===";
                break;
            case 2:
                padding = "==";
                break;
            default:
                padding = "=";
        }
        return correctedInput + padding;
    }

    static byte[] enhancedBase64UrlSafeDecode(String input) {
        try {
            return Base64.getDecoder().decode(makeUrlSafe(input).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new FacebookUrlException(e);
        }
    }

    static FacebookSignedRequest createAndCheckFacebookSignedRequest(String facebookAppSecret, String signedRequestParameter) {
        String[] signedRequestParts = FacebookUtil.splitSignedRequest(signedRequestParameter);

        FacebookSignedRequest facebookSignedRequest = FacebookUtil.getFacebookSignedRequest(signedRequestParts[1]);
        if (!facebookSignedRequest.getAlgorithm().toUpperCase().equals("HMAC-SHA256")) {
            throw new FacebookUrlException("Invalid signature algorithm received: " + facebookSignedRequest.getAlgorithm());
        }

        FacebookUtil.checkSignature(facebookAppSecret, signedRequestParts[1], signedRequestParts[0]);
        return facebookSignedRequest;
    }

    static String[] splitSignedRequest(String signedRequestParameter) {
        if (signedRequestParameter == null || signedRequestParameter.isEmpty()) {
            throw new FacebookUrlException("Empty signed_request received");
        }
        String[] paramParts = signedRequestParameter.split("\\.");
        if (paramParts.length != 2) {
            throw new FacebookUrlException("Invalid signed request parameter received. Exactly one '.' expected. Received: " + (paramParts.length - 1) + " signedRequestParameter: " + signedRequestParameter);
        }
        return paramParts;
    }

    static FacebookSignedRequest getFacebookSignedRequest(String playLoad) {
        // byte[] payloadBytes = enhancedBase64UrlSafeDecode(playLoad);
        // Gson gson = new Gson();
        // return gson.fromJson(new String(payloadBytes), FacebookSignedRequest.class);
        throw new UnsupportedOperationException();
    }

    static void checkSignature(String secret, String payload, String base64UrlSafeSignature) {
        byte[] signature = enhancedBase64UrlSafeDecode(base64UrlSafeSignature);
        byte[] calculatedSignature = SocialUtil.getHmacSha256Hash(secret, payload.getBytes());
        if (!Objects.deepEquals(signature, calculatedSignature)) {
            throw new FacebookUrlException("Signature does not match");
        }
    }
}
