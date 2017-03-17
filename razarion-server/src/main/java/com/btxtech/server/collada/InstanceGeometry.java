package com.btxtech.server.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 19.08.2015.
 */
public class InstanceGeometry extends ColladaXml {
    private String url;
    private String materialTargetUri;

    public InstanceGeometry(Node node) {
        url = getUrl(node, ATTRIBUTE_URL);
        Node instanceMaterial = getChainedChild(node, ELEMENT_BIND_MATERIAL, ELEMENT_TECHNIQUE_COMMON, ELEMENT_INSTANCE_MATERIAL);
        if(instanceMaterial != null) {
            materialTargetUri = getUrl(instanceMaterial, ATTRIBUTE_TARGET);
        }
    }

    public String getUrl() {
        return url;
    }

    public String getMaterialTargetUri() {
        return materialTargetUri;
    }

    @Override
    public String toString() {
        return "InstanceGeometry{" +
                "url='" + url + '\'' +
                "materialTargetUri='" + materialTargetUri + '\'' +
                '}';
    }
}
