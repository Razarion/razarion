package com.btxtech.servercommon.collada;

import com.btxtech.shared.datatypes.Matrix4;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Created by Beat
 * 31.07.2016.
 */
public class Translate extends ColladaXml {
    private Matrix4 matrix4;

    public Translate(Node node) {
        List<Double> doubleList = getElementAsDoubleList(node);
        if (doubleList.size() != 3) {
            throw new ColladaRuntimeException("Translation length must be 3. Current length: " + doubleList.size());
        }

        matrix4 = Matrix4.createTranslation(doubleList.get(0), doubleList.get(1), doubleList.get(2));
    }

    public Matrix4 getMatrix4() {
        return matrix4;
    }
}
