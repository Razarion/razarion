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
    private Color diffuse;
    private Color specular;
    private Double shininess;
    private Color emission;

    public Technique(Node node) {
        getChildren(node, ELEMENT_LAMBERT).forEach(this::readLambertValues);
        getChildren(node, ELEMENT_PHONG).forEach(this::readPhongValues);
        getChildren(node, ELEMENT_BLINN).forEach(this::readBlinnValues);
    }

    private void readLambertValues(Node node) {
        shaderModel = ELEMENT_LAMBERT;
        diffuse = toColor(node, ELEMENT_DIFFUSE, ELEMENT_COLOR);
        specular = toColor(node, ELEMENT_SPECULAR, ELEMENT_COLOR);
        shininess = readElementInnerValueAsDouble(node, ELEMENT_SHININESS, ELEMENT_FLOAT);
        emission = toColor(node, ELEMENT_EMISSION, ELEMENT_COLOR);
    }

    private void readPhongValues(Node node) {
        shaderModel = ELEMENT_PHONG;
        diffuse = toColor(node, ELEMENT_DIFFUSE, ELEMENT_COLOR);
        specular = toColor(node, ELEMENT_SPECULAR, ELEMENT_COLOR);
        shininess = readElementInnerValueAsDouble(node, ELEMENT_SHININESS, ELEMENT_FLOAT);
        emission = toColor(node, ELEMENT_EMISSION, ELEMENT_COLOR);
    }

    private void readBlinnValues(Node node) {
        shaderModel = ELEMENT_BLINN;
        diffuse = toColor(node, ELEMENT_DIFFUSE, ELEMENT_COLOR);
        specular = toColor(node, ELEMENT_SPECULAR, ELEMENT_COLOR);
        shininess = readElementInnerValueAsDouble(node, ELEMENT_SHININESS, ELEMENT_FLOAT);
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

    public Color getDiffuse() {
        return diffuse;
    }

    public Color getSpecular() {
        return specular;
    }

    public Double getShininess() {
        return shininess;
    }

    public Color getEmission() {
        return emission;
    }

    @Override
    public String toString() {
        return "Technique{" +
                "shaderModel='" + shaderModel + '\'' +
                ", diffuse=" + diffuse +
                ", specular=" + specular +
                ", shininess=" + shininess +
                ", emission=" + emission +
                '}';
    }
}
