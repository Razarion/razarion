package com.btxtech.client.utils;

import com.google.gwt.user.client.ui.Widget;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.MouseEvent;
import elemental.events.WheelEvent;
import elemental.html.ImageElement;
import org.jboss.errai.common.client.dom.Node;

/**
 * Created by Beat
 * 20.06.2016.
 */
public class GwtUtils {
    public native static Node castElementToJBossNode(com.google.gwt.dom.client.Element e) /*-{
        return e;
    }-*/;

    public native static com.google.gwt.dom.client.Element castElementToElement(elemental.dom.Element e) /*-{
        return e;
    }-*/;

    public native static elemental.dom.Element castElementToElement(com.google.gwt.dom.client.Element e) /*-{
        return e;
    }-*/;

    public native static ImageElement castImageElement(com.google.gwt.dom.client.ImageElement imageElement) /*-{
        return imageElement;
    }-*/;

    public native static int getButtons(MouseEvent event) /*-{
        return event.buttons;
    }-*/;

    public static boolean isButtonResponsible4Event(MouseEvent event, int button) {
        return event.getButton() == button;
    }

    public static boolean isButtonDown(MouseEvent event, int button) {
        return (getButtons(event) & button) == button;
    }

    public static void preventContextMenu(Widget widget) {
        preventContextMenu(castElementToElement(widget.getElement()));
    }

    public static void preventContextMenu(Element widget) {
        widget.addEventListener(Event.CONTEXTMENU, Event::preventDefault, true);
    }

    public static native void toggleFullscreen(com.google.gwt.dom.client.Element element) /*-{
        if (element.requestFullscreen) {
            if ($doc.fullScreenElement) {
                $doc.cancelFullScreen();
            } else {
                element.requestFullscreen();
            }
        } else if (element.msRequestFullscreen) {
            if ($doc.msFullscreenElement) {
                $doc.msExitFullscreen();
            } else {
                element.msRequestFullscreen();
            }
        } else if (element.mozRequestFullScreen) {
            if ($doc.mozFullScreenElement) {
                $doc.mozCancelFullScreen();
            } else {
                element.mozRequestFullScreen();
            }
        } else if (element.webkitRequestFullscreen) {
            if ($doc.webkitFullscreenElement || $doc.webkitCurrentFullScreenElement) {
                $doc.webkitCancelFullScreen();
            } else {
                element.webkitRequestFullscreen();
            }
        }
    }-*/;

    public static native double getDeltaYFromWheelEvent(WheelEvent wheelEvent) /*-{
        return wheelEvent.deltaY;
    }-*/;

}
