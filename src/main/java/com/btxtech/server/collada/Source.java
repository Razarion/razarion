package com.btxtech.server.collada;

import com.btxtech.shared.primitives.TextureCoordinate;
import com.btxtech.shared.primitives.Vertex;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class Source extends NameIdColladaXml {
    private TechniqueCommon techniqueCommon;
    private FloatArray floatArray;

    public Source(Node node) {
        super(node);

        techniqueCommon = new TechniqueCommon(getChild(node, ELEMENT_TECHNIQUE_COMMON));
        floatArray = new FloatArray(getChild(node, ELEMENT_FLOAT_ARRAY));
    }


    public List<Vertex> setupVertices() {
        return techniqueCommon.getAccessor().convertToVertex(floatArray.getFloatArray(), floatArray.getCount());
    }

    public List<TextureCoordinate> setupTextureCoordinates() {
        return techniqueCommon.getAccessor().convertToTextureCoordinate(floatArray.getFloatArray(), floatArray.getCount());
    }

    @Override
    public String toString() {
        return "Source{" +
                "techniqueCommon=" + techniqueCommon +
                "floatArray=" + floatArray +
                "} " + super.toString();
    }
}
