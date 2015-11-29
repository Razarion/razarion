package com.btxtech.client.editor;

import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.game.jsre.client.common.Index;
import com.google.gwt.dom.client.Element;
import elemental.client.Browser;
import elemental.events.MouseEvent;
import elemental.svg.SVGGElement;
import elemental.svg.SVGLineElement;
import elemental.svg.SVGPoint;
import elemental.svg.SVGSVGElement;
import elemental.svg.SVGTransform;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 12.05.2015.
 */
public class SlopeEditor {
    @Deprecated
    private static final int HEIGHT = 400; // This is not the real SVG element height
    private SVGSVGElement svg;
    private List<EditorCorner> editorCorners = new ArrayList<>();
    private SVGGElement group;
    private Logger logger = Logger.getLogger(SlopeEditor.class.getName());
    @Inject
    private TerrainSurface terrainSurface;

    protected void init(Element svgElement) {
        this.svg = (SVGSVGElement) svgElement;

        group = Browser.getDocument().createSVGGElement();
        // Invert Y axis and shift to middle
        SVGTransform transform = svg.createSVGTransform();
        transform.setTranslate(0, HEIGHT / 2);
        group.getAnimatedTransform().getBaseVal().appendItem(transform);
        transform = svg.createSVGTransform();
        transform.setScale(1, -1);
        group.getAnimatedTransform().getBaseVal().appendItem(transform);
        drawEnvironment();
        svg.appendChild(group);

        for (Index index : terrainSurface.getPlateau().getSlopeIndexes()) {
            addEditorCorner(index);
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

        SVGLineElement top = Browser.getDocument().createSVGLineElement();
        top.getX1().getBaseVal().setValue(-1000);
        top.getY1().getBaseVal().setValue(terrainSurface.getPlateau().getPlateauConfigEntity().getTop());
        top.getX2().getBaseVal().setValue(1000);
        top.getY2().getBaseVal().setValue(terrainSurface.getPlateau().getPlateauConfigEntity().getTop());
        top.getStyle().setProperty("stroke", "#888888");
        top.getStyle().setProperty("stroke-width", "1");
        group.appendChild(top);
    }

    private void addEditorCorner(Index position) {
        try {
            editorCorners.add(new EditorCorner(position, this));
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Editor.addEditorCorner()", t);
        }
    }

    public SVGGElement getGroup() {
        return group;
    }

    public Index convertMouseToSvg(MouseEvent event) {
        SVGPoint point = svg.createSVGPoint();
        point.setX(event.getOffsetX());
        point.setY(event.getOffsetY());
        SVGPoint convertedPoint = point.matrixTransform(group.getCTM().inverse());
        return new Index((int) convertedPoint.getX(), (int) convertedPoint.getY());
    }

    public void onChanged() {
        terrainSurface.getPlateau().setSlopeIndexes(setupIndexes());
    }

    private List<Index> setupIndexes() {
        List<Index> indexes = new ArrayList<>();
        for (EditorCorner editorCorner : editorCorners) {
            indexes.add(editorCorner.getPosition());
        }
        return indexes;
    }
}
