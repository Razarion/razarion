package com.btxtech.client.editor.slopeeditor;

import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.google.gwt.dom.client.Element;
import elemental.client.Browser;
import elemental.events.MouseEvent;
import elemental.svg.SVGGElement;
import elemental.svg.SVGLineElement;
import elemental.svg.SVGPoint;
import elemental.svg.SVGSVGElement;
import elemental.svg.SVGTransform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 12.05.2015.
 */
public class ShapeEditor implements Model {
    @Deprecated
    private static final int HEIGHT = 400; // This is not the real SVG element height
    private float scale = 10f;
    private SVGSVGElement svg;
    private List<Corner> corners = new ArrayList<>();
    private List<Line> lines = new ArrayList<>();
    private SVGGElement group;
    // private Logger logger = Logger.getLogger(SlopeEditor.class.getName());
    private SVGTransform translateTransform;
    private SVGTransform scaleTransform;
    private DecimalPosition lastScrollPosition;
    private SlopeConfig slopeConfig;
    private SelectedCornerListener selectedCornerListener;
    private SVGLineElement helperLine;
    private Corner selected;

    protected void init(Element svgElement, SlopeConfig slopeConfig, SelectedCornerListener selectedCornerListener, Double scale) {
        this.slopeConfig = slopeConfig;
        this.selectedCornerListener = selectedCornerListener;
        this.svg = (SVGSVGElement) svgElement;

        group = Browser.getDocument().createSVGGElement();
        // Invert Y axis and shift to middle
        translateTransform = svg.createSVGTransform();
        translateTransform.setTranslate(0, HEIGHT / 2);
        group.getAnimatedTransform().getBaseVal().appendItem(translateTransform);
        scaleTransform = svg.createSVGTransform();
        if (scale != null) {
            this.scale = scale.floatValue();
        }
        scaleTransform.setScale(this.scale, -this.scale);
        group.getAnimatedTransform().getBaseVal().appendItem(scaleTransform);
        drawEnvironment();
        svg.addEventListener("mousedown", evt -> {
            MouseEvent event = (MouseEvent) evt;
            if (event.getButton() == MouseEvent.Button.PRIMARY) {
                lastScrollPosition = new DecimalPosition(event.getX(), event.getY());
            }
        }, false);

        svg.addEventListener("mousemove", evt -> {
            MouseEvent event = (MouseEvent) evt;
            int buttons = GwtUtils.getButtons(event);
            if ((buttons & 1) == 0) {
                lastScrollPosition = null;
            } else if (lastScrollPosition != null) {
                double sx = translateTransform.getMatrix().getE() + event.getX() - lastScrollPosition.getX();
                double sy = translateTransform.getMatrix().getF() + event.getY() - lastScrollPosition.getY();
                translateTransform.setTranslate((float) sx, (float) sy);
                lastScrollPosition = new DecimalPosition(event.getX(), event.getY());
            }
        }, false);
        svg.getStyle().setCursor("all-scroll");
        svg.appendChild(group);

        // TODO setup(slopeConfig.getSlopeShapes());
    }

    private void setup(List<SlopeShape> shapeEntry) {
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
        for (int i = 0; i < shapeEntry.size(); i++) {
            Corner corner = new Corner(shapeEntry.get(i), this);
            corners.add(corner);
            if (i + 1 < shapeEntry.size()) {
                lines.add(new Line(corner, shapeEntry.get(i + 1).getPosition(), this));
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
        xAxis.getStyle().setProperty("stroke-width", "0.1");
        group.appendChild(xAxis);
    }

    @Override
    public DecimalPosition convertMouseToSvg(MouseEvent event) {
        SVGPoint point = svg.createSVGPoint();
        point.setX(event.getOffsetX());
        point.setY(event.getOffsetY());
        SVGPoint convertedPoint = point.matrixTransform(group.getCTM().inverse());
        return new DecimalPosition(convertedPoint.getX(), convertedPoint.getY());
    }

    @Override
    public void createCorner(DecimalPosition position, Corner previous) {
        selectionChanged(null);
        // TODO int index = slopeConfig.getSlopeShapes().indexOf(previous.getSlopeShape());
        // TODO slopeConfig.getSlopeShapes().add(index + 1, new SlopeShape(position, 0));
        // TODO setup(slopeConfig.getSlopeShapes());
    }

    public void deleteSelectedCorner() {
        SlopeShape slopeShape = selected.getSlopeShape();
        selectionChanged(null);
        // TODO slopeConfig.getSlopeShapes().remove(slopeShape);
        // TODO setup(slopeConfig.getSlopeShapes());
    }

    @Override
    public void cornerMoved(DecimalPosition position, Corner corner) {
        int cornerIndex = corners.indexOf(corner);
        corner.move(position);
        if (cornerIndex < lines.size()) {
            lines.get(cornerIndex).moveStart(position);
        }
        if (cornerIndex > 0) {
            lines.get(cornerIndex - 1).moveEnd(position);
        }
        selectedCornerListener.onSelectionChanged(corner);
    }

    public void moveSelected(DecimalPosition position) {
        cornerMoved(position, selected);
    }

    public void setSlopeFactorSelected(double slopeFactor) {
        selected.getSlopeShape().setSlopeFactor((float) slopeFactor);
    }

    public void zoomIn() {
        scale += 1;
        scaleTransform.setScale(scale, -scale);
    }

    public void zoomOut() {
        if (scale - 1 <= 0.0) {
            return;
        }
        scale -= 1;
        scaleTransform.setScale(scale, -scale);
    }

    public float getScale() {
        return scale;
    }

    public void setHelperLine(Double value) {
        if (value != null) {
            if (helperLine == null) {
                helperLine = Browser.getDocument().createSVGLineElement();
                helperLine.getStyle().setProperty("stroke", "#222222");
                helperLine.getStyle().setProperty("stroke-width", "0.1");
                helperLine.getX1().getBaseVal().setValue(-1000);
                helperLine.getX2().getBaseVal().setValue(1000);
                group.appendChild(helperLine);
            }
            helperLine.getY1().getBaseVal().setValue(value.floatValue());
            helperLine.getY2().getBaseVal().setValue(value.floatValue());
        } else {
            if (helperLine != null) {
                group.removeChild(helperLine);
                helperLine = null;
            }
        }
    }

    @Override
    public void selectionChanged(Corner corner) {
        if (selected != null) {
            selected.setSelected(false);
        }
        if (corner != null) {
            selected = corner;
            selected.setSelected(true);
        } else {
            selected = null;
        }
        selectedCornerListener.onSelectionChanged(selected);
    }
}
