package com.btxtech.common;

/**
 * Created by Beat
 * on 02.03.2018.
 */
public class GwtCommonUtils {

    public native static String jsonStringify(Object object) /*-{
        return JSON.stringify(object);
    }-*/;

}
