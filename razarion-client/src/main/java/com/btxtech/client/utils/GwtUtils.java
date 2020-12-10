package com.btxtech.client.utils;

import com.btxtech.shared.datatypes.Index;
import com.google.gwt.user.client.ui.Widget;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.MouseEvent;
import elemental.html.ImageElement;
import elemental2.dom.HTMLElement;
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

    public static boolean isButtonResponsible4Event(elemental2.dom.MouseEvent event, double button) {
        return event.button == button;
    }

    public static boolean isButtonDown(elemental2.dom.MouseEvent event, int button) {
        return ((int)event.buttons & button) == button;
    }

    public static void preventContextMenu(Widget widget) {
        preventContextMenu(castElementToElement(widget.getElement()));
    }

    public static void preventContextMenu(Element widget) {
        widget.addEventListener(Event.CONTEXTMENU, Event::preventDefault, true);
    }

    public static void preventContextMenu(HTMLElement element) {
        element.addEventListener(Event.CONTEXTMENU, elemental2.dom.Event::preventDefault, true);
    }

    public static native void toggleFullscreen(HTMLElement element) /*-{
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

    /**
     * @param integer to correct
     * @return corrected integer
     */
    public static int correctInt(int integer) {
        return (int) Math.floor(integer);
    }

    public static Index correctIndex(int x, int y) {
        return new Index(correctInt(x), correctInt(y));
    }
}
