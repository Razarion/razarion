package com.btxtech.client.slopeeditor;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.SlopeShapeEntity;
import com.google.gwt.dom.client.Style;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.EventRemover;
import elemental.events.MouseEvent;
import elemental.svg.SVGCircleElement;

/**
 * Created by Beat
 * 02.05.2015.
 */
public class Corner {
    private static final float RADIUS = 5;
    private SVGCircleElement circle;
    private SlopeShapeEntity slopeShapeEntity;
    // private Logger logger = Logger.getLogger(Corner.class.getName());
    private EventRemover onMouseMoveEventRemover;
    private EventRemover onMouseUpEventRemover;
    private Model model;

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
            }
        }, false);
        circle.getStyle().setCursor(Style.Cursor.CROSSHAIR.getCssName());
        circle.getStyle().setProperty("fill", "blue");
    }

    public void move(Index position) {
        slopeShapeEntity.setPosition(position);
        circle.getCx().getBaseVal().setValue(position.getX());
        circle.getCy().getBaseVal().setValue(position.getY());
    }

    private void move(MouseEvent event) {
        model.cornerMoved(model.convertMouseToSvg(event), this);
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
}
