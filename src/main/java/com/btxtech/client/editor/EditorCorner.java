package com.btxtech.client.editor;

import com.btxtech.game.jsre.client.common.Index;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import org.vectomatic.dom.svg.OMSVGCircleElement;
import org.vectomatic.dom.svg.OMSVGLineElement;
import org.vectomatic.dom.svg.utils.DOMHelper;
import org.vectomatic.dom.svg.utils.SVGConstants;

/**
 * Created by Beat
 * 02.05.2015.
 */
public class EditorCorner implements MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOutHandler {
    private static final float RADIUS = 5;
    private Index position;
    private EditorCorner predecessor;
    private SvgEditor svgEditor;
    private OMSVGCircleElement circle;
    private boolean dragging;
    private EditorCorner successor;
    private OMSVGLineElement line;
    private String normalColor;

    public EditorCorner(Index position, EditorCorner predecessor, SvgEditor svgEditor) {
        this.position = position;
        this.predecessor = predecessor;
        this.svgEditor = svgEditor;
        circle = svgEditor.getDoc().createSVGCircleElement(position.getX(), position.getY(), RADIUS);
        circle.addMouseDownHandler(this);
        circle.addMouseUpHandler(this);
        circle.addMouseMoveHandler(this);
        circle.addMouseOutHandler(this);
        circle.getStyle().setCursor(Style.Cursor.MOVE);
        svgEditor.getGroup().appendChild(circle);
        if (predecessor != null) {
            predecessor.successor = this;
            line = svgEditor.getDoc().createSVGLineElement(position.getX(), position.getY(), predecessor.position.getX(), predecessor.position.getY());
            line.getStyle().setSVGProperty(SVGConstants.CSS_STROKE_PROPERTY, "blue");
            line.getStyle().setSVGProperty(SVGConstants.CSS_STROKE_WIDTH_PROPERTY, Integer.toString(2));
            svgEditor.getGroup().appendChild(line);
            normalColor = "blue";
        } else {
            normalColor = "black";
        }
        circle.getStyle().setSVGProperty(SVGConstants.CSS_FILL_PROPERTY, normalColor);
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {
        if (svgEditor.isDeleteMode()) {
            DOMHelper.releaseCaptureElement();
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
            DOMHelper.setCaptureElement(circle, null);
        }
        event.stopPropagation();
        event.preventDefault();
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        dragging = false;
        DOMHelper.releaseCaptureElement();
        event.stopPropagation();
        event.preventDefault();
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        circle.getStyle().setSVGProperty(SVGConstants.CSS_FILL_PROPERTY, "red");
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

    @Override
    public void onMouseOut(MouseOutEvent event) {
        circle.getStyle().setSVGProperty(SVGConstants.CSS_FILL_PROPERTY, normalColor);
    }

    public Index getPosition() {
        return position;
    }

    public void setDeleteMode(boolean mode) {
        if (mode) {
            circle.getStyle().setCursor(Style.Cursor.CROSSHAIR);
        } else {
            circle.getStyle().setCursor(Style.Cursor.POINTER);
            circle.getStyle().setSVGProperty(SVGConstants.CSS_FILL_PROPERTY, normalColor);
        }
    }
}
