package com.btxtech.client.utils;

import com.google.gwt.user.client.ui.Widget;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.MouseEvent;

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
}
