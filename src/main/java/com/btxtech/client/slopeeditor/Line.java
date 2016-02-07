package com.btxtech.client.slopeeditor;

import com.btxtech.game.jsre.client.common.Index;
import com.google.gwt.dom.client.Style;
import elemental.client.Browser;
import elemental.dom.Node;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.MouseEvent;
import elemental.svg.SVGLineElement;

/**
 * Created by Beat
 * 06.02.2016.
 */
public class Line {
    private SVGLineElement line;

    public Line(final Corner previous, Index next, final Model model) {
        line = Browser.getDocument().createSVGLineElement();
        line.getX1().getBaseVal().setValue(previous.getPosition().getX());
        line.getY1().getBaseVal().setValue(previous.getPosition().getY());
        line.getX2().getBaseVal().setValue(next.getX());
        line.getY2().getBaseVal().setValue(next.getY());
        line.getStyle().setProperty("stroke", "blue");
        line.getStyle().setProperty("stroke-width", "2");
        line.getStyle().setCursor("copy");
        line.addEventListener("mousedown", new EventListener() {

            @Override
            public void handleEvent(Event evt) {
                model.createCorner(model.convertMouseToSvg((MouseEvent)evt), previous);
            }
        }, false);

    }

    public Node getSvgElement() {
        return line;
    }

    public void moveStart(Index position) {
        line.getX1().getBaseVal().setValue(position.getX());
        line.getY1().getBaseVal().setValue(position.getY());
    }

    public void moveEnd(Index position) {
        line.getX2().getBaseVal().setValue(position.getX());
        line.getY2().getBaseVal().setValue(position.getY());
    }
}
