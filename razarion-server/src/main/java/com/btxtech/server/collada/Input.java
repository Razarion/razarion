package com.btxtech.server.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 15.08.2015.
 */
public class Input extends ColladaXml {
    private String sourceId;
    private String semantic;
    private Integer offset;

    public Input(Node node) {
        this.sourceId = getUrl(node, ATTRIBUTE_SOURCE);

        semantic = getAttributeAsStringSafe(node, ATTRIBUTE_SEMANTIC);
        offset = getAttributeAsInteger(node, ATTRIBUTE_OFFSET);
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getSemantic() {
        return semantic;
    }

    public int getOffset() {
        if (offset == null) {
            throw new ColladaRuntimeException("Offset not available");
        }
        return offset;
    }

    @Override
    public String toString() {
        return "Input{" +
                "sourceId='" + sourceId + '\'' +
                ", semantic='" + semantic + '\'' +
                ", offset='" + offset + '\'' +
                '}';
    }
}
