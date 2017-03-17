package com.btxtech.server.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 06.06.2016.
 */
public class Material extends NameIdColladaXml {
    private String instanceEffectUrl;

    public Material(Node node) {
        super(node);
        instanceEffectUrl = getUrl(getChild(node, ELEMENT_INSTANCE_EFFECT), ATTRIBUTE_URL);
    }

    public String getInstanceEffectUrl() {
        return instanceEffectUrl;
    }

    @Override
    public String toString() {
        return "Material{" +
                "super=" + super.toString() +
                "instanceEffectUrl='" + instanceEffectUrl + '\'' +
                '}';
    }
}
