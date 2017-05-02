package com.btxtech.server.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * User: beat
 * Date: 22.07.12
 * Time: 12:34
 */
public class SocialUtil {

    public static byte[] getHmacSha256Hash(String myKey, byte[] payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret = new SecretKeySpec(myKey.getBytes(), "HmacSHA256");
            mac.init(secret);
            return mac.doFinal(payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
