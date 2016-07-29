package com.btxtech.servercommon.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 28.07.2016.
 */
public class Channel extends ColladaXml {
    private String sourceId;
    private String target;

    public Channel(Node node) {
        sourceId = getUrl(node, ATTRIBUTE_SOURCE);
        target = getAttributeAsString(node, ATTRIBUTE_TARGET);
    }

    public String getTargetId() {
        return target.substring(0, target.indexOf("/"));
    }

    public String getModification() {
        return target.substring(target.indexOf("/") + 1, target.indexOf("."));
    }

    public String getAxis() {
        return target.substring(target.indexOf(".") + 1);
    }

    @Override
    public String toString() {
        return "Channel{" +
                "sourceId='" + sourceId + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
