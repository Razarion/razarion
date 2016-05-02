package com.btxtech.client.slopeeditor;

import com.btxtech.shared.SlopeShapeEntity;
import com.btxtech.game.jsre.client.common.Index;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseDownEvent;
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
public class Corner {
    // private Logger logger = Logger.getLogger(Corner.class.getName());
    private static final String COLOR_NORMAL = "blue";
    private static final String COLOR_SELECT = "red";
    private static final float RADIUS = 5;
    private SVGCircleElement circle;
    private SlopeShapeEntity slopeShapeEntity;
    private EventRemover onMouseMoveEventRemover;
    private EventRemover onMouseUpEventRemover;
    private Model model;
    private Index mouseOffset;

    public Corner(SlopeShapeEntity slopeShapeEntity, Model model) {
        this.slopeShapeEntity = slopeShapeEntity;
        this.model = model;
        circle = Browser.getDocument().createSVGCircleElement();
        circle.getCx().getBaseVal().setValue(slopeShapeEntity.getPosition().getX());
        circle.getCy().getBaseVal().setValue(slopeShapeEntity.getPosition().getY());
        circle.getR().getBaseVal().setValue(RADIUS);
        circle.addEventListener("mousedown", new EventListener() {
            @Override
            public void handleEvent(Event event) {
                select();
            }
        }, false);
        circle.getStyle().setCursor(Style.Cursor.CROSSHAIR.getCssName());
        circle.getStyle().setProperty("fill", COLOR_NORMAL);
    }

    public void move(Index position) {
        slopeShapeEntity.setPosition(position);
        circle.getCx().getBaseVal().setValue(position.getX());
        circle.getCy().getBaseVal().setValue(position.getY());
    }

    private void move(MouseEvent event) {
        Index mousePosition = model.convertMouseToSvg(event);
        if(mouseOffset == null) {
            mouseOffset = mousePosition.sub(slopeShapeEntity.getPosition());
        }
        model.cornerMoved(mousePosition.sub(mouseOffset), this);
    }

    private void select() {
        mouseOffset = null;
        onMouseMoveEventRemover = Browser.getWindow().addEventListener("mousemove", new EventListener() {
            @Override
            public void handleEvent(Event event) {
                move((MouseEvent) event);
                event.stopPropagation();
                // event.preventDefault();
            }
        }, true);
        onMouseUpEventRemover = Browser.getWindow().addEventListener("mouseup", new EventListener() {
            @Override
            public void handleEvent(Event event) {
                deselect();
            }
        }, true);
        model.selectionChanged(this);
    }

    private void deselect() {
        if (onMouseMoveEventRemover != null) {
            onMouseMoveEventRemover.remove();
        }
        if (onMouseUpEventRemover != null) {
            onMouseUpEventRemover.remove();
        }
    }

    public SVGCircleElement getSvgElement() {
        return circle;
    }

    public SlopeShapeEntity getSlopeShapeEntity() {
        return slopeShapeEntity;
    }

    public void setSelected(boolean selected) {
        if(selected) {
            circle.getStyle().setProperty("fill", COLOR_SELECT);
        } else {
            circle.getStyle().setProperty("fill", COLOR_NORMAL);
        }
    }
}
