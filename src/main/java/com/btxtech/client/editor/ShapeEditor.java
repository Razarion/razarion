package com.btxtech.client.editor;

import com.btxtech.shared.primitives.Vertex;
import com.btxtech.game.jsre.client.common.Index;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGLineElement;
import org.vectomatic.dom.svg.utils.SVGConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 02.05.2015.
 */
@Deprecated
public class ShapeEditor extends SvgEditor {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    private ShapeEditor() {
        super(WIDTH, HEIGHT, true);
    }

    public static void showEditor() {
        ShapeEditor shapeEditor = new ShapeEditor();
        shapeEditor.show();
        shapeEditor.setFocus();
    }

    protected void setupGrid() {
        OMSVGGElement gridGroup = getDoc().createSVGGElement();
        OMSVGLineElement xNull = getDoc().createSVGLineElement(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
        xNull.getStyle().setSVGProperty(SVGConstants.CSS_STROKE_PROPERTY, "red");
        xNull.getStyle().setSVGProperty(SVGConstants.CSS_STROKE_WIDTH_PROPERTY, Integer.toString(2));
        gridGroup.appendChild(xNull);
        getSvg().appendChild(gridGroup);
    }

    @Override
    protected List<Index> getIndexes() {
        List<Index> indexes = new ArrayList<>();
//        for (Vertex vertex : Plateau.shape) {
//            indexes.add(new Index((int) vertex.getX(), (int) vertex.getZ()));
//        }
        return indexes;
    }

    @Override
    protected void setIndexes(List<Index> indexes) {
        // Plateau.shape = toShapeVector(indexes);
        // TODO Webgl.instance.fillBuffers();
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
            if(i + 1 < shapeVertices.size()) {
                string += ", ";
            }
        }
        string += ");";
        logger.severe(string);
    }

}
