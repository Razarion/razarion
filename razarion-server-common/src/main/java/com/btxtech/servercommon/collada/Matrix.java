package com.btxtech.servercommon.collada;

import com.btxtech.shared.datatypes.Matrix4;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Created by Beat
 * 18.08.2015.
 */
public class Matrix extends ColladaXml {
    private Matrix4 matrix4;

    public Matrix(Node node) {
        List<Double> doubleList = getElementAsDoubleList(node);
        if (doubleList.size() != 16) {
            throw new ColladaRuntimeException("Matrix length mus be 16. Current length: " + doubleList.size());
        }

        double doubleArray[] = new double[16];
        for (int i = 0; i < doubleList.size(); i++) {
            doubleArray[i] = doubleList.get(i);
        }

        matrix4 = new Matrix4(doubleArray);
    }

    public Matrix4 getMatrix4() {
        return matrix4;
    }

    @Override
    public String toString() {
        return "Matrix{" +
                "matrix4=" + matrix4 +
                '}';
    }
}
