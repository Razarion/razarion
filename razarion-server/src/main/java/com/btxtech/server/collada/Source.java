package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Vertex;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class Source extends NameIdColladaXml {
    private TechniqueCommon techniqueCommon;
    private FloatArray floatArray;
    private NameArray nameArray;

    public Source(Node node) {
        super(node);

        techniqueCommon = new TechniqueCommon(getChild(node, ELEMENT_TECHNIQUE_COMMON));
        Node floatArrayChild = getChildOrNull(node, ELEMENT_FLOAT_ARRAY);
        if(floatArrayChild != null) {
            floatArray = new FloatArray(floatArrayChild);
        } else {
            Node nameArrayChild = getChildOrNull(node, ELEMENT_NAME_ARRAY);
            if(nameArrayChild != null) {
                nameArray = new NameArray(nameArrayChild);
            }
        }
        if((floatArray == null) == (nameArray == null)) {
            throw new IllegalStateException();
        }
    }


    public List<Vertex> setupVertices() {
        return techniqueCommon.getAccessor().convertToVertex(floatArray.getFloatArray(), floatArray.getCount());
    }

    public List<TextureCoordinate> setupTextureCoordinates() {
        return techniqueCommon.getAccessor().convertToTextureCoordinate(floatArray.getFloatArray(), floatArray.getCount());
    }

    public TechniqueCommon getTechniqueCommon() {
        return techniqueCommon;
    }

    public FloatArray getFloatArray() {
        return floatArray;
    }

    public NameArray getNameArray() {
        return nameArray;
    }

    @Override
    public String toString() {
        return "Source{" +
                "techniqueCommon=" + techniqueCommon +
                ", floatArray=" + floatArray +
                ", nameArray=" + nameArray +
                "} " + super.toString();
    }
}
