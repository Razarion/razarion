package com.btxtech.server.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class VerticesInput extends ColladaXml {
    private String sourceId;

    public VerticesInput(Node node) {
        Node inputNode = getChild(node, ELEMENT_INPUT);
        String sourceId = getAttributeAsStringSafe(inputNode, ATTRIBUTE_SOURCE);
        if (sourceId == null) {
            throw new ColladaRuntimeException("No source id for node: " + inputNode);
        }
        if (sourceId.charAt(0) != '#') {
            throw new ColladaRuntimeException("First character in the source id (URIFragmentType) must be '#' but is " + sourceId + " for node: " + inputNode);
        }
        this.sourceId = sourceId.substring(1);

        String semantic = getAttributeAsStringSafe(inputNode, ATTRIBUTE_SEMANTIC);
        if (!semantic.equals("POSITION")) {
            throw new ColladaRuntimeException("Input must be POSITION for node: " + inputNode);
        }
    }

    public String getSourceId() {
        return sourceId;
    }

    @Override
    public String toString() {
        return "VerticesInput{" +
                "sourceId='" + sourceId + '\'' +
                "} " + super.toString();
    }
}
