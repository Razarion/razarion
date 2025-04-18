package com.btxtech.server.model;


import java.util.Base64;

/**
 * Created by Beat
 * 21.06.2016.
 */
public class DataUrlDecoder {
    private static final String DATA = "data:";
    private static final String BASE_64 = "base64,";
    private byte[] data;
    private String type;

    public DataUrlDecoder(String dataUrl) {
        int contentStartIndex = dataUrl.indexOf(BASE_64) + BASE_64.length();
        data = Base64.getDecoder().decode(dataUrl.substring(contentStartIndex));
        type = dataUrl.substring(DATA.length(), dataUrl.indexOf(";"));
    }

    public byte[] getData() {
        return data;
    }

    public long getDataLength() {
        return data.length;
    }

    public String getType() {
        return type;
    }
}
