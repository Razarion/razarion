package com.btxtech.servercommon.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 19.08.2015.
 */
public class InstanceVisualScene extends ColladaXml {
    private String url;

    public InstanceVisualScene(Node node) {
        url = getUrl(node, ATTRIBUTE_URL);
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "InstanceVisualScene{" +
                "url='" + url + '\'' +
                '}';
    }
}
