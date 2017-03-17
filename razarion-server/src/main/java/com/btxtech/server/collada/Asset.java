package com.btxtech.server.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 18.12.2015.
 */
public class Asset extends ColladaXml {
    private String modified;
    private String created;

    public Asset(Node assetNode) {
        modified = getChild(assetNode, ELEMENT_MODIFIED).getFirstChild().getNodeValue();
        created = getChild(assetNode, ELEMENT_MODIFIED).getFirstChild().getNodeValue();
    }

    @Override
    public String toString() {
        return "Asset{" +
                "modified='" + modified + '\'' +
                ", created='" + created + '\'' +
                '}';
    }
}
