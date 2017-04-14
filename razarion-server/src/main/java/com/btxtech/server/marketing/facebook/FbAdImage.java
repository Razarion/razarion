package com.btxtech.server.marketing.facebook;

import com.facebook.ads.sdk.AdImage;

/**
 * Created by Beat
 * 14.04.2017.
 */
public class FbAdImage {
    private String hash;
    private String url;
    private String url128;

    public FbAdImage(AdImage adImage) {
        hash = adImage.getFieldHash();
        url = adImage.getFieldUrl();
        url128 = adImage.getFieldUrl128();
    }

    public String getHash() {
        return hash;
    }

    public String getUrl() {
        return url;
    }

    public String getUrl128() {
        return url128;
    }
}
