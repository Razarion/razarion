package com.btxtech.client.editor;

import com.btxtech.game.jsre.client.common.Index;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.EventRemover;
import elemental.events.MouseEvent;
import elemental.svg.SVGCircleElement;
import elemental.svg.SVGLineElement;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 02.05.2015.
 */
public class EditorCorner implements EventListener {
    private static final float RADIUS = 5;
    private Index position;
    private EditorCorner predecessor;
    private SvgEditor svgEditor;
    private SVGCircleElement circle;
    private boolean dragging;
    private EditorCorner successor;
    private SVGLineElement line;
    private String normalColor;
    private Logger logger = Logger.getLogger(EditorCorner.class.getName());
    private EventRemover onMouseMoveEventRemover;
    // private EventRemover onMouseOutEventRemover;
    private EventRemover onMouseUpEventRemover;

    public EditorCorner(Index position, EditorCorner predecessor, SvgEditor svgEditor) {
        this.position = position;
        this.predecessor = predecessor;
        this.svgEditor = svgEditor;
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
//                onMouseOutEventRemover = Browser.getWindow().addEventListener("mouseout", new EventListener() {
//                    @Override
//                    public void handleEvent(Event event) {
//                        deselect();
//                    }
//                }, true);
                onMouseUpEventRemover = Browser.getWindow().addEventListener("mouseup", new EventListener() {
                    @Override
                    public void handleEvent(Event event) {
                        deselect();
                    }
                }, true);
            }
        }, false);
        circle.getStyle().setCursor(Style.Cursor.MOVE.getCssName());
//        if (predecessor != null) {
//            predecessor.successor = this;
//            line = Browser.getDocument().createSVGLineElement();
//            line.getX1().getBaseVal().setValue(position.getX());
//            line.getY1().getBaseVal().setValue(position.getY());
//            line.getX2().getBaseVal().setValue(predecessor.position.getX());
//            line.getY2().getBaseVal().setValue(predecessor.position.getY());
//            line.getStyle().setProperty("stroke", "red");
//            line.getStyle().setProperty("stroke-width", "2");
//            svgEditor.getGroup().appendChild(line);
//            normalColor = "blue";
//        } else {
//            normalColor = "black";
//        }
        svgEditor.getGroup().appendChild(circle);
        circle.getStyle().setProperty("fill", normalColor);
    }

    private void move(MouseEvent event) {
        position = new Index(position.getX(), svgEditor.convertMouseToSvg(event).getY());
        circle.getCx().getBaseVal().setValue(position.getX());
        circle.getCy().getBaseVal().setValue(position.getY());
        if (line != null) {
            line.getX1().getBaseVal().setValue(position.getX());
            line.getY1().getBaseVal().setValue(position.getY());
        }
        if (successor != null) {
            successor.line.getX2().getBaseVal().setValue(position.getX());
            successor.line.getY2().getBaseVal().setValue(position.getY());
        }
    }

    private void deselect() {
        if (onMouseMoveEventRemover != null) {
            onMouseMoveEventRemover.remove();
        }
//        if (onMouseOutEventRemover != null) {
//            onMouseOutEventRemover.remove();
//        }
        if (onMouseUpEventRemover != null) {
            onMouseUpEventRemover.remove();
        }
        svgEditor.onChanged();
    }

    // @Override
    public void onMouseDown(MouseDownEvent event) {
        if (svgEditor.isDeleteMode()) {
            Browser.getDocument().captureEvents();
            svgEditor.getGroup().removeChild(circle);
            if (line != null) {
                svgEditor.getGroup().removeChild(line);
            }
            svgEditor.removeCorner(this);
            if (successor != null && predecessor != null) {
                successor.predecessor = predecessor;
                predecessor.successor = successor;
                successor.line.getX2().getBaseVal().setValue(predecessor.position.getX());
                successor.line.getY2().getBaseVal().setValue(predecessor.position.getY());
            } else if (successor != null) {
                successor.predecessor = null;
                svgEditor.getGroup().removeChild(successor.line);
                successor.line = null;
            } else if (predecessor != null) {
                predecessor.successor = null;
            }
        } else {
            dragging = true;
            Browser.getDocument().releaseEvents();
        }
        event.stopPropagation();
        event.preventDefault();
    }

    // @Override
    public void onMouseUp(MouseUpEvent event) {
        dragging = false;
        Browser.getDocument().releaseEvents();
        event.stopPropagation();
        event.preventDefault();
    }

    // @Override
    public void onMouseMove(MouseEvent event) {
        circle.getStyle().setProperty("fill", "red");
        if (dragging) {
            position = svgEditor.convertMouseToSvg(event);
            //position = new Vec2dInt(event.getRelativeX(editor.getSvg().getElement()), event.getRelativeY(editor.getSvg().getElement()));
            circle.getCx().getBaseVal().setValue(position.getX());
            circle.getCy().getBaseVal().setValue(position.getY());
            if (line != null) {
                line.getX1().getBaseVal().setValue(position.getX());
                line.getY1().getBaseVal().setValue(position.getY());
            }
            if (successor != null) {
                successor.line.getX2().getBaseVal().setValue(position.getX());
                successor.line.getY2().getBaseVal().setValue(position.getY());
            }
            svgEditor.onChanged();
        }
        event.stopPropagation();
        event.preventDefault();
    }

    // @Override
    public void onMouseOut(MouseOutEvent event) {
        circle.getStyle().setProperty("fill", normalColor);
    }


    @Override
    public void handleEvent(Event event) {
        logger.severe("event: " + event);
    }

    public Index getPosition() {
        return position;
    }

    public void setDeleteMode(boolean mode) {
        if (mode) {
            circle.getStyle().setCursor(Style.Cursor.CROSSHAIR.getCssName());
        } else {
            circle.getStyle().setCursor(Style.Cursor.POINTER.getCssName());
            circle.getStyle().setProperty("fill", normalColor);
        }
    }
}
