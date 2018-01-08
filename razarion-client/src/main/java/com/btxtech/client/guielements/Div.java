package com.btxtech.client.guielements;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.errai.common.client.api.annotations.Element;

/**
 * Created by Beat
 * on 28.12.2017.
 */
@JsType(isNative = true)
@Element("div")
public interface Div extends org.jboss.errai.common.client.dom.Div {
    @JsProperty
    int getOffsetLeft ();

    @JsProperty
    int getOffsetTop();

    @JsProperty
    int getClientWidth();

    @JsProperty
    int getClientHeight();

    @JsProperty
    int getScrollTop();

    @JsProperty
    void setScrollTop(int y);
}
