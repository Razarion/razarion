package com.btxtech.client.editor;

import com.btxtech.game.jsre.client.common.Index;
import com.google.gwt.dom.client.Style;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.EventRemover;
import elemental.events.MouseEvent;
import elemental.svg.SVGCircleElement;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 02.05.2015.
 */
public class EditorCorner implements EventListener {
    private static final float RADIUS = 5;
    private Index position;
    private SlopeEditor slopeEditor;
    private SVGCircleElement circle;
    private Logger logger = Logger.getLogger(EditorCorner.class.getName());
    private EventRemover onMouseMoveEventRemover;
    private EventRemover onMouseUpEventRemover;

    public EditorCorner(Index position,  SlopeEditor slopeEditor) {
        this.position = position;
        this.slopeEditor = slopeEditor;
        circle = Browser.getDocument().createSVGCircleElement();
        circle.getCx().getBaseVal().setValue(position.getX());
        circle.getCy().getBaseVal().setValue(position.getY());
        circle.getR().getBaseVal().setValue(RADIUS);
        circle.addEventListener("mousedown", new EventListener() {
            @Override
            public void handleEvent(Event event) {

                onMouseMoveEventRemover = Browser.getWindow().addEventListener("mousemove", new EventListener() {
                    @Override
                    public void handleEvent(Event event) {
                        move((MouseEvent) event);
                    }
                }, true);
                onMouseUpEventRemover = Browser.getWindow().addEventListener("mouseup", new EventListener() {
                    @Override
                    public void handleEvent(Event event) {
                        deselect();
                    }
                }, true);
            }
        }, false);
        circle.getStyle().setCursor(Style.Cursor.MOVE.getCssName());
        slopeEditor.getGroup().appendChild(circle);
        circle.getStyle().setProperty("fill", "blue");
    }

    private void move(MouseEvent event) {
        position = new Index(position.getX(), slopeEditor.convertMouseToSvg(event).getY());
        circle.getCx().getBaseVal().setValue(position.getX());
        circle.getCy().getBaseVal().setValue(position.getY());
    }

    private void deselect() {
        if (onMouseMoveEventRemover != null) {
            onMouseMoveEventRemover.remove();
        }
        if (onMouseUpEventRemover != null) {
            onMouseUpEventRemover.remove();
        }
        slopeEditor.onChanged();
    }

    @Override
    public void handleEvent(Event event) {
        logger.severe("event: " + event);
    }

    public Index getPosition() {
        return position;
    }
}
