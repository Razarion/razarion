package com.btxtech.server.collada;

import org.w3c.dom.Node;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class TechniqueCommon extends ColladaXml {
    private Accessor accessor;

    public TechniqueCommon(Node node) {
        accessor = new Accessor(getChild(node, ELEMENT_TECHNIQUE_ACCESSOR));
    }

    public Accessor getAccessor() {
        return accessor;
    }

    @Override
    public String toString() {
        return "TechniqueCommon{" +
                "accessor=" + accessor +
                "} " + super.toString();
    }
}
