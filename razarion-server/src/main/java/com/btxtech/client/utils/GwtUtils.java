package com.btxtech.client.utils;

/**
 * Created by Beat
 * 20.06.2016.
 */
public class GwtUtils {
    public native static com.google.gwt.dom.client.Element castElementToElement(elemental.dom.Element e) /*-{
        return e;
    }-*/;

    public native static elemental.dom.Element castElementToElement(com.google.gwt.dom.client.Element e) /*-{
        return e;
    }-*/;
}
