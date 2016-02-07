package com.btxtech.client.slopeeditor;

import com.btxtech.client.ViewFieldMover;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.game.jsre.client.common.Index;
import com.google.gwt.dom.client.Element;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.MouseEvent;
import elemental.svg.SVGGElement;
import elemental.svg.SVGLineElement;
import elemental.svg.SVGPoint;
import elemental.svg.SVGSVGElement;
import elemental.svg.SVGTransform;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 12.05.2015.
 */
public class SlopeEditor implements Model {
    @Deprecated
    private static final int HEIGHT = 400; // This is not the real SVG element height
    private float scale = 0.3f;
    private SVGSVGElement svg;
    private List<Corner> corners = new ArrayList<>();
    private List<Line> lines = new ArrayList<>();
    private SVGGElement group;
    private Logger logger = Logger.getLogger(SlopeEditor.class.getName());
    private SVGTransform translateTransform;
    private SVGTransform scaleTransform;
    private Index lastScrollPosition;
    @Inject
    private TerrainSurface terrainSurface;

    protected void init(Element svgElement) {
        this.svg = (SVGSVGElement) svgElement;

        group = Browser.getDocument().createSVGGElement();
        // Invert Y axis and shift to middle
        translateTransform = svg.createSVGTransform();
        translateTransform.setTranslate(0, HEIGHT / 2);
        group.getAnimatedTransform().getBaseVal().appendItem(translateTransform);
        scaleTransform = svg.createSVGTransform();
        scaleTransform.setScale(scale, -scale);
        group.getAnimatedTransform().getBaseVal().appendItem(scaleTransform);
        drawEnvironment();
        svg.addEventListener("mousedown", new EventListener() {

            @Override
            public void handleEvent(Event evt) {
                MouseEvent event = (MouseEvent) evt;
                if (event.getButton() == MouseEvent.Button.PRIMARY) {
                    lastScrollPosition = new Index(event.getX(), event.getY());
                }
            }
        }, false);

        svg.addEventListener("mousemove", new EventListener() {

            @Override
            public void handleEvent(Event evt) {
                MouseEvent event = (MouseEvent) evt;
                int buttons = getButtons(event);
                if ((buttons & 1) == 0) {
                    lastScrollPosition = null;
                } else if (lastScrollPosition != null) {
                    int sx = (int) translateTransform.getMatrix().getE() + event.getX() - lastScrollPosition.getX();
                    int sy = (int) translateTransform.getMatrix().getF() + event.getY() - lastScrollPosition.getY();
                    translateTransform.setTranslate(sx, sy);
                    lastScrollPosition = new Index(event.getX(), event.getY());
                }
            }
        }, false);
        svg.getStyle().setCursor("all-scroll");
        svg.appendChild(group);

        setup(terrainSurface.getPlateauConfigEntity().getShape());
    }

    private void setup(List<Index> cornerPositions) {
        // remove old
        for (Corner corner : corners) {
            group.removeChild(corner.getSvgElement());
        }
        corners.clear();
        for (Line line : lines) {
            group.removeChild(line.getSvgElement());
        }
        lines.clear();
        // setup
        for (int i = 0; i < cornerPositions.size(); i++) {
            Index index = cornerPositions.get(i);
            Corner corner = new Corner(index, this);
            corners.add(corner);
            if (i + 1 < cornerPositions.size()) {
                lines.add(new Line(corner, cornerPositions.get(i + 1), this));
            }
        }
        // append. zIndex does not work in SVG 1.1. Append order is used. First added first draw. Latter added are draw on top.
        for (Line line : lines) {
            group.appendChild(line.getSvgElement());
        }
        for (Corner corner : corners) {
            group.appendChild(corner.getSvgElement());
        }
    }

    private void drawEnvironment() {
        SVGLineElement xAxis = Browser.getDocument().createSVGLineElement();
        xAxis.getX1().getBaseVal().setValue(-1000);
        xAxis.getY1().getBaseVal().setValue(0);
        xAxis.getX2().getBaseVal().setValue(1000);
        xAxis.getY2().getBaseVal().setValue(0);
        xAxis.getStyle().setProperty("stroke", "#555555");
        xAxis.getStyle().setProperty("stroke-width", "1");
        group.appendChild(xAxis);
    }

    @Override
    public Index convertMouseToSvg(MouseEvent event) {
        SVGPoint point = svg.createSVGPoint();
        point.setX(event.getOffsetX());
        point.setY(event.getOffsetY());
        SVGPoint convertedPoint = point.matrixTransform(group.getCTM().inverse());
        return new Index((int) convertedPoint.getX(), (int) convertedPoint.getY());
    }

    @Override
    public void onChanged() {
        terrainSurface.getPlateauConfigEntity().setShape(setupIndexes());
    }

    @Override
    public void createCorner(Index position, Corner previous) {
        List<Index> cornerPositions = new ArrayList<>();
        for (Corner corner : corners) {
            cornerPositions.add(corner.getPosition());
            if (corner.equals(previous)) {
                cornerPositions.add(position);
            }
        }
        setup(cornerPositions);
        onChanged();
    }

    @Override
    public void cornerMoved(Index position, Corner corner) {
        int cornerIndex = corners.indexOf(corner);
        corner.move(position);
        if (cornerIndex < lines.size()) {
            lines.get(cornerIndex).moveStart(position);
        }
        if (cornerIndex > 0) {
            lines.get(cornerIndex - 1).moveEnd(position);
        }

    }

    private List<Index> setupIndexes() {
        List<Index> indexes = new ArrayList<>();
        for (Corner corner : corners) {
            indexes.add(corner.getPosition());
        }
        return indexes;
    }

    public void zoomIn() {
        scale += 0.1;
        scaleTransform.setScale(scale, -scale);
    }

    public void zoomOut() {
        if (scale - 0.1 <= 0.0) {
            return;
        }
        scale -= 0.1;
        scaleTransform.setScale(scale, -scale);
    }

    private native int getButtons(MouseEvent event) /*-{
        return event.buttons;
    }-*/;

}
