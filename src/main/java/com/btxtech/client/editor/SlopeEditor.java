package com.btxtech.client.editor;

import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.primitives.Vertex;
import com.google.gwt.dom.client.Element;
import elemental.svg.SVGSVGElement;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 02.05.2015.
 */
@Dependent
public class SlopeEditor extends SvgEditor {
    private static final int WIDTH = 366;
    private static final int HEIGHT = 400;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private RenderService renderService;
    // private Logger logger = Logger.getLogger(SlopeEditor.class.getName());

    public void init(Element svgElement) {
        init(svgElement, WIDTH, HEIGHT, false);
    }

    protected void setupGrid() {
//        OMSVGGElement gridGroup = getDoc().createSVGGElement();
//        OMSVGLineElement xNull = getDoc().createSVGLineElement(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
//        xNull.getStyle().setSVGProperty(SVGConstants.CSS_STROKE_PROPERTY, "red");
//        xNull.getStyle().setSVGProperty(SVGConstants.CSS_STROKE_WIDTH_PROPERTY, Integer.toString(2));
//        gridGroup.appendChild(xNull);
//        getSvg().appendChild(gridGroup);
    }

    @Override
    protected List<Index> getIndexes() {
        return terrainSurface.getPlateau().getSlopeIndexes();
    }

    @Override
    protected void setIndexes(List<Index> indexes) {
        terrainSurface.getPlateau().setSlopeIndexes(indexes);
    }

    private List<Vertex> toShapeVector(List<Index> indexes) {
        List<Vertex> shape = new ArrayList<>();
        for (Index index : indexes) {
            shape.add(new Vertex(index.getX(), 0, index.getY()));
        }
        return shape;
    }


    @Override
    protected void onDump(List<Index> indexes, Logger logger) {
        String string = "public static List<Vertex> shape = Arrays.asList(";
        List<Vertex> shapeVertices = toShapeVector(indexes);
        for (int i = 0; i < shapeVertices.size(); i++) {
            Vertex vertex = shapeVertices.get(i);
            string += vertex.testString();
            if (i + 1 < shapeVertices.size()) {
                string += ", ";
            }
        }
        string += ");";
        logger.severe(string);
    }
}
