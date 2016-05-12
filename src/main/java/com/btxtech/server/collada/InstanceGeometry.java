package com.btxtech.server.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 19.08.2015.
 */
public class InstanceGeometry extends ColladaXml {
    private String url;

    public InstanceGeometry(Node node) {
        url = getUrl(node, ATTRIBUTE_URL);
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "InstanceGeometry{" +
                "url='" + url + '\'' +
                '}';
    }
}
