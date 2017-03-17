package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.Color;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Created by Beat
 * 06.06.2016.
 */
public class Technique extends ColladaXml {
    private String shaderModel;
    private Color ambient;
    private Color diffuse;
    private Color specular;
    private Color emission;

    public Technique(Node node) {
        List<Node> lamberts = getChildren(node, ELEMENT_LAMBERT);
        if (!lamberts.isEmpty()) {
            shaderModel = ELEMENT_LAMBERT;
            readValues(lamberts.get(0));
        } else {
            List<Node> phongs = getChildren(node, ELEMENT_PHONG);
            shaderModel = ELEMENT_PHONG;
            readValues(phongs.get(0));
        }
    }

    private void readValues(Node node) {
        ambient = toColor(node, ELEMENT_AMBIENT, ELEMENT_COLOR);
        diffuse = toColor(node, ELEMENT_DIFFUSE, ELEMENT_COLOR);
        specular = toColor(node, ELEMENT_SPECULAR, ELEMENT_COLOR);
        emission = toColor(node, ELEMENT_EMISSION, ELEMENT_COLOR);
    }

    private Color toColor(Node node, String... elementNames) {
        List<Double> doubles = readElementInnerValueAsDoubles(node, elementNames);
        if (doubles == null || doubles.size() < 3) {
            return null;
        }
        if (doubles.size() == 3) {
            return new Color(doubles.get(0), doubles.get(1), doubles.get(2));
        } else {
            return new Color(doubles.get(0), doubles.get(1), doubles.get(2), doubles.get(3));
        }
    }

    public String getShaderModel() {
        return shaderModel;
    }

    public Color getAmbient() {
        return ambient;
    }

    public Color getDiffuse() {
        return diffuse;
    }

    public Color getSpecular() {
        return specular;
    }

    public Color getEmission() {
        return emission;
    }

    @Override
    public String toString() {
        return "Technique{" +
                "shaderModel='" + shaderModel + '\'' +
                ", ambient=" + ambient +
                ", diffuse=" + diffuse +
                ", specular=" + specular +
                ", emission=" + emission +
                '}';
    }
}
